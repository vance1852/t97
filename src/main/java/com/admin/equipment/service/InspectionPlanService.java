package com.admin.equipment.service;

import com.admin.equipment.model.Equipment;
import com.admin.equipment.model.InspectionPlan;
import com.admin.equipment.model.InspectionTemplate;
import com.admin.equipment.repo.EquipmentRepository;
import com.admin.equipment.repo.InspectionPlanRepository;
import com.admin.equipment.repo.InspectionTemplateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class InspectionPlanService {

    private final InspectionPlanRepository planRepo;
    private final EquipmentRepository equipmentRepo;
    private final InspectionTemplateRepository templateRepo;
    private final InspectionOrderService orderService;

    public InspectionPlanService(InspectionPlanRepository planRepo,
                                 EquipmentRepository equipmentRepo,
                                 InspectionTemplateRepository templateRepo,
                                 InspectionOrderService orderService) {
        this.planRepo = planRepo;
        this.equipmentRepo = equipmentRepo;
        this.templateRepo = templateRepo;
        this.orderService = orderService;
    }

    public List<InspectionPlan> listPlans(String status) {
        if (status != null && !status.isEmpty()) {
            return planRepo.findByStatusOrderByIdDesc(status);
        }
        return planRepo.findAllByOrderByIdDesc();
    }

    public Optional<InspectionPlan> getPlan(Long id) {
        return planRepo.findById(id);
    }

    @Transactional
    public InspectionPlan createPlan(InspectionPlan plan) {
        if (plan.getName() == null || plan.getName().isEmpty()) {
            throw new IllegalArgumentException("计划名称不能为空");
        }
        if (plan.getTemplateId() == null) {
            throw new IllegalArgumentException("模板不能为空");
        }
        if (!templateRepo.existsById(plan.getTemplateId())) {
            throw new IllegalArgumentException("模板不存在");
        }

        InspectionPlan p = new InspectionPlan();
        p.setName(plan.getName());
        p.setTemplateId(plan.getTemplateId());
        p.setEquipmentType(plan.getEquipmentType() == null ? "" : plan.getEquipmentType());
        p.setEquipmentIds(plan.getEquipmentIds() == null ? "" : plan.getEquipmentIds());
        p.setCycleType(plan.getCycleType() == null ? "daily" : plan.getCycleType());
        p.setCycleValue(plan.getCycleValue() == null ? 1 : plan.getCycleValue());
        p.setCronExpr(plan.getCronExpr() == null ? "" : plan.getCronExpr());
        p.setExecutor(plan.getExecutor() == null ? "" : plan.getExecutor());
        p.setStatus(plan.getStatus() == null ? "active" : plan.getStatus());
        p.setNextRunAt(calculateNextRun(p.getCycleType(), p.getCycleValue()));

        return planRepo.save(p);
    }

    @Transactional
    public InspectionPlan updatePlan(Long id, InspectionPlan plan) {
        InspectionPlan p = planRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("计划不存在"));

        if (plan.getName() != null) p.setName(plan.getName());
        if (plan.getTemplateId() != null) {
            if (!templateRepo.existsById(plan.getTemplateId())) {
                throw new IllegalArgumentException("模板不存在");
            }
            p.setTemplateId(plan.getTemplateId());
        }
        if (plan.getEquipmentType() != null) p.setEquipmentType(plan.getEquipmentType());
        if (plan.getEquipmentIds() != null) p.setEquipmentIds(plan.getEquipmentIds());
        if (plan.getCycleType() != null) p.setCycleType(plan.getCycleType());
        if (plan.getCycleValue() != null) p.setCycleValue(plan.getCycleValue());
        if (plan.getCronExpr() != null) p.setCronExpr(plan.getCronExpr());
        if (plan.getExecutor() != null) p.setExecutor(plan.getExecutor());
        if (plan.getStatus() != null) p.setStatus(plan.getStatus());

        p.setUpdatedAt(LocalDateTime.now());
        return planRepo.save(p);
    }

    @Transactional
    public void deletePlan(Long id) {
        if (!planRepo.existsById(id)) {
            throw new IllegalArgumentException("计划不存在");
        }
        planRepo.deleteById(id);
    }

    @Transactional
    public List<Long> executePlan(Long planId) {
        InspectionPlan plan = planRepo.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("计划不存在"));

        if (!"active".equals(plan.getStatus())) {
            throw new IllegalStateException("计划未启用");
        }

        List<Long> equipmentIds = resolveEquipmentIds(plan);
        List<Long> orderIds = new ArrayList<>();

        for (Long equipmentId : equipmentIds) {
            try {
                var order = orderService.createOrderByEquipment(
                        equipmentId,
                        plan.getExecutor(),
                        "plan",
                        planId,
                        null
                );
                orderIds.add(order.getId());
            } catch (Exception e) {
                // 跳过无法生成点检单的设备
            }
        }

        plan.setLastRunAt(LocalDateTime.now());
        plan.setNextRunAt(calculateNextRun(plan.getCycleType(), plan.getCycleValue()));
        planRepo.save(plan);

        return orderIds;
    }

    private List<Long> resolveEquipmentIds(InspectionPlan plan) {
        List<Long> ids = new ArrayList<>();

        if (plan.getEquipmentType() != null && !plan.getEquipmentType().isEmpty()) {
            List<Equipment> equipments = equipmentRepo.findByType(plan.getEquipmentType());
            for (Equipment e : equipments) {
                ids.add(e.getId());
            }
        }

        if (plan.getEquipmentIds() != null && !plan.getEquipmentIds().isEmpty()) {
            String[] parts = plan.getEquipmentIds().split(",");
            for (String part : parts) {
                try {
                    Long id = Long.parseLong(part.trim());
                    if (!ids.contains(id)) {
                        ids.add(id);
                    }
                } catch (NumberFormatException ignored) {}
            }
        }

        return ids;
    }

    private LocalDateTime calculateNextRun(String cycleType, Integer cycleValue) {
        LocalDateTime now = LocalDateTime.now();
        int value = cycleValue == null ? 1 : cycleValue;

        switch (cycleType == null ? "daily" : cycleType) {
            case "hourly":
                return now.plusHours(value);
            case "weekly":
                return now.plusWeeks(value);
            case "monthly":
                return now.plusMonths(value);
            case "daily":
            default:
                return now.plusDays(value);
        }
    }

    @Transactional
    public List<Long> executeDuePlans() {
        List<InspectionPlan> duePlans = planRepo
                .findByStatusAndNextRunAtBefore("active", LocalDateTime.now());

        List<Long> allOrderIds = new ArrayList<>();
        for (InspectionPlan plan : duePlans) {
            try {
                allOrderIds.addAll(executePlan(plan.getId()));
            } catch (Exception e) {
                // 跳过执行失败的计划
            }
        }
        return allOrderIds;
    }
}
