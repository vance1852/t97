package com.admin.equipment.web;

import com.admin.equipment.model.InspectionOrder;
import com.admin.equipment.model.InspectionResultItem;
import com.admin.equipment.service.InspectionOrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inspection-orders")
public class InspectionOrderController {

    private final InspectionOrderService orderService;

    public InspectionOrderController(InspectionOrderService orderService) {
        this.orderService = orderService;
    }

    public record CreateOrderRequest(Long equipmentId, String executor, String sourceType,
                                     Long sourceId, Long parentOrderId) {}

    public record UpdateResultRequest(String textValue, Double numericValue, Boolean boolValue,
                                      String abnormalRemark, String photoUrl) {}

    public record SubmitOrderRequest(String executor) {}

    @GetMapping
    public List<InspectionOrder> list(@RequestParam(required = false) Long equipmentId,
                                      @RequestParam(required = false) Long templateId,
                                      @RequestParam(required = false) String status) {
        return orderService.listOrders(equipmentId, templateId, status);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return orderService.getOrder(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("detail", "点检单不存在")));
    }

    @GetMapping("/{id}/results")
    public ResponseEntity<?> getResults(@PathVariable Long id) {
        if (!orderService.getOrder(id).isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("detail", "点检单不存在"));
        }
        return ResponseEntity.ok(orderService.getOrderResults(id));
    }

    @GetMapping("/{id}/abnormal-items")
    public ResponseEntity<?> getAbnormalItems(@PathVariable Long id) {
        if (!orderService.getOrder(id).isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("detail", "点检单不存在"));
        }
        return ResponseEntity.ok(orderService.getAbnormalItems(id));
    }

    @GetMapping("/{id}/reinspections")
    public ResponseEntity<?> getReinspections(@PathVariable Long id) {
        if (!orderService.getOrder(id).isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("detail", "点检单不存在"));
        }
        return ResponseEntity.ok(orderService.getReinspections(id));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateOrderRequest req) {
        if (req.equipmentId() == null) {
            return ResponseEntity.unprocessableEntity().body(Map.of("detail", "设备ID必填"));
        }
        try {
            InspectionOrder order = orderService.createOrderByEquipment(
                    req.equipmentId(), req.executor(), req.sourceType(),
                    req.sourceId(), req.parentOrderId());
            return ResponseEntity.status(HttpStatus.CREATED).body(order);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.unprocessableEntity().body(Map.of("detail", e.getMessage()));
        }
    }

    @PatchMapping("/results/{resultId}")
    public ResponseEntity<?> updateResult(@PathVariable Long resultId,
                                          @RequestBody UpdateResultRequest req) {
        try {
            InspectionResultItem result = orderService.updateResultItem(
                    resultId, req.textValue(), req.numericValue(), req.boolValue(),
                    req.abnormalRemark(), req.photoUrl());
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("detail", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.unprocessableEntity().body(Map.of("detail", e.getMessage()));
        }
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<?> submit(@PathVariable Long id,
                                    @RequestBody(required = false) SubmitOrderRequest req) {
        try {
            String executor = req != null ? req.executor() : null;
            InspectionOrder order = orderService.submitOrder(id, executor);
            return ResponseEntity.ok(order);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("detail", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.unprocessableEntity().body(Map.of("detail", e.getMessage()));
        }
    }
}
