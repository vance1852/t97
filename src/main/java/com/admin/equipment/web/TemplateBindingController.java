package com.admin.equipment.web;

import com.admin.equipment.model.InspectionTemplate;
import com.admin.equipment.model.TemplateBinding;
import com.admin.equipment.service.TemplateBindingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/template-bindings")
public class TemplateBindingController {

    private final TemplateBindingService bindingService;

    public TemplateBindingController(TemplateBindingService bindingService) {
        this.bindingService = bindingService;
    }

    public record BindEquipmentRequest(Long templateId, Long equipmentId) {}

    public record BindEquipmentTypeRequest(Long templateId, String equipmentType) {}

    @GetMapping("/equipment/{equipmentId}")
    public ResponseEntity<?> getTemplateForEquipment(@PathVariable Long equipmentId) {
        return bindingService.getTemplateForEquipment(equipmentId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("detail", "该设备未绑定点检模板")));
    }

    @GetMapping("/template/{templateId}")
    public List<TemplateBinding> listByTemplate(@PathVariable Long templateId) {
        return bindingService.listBindingsByTemplate(templateId);
    }

    @GetMapping("/equipment-type/{equipmentType}")
    public List<TemplateBinding> listByEquipmentType(@PathVariable String equipmentType) {
        return bindingService.listBindingsByEquipmentType(equipmentType);
    }

    @PostMapping("/equipment")
    public ResponseEntity<?> bindEquipment(@RequestBody BindEquipmentRequest req) {
        if (req.templateId() == null || req.equipmentId() == null) {
            return ResponseEntity.unprocessableEntity()
                    .body(Map.of("detail", "模板ID和设备ID必填"));
        }
        try {
            TemplateBinding binding = bindingService.bindToEquipment(
                    req.templateId(), req.equipmentId());
            return ResponseEntity.status(HttpStatus.CREATED).body(binding);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.unprocessableEntity().body(Map.of("detail", e.getMessage()));
        }
    }

    @PostMapping("/equipment-type")
    public ResponseEntity<?> bindEquipmentType(@RequestBody BindEquipmentTypeRequest req) {
        if (req.templateId() == null || req.equipmentType() == null
                || req.equipmentType().isBlank()) {
            return ResponseEntity.unprocessableEntity()
                    .body(Map.of("detail", "模板ID和设备类型必填"));
        }
        try {
            TemplateBinding binding = bindingService.bindToEquipmentType(
                    req.templateId(), req.equipmentType());
            return ResponseEntity.status(HttpStatus.CREATED).body(binding);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.unprocessableEntity().body(Map.of("detail", e.getMessage()));
        }
    }

    @DeleteMapping("/equipment/{equipmentId}")
    public ResponseEntity<?> unbindEquipment(@PathVariable Long equipmentId) {
        bindingService.unbindFromEquipment(equipmentId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/equipment-type/{equipmentType}")
    public ResponseEntity<?> unbindEquipmentType(@PathVariable String equipmentType) {
        bindingService.unbindFromEquipmentType(equipmentType);
        return ResponseEntity.noContent().build();
    }
}
