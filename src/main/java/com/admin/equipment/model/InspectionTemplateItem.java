package com.admin.equipment.model;

import jakarta.persistence.*;

@Entity
@Table(name = "inspection_template_items")
public class InspectionTemplateItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "version_id", nullable = false)
    private Long versionId;

    @Column(name = "item_key", nullable = false, length = 64)
    private String itemKey;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(length = 16)
    private String type = "text";

    @Column(length = 32)
    private String unit = "";

    @Column(length = 256)
    private String prompt = "";

    @Column(nullable = false)
    private Boolean required = true;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "numeric_min")
    private Double numericMin;

    @Column(name = "numeric_max")
    private Double numericMax;

    @Column(name = "option_values", length = 1024)
    private String optionValues;

    @Column(name = "pass_options", length = 1024)
    private String passOptions;

    @Column(name = "boolean_pass_value")
    private Boolean booleanPassValue = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getVersionId() { return versionId; }
    public void setVersionId(Long versionId) { this.versionId = versionId; }
    public String getItemKey() { return itemKey; }
    public void setItemKey(String itemKey) { this.itemKey = itemKey; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }
    public Boolean getRequired() { return required; }
    public void setRequired(Boolean required) { this.required = required; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Double getNumericMin() { return numericMin; }
    public void setNumericMin(Double numericMin) { this.numericMin = numericMin; }
    public Double getNumericMax() { return numericMax; }
    public void setNumericMax(Double numericMax) { this.numericMax = numericMax; }
    public String getOptionValues() { return optionValues; }
    public void setOptionValues(String optionValues) { this.optionValues = optionValues; }
    public String getPassOptions() { return passOptions; }
    public void setPassOptions(String passOptions) { this.passOptions = passOptions; }
    public Boolean getBooleanPassValue() { return booleanPassValue; }
    public void setBooleanPassValue(Boolean booleanPassValue) { this.booleanPassValue = booleanPassValue; }
}
