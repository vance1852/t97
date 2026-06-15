package com.admin.equipment.model;

import jakarta.persistence.*;

@Entity
@Table(name = "inspection_result_items")
public class InspectionResultItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(name = "item_key", length = 64)
    private String itemKey;

    @Column(name = "item_name", length = 128)
    private String itemName;

    @Column(name = "item_type", length = 16)
    private String itemType;

    @Column(name = "value_text", length = 512)
    private String valueText;

    @Column(name = "value_numeric")
    private Double valueNumeric;

    @Column(name = "value_boolean")
    private Boolean valueBoolean;

    @Column(name = "is_passed")
    private Boolean isPassed;

    @Column(name = "is_abnormal")
    private Boolean isAbnormal = false;

    @Column(name = "abnormal_remark", length = 512)
    private String abnormalRemark = "";

    @Column(name = "photo_url", length = 256)
    private String photoUrl = "";

    @Column(name = "work_order_id")
    private Long workOrderId;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    public String getItemKey() { return itemKey; }
    public void setItemKey(String itemKey) { this.itemKey = itemKey; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }
    public String getValueText() { return valueText; }
    public void setValueText(String valueText) { this.valueText = valueText; }
    public Double getValueNumeric() { return valueNumeric; }
    public void setValueNumeric(Double valueNumeric) { this.valueNumeric = valueNumeric; }
    public Boolean getValueBoolean() { return valueBoolean; }
    public void setValueBoolean(Boolean valueBoolean) { this.valueBoolean = valueBoolean; }
    public Boolean getIsPassed() { return isPassed; }
    public void setIsPassed(Boolean isPassed) { this.isPassed = isPassed; }
    public Boolean getIsAbnormal() { return isAbnormal; }
    public void setIsAbnormal(Boolean isAbnormal) { this.isAbnormal = isAbnormal; }
    public String getAbnormalRemark() { return abnormalRemark; }
    public void setAbnormalRemark(String abnormalRemark) { this.abnormalRemark = abnormalRemark; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public Long getWorkOrderId() { return workOrderId; }
    public void setWorkOrderId(Long workOrderId) { this.workOrderId = workOrderId; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
