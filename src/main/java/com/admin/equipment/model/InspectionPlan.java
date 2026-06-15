package com.admin.equipment.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "inspection_plans")
public class InspectionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(name = "template_id", nullable = false)
    private Long templateId;

    @Column(name = "equipment_type", length = 32)
    private String equipmentType;

    @Column(name = "equipment_ids", length = 512)
    private String equipmentIds = "";

    @Column(length = 16)
    private String cycleType = "daily";

    @Column(name = "cycle_value")
    private Integer cycleValue = 1;

    @Column(name = "cron_expr", length = 64)
    private String cronExpr = "";

    @Column(name = "executor", length = 64)
    private String executor = "";

    @Column(length = 16)
    private String status = "active";

    @Column(name = "last_run_at")
    private LocalDateTime lastRunAt;

    @Column(name = "next_run_at")
    private LocalDateTime nextRunAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getTemplateId() { return templateId; }
    public void setTemplateId(Long templateId) { this.templateId = templateId; }
    public String getEquipmentType() { return equipmentType; }
    public void setEquipmentType(String equipmentType) { this.equipmentType = equipmentType; }
    public String getEquipmentIds() { return equipmentIds; }
    public void setEquipmentIds(String equipmentIds) { this.equipmentIds = equipmentIds; }
    public String getCycleType() { return cycleType; }
    public void setCycleType(String cycleType) { this.cycleType = cycleType; }
    public Integer getCycleValue() { return cycleValue; }
    public void setCycleValue(Integer cycleValue) { this.cycleValue = cycleValue; }
    public String getCronExpr() { return cronExpr; }
    public void setCronExpr(String cronExpr) { this.cronExpr = cronExpr; }
    public String getExecutor() { return executor; }
    public void setExecutor(String executor) { this.executor = executor; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getLastRunAt() { return lastRunAt; }
    public void setLastRunAt(LocalDateTime lastRunAt) { this.lastRunAt = lastRunAt; }
    public LocalDateTime getNextRunAt() { return nextRunAt; }
    public void setNextRunAt(LocalDateTime nextRunAt) { this.nextRunAt = nextRunAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
