package com.admin.equipment.repo;

import com.admin.equipment.model.InspectionTemplateVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InspectionTemplateVersionRepository extends JpaRepository<InspectionTemplateVersion, Long> {
    List<InspectionTemplateVersion> findByTemplateIdOrderByVersionDesc(Long templateId);
    Optional<InspectionTemplateVersion> findByTemplateIdAndVersion(Long templateId, Integer version);
    Optional<InspectionTemplateVersion> findFirstByTemplateIdOrderByVersionDesc(Long templateId);
}
