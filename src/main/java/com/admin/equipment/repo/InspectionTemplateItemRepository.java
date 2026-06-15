package com.admin.equipment.repo;

import com.admin.equipment.model.InspectionTemplateItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InspectionTemplateItemRepository extends JpaRepository<InspectionTemplateItem, Long> {
    List<InspectionTemplateItem> findByVersionIdOrderBySortOrderAsc(Long versionId);
    List<InspectionTemplateItem> findByVersionIdOrderBySortOrderDesc(Long versionId);
    void deleteByVersionId(Long versionId);
}
