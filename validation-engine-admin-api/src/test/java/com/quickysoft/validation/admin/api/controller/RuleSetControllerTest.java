package com.quickysoft.validation.admin.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickysoft.validation.admin.api.dto.RuleRequest;
import com.quickysoft.validation.admin.api.dto.RuleSetRequest;
import com.quickysoft.validation.core.model.Severity;
import com.quickysoft.validation.core.model.ScriptLocationType;
import com.quickysoft.validation.persistence.cache.RuleSetCache;
import com.quickysoft.validation.persistence.entity.RuleEntity;
import com.quickysoft.validation.persistence.entity.RuleSetEntity;
import com.quickysoft.validation.persistence.entity.RuleType;
import com.quickysoft.validation.persistence.repository.RuleSetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.data.redis.host=localhost",
        "spring.data.redis.port=6379"
})
@Transactional
class RuleSetControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private RuleSetRepository ruleSetRepository;
    
    @MockBean
    private RuleSetCache ruleSetCache;
    
    private String tenantId = "tenant-123";
    private String ruleSetCode = "customer-onboarding";
    private String version = "1.0";
    
    @BeforeEach
    void setUp() {
        ruleSetRepository.deleteAll();
        reset(ruleSetCache);
    }
    
    @Test
    void testCreateRuleSet() throws Exception {
        RuleSetRequest request = new RuleSetRequest(
                ruleSetCode,
                "Customer Onboarding Rules",
                "Rules for customer onboarding",
                version,
                true,
                List.of(
                        new RuleRequest(
                                "age-check",
                                "Age Validation",
                                "Check if customer is 18 or older",
                                1,
                                true,
                                Severity.ERROR,
                                RuleRequest.RuleType.EXPRESSION,
                                "payload.age >= 18",
                                null,
                                null,
                                null,
                                Set.of(),
                                null
                        )
                ),
                "admin",
                null
        );
        
        mockMvc.perform(post("/tenants/{tenantId}/rulesets", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(ruleSetCode))
                .andExpect(jsonPath("$.name").value("Customer Onboarding Rules"))
                .andExpect(jsonPath("$.rules").isArray())
                .andExpect(jsonPath("$.rules[0].ruleCode").value("age-check"));
        
        // Verify cache was updated
        verify(ruleSetCache, times(1)).putRuleSet(any());
    }
    
    @Test
    void testGetRuleSet() throws Exception {
        // Create a rule set first
        RuleSetEntity entity = new RuleSetEntity();
        entity.setTenantId(tenantId);
        entity.setCode(ruleSetCode);
        entity.setName("Test Rules");
        entity.setVersion(version);
        entity.setActive(true);
        
        RuleEntity rule = new RuleEntity();
        rule.setTenantId(tenantId);
        rule.setRuleSet(entity);
        rule.setRuleCode("test-rule");
        rule.setName("Test Rule");
        rule.setPriority(1);
        rule.setEnabled(true);
        rule.setSeverity(Severity.ERROR);
        rule.setRuleType(RuleType.EXPRESSION);
        rule.setExpression("true");
        
        entity.setRules(List.of(rule));
        ruleSetRepository.save(entity);
        
        mockMvc.perform(get("/tenants/{tenantId}/rulesets/{code}/versions/{version}",
                        tenantId, ruleSetCode, version))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ruleSetCode))
                .andExpect(jsonPath("$.rules").isArray());
    }
    
    @Test
    void testListRuleSets() throws Exception {
        // Create multiple rule sets
        for (int i = 1; i <= 3; i++) {
            RuleSetEntity entity = new RuleSetEntity();
            entity.setTenantId(tenantId);
            entity.setCode("ruleset-" + i);
            entity.setName("Rule Set " + i);
            entity.setVersion("1.0");
            entity.setActive(true);
            ruleSetRepository.save(entity);
        }
        
        mockMvc.perform(get("/tenants/{tenantId}/rulesets", tenantId)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(3));
    }
    
    @Test
    void testUpdateRuleSet() throws Exception {
        // Create a rule set first
        RuleSetEntity entity = new RuleSetEntity();
        entity.setTenantId(tenantId);
        entity.setCode(ruleSetCode);
        entity.setName("Original Name");
        entity.setVersion(version);
        entity.setActive(true);
        ruleSetRepository.save(entity);
        
        RuleSetRequest request = new RuleSetRequest(
                ruleSetCode,
                "Updated Name",
                "Updated description",
                version,
                true,
                List.of(),
                null,
                "admin"
        );
        
        mockMvc.perform(put("/tenants/{tenantId}/rulesets/{code}/versions/{version}",
                        tenantId, ruleSetCode, version)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
        
        // Verify cache was evicted and updated
        verify(ruleSetCache, times(1)).evictRuleSet(tenantId, ruleSetCode, version);
        verify(ruleSetCache, times(1)).putRuleSet(any());
    }
    
    @Test
    void testDeleteRuleSet() throws Exception {
        // Create a rule set first
        RuleSetEntity entity = new RuleSetEntity();
        entity.setTenantId(tenantId);
        entity.setCode(ruleSetCode);
        entity.setName("Test Rules");
        entity.setVersion(version);
        entity.setActive(true);
        ruleSetRepository.save(entity);
        
        mockMvc.perform(delete("/tenants/{tenantId}/rulesets/{code}/versions/{version}",
                        tenantId, ruleSetCode, version))
                .andExpect(status().isNoContent());
        
        // Verify cache was evicted
        verify(ruleSetCache, times(1)).evictRuleSet(tenantId, ruleSetCode, version);
    }
    
    @Test
    void testTenantIsolation() throws Exception {
        // Create rule set for tenant-123
        RuleSetEntity entity1 = new RuleSetEntity();
        entity1.setTenantId("tenant-123");
        entity1.setCode(ruleSetCode);
        entity1.setName("Tenant 123 Rules");
        entity1.setVersion(version);
        entity1.setActive(true);
        ruleSetRepository.save(entity1);
        
        // Try to access from tenant-456
        mockMvc.perform(get("/tenants/tenant-456/rulesets/{code}/versions/{version}",
                        ruleSetCode, version))
                .andExpect(status().isNotFound());
    }
}

