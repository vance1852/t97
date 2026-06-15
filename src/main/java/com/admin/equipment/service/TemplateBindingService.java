package com.admin.equipment.service;

import com.admin.equipment.model.Equipment;
import com.admin.equipment.model.InspectionTemplate;
import com.admin.equipment.model.InspectionTemplateVersion;
import com.admin.equipment.model.TemplateBinding;
import com.admin.equipment.repo.EquipmentRepository;
import com.admin.equipment.repo.InspectionTemplateRepository;
import com.admin.equipment.repo.InspectionTemplateVersionRepository;
import com.admin.equipment.repo.TemplateBindingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TemplateBindingService {

    private final TemplateBindingRepository bindingRepo;
    private final EquipmentRepository equipmentRepo;
    private final InspectionTemplateRepository templateRepo;
    private final InspectionTemplateVersionRepository versionRepo;

    public TemplateBindingService(TemplateBindingRepository bindingRepo,
                                  EquipmentRepository equipmentRepo,
                                  InspectionTemplateRepository templateRepo,
                                  InspectionTemplateVersionRepository versionRepo) {
        this.bindingRepo = bindingRepo;
        this.equipmentRepo = equipmentRepo;
        this.templateRepo = templateRepo;
        this.versionRepo = versionRepo;
    }

    public Optional<InspectionTemplate> getTemplateForEquipment(Long equipmentId) {
        TemplateBinding specificBinding = bindingRepo.findByEquipmentId(equipmentId).orElse(null);
        if (specificBinding != null) {
            return templateRepo.findById(specificBinding.getTemplateId());
        }

        Equipment equipment = equipmentRepo.findById(equipmentId).orElse(null);
        if (equipment != null && equipment.getType() != null && !equipment.getType().isEmpty()) {
            TemplateBinding typeBinding = bindingRepo
                    .findByEquipmentTypeAndEquipmentIdIsNull(equipment.getType())
                    .orElse(null);
            if (typeBinding != null) {
                return templateRepo.findById(typeBinding.getTemplateId());
            }
        }

        return Optional.empty();
    }

    public Optional<InspectionTemplateVersion> getLatestVersionForEquipment(Long equipmentId) {
        return getTemplateForEquipment(equipmentId)
                .flatMap(t -> versionRepo.findFirstByTemplateIdOrderByVersionDesc(t.getId()));
    }

    public List<TemplateBinding> listBindingsByTemplate(Long templateId) {
        return bindingRepo.findByTemplateId(templateId);
    }

    public List<TemplateBinding> listBindingsByEquipmentType(String equipmentType) {
        return bindingRepo.findByEquipmentType(equipmentType);
    }

    public Optional<TemplateBinding> getBindingByEquipment(Long equipmentId) {
        return bindingRepo.findByEquipmentId(equipmentId);
    }

    @Transactional
    public TemplateBinding bindToEquipment(Long templateId, Long equipmentId) {
        if (!templateRepo.existsById(templateId)) {
            throw new IllegalArgumentException("模板不存在");
        }
        if (!equipmentRepo.existsById(equipmentId)) {
            throw new IllegalArgumentException("设备不存在");
        }

        TemplateBinding existing = bindingRepo.findByEquipmentId(equipmentId).orElse(null);
        if (existing != null) {
            existing.setTemplateId(templateId);
            return bindingRepo.save(existing);
        }

        TemplateBinding binding = new TemplateBinding();
        binding.setTemplateId(templateId);
        binding.setEquipmentId(equipmentId);
        return bindingRepo.save(binding);
    }

    @Transactional
    public TemplateBinding bindToEquipmentType(Long templateId, String equipmentType) {
        if (!templateRepo.existsById(templateId)) {
            throw new IllegalArgumentException("模板不存在");
        }
        if (equipmentType == null || equipmentType.isEmpty()) {
            throw new IllegalArgumentException("设备类型不能为空");
        }

        TemplateBinding existing = bindingRepo
                .findByEquipmentTypeAndEquipmentIdIsNull(equipmentType)
                .orElse(null);
        if (existing != null) {
            existing.setTemplateId(templateId);
            return bindingRepo.save(existing);
        }

        TemplateBinding binding = new TemplateBinding();
        binding.setTemplateId(templateId);
        binding.setEquipmentType(equipmentType);
        return bindingRepo.save(binding);
    }

    @Transactional
    public void unbindFromEquipment(Long equipmentId) {
        bindingRepo.deleteByEquipmentId(equipmentId);
    }

    @Transactional
    public void unbindFromEquipmentType(String equipmentType) {
        TemplateBinding binding = bindingRepo
                .findByEquipmentTypeAndEquipmentIdIsNull(equipmentType)
                .orElse(null);
        if (binding != null) {
            bindingRepo.delete(binding);
        }
    }

    @Transactional
    public void unbindTemplate(Long templateId) {
        bindingRepo.deleteByTemplateId(templateId);
    }
}
