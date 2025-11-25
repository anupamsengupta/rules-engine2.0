/**
 * Canonical multi-tenant domain model for the rule-based validation engine.
 * 
 * <h2>Overview</h2>
 * 
 * This package contains the core domain model for a multi-tenant validation engine.
 * All types are designed to be framework-agnostic (no Spring dependencies) and
 * explicitly include tenantId for multi-tenant isolation.
 * 
 * <h2>Core Concepts</h2>
 * 
 * <h3>RuleSet</h3>
 * A collection of rules grouped together for a specific validation purpose.
 * Rule sets are versioned and have a unique code within a tenant.
 * 
 * <h3>Rule (Sealed Interface)</h3>
 * A validation rule that can be either:
 * <ul>
 *   <li>{@link ExpressionRule} - Evaluates a boolean expression (e.g., SpEL)</li>
 *   <li>{@link GroovyScriptRule} - Executes a Groovy script from various sources</li>
 * </ul>
 * 
 * Rules have severity levels (INFO, WARN, ERROR) and can be tagged with
 * applicable contexts to control when they are evaluated.
 * 
 * <h3>ValidationContext</h3>
 * The input for rule evaluation, containing:
 * <ul>
 *   <li>payload - The primary object being validated (generic type)</li>
 *   <li>contextAttributes - Additional contextual data (userId, market, country, etc.)</li>
 * </ul>
 * 
 * <h3>RuleResult</h3>
 * The result of evaluating a single rule, containing:
 * <ul>
 *   <li>Rule identification (id, tenantId, code, name)</li>
 *   <li>Evaluation status (PASSED, FAILED, SKIPPED, ERROR)</li>
 *   <li>Message and optional details</li>
 *   <li>Error information if evaluation failed</li>
 * </ul>
 * 
 * <h3>RuleSetResult</h3>
 * The result of evaluating a complete rule set, containing:
 * <ul>
 *   <li>Rule set identification</li>
 *   <li>Overall status (PASS, WARN, FAIL, ERROR)</li>
 *   <li>List of individual rule results</li>
 * </ul>
 * 
 * <h2>Multi-Tenancy</h2>
 * 
 * All domain types explicitly include a {@code tenantId} field to ensure
 * tenant isolation at every level:
 * <ul>
 *   <li>Rule sets are unique per tenant: (tenantId, code, version)</li>
 *   <li>Rules belong to a tenant and are evaluated in tenant context</li>
 *   <li>Results include tenantId for logging and tracking</li>
 * </ul>
 * 
 * <h2>Usage Example</h2>
 * 
 * <pre>{@code
 * // Create a rule set
 * RuleSet ruleSet = RuleSet.builder()
 *     .tenantId("tenant-123")
 *     .code("customer-onboarding")
 *     .version("1.0")
 *     .name("Customer Onboarding Rules")
 *     .active(true)
 *     .rules(List.of(
 *         ExpressionRule.builder()
 *             .tenantId("tenant-123")
 *             .ruleCode("age-check")
 *             .name("Age Validation")
 *             .expression("payload.age >= 18")
 *             .severity(Severity.ERROR)
 *             .build()
 *     ))
 *     .build();
 * 
 * // Create validation context
 * Customer customer = new Customer("John", 25);
 * ValidationContext<Customer> context = new ValidationContext<>(
 *     customer,
 *     Map.of("userId", "user-456", "market", "US")
 * );
 * 
 * // Evaluate (via ValidationEngine)
 * RuleSetResult result = validationEngine.evaluate(ruleSet, context);
 * }</pre>
 * 
 * @author Validation Engine Team
 * @version 2.0
 */
package com.quickysoft.validation.core.model;

