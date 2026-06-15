package com.admin.equipment.repo;

import com.admin.equipment.model.InspectionResultItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InspectionResultItemRepository extends JpaRepository<InspectionResultItem, Long> {
    List<InspectionResultItem> findByOrderIdOrderBySortOrderAsc(Long orderId);
    List<InspectionResultItem> findByOrderIdAndIsAbnormalTrue(Long orderId);
    List<InspectionResultItem> findByWorkOrderId(Long workOrderId);
    void deleteByOrderId(Long orderId);

    @Query("SELECT r.itemKey, r.itemName, COUNT(r) AS cnt FROM InspectionResultItem r " +
           "WHERE r.isAbnormal = true AND r.orderId IN " +
           "(SELECT o.id FROM InspectionOrder o WHERE o.createdAt BETWEEN :start AND :end) " +
           "GROUP BY r.itemKey, r.itemName ORDER BY cnt DESC")
    List<Object[]> countAbnormalItems(@Param("start") java.time.LocalDateTime start,
                                      @Param("end") java.time.LocalDateTime end);

    @Query("SELECT COUNT(r) FROM InspectionResultItem r WHERE r.isAbnormal = true AND r.orderId IN " +
           "(SELECT o.id FROM InspectionOrder o WHERE o.createdAt BETWEEN :start AND :end)")
    long countAbnormalItemsTotal(@Param("start") java.time.LocalDateTime start,
                                 @Param("end") java.time.LocalDateTime end);
}
