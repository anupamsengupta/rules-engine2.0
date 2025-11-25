package com.quickysoft.validation.core.engine;

import com.quickysoft.validation.core.provider.RuleSetProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory for creating ValidationEngine instances with default configurations.
 * 
 * Provides a convenient way to construct a fully configured ValidationEngine
 * with all necessary executors and loaders.
 */
public class ValidationEngineFactory {
    
    /**
     * Creates a ValidationEngine with default configuration.
     * 
     * Includes:
     * - ExpressionRuleExecutor
     * - GroovyScriptRuleExecutor with file system and S3 script loaders
     * - Tenant-aware Groovy script caching
     * 
     * @param ruleSetProvider the rule set provider
     * @return a configured ValidationEngine
     */
    public static ValidationEngine createDefault(RuleSetProvider ruleSetProvider) {
        // Create script cache
        GroovyScriptCache scriptCache = new GroovyScriptCache();
        
        // Create script loaders
        CompositeScriptLoader scriptLoader = new CompositeScriptLoader();
        scriptLoader.addLoader(new FileSystemScriptLoader());
        scriptLoader.addLoader(new S3ScriptLoader());
        
        // Create rule executors
        List<RuleExecutor> executors = new ArrayList<>();
        executors.add(new ExpressionRuleExecutor());
        executors.add(new GroovyScriptRuleExecutor(scriptLoader, scriptCache));
        
        // Create result calculator
        RuleSetResultCalculator calculator = new RuleSetResultCalculator();
        
        // Create and return engine
        return new DefaultValidationEngine(ruleSetProvider, executors, calculator);
    }
    
    /**
     * Creates a ValidationEngine with custom configuration.
     * 
     * @param ruleSetProvider the rule set provider
     * @param executors custom rule executors
     * @param scriptLoader custom script loader (can be composite)
     * @param scriptCache custom script cache (optional, creates default if null)
     * @return a configured ValidationEngine
     */
    public static ValidationEngine create(
            RuleSetProvider ruleSetProvider,
            List<RuleExecutor> executors,
            ScriptLoader scriptLoader,
            GroovyScriptCache scriptCache
    ) {
        // Ensure we have a script cache
        if (scriptCache == null) {
            scriptCache = new GroovyScriptCache();
        }
        
        // Ensure GroovyScriptRuleExecutor is in the executors list if script loader is provided
        if (scriptLoader != null) {
            boolean hasGroovyExecutor = executors.stream()
                    .anyMatch(e -> e instanceof GroovyScriptRuleExecutor);
            
            if (!hasGroovyExecutor) {
                executors = new ArrayList<>(executors);
                executors.add(new GroovyScriptRuleExecutor(scriptLoader, scriptCache));
            }
        }
        
        RuleSetResultCalculator calculator = new RuleSetResultCalculator();
        return new DefaultValidationEngine(ruleSetProvider, executors, calculator);
    }
}

