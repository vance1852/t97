package com.admin.equipment.service;

import com.admin.equipment.model.InspectionTemplate;
import com.admin.equipment.model.InspectionTemplateItem;
import com.admin.equipment.model.InspectionTemplateVersion;
import com.admin.equipment.repo.InspectionTemplateItemRepository;
import com.admin.equipment.repo.InspectionTemplateRepository;
import com.admin.equipment.repo.InspectionTemplateVersionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class InspectionTemplateService {

    private final InspectionTemplateRepository templateRepo;
    private final InspectionTemplateVersionRepository versionRepo;
    private final InspectionTemplateItemRepository itemRepo;

    public InspectionTemplateService(InspectionTemplateRepository templateRepo,
                                     InspectionTemplateVersionRepository versionRepo,
                                     InspectionTemplateItemRepository itemRepo) {
        this.templateRepo = templateRepo;
        this.versionRepo = versionRepo;
        this.itemRepo = itemRepo;
    }

    public List<InspectionTemplate> listTemplates(String status) {
        if (status != null && !status.isEmpty()) {
            return templateRepo.findByStatusOrderByIdDesc(status);
        }
        return templateRepo.findAllByOrderByIdDesc();
    }

    public Optional<InspectionTemplate> getTemplate(Long id) {
        return templateRepo.findById(id);
    }

    public Optional<InspectionTemplateVersion> getLatestVersion(Long templateId) {
        return versionRepo.findFirstByTemplateIdOrderByVersionDesc(templateId);
    }

    public List<InspectionTemplateVersion> listVersions(Long templateId) {
        return versionRepo.findByTemplateIdOrderByVersionDesc(templateId);
    }

    public List<InspectionTemplateItem> getVersionItems(Long versionId) {
        return itemRepo.findByVersionIdOrderBySortOrderAsc(versionId);
    }

    public Optional<InspectionTemplateVersion> getVersion(Long versionId) {
        return versionRepo.findById(versionId);
    }

    @Transactional
    public InspectionTemplate createTemplate(String name, String description, String category,
                                             List<InspectionTemplateItem> items, String createdBy) {
        InspectionTemplate template = new InspectionTemplate();
        template.setName(name);
        template.setDescription(description == null ? "" : description);
        template.setCategory(category == null ? "" : category);
        template.setCurrentVersion(1);
        template.setStatus("active");
        template = templateRepo.save(template);

        createVersion(template.getId(), 1, "初始版本", createdBy, items);

        return template;
    }

    @Transactional
    public InspectionTemplateVersion createNewVersion(Long templateId, String changeLog,
                                                      String createdBy,
                                                      List<InspectionTemplateItem> items) {
        InspectionTemplate template = templateRepo.findById(templateId).orElseThrow(
                () -> new IllegalArgumentException("模板不存在"));

        int newVersion = template.getCurrentVersion() + 1;
        InspectionTemplateVersion version = createVersion(templateId, newVersion,
                changeLog, createdBy, items);

        template.setCurrentVersion(newVersion);
        template.setUpdatedAt(LocalDateTime.now());
        templateRepo.save(template);

        return version;
    }

    private InspectionTemplateVersion createVersion(Long templateId, int versionNum,
                                                    String changeLog, String createdBy,
                                                    List<InspectionTemplateItem> items) {
        InspectionTemplateVersion version = new InspectionTemplateVersion();
        version.setTemplateId(templateId);
        version.setVersion(versionNum);
        version.setChangeLog(changeLog == null ? "" : changeLog);
        version.setCreatedBy(createdBy == null ? "" : createdBy);
        version = versionRepo.save(version);

        if (items != null) {
            int sortOrder = 0;
            for (InspectionTemplateItem item : items) {
                item.setVersionId(version.getId());
                item.setSortOrder(sortOrder++);
                itemRepo.save(item);
            }
        }

        return version;
    }

    @Transactional
    public InspectionTemplate updateTemplate(Long id, String name, String description,
                                             String category, String status) {
        InspectionTemplate template = templateRepo.findById(id).orElseThrow(
                () -> new IllegalArgumentException("模板不存在"));

        if (name != null && !name.isEmpty()) {
            template.setName(name);
        }
        if (description != null) {
            template.setDescription(description);
        }
        if (category != null) {
            template.setCategory(category);
        }
        if (status != null && !status.isEmpty()) {
            template.setStatus(status);
        }
        template.setUpdatedAt(LocalDateTime.now());

        return templateRepo.save(template);
    }

    @Transactional
    public void deleteTemplate(Long id) {
        if (!templateRepo.existsById(id)) {
            throw new IllegalArgumentException("模板不存在");
        }
        templateRepo.deleteById(id);
    }

    public InspectionTemplateItem createItem(InspectionTemplateItem item) {
        return itemRepo.save(item);
    }

    public Optional<InspectionTemplateItem> getItem(Long itemId) {
        return itemRepo.findById(itemId);
    }

    @Transactional
    public void deleteItem(Long itemId) {
        itemRepo.deleteById(itemId);
    }
}
