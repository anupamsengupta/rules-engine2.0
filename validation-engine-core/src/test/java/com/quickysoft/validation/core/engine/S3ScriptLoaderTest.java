package com.quickysoft.validation.core.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for S3ScriptLoader.
 */
class S3ScriptLoaderTest {
    
    private S3ScriptLoader loader;
    
    @BeforeEach
    void setUp() {
        loader = new S3ScriptLoader("test-bucket", "scripts/", true);
    }
    
    @Test
    void testSupports() {
        assertThat(loader.supports("s3://bucket/key")).isTrue();
        assertThat(loader.supports("scripts/my-script.groovy")).isTrue();
        assertThat(loader.supports("/absolute/path")).isFalse();
        assertThat(loader.supports("C:\\Windows\\path")).isFalse();
    }
    
    @Test
    void testGenerateS3KeyTenantScoped() {
        S3ScriptLoader tenantScopedLoader = new S3ScriptLoader("bucket", "scripts/", true);
        
        // Would test generateS3Key if it were public
        // For now, test through loadScript which will fail but show the key generation
        assertThatThrownBy(() -> tenantScopedLoader.loadScript("tenant-123", "my-script.groovy"))
                .isInstanceOf(ScriptLoadException.class)
                .hasMessageContaining("tenant-123");
    }
    
    @Test
    void testGenerateS3KeyNotTenantScoped() {
        S3ScriptLoader notTenantScopedLoader = new S3ScriptLoader("bucket", "scripts/", false);
        
        assertThatThrownBy(() -> notTenantScopedLoader.loadScript("tenant-123", "my-script.groovy"))
                .isInstanceOf(ScriptLoadException.class)
                .hasMessageContaining("my-script.groovy");
    }
    
    @Test
    void testCacheEviction() {
        // Test cache eviction methods exist and don't throw
        loader.evictFromCache("tenant-123", "script.groovy");
        loader.evictTenant("tenant-123");
        loader.clearCache();
    }
    
    @Test
    void testLoadScriptWithoutBucket() {
        S3ScriptLoader loaderWithoutBucket = new S3ScriptLoader(null, "scripts/", false);
        
        assertThatThrownBy(() -> loaderWithoutBucket.loadScript("tenant-123", "script.groovy"))
                .isInstanceOf(ScriptLoadException.class)
                .hasMessageContaining("S3 bucket name not configured");
    }
}

