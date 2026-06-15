package com.admin.equipment.repo;

import com.admin.equipment.model.TemplateBinding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TemplateBindingRepository extends JpaRepository<TemplateBinding, Long> {
    List<TemplateBinding> findByTemplateId(Long templateId);
    List<TemplateBinding> findByEquipmentType(String equipmentType);
    Optional<TemplateBinding> findByEquipmentId(Long equipmentId);
    Optional<TemplateBinding> findByEquipmentTypeAndEquipmentIdIsNull(String equipmentType);
    void deleteByTemplateId(Long templateId);
    void deleteByEquipmentId(Long equipmentId);
    boolean existsByTemplateIdAndEquipmentId(Long templateId, Long equipmentId);
    boolean existsByTemplateIdAndEquipmentTypeAndEquipmentIdIsNull(Long templateId, String equipmentType);
}
