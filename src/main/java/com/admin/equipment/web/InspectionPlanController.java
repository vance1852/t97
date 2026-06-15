package com.admin.equipment.web;

import com.admin.equipment.model.InspectionPlan;
import com.admin.equipment.service.InspectionPlanService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inspection-plans")
public class InspectionPlanController {

    private final InspectionPlanService planService;

    public InspectionPlanController(InspectionPlanService planService) {
        this.planService = planService;
    }

    @GetMapping
    public List<InspectionPlan> list(@RequestParam(required = false) String status) {
        return planService.listPlans(status);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return planService.getPlan(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("detail", "计划不存在")));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody InspectionPlan plan) {
        try {
            InspectionPlan p = planService.createPlan(plan);
            return ResponseEntity.status(HttpStatus.CREATED).body(p);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.unprocessableEntity().body(Map.of("detail", e.getMessage()));
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody InspectionPlan plan) {
        try {
            InspectionPlan p = planService.updatePlan(id, plan);
            return ResponseEntity.ok(p);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("detail", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            planService.deletePlan(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("detail", e.getMessage()));
        }
    }

    @PostMapping("/{id}/execute")
    public ResponseEntity<?> execute(@PathVariable Long id) {
        try {
            List<Long> orderIds = planService.executePlan(id);
            return ResponseEntity.ok(Map.of(
                    "executed", true,
                    "orderCount", orderIds.size(),
                    "orderIds", orderIds
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("detail", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.unprocessableEntity().body(Map.of("detail", e.getMessage()));
        }
    }

    @PostMapping("/execute-due")
    public ResponseEntity<?> executeDue() {
        List<Long> orderIds = planService.executeDuePlans();
        return ResponseEntity.ok(Map.of(
                "executed", true,
                "orderCount", orderIds.size(),
                "orderIds", orderIds
        ));
    }
}
