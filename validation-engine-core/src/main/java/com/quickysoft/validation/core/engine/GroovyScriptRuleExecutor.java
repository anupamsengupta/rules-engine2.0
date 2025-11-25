package com.quickysoft.validation.core.engine;

import com.quickysoft.validation.core.model.GroovyScriptRule;
import com.quickysoft.validation.core.model.Rule;
import com.quickysoft.validation.core.model.RuleResult;
import com.quickysoft.validation.core.model.RuleStatus;
import com.quickysoft.validation.core.model.ScriptLocationType;
import com.quickysoft.validation.core.model.ValidationContext;
import groovy.lang.Script;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Executor for Groovy script-based rules.
 * 
 * Loads and executes Groovy scripts with tenant-aware caching of compiled scripts.
 */
public class GroovyScriptRuleExecutor implements RuleExecutor {
    
    private static final Logger logger = LoggerFactory.getLogger(GroovyScriptRuleExecutor.class);
    
    private final ScriptLoader scriptLoader;
    private final GroovyScriptCache scriptCache;
    
    public GroovyScriptRuleExecutor(ScriptLoader scriptLoader, GroovyScriptCache scriptCache) {
        this.scriptLoader = scriptLoader;
        this.scriptCache = scriptCache;
    }
    
    @Override
    public RuleResult execute(Rule rule, ValidationContext<?> context) throws RuleExecutionException {
        return execute(rule, context, null);
    }
    
    /**
     * Executes a rule with the rule set code for better execution context.
     */
    public RuleResult execute(Rule rule, ValidationContext<?> context, String ruleSetCode) throws RuleExecutionException {
        if (!(rule instanceof GroovyScriptRule groovyRule)) {
            throw new IllegalArgumentException("Rule must be a GroovyScriptRule");
        }
        
        try {
            // Load script content
            String scriptContent = loadScriptContent(groovyRule);
            
            // Get or compile script class (with caching)
            Class<? extends Script> scriptClass = scriptCache.getOrCompile(
                    rule.tenantId(),
                    groovyRule.scriptReference(),
                    scriptContent
            );
            
            // Create script instance
            Script script = scriptClass.getDeclaredConstructor().newInstance();
            
            // Create execution context
            RuleExecutionContext executionContext = new RuleExecutionContext(
                    rule.id(),
                    rule.tenantId(),
                    ruleSetCode != null ? ruleSetCode : "unknown",
                    rule.ruleCode(),
                    rule.name(),
                    rule.metadata()
            );
            
            // Bind context variables
            bindContextVariables(script, context, executionContext);
            
            // Execute script
            Object result = script.run();
            
            // Evaluate result (script can return boolean or map)
            return evaluateResult(rule, result, groovyRule, context);
        } catch (ScriptLoadException e) {
            logger.error("Failed to load Groovy script for rule {} (tenantId: {}, ruleSetCode: {}, scriptReference: {}): {}", 
                    rule.ruleCode(), rule.tenantId(), ruleSetCode != null ? ruleSetCode : "unknown", 
                    groovyRule.scriptReference(), e.getMessage(), e);
            return RuleResult.error(
                    rule.id(),
                    rule.tenantId(),
                    rule.ruleCode(),
                    rule.name(),
                    e
            );
        } catch (Exception e) {
            logger.error("Error executing Groovy script rule {} (tenantId: {}, ruleSetCode: {}, scriptReference: {}): {}", 
                    rule.ruleCode(), rule.tenantId(), ruleSetCode != null ? ruleSetCode : "unknown",
                    groovyRule.scriptReference(), e.getMessage(), e);
            return RuleResult.error(
                    rule.id(),
                    rule.tenantId(),
                    rule.ruleCode(),
                    rule.name(),
                    e
            );
        }
    }
    
    @Override
    public boolean supports(Rule rule) {
        return rule instanceof GroovyScriptRule;
    }
    
    /**
     * Loads script content based on the script location type.
     */
    private String loadScriptContent(GroovyScriptRule rule) throws ScriptLoadException {
        if (rule.scriptLocationType() == ScriptLocationType.INLINE) {
            // For inline scripts, the scriptReference contains the script content directly
            return rule.scriptReference();
        } else {
            // For file system or S3, use the script loader
            if (!scriptLoader.supports(rule.scriptReference())) {
                throw new ScriptLoadException(
                        "No script loader supports location: " + rule.scriptReference()
                );
            }
            return scriptLoader.loadScript(rule.tenantId(), rule.scriptReference());
        }
    }
    
