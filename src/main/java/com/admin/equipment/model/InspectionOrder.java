package com.admin.equipment.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "inspection_orders")
public class InspectionOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_no", unique = true, length = 32)
    private String orderNo;

    @Column(name = "equipment_id", nullable = false)
    private Long equipmentId;

    @Column(name = "template_id", nullable = false)
    private Long templateId;

    @Column(name = "template_version_id", nullable = false)
    private Long templateVersionId;

    @Column(name = "template_version")
    private Integer templateVersion;

    @Column(length = 16)
    private String status = "draft";

    @Column(name = "is_passed")
    private Boolean isPassed;

    @Column(name = "executor", length = 64)
    private String executor = "";

    @Column(name = "executed_at")
    private LocalDateTime executedAt;

    @Column(name = "remark", length = 512)
    private String remark = "";

    @Column(name = "source_type", length = 16)
    private String sourceType = "manual";

    @Column(name = "source_id")
    private Long sourceId;

    @Column(name = "parent_order_id")
    private Long parentOrderId;

    @Column(name = "reinspection_count")
    private Integer reinspectionCount = 0;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Long getEquipmentId() { return equipmentId; }
    public void setEquipmentId(Long equipmentId) { this.equipmentId = equipmentId; }
    public Long getTemplateId() { return templateId; }
    public void setTemplateId(Long templateId) { this.templateId = templateId; }
    public Long getTemplateVersionId() { return templateVersionId; }
    public void setTemplateVersionId(Long templateVersionId) { this.templateVersionId = templateVersionId; }
    public Integer getTemplateVersion() { return templateVersion; }
    public void setTemplateVersion(Integer templateVersion) { this.templateVersion = templateVersion; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Boolean getIsPassed() { return isPassed; }
    public void setIsPassed(Boolean isPassed) { this.isPassed = isPassed; }
    public String getExecutor() { return executor; }
    public void setExecutor(String executor) { this.executor = executor; }
    public LocalDateTime getExecutedAt() { return executedAt; }
    public void setExecutedAt(LocalDateTime executedAt) { this.executedAt = executedAt; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public Long getSourceId() { return sourceId; }
    public void setSourceId(Long sourceId) { this.sourceId = sourceId; }
    public Long getParentOrderId() { return parentOrderId; }
    public void setParentOrderId(Long parentOrderId) { this.parentOrderId = parentOrderId; }
    public Integer getReinspectionCount() { return reinspectionCount; }
    public void setReinspectionCount(Integer reinspectionCount) { this.reinspectionCount = reinspectionCount; }
    public LocalDateTime getClosedAt() { return closedAt; }
    public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
