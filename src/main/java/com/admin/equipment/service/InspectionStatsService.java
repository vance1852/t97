package com.admin.equipment.service;

import com.admin.equipment.model.InspectionOrder;
import com.admin.equipment.model.InspectionResultItem;
import com.admin.equipment.model.WorkOrder;
import com.admin.equipment.repo.InspectionOrderRepository;
import com.admin.equipment.repo.InspectionResultItemRepository;
import com.admin.equipment.repo.WorkOrderRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class InspectionStatsService {

    private final InspectionOrderRepository orderRepo;
    private final InspectionResultItemRepository resultRepo;
    private final WorkOrderRepository workOrderRepo;

    public InspectionStatsService(InspectionOrderRepository orderRepo,
                                  InspectionResultItemRepository resultRepo,
                                  WorkOrderRepository workOrderRepo) {
        this.orderRepo = orderRepo;
        this.resultRepo = resultRepo;
        this.workOrderRepo = workOrderRepo;
    }

    public Map<String, Object> getPassRateStats(LocalDateTime start, LocalDateTime end,
                                                Long equipmentId, Long templateId) {
        List<InspectionOrder> orders;
        if (start != null && end != null) {
            orders = orderRepo.findByCreatedAtBetween(start, end);
        } else {
            orders = orderRepo.findAllByOrderByIdDesc();
        }

        if (equipmentId != null) {
            orders.removeIf(o -> !o.getEquipmentId().equals(equipmentId));
        }
        if (templateId != null) {
            orders.removeIf(o -> !o.getTemplateId().equals(templateId));
        }

        long submittedCount = orders.stream()
                .filter(o -> "submitted".equals(o.getStatus()) || "closed".equals(o.getStatus()))
                .count();

        long passedCount = orders.stream()
                .filter(o -> ("submitted".equals(o.getStatus()) || "closed".equals(o.getStatus()))
                        && Boolean.TRUE.equals(o.getIsPassed()))
                .count();

        double passRate = submittedCount > 0 ? (double) passedCount / submittedCount * 100 : 0;

        Map<String, Object> result = new HashMap<>();
        result.put("totalOrders", orders.size());
        result.put("submittedOrders", submittedCount);
        result.put("passedOrders", passedCount);
        result.put("passRate", Math.round(passRate * 100) / 100.0);

        return result;
    }

    public Map<String, Object> getAbnormalDistribution(LocalDateTime start, LocalDateTime end) {
        if (start == null) {
            start = LocalDateTime.now().minusDays(30);
        }
        if (end == null) {
            end = LocalDateTime.now();
        }

        List<Object[]> rawData = resultRepo.countAbnormalItems(start, end);
        long totalAbnormal = resultRepo.countAbnormalItemsTotal(start, end);

        List<Map<String, Object>> items = new ArrayList<>();
        for (Object[] row : rawData) {
            Map<String, Object> item = new HashMap<>();
            item.put("itemKey", row[0]);
            item.put("itemName", row[1]);
            item.put("count", row[2]);
            items.add(item);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalAbnormalItems", totalAbnormal);
        result.put("items", items);
        result.put("startTime", start);
        result.put("endTime", end);

        return result;
    }

    public Map<String, Object> getClosureRateStats(LocalDateTime start, LocalDateTime end) {
        if (start == null) {
            start = LocalDateTime.now().minusDays(30);
        }
        if (end == null) {
            end = LocalDateTime.now();
        }

        List<InspectionOrder> orders = orderRepo.findByCreatedAtBetween(start, end);

        long withAbnormalCount = 0;
        long closedCount = 0;
        long onTimeClosedCount = 0;

        for (InspectionOrder order : orders) {
            if (!"submitted".equals(order.getStatus()) && !"closed".equals(order.getStatus())) {
                continue;
            }

            List<InspectionResultItem> abnormalItems =
                    resultRepo.findByOrderIdAndIsAbnormalTrue(order.getId());

            if (abnormalItems.isEmpty()) {
                continue;
            }

            withAbnormalCount++;

            boolean isClosed = "closed".equals(order.getStatus());
            if (isClosed) {
                closedCount++;
            }

            if (isClosed && order.getClosedAt() != null) {
                long hours = java.time.Duration
                        .between(order.getCreatedAt(), order.getClosedAt())
                        .toHours();
                if (hours <= 72) {
                    onTimeClosedCount++;
                }
            }
        }

        double closureRate = withAbnormalCount > 0
                ? (double) closedCount / withAbnormalCount * 100 : 0;
        double onTimeRate = withAbnormalCount > 0
                ? (double) onTimeClosedCount / withAbnormalCount * 100 : 0;

        Map<String, Object> result = new HashMap<>();
        result.put("totalAbnormalOrders", withAbnormalCount);
        result.put("closedOrders", closedCount);
        result.put("closureRate", Math.round(closureRate * 100) / 100.0);
        result.put("onTimeClosedOrders", onTimeClosedCount);
        result.put("onTimeClosureRate", Math.round(onTimeRate * 100) / 100.0);

        return result;
    }

    public Map<String, Object> getEquipmentStats(Long equipmentId) {
        Map<String, Object> result = new HashMap<>();

        long totalOrders = orderRepo.countByEquipmentId(equipmentId);
        List<InspectionOrder> recentOrders = orderRepo
                .findByEquipmentIdOrderByIdDesc(equipmentId);

        long submitted = recentOrders.stream()
                .filter(o -> "submitted".equals(o.getStatus()) || "closed".equals(o.getStatus()))
                .count();
        long passed = recentOrders.stream()
                .filter(o -> ("submitted".equals(o.getStatus()) || "closed".equals(o.getStatus()))
                        && Boolean.TRUE.equals(o.getIsPassed()))
                .count();

        result.put("totalOrders", totalOrders);
        result.put("submittedOrders", submitted);
        result.put("passedOrders", passed);
        result.put("passRate", submitted > 0 ? Math.round((double) passed / submitted * 10000) / 100.0 : 0);

        long openWorkOrders = 0;
        for (InspectionOrder order : recentOrders) {
            List<InspectionResultItem> abnormals =
                    resultRepo.findByOrderIdAndIsAbnormalTrue(order.getId());
            for (InspectionResultItem item : abnormals) {
                if (item.getWorkOrderId() != null) {
                    WorkOrder wo = workOrderRepo.findById(item.getWorkOrderId()).orElse(null);
                    if (wo != null && !"done".equals(wo.getStatus())) {
                        openWorkOrders++;
                    }
                }
            }
        }
        result.put("openWorkOrders", openWorkOrders);

        return result;
    }

    public Map<String, Object> getTemplateStats(Long templateId) {
        Map<String, Object> result = new HashMap<>();

        long totalOrders = orderRepo.countByTemplateId(templateId);
        List<InspectionOrder> orders = orderRepo.findByTemplateIdOrderByIdDesc(templateId);

        long submitted = orders.stream()
                .filter(o -> "submitted".equals(o.getStatus()) || "closed".equals(o.getStatus()))
                .count();
        long passed = orders.stream()
                .filter(o -> ("submitted".equals(o.getStatus()) || "closed".equals(o.getStatus()))
                        && Boolean.TRUE.equals(o.getIsPassed()))
                .count();

        result.put("totalOrders", totalOrders);
        result.put("submittedOrders", submitted);
        result.put("passedOrders", passed);
        result.put("passRate", submitted > 0 ? Math.round((double) passed / submitted * 10000) / 100.0 : 0);

        return result;
    }

    public Map<String, Object> getOverview() {
        Map<String, Object> result = new HashMap<>();

        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime todayEnd = LocalDateTime.now();
        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);

        Map<String, Object> todayPassRate = getPassRateStats(todayStart, todayEnd, null, null);
        Map<String, Object> monthPassRate = getPassRateStats(monthStart, todayEnd, null, null);
        Map<String, Object> abnormalDist = getAbnormalDistribution(monthStart, todayEnd);
        Map<String, Object> closureRate = getClosureRateStats(monthStart, todayEnd);

        result.put("today", todayPassRate);
        result.put("thisMonth", monthPassRate);
        result.put("abnormalDistribution", abnormalDist);
        result.put("closureStats", closureRate);

        return result;
    }
}
