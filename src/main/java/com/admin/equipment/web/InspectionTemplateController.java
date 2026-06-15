package com.admin.equipment.web;

import com.admin.equipment.model.InspectionTemplate;
import com.admin.equipment.model.InspectionTemplateItem;
import com.admin.equipment.model.InspectionTemplateVersion;
import com.admin.equipment.service.InspectionTemplateService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inspection-templates")
public class InspectionTemplateController {

    private final InspectionTemplateService templateService;

    public InspectionTemplateController(InspectionTemplateService templateService) {
        this.templateService = templateService;
    }

    public record CreateTemplateRequest(String name, String description, String category,
                                        List<InspectionTemplateItem> items, String createdBy) {}

    public record UpdateTemplateRequest(String name, String description, String category, String status) {}

    public record CreateVersionRequest(String changeLog, String createdBy, List<InspectionTemplateItem> items) {}

    @GetMapping
    public List<InspectionTemplate> list(@RequestParam(required = false) String status) {
        return templateService.listTemplates(status);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return templateService.getTemplate(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("detail", "模板不存在")));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateTemplateRequest req) {
        if (req.name() == null || req.name().isBlank()) {
            return ResponseEntity.unprocessableEntity().body(Map.of("detail", "模板名称必填"));
        }
        try {
            InspectionTemplate template = templateService.createTemplate(
                    req.name(), req.description(), req.category(), req.items(), req.createdBy());
            return ResponseEntity.status(HttpStatus.CREATED).body(template);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.unprocessableEntity().body(Map.of("detail", e.getMessage()));
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody UpdateTemplateRequest req) {
        try {
            InspectionTemplate template = templateService.updateTemplate(
                    id, req.name(), req.description(), req.category(), req.status());
            return ResponseEntity.ok(template);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("detail", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            templateService.deleteTemplate(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("detail", e.getMessage()));
        }
    }

    @GetMapping("/{id}/versions")
    public ResponseEntity<?> listVersions(@PathVariable Long id) {
        if (!templateService.getTemplate(id).isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("detail", "模板不存在"));
        }
        return ResponseEntity.ok(templateService.listVersions(id));
    }

    @GetMapping("/{id}/latest-version")
    public ResponseEntity<?> getLatestVersion(@PathVariable Long id) {
        return templateService.getLatestVersion(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("detail", "无版本记录")));
    }

    @PostMapping("/{id}/versions")
    public ResponseEntity<?> createVersion(@PathVariable Long id,
                                           @RequestBody CreateVersionRequest req) {
        try {
            InspectionTemplateVersion version = templateService.createNewVersion(
                    id, req.changeLog(), req.createdBy(), req.items());
            return ResponseEntity.status(HttpStatus.CREATED).body(version);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.unprocessableEntity().body(Map.of("detail", e.getMessage()));
        }
    }

    @GetMapping("/versions/{versionId}/items")
    public ResponseEntity<?> getVersionItems(@PathVariable Long versionId) {
        if (!templateService.getVersion(versionId).isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("detail", "版本不存在"));
        }
        return ResponseEntity.ok(templateService.getVersionItems(versionId));
    }

    @GetMapping("/items/{itemId}")
    public ResponseEntity<?> getItem(@PathVariable Long itemId) {
        return templateService.getItem(itemId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("detail", "点检项不存在")));
    }
}
