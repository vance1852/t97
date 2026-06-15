package com.admin.equipment.repo;

import com.admin.equipment.model.InspectionPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface InspectionPlanRepository extends JpaRepository<InspectionPlan, Long> {
    List<InspectionPlan> findAllByOrderByIdDesc();
    List<InspectionPlan> findByStatusOrderByIdDesc(String status);
    List<InspectionPlan> findByTemplateId(Long templateId);
    List<InspectionPlan> findByStatusAndNextRunAtBefore(String status, LocalDateTime dateTime);
}
