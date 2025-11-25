package com.quickysoft.validation.persistence.entity;

import com.quickysoft.validation.core.model.ScriptLocationType;
import com.quickysoft.validation.core.model.Severity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity for Rule.
 * Supports both expression-based and Groovy script-based rules.
 * 
 * For Groovy script rules, includes versioning support:
 * - scriptVersion: Version identifier for the script
 * - scriptChecksum: SHA-256 checksum for script content validation
 * - updatedAt: Last modified timestamp (automatically maintained)
 */
@Entity
@Table(name = "rules", uniqueConstraints = {
    @UniqueConstraint(name = "uk_rule_tenant_set_code", 
                     columnNames = {"tenant_id", "rule_set_id", "rule_code"})
})
public class RuleEntity {
    
    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;
    
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
    
    @Column(name = "priority")
    private Integer priority;
    
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20)
    private Severity severity;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", nullable = false, length = 20)
    private RuleType ruleType;
    
    // Expression rule fields
    @Column(name = "expression", columnDefinition = "TEXT")
    private String expression;
    
    // Groovy script rule fields
    @Enumerated(EnumType.STRING)
    @Column(name = "script_location_type", length = 20)
    private ScriptLocationType scriptLocationType;
    
    @Column(name = "script_reference", length = 500)
    private String scriptReference;
    
    /**
     * Script version identifier (e.g., "1.0", "v2.3", git commit hash).
     * Used for tracking script changes and validation.
     */
    @Column(name = "script_version", length = 50)
    private String scriptVersion;
    
    /**
     * SHA-256 checksum of the script content.
     * Used to validate script integrity and detect changes.
     */
    @Column(name = "script_checksum", length = 64)
    private String scriptChecksum;
    
    // Common fields
    @Column(name = "failure_message_template", columnDefinition = "TEXT")
    private String failureMessageTemplate;
    
    @Convert(converter = MetadataConverter.class)
    @Column(name = "metadata", columnDefinition = "TEXT")
    private java.util.Map<String, String> metadata;
    
    @Column(name = "applicable_contexts", length = 500)
    private String applicableContexts; // Comma-separated or JSON array
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    /**
     * Last modified timestamp.
     * Automatically updated on entity modification.
     * For Groovy scripts, this can be used to track when the script was last updated.
     */
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    @Column(name = "created_by", length = 100)
    private String createdBy;
    
    @Column(name = "updated_by", length = 100)
    private String updatedBy;
    
    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
    
    // Getters and setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
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
    
    public Integer getPriority() {
        return priority;
    }
    
    public void setPriority(Integer priority) {
        this.priority = priority;
    }
    
    public Boolean getEnabled() {
        return enabled;
    }
    
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
    
    public Severity getSeverity() {
        return severity;
    }
    
    public void setSeverity(Severity severity) {
        this.severity = severity;
    }
    
    public RuleType getRuleType() {
        return ruleType;
    }
    
    public void setRuleType(RuleType ruleType) {
        this.ruleType = ruleType;
    }
    
    public String getExpression() {
        return expression;
    }
    
    public void setExpression(String expression) {
        this.expression = expression;
    }
    
    public ScriptLocationType getScriptLocationType() {
        return scriptLocationType;
    }
    
    public void setScriptLocationType(ScriptLocationType scriptLocationType) {
        this.scriptLocationType = scriptLocationType;
    }
    
    public String getScriptReference() {
        return scriptReference;
    }
    
    public void setScriptReference(String scriptReference) {
        this.scriptReference = scriptReference;
    }
    
    public String getScriptVersion() {
        return scriptVersion;
    }
    
    public void setScriptVersion(String scriptVersion) {
        this.scriptVersion = scriptVersion;
    }
    
    public String getScriptChecksum() {
        return scriptChecksum;
    }
    
    public void setScriptChecksum(String scriptChecksum) {
        this.scriptChecksum = scriptChecksum;
    }
    
    public String getFailureMessageTemplate() {
        return failureMessageTemplate;
    }
    
    public void setFailureMessageTemplate(String failureMessageTemplate) {
        this.failureMessageTemplate = failureMessageTemplate;
    }
    
    public java.util.Map<String, String> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(java.util.Map<String, String> metadata) {
        this.metadata = metadata;
    }
    
    public String getApplicableContexts() {
        return applicableContexts;
    }
    
    public void setApplicableContexts(String applicableContexts) {
        this.applicableContexts = applicableContexts;
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
