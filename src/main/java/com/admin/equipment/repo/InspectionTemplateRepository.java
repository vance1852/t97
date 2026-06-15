package com.admin.equipment.repo;

import com.admin.equipment.model.InspectionTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InspectionTemplateRepository extends JpaRepository<InspectionTemplate, Long> {
    List<InspectionTemplate> findAllByOrderByIdDesc();
    List<InspectionTemplate> findByStatusOrderByIdDesc(String status);
    Optional<InspectionTemplate> findByName(String name);
    boolean existsByName(String name);
}
