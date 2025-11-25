package com.quickysoft.validationengine.persistence.entity;

import com.quickysoft.validationengine.core.domain.RuleType;
import jakarta.persistence.*;
import java.time.Instant;

/**
 * JPA entity for Rule.
 */
@Entity
@Table(name = "rules", uniqueConstraints = {
    @UniqueConstraint(name = "uk_rule_tenant_set_code", 
                     columnNames = {"tenant_id", "rule_set_id", "rule_code"})
})
public class RuleEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "tenant_id", nullable = false, length = 100)
    private String tenantId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_set_id", nullable = false)
    private RuleSetEntity ruleSet;
    
    @Column(name = "rule_code", nullable = false, length = 100)
    private String ruleCode;
    
    @Column(name = "name", nullable = false, length = 255)
    private String name;
    
    @Column(name = "description", length = 1000)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private RuleType type;
    
    @Column(name = "expression", columnDefinition = "TEXT")
    private String expression;
    
    @Column(name = "script_location", length = 500)
    private String scriptLocation;
    
    @Column(name = "priority")
    private Integer priority;
    
    @Column(name = "active", nullable = false)
    private Boolean active = true;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    @Column(name = "created_by", length = 100)
    private String createdBy;
    
    @Column(name = "updated_by", length = 100)
    private String updatedBy;
    
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
    
    // Getters and setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTenantId() {
        return tenantId;
    }
    
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
    
    public RuleSetEntity getRuleSet() {
        return ruleSet;
    }
    
    public void setRuleSet(RuleSetEntity ruleSet) {
        this.ruleSet = ruleSet;
    }
    
    public String getRuleCode() {
        return ruleCode;
    }
    
    public void setRuleCode(String ruleCode) {
        this.ruleCode = ruleCode;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public RuleType getType() {
        return type;
    }
    
    public void setType(RuleType type) {
        this.type = type;
    }
    
    public String getExpression() {
        return expression;
    }
    
    public void setExpression(String expression) {
        this.expression = expression;
    }
    
    public String getScriptLocation() {
        return scriptLocation;
    }
    
    public void setScriptLocation(String scriptLocation) {
        this.scriptLocation = scriptLocation;
    }
    
    public Integer getPriority() {
        return priority;
    }
    
    public void setPriority(Integer priority) {
        this.priority = priority;
    }
    
    public Boolean getActive() {
        return active;
    }
    
    public void setActive(Boolean active) {
        this.active = active;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
    
    public Instant getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public String getUpdatedBy() {
        return updatedBy;
    }
    
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}