    /**
     * Binds context variables to the Groovy script.
     * 
     * Scripts can access:
     * - payload: the validation payload
     * - context: the context attributes map
     * - executionContext: the rule execution context (includes tenantId, ruleSetCode, etc.)
     * - tenantId: the tenant identifier (for convenience)
     */
    private void bindContextVariables(Script script, ValidationContext<?> context, RuleExecutionContext executionContext) {
        script.setProperty("payload", context.payload());
        script.setProperty("context", context.contextAttributes());
        script.setProperty("executionContext", executionContext);
        script.setProperty("tenantId", executionContext.tenantId());
    }
    
    /**
     * Evaluates the script result.
     * 
     * Scripts can return:
     * - boolean/Boolean: true = pass, false = fail
     * - Map with keys: status (boolean), message (String), details (Map)
     */
    @SuppressWarnings("unchecked")
    private RuleResult evaluateResult(
            Rule rule, 
            Object result, 
            GroovyScriptRule groovyRule, 
            ValidationContext<?> context
    ) {
        if (result == null) {
            return RuleResult.failed(
                    rule.id(),
                    rule.tenantId(),
                    rule.ruleCode(),
                    rule.name(),
                    rule.severity(),
                    "Script returned null"
            );
        }
        
        // Handle boolean result
        if (result instanceof Boolean bool) {
            if (bool) {
                return RuleResult.passed(
                        rule.id(),
                        rule.tenantId(),
                        rule.ruleCode(),
                        rule.name(),
                        rule.severity()
                );
            } else {
                String message = formatFailureMessage(groovyRule, context);
                return RuleResult.failed(
                        rule.id(),
                        rule.tenantId(),
                        rule.ruleCode(),
                        rule.name(),
                        rule.severity(),
                        message
                );
            }
        }
        
        // Handle map result
        if (result instanceof Map<?, ?> resultMap) {
            Map<String, Object> map = (Map<String, Object>) resultMap;
            Boolean status = extractStatus(map);
            String message = extractMessage(map, groovyRule, context);
            Map<String, Object> details = extractDetails(map);
            
            if (Boolean.TRUE.equals(status)) {
                return RuleResult.passed(
                        rule.id(),
                        rule.tenantId(),
                        rule.ruleCode(),
                        rule.name(),
                        rule.severity()
                );
            } else {
                return RuleResult.failed(
                        rule.id(),
                        rule.tenantId(),
                        rule.ruleCode(),
                        rule.name(),
                        rule.severity(),
                        message,
                        details
                );
            }
        }
        
        // Try to convert to boolean
        boolean passed = Boolean.TRUE.equals(result);
        if (passed) {
            return RuleResult.passed(
                    rule.id(),
                    rule.tenantId(),
                    rule.ruleCode(),
                    rule.name(),
                    rule.severity()
            );
        } else {
            String message = formatFailureMessage(groovyRule, context);
            return RuleResult.failed(
                    rule.id(),
                    rule.tenantId(),
                    rule.ruleCode(),
                    rule.name(),
                    rule.severity(),
                    message
            );
        }
    }
    
    private Boolean extractStatus(Map<String, Object> map) {
        Object status = map.get("status");
        if (status instanceof Boolean) {
            return (Boolean) status;
        }
        if (status instanceof String) {
            return Boolean.parseBoolean((String) status);
        }
        return false;
    }
    
    private String extractMessage(Map<String, Object> map, GroovyScriptRule rule, ValidationContext<?> context) {
        Object message = map.get("message");
        if (message instanceof String) {
            return (String) message;
        }
        return formatFailureMessage(rule, context);
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> extractDetails(Map<String, Object> map) {
        Object details = map.get("details");
        if (details instanceof Map) {
            return (Map<String, Object>) details;
        }
        return Map.of();
    }
    
    /**
     * Formats the failure message, replacing placeholders if a template is provided.
     */
    private String formatFailureMessage(GroovyScriptRule rule, ValidationContext<?> context) {
        if (rule.failureMessageTemplate() != null && !rule.failureMessageTemplate().isBlank()) {
            // TODO: Replace placeholders in template
            return rule.failureMessageTemplate();
        }
        return String.format("Rule '%s' failed", rule.name());
    }
}

