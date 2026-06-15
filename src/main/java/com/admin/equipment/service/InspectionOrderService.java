package com.admin.equipment.service;

import com.admin.equipment.model.*;
import com.admin.equipment.repo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class InspectionOrderService {

    private static final AtomicInteger ORDER_SEQ = new AtomicInteger(0);

    private final InspectionOrderRepository orderRepo;
    private final InspectionResultItemRepository resultRepo;
    private final InspectionTemplateService templateService;
    private final TemplateBindingService bindingService;
    private final InspectionRuleEngineService ruleEngine;
    private final EquipmentRepository equipmentRepo;

    public InspectionOrderService(InspectionOrderRepository orderRepo,
                                  InspectionResultItemRepository resultRepo,
                                  InspectionTemplateService templateService,
                                  TemplateBindingService bindingService,
                                  InspectionRuleEngineService ruleEngine,
                                  EquipmentRepository equipmentRepo) {
        this.orderRepo = orderRepo;
        this.resultRepo = resultRepo;
        this.templateService = templateService;
        this.bindingService = bindingService;
        this.ruleEngine = ruleEngine;
        this.equipmentRepo = equipmentRepo;
    }

    public List<InspectionOrder> listOrders(Long equipmentId, Long templateId, String status) {
        if (equipmentId != null) {
            if (status != null) {
                return orderRepo.findByEquipmentIdAndStatusOrderByIdDesc(equipmentId, status);
            }
            return orderRepo.findByEquipmentIdOrderByIdDesc(equipmentId);
        }
        if (templateId != null) {
            return orderRepo.findByTemplateIdOrderByIdDesc(templateId);
        }
        if (status != null) {
            return orderRepo.findByStatusOrderByIdDesc(status);
        }
        return orderRepo.findAllByOrderByIdDesc();
    }

    public Optional<InspectionOrder> getOrder(Long id) {
        return orderRepo.findById(id);
    }

    public List<InspectionResultItem> getOrderResults(Long orderId) {
        return resultRepo.findByOrderIdOrderBySortOrderAsc(orderId);
    }

    @Transactional
    public InspectionOrder createOrderByEquipment(Long equipmentId, String executor,
                                                  String sourceType, Long sourceId,
                                                  Long parentOrderId) {
        if (!equipmentRepo.existsById(equipmentId)) {
            throw new IllegalArgumentException("设备不存在");
        }

        InspectionTemplateVersion version = bindingService.getLatestVersionForEquipment(equipmentId)
                .orElseThrow(() -> new IllegalArgumentException("设备未绑定点检模板"));

        InspectionTemplate template = templateService.getTemplate(version.getTemplateId())
                .orElseThrow(() -> new IllegalArgumentException("模板不存在"));

        InspectionOrder order = new InspectionOrder();
        order.setOrderNo(generateOrderNo());
        order.setEquipmentId(equipmentId);
        order.setTemplateId(template.getId());
        order.setTemplateVersionId(version.getId());
        order.setTemplateVersion(version.getVersion());
        order.setStatus("draft");
        order.setExecutor(executor == null ? "" : executor);
        order.setSourceType(sourceType == null ? "manual" : sourceType);
        order.setSourceId(sourceId);
        order.setParentOrderId(parentOrderId);
        if (parentOrderId != null) {
            InspectionOrder parent = orderRepo.findById(parentOrderId).orElse(null);
            if (parent != null) {
                order.setReinspectionCount(parent.getReinspectionCount() + 1);
            }
        }
        order = orderRepo.save(order);

        List<InspectionTemplateItem> items = templateService.getVersionItems(version.getId());
        for (InspectionTemplateItem item : items) {
            InspectionResultItem result = new InspectionResultItem();
            result.setOrderId(order.getId());
            result.setItemId(item.getId());
            result.setItemKey(item.getItemKey());
            result.setItemName(item.getName());
            result.setItemType(item.getType());
            result.setSortOrder(item.getSortOrder());
            result.setIsPassed(null);
            result.setIsAbnormal(false);
            resultRepo.save(result);
        }

        return order;
    }

    @Transactional
    public InspectionResultItem updateResultItem(Long resultId, String textValue,
                                                 Double numericValue, Boolean boolValue,
                                                 String abnormalRemark, String photoUrl) {
        InspectionResultItem result = resultRepo.findById(resultId)
                .orElseThrow(() -> new IllegalArgumentException("结果项不存在"));

        InspectionOrder order = orderRepo.findById(result.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("点检单不存在"));

        if (!"draft".equals(order.getStatus())) {
            throw new IllegalStateException("点检单已提交，不能修改");
        }

        InspectionTemplateItem templateItem = templateService.getItem(result.getItemId())
                .orElseThrow(() -> new IllegalArgumentException("模板项不存在"));

        String vText = textValue;
        Double vNum = numericValue;
        Boolean vBool = boolValue;

        if ("numeric".equals(result.getItemType())) {
            result.setValueNumeric(numericValue);
            vText = null;
            vBool = null;
        } else if ("boolean".equals(result.getItemType())) {
            result.setValueBoolean(boolValue);
            vText = null;
            vNum = null;
        } else {
            result.setValueText(textValue);
            vNum = null;
            vBool = null;
        }

        InspectionRuleEngineService.ItemJudgmentResult judgment =
                ruleEngine.judgeItem(templateItem, vText, vNum, vBool);

        result.setIsPassed(judgment.isPassed());
        result.setIsAbnormal(judgment.hasValue() && !judgment.isPassed());

        if (abnormalRemark != null) {
            result.setAbnormalRemark(abnormalRemark);
        }
        if (photoUrl != null) {
            result.setPhotoUrl(photoUrl);
        }

        return resultRepo.save(result);
    }

    @Transactional
    public InspectionOrder submitOrder(Long orderId, String executor) {
        InspectionOrder order = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("点检单不存在"));

        if (!"draft".equals(order.getStatus())) {
            throw new IllegalStateException("点检单已提交");
        }

        List<InspectionResultItem> results = resultRepo.findByOrderIdOrderBySortOrderAsc(orderId);
        InspectionTemplateVersion version = templateService.getVersion(order.getTemplateVersionId())
                .orElseThrow(() -> new IllegalArgumentException("模板版本不存在"));
        List<InspectionTemplateItem> templateItems = templateService.getVersionItems(version.getId());

        List<String> missingRequired = new ArrayList<>();
        for (InspectionTemplateItem item : templateItems) {
            if (item.getRequired()) {
                InspectionResultItem result = results.stream()
                        .filter(r -> r.getItemId().equals(item.getId()))
                        .findFirst().orElse(null);
                if (result == null || !hasValue(result)) {
                    missingRequired.add(item.getName());
                }
            }
        }

        if (!missingRequired.isEmpty()) {
            throw new IllegalStateException("必填项未填写: " + String.join(", ", missingRequired));
        }

        boolean allPassed = results.stream()
                .filter(r -> r.getIsPassed() != null)
                .allMatch(InspectionResultItem::getIsPassed);

        order.setStatus("submitted");
        order.setIsPassed(allPassed);
        order.setExecutedAt(LocalDateTime.now());
        if (executor != null && !executor.isEmpty()) {
            order.setExecutor(executor);
        }

        if (allPassed) {
            order.setStatus("closed");
            order.setClosedAt(LocalDateTime.now());

            if (order.getParentOrderId() != null) {
                closeAncestorChain(order.getParentOrderId());
            }
        }

        return orderRepo.save(order);
    }

    private void closeAncestorChain(Long parentOrderId) {
        InspectionOrder parent = orderRepo.findById(parentOrderId).orElse(null);
        if (parent == null) {
            return;
        }
        if (!"submitted".equals(parent.getStatus()) || !Boolean.FALSE.equals(parent.getIsPassed())) {
            return;
        }

        List<InspectionOrder> reinspections = orderRepo.findByParentOrderIdOrderByIdDesc(parent.getId());
        boolean anyReinspectionPassed = reinspections.stream()
                .anyMatch(r -> "closed".equals(r.getStatus()) && Boolean.TRUE.equals(r.getIsPassed()));
        if (!anyReinspectionPassed) {
            return;
        }

        parent.setIsPassed(true);
        parent.setStatus("closed");
        parent.setClosedAt(LocalDateTime.now());
        orderRepo.save(parent);

        if (parent.getParentOrderId() != null) {
            closeAncestorChain(parent.getParentOrderId());
        }
    }

    private boolean hasValue(InspectionResultItem result) {
        String type = result.getItemType();
        switch (type) {
            case "numeric":
                return result.getValueNumeric() != null;
            case "boolean":
                return result.getValueBoolean() != null;
            default:
                return result.getValueText() != null && !result.getValueText().trim().isEmpty();
        }
    }

    private String generateOrderNo() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int seq = ORDER_SEQ.incrementAndGet();
        return "IO" + dateStr + String.format("%04d", seq % 10000);
    }

    public List<InspectionResultItem> getAbnormalItems(Long orderId) {
        return resultRepo.findByOrderIdAndIsAbnormalTrue(orderId);
    }

    public List<InspectionOrder> getReinspections(Long parentOrderId) {
        return orderRepo.findByParentOrderIdOrderByIdDesc(parentOrderId);
    }
}
