package com.admin.equipment.web;

import com.admin.equipment.model.InspectionOrder;
import com.admin.equipment.model.WorkOrder;
import com.admin.equipment.service.InspectionWorkOrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inspection-work-orders")
public class InspectionWorkOrderController {

    private final InspectionWorkOrderService woService;

    public InspectionWorkOrderController(InspectionWorkOrderService woService) {
        this.woService = woService;
    }

    public record ConvertRequest(String type, String priority, String description, String assignee) {}

    public record BatchConvertRequest(String type, String priority, String assignee) {}

    public record ReinspectionRequest(String executor) {}

    @PostMapping("/result/{resultId}/convert")
    public ResponseEntity<?> convertToWorkOrder(@PathVariable Long resultId,
                                                @RequestBody ConvertRequest req) {
        try {
            WorkOrder wo = woService.convertToWorkOrder(
                    resultId, req.type(), req.priority(), req.description(), req.assignee());
            return ResponseEntity.status(HttpStatus.CREATED).body(wo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("detail", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.unprocessableEntity().body(Map.of("detail", e.getMessage()));
        }
    }

    @PostMapping("/order/{orderId}/batch-convert")
    public ResponseEntity<?> batchConvert(@PathVariable Long orderId,
                                          @RequestBody BatchConvertRequest req) {
        try {
            List<WorkOrder> orders = woService.batchConvertToWorkOrder(
                    orderId, req.type(), req.priority(), req.assignee());
            return ResponseEntity.status(HttpStatus.CREATED).body(orders);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("detail", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.unprocessableEntity().body(Map.of("detail", e.getMessage()));
        }
    }

    @GetMapping("/result/{resultId}/work-order")
    public ResponseEntity<?> getWorkOrderForResult(@PathVariable Long resultId) {
        return woService.getWorkOrderForResult(resultId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("detail", "该异常项暂无关联工单")));
    }

    @GetMapping("/order/{orderId}/work-orders")
    public ResponseEntity<?> listWorkOrdersForOrder(@PathVariable Long orderId) {
        if (woService.listWorkOrdersForOrder(orderId).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("detail", "该点检单暂无关联工单"));
        }
        return ResponseEntity.ok(woService.listWorkOrdersForOrder(orderId));
    }

    @PostMapping("/order/{orderId}/reinspect")
    public ResponseEntity<?> createReinspection(@PathVariable Long orderId,
                                                @RequestBody ReinspectionRequest req) {
        try {
            InspectionOrder reinspection = woService.createReinspection(
                    orderId, req != null ? req.executor() : null);
            return ResponseEntity.status(HttpStatus.CREATED).body(reinspection);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("detail", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.unprocessableEntity().body(Map.of("detail", e.getMessage()));
        }
    }

    @PostMapping("/work-order/{workOrderId}/reinspect")
    public ResponseEntity<?> createReinspectionFromWorkOrder(@PathVariable Long workOrderId,
                                                             @RequestBody ReinspectionRequest req) {
        try {
            InspectionOrder reinspection = woService.createReinspectionFromWorkOrder(
                    workOrderId, req != null ? req.executor() : null);
            return ResponseEntity.status(HttpStatus.CREATED).body(reinspection);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("detail", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.unprocessableEntity().body(Map.of("detail", e.getMessage()));
        }
    }

    @PostMapping("/order/{orderId}/close")
    public ResponseEntity<?> closeOrder(@PathVariable Long orderId) {
        try {
            InspectionOrder order = woService.closeOrderIfPassed(orderId);
            return ResponseEntity.ok(order);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("detail", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.unprocessableEntity().body(Map.of("detail", e.getMessage()));
        }
    }
}
