package com.validationengine.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity for RuleSet.
 */
@Entity
@Table(name = "rule_sets", uniqueConstraints = {
    @UniqueConstraint(name = "uk_rule_set_tenant_code_version", 
                     columnNames = {"tenant_id", "rule_set_code", "version"})
})
public class RuleSetEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "tenant_id", nullable = false, length = 100)
    private String tenantId;
    
    @Column(name = "rule_set_code", nullable = false, length = 100)
    private String ruleSetCode;
    
    @Column(name = "version", nullable = false, length = 50)
    private String version;
    
    @Column(name = "name", nullable = false, length = 255)
    private String name;
    
    @Column(name = "description", length = 1000)
    private String description;
    
    @OneToMany(mappedBy = "ruleSet", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("priority ASC, ruleCode ASC")
    private List<RuleEntity> rules = new ArrayList<>();
    
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
    
    public String getRuleSetCode() {
        return ruleSetCode;
    }
    
    public void setRuleSetCode(String ruleSetCode) {
        this.ruleSetCode = ruleSetCode;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
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
    
    public List<RuleEntity> getRules() {
        return rules;
    }
    
    public void setRules(List<RuleEntity> rules) {
        this.rules = rules;
        if (rules != null) {
            rules.forEach(rule -> rule.setRuleSet(this));
        }
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

