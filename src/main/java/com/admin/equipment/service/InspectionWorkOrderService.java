package com.admin.equipment.service;

import com.admin.equipment.model.InspectionOrder;
import com.admin.equipment.model.InspectionResultItem;
import com.admin.equipment.model.WorkOrder;
import com.admin.equipment.repo.InspectionOrderRepository;
import com.admin.equipment.repo.InspectionResultItemRepository;
import com.admin.equipment.repo.WorkOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class InspectionWorkOrderService {

    private final InspectionOrderRepository orderRepo;
    private final InspectionResultItemRepository resultRepo;
    private final WorkOrderRepository workOrderRepo;
    private final InspectionOrderService orderService;

    public InspectionWorkOrderService(InspectionOrderRepository orderRepo,
                                      InspectionResultItemRepository resultRepo,
                                      WorkOrderRepository workOrderRepo,
                                      InspectionOrderService orderService) {
        this.orderRepo = orderRepo;
        this.resultRepo = resultRepo;
        this.workOrderRepo = workOrderRepo;
        this.orderService = orderService;
    }

    @Transactional
    public WorkOrder convertToWorkOrder(Long resultItemId, String type, String priority,
                                        String description, String assignee) {
        InspectionResultItem resultItem = resultRepo.findById(resultItemId)
                .orElseThrow(() -> new IllegalArgumentException("结果项不存在"));

        if (resultItem.getWorkOrderId() != null) {
            throw new IllegalStateException("该异常项已创建工单，工单ID: " + resultItem.getWorkOrderId());
        }

        InspectionOrder order = orderRepo.findById(resultItem.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("点检单不存在"));

        String woType = (type != null && !type.isEmpty()) ? type : "repair";
        String woPriority = (priority != null && !priority.isEmpty()) ? priority : "medium";

        WorkOrder wo = new WorkOrder();
        wo.setEquipmentId(order.getEquipmentId());
        wo.setTitle("点检异常: " + resultItem.getItemName());

        StringBuilder desc = new StringBuilder();
        desc.append("来源：点检单 ").append(order.getOrderNo()).append("\n");
        desc.append("异常项：").append(resultItem.getItemName()).append("\n");
        if (resultItem.getValueText() != null) {
            desc.append("检查结果：").append(resultItem.getValueText()).append("\n");
        }
        if (resultItem.getValueNumeric() != null) {
            desc.append("检查数值：").append(resultItem.getValueNumeric()).append("\n");
        }
        if (resultItem.getAbnormalRemark() != null && !resultItem.getAbnormalRemark().isEmpty()) {
            desc.append("异常说明：").append(resultItem.getAbnormalRemark()).append("\n");
        }
        if (description != null && !description.isEmpty()) {
            desc.append("补充描述：").append(description).append("\n");
        }
        wo.setDescription(desc.toString());

        wo.setType(woType);
        wo.setPriority(woPriority);
        wo.setAssignee(assignee == null ? "" : assignee);
        wo.setStatus("open");
        wo = workOrderRepo.save(wo);

        resultItem.setWorkOrderId(wo.getId());
        resultRepo.save(resultItem);

        return wo;
    }

    @Transactional
    public List<WorkOrder> batchConvertToWorkOrder(Long orderId, String type, String priority,
                                                   String assignee) {
        List<InspectionResultItem> abnormalItems = resultRepo.findByOrderIdAndIsAbnormalTrue(orderId);
        if (abnormalItems.isEmpty()) {
            throw new IllegalStateException("该点检单无异常项");
        }

        List<WorkOrder> createdOrders = new ArrayList<>();
        for (InspectionResultItem item : abnormalItems) {
            if (item.getWorkOrderId() == null) {
                WorkOrder wo = convertToWorkOrder(item.getId(), type, priority, null, assignee);
                createdOrders.add(wo);
            }
        }
        return createdOrders;
    }

    @Transactional
    public InspectionOrder createReinspection(Long orderId, String executor) {
        InspectionOrder originalOrder = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("点检单不存在"));

        if (!"submitted".equals(originalOrder.getStatus()) && !"closed".equals(originalOrder.getStatus())) {
            throw new IllegalStateException("点检单状态不支持复检");
        }

        return orderService.createOrderByEquipment(
                originalOrder.getEquipmentId(),
                executor,
                "reinspection",
                null,
                orderId
        );
    }

    @Transactional
    public InspectionOrder createReinspectionFromWorkOrder(Long workOrderId, String executor) {
        List<InspectionResultItem> items = resultRepo.findByWorkOrderId(workOrderId);
        if (items.isEmpty()) {
            throw new IllegalArgumentException("该工单无关联的点检异常项");
        }

        WorkOrder wo = workOrderRepo.findById(workOrderId)
                .orElseThrow(() -> new IllegalArgumentException("工单不存在"));

        if (!"done".equals(wo.getStatus())) {
            throw new IllegalStateException("工单未完成，不能发起复检");
        }

        InspectionResultItem firstItem = items.get(0);
        InspectionOrder originalOrder = orderRepo.findById(firstItem.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("原始点检单不存在"));

        return createReinspection(originalOrder.getId(), executor);
    }

    public boolean isOrderClosed(Long orderId) {
        InspectionOrder order = orderRepo.findById(orderId).orElse(null);
        if (order == null) {
            return false;
        }
        return "closed".equals(order.getStatus()) && Boolean.TRUE.equals(order.getIsPassed());
    }

    @Transactional
    public InspectionOrder closeOrderIfPassed(Long orderId) {
        InspectionOrder order = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("点检单不存在"));

        if (!"submitted".equals(order.getStatus())) {
            throw new IllegalStateException("点检单未提交");
        }

        if (!Boolean.TRUE.equals(order.getIsPassed())) {
            throw new IllegalStateException("点检单未通过，不能直接闭环");
        }

        order.setStatus("closed");
        order.setClosedAt(java.time.LocalDateTime.now());
        return orderRepo.save(order);
    }

    public Optional<WorkOrder> getWorkOrderForResult(Long resultItemId) {
        InspectionResultItem item = resultRepo.findById(resultItemId).orElse(null);
        if (item == null || item.getWorkOrderId() == null) {
            return Optional.empty();
        }
        return workOrderRepo.findById(item.getWorkOrderId());
    }

    public List<WorkOrder> listWorkOrdersForOrder(Long orderId) {
        List<InspectionResultItem> items = resultRepo.findByOrderIdAndIsAbnormalTrue(orderId);
        List<WorkOrder> orders = new ArrayList<>();
        for (InspectionResultItem item : items) {
            if (item.getWorkOrderId() != null) {
                workOrderRepo.findById(item.getWorkOrderId()).ifPresent(orders::add);
            }
        }
        return orders;
    }
}
