package com.admin.equipment.repo;

import com.admin.equipment.model.InspectionOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface InspectionOrderRepository extends JpaRepository<InspectionOrder, Long> {
    List<InspectionOrder> findAllByOrderByIdDesc();
    List<InspectionOrder> findByEquipmentIdOrderByIdDesc(Long equipmentId);
    List<InspectionOrder> findByTemplateIdOrderByIdDesc(Long templateId);
    List<InspectionOrder> findByStatusOrderByIdDesc(String status);
    List<InspectionOrder> findByEquipmentIdAndStatusOrderByIdDesc(Long equipmentId, String status);
    List<InspectionOrder> findByParentOrderIdOrderByIdDesc(Long parentOrderId);

    @Query("SELECT o FROM InspectionOrder o WHERE o.createdAt BETWEEN :start AND :end ORDER BY o.id DESC")
    List<InspectionOrder> findByCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(o) FROM InspectionOrder o WHERE o.status = 'submitted' AND o.isPassed = true AND o.createdAt BETWEEN :start AND :end")
    long countPassedOrders(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(o) FROM InspectionOrder o WHERE o.status = 'submitted' AND o.createdAt BETWEEN :start AND :end")
    long countSubmittedOrders(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(o) FROM InspectionOrder o WHERE o.equipmentId = :equipmentId AND o.status = 'submitted'")
    long countByEquipmentId(@Param("equipmentId") Long equipmentId);

    @Query("SELECT COUNT(o) FROM InspectionOrder o WHERE o.templateId = :templateId AND o.status = 'submitted'")
    long countByTemplateId(@Param("templateId") Long templateId);
}
