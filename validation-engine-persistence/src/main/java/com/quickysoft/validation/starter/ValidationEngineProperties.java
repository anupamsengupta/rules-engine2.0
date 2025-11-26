package com.quickysoft.validation.starter;

import com.quickysoft.validation.core.engine.expression.ExpressionEvaluatorType;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Validation Engine.
 */
@ConfigurationProperties(prefix = "quickysoft.validation")
public class ValidationEngineProperties {
    
    /**
     * Cache configuration.
     */
    private Cache cache = new Cache();
    
    /**
     * Script loading configuration.
     */
    private Script script = new Script();
    
    /**
     * Expression evaluation configuration.
     */
    private Expression expression = new Expression();
    
    public Cache getCache() {
        return cache;
    }
    
    public void setCache(Cache cache) {
        this.cache = cache;
    }
    
    public Script getScript() {
        return script;
    }
    
    public void setScript(Script script) {
        this.script = script;
    }
    
    public Expression getExpression() {
        return expression;
    }
    
    public void setExpression(Expression expression) {
        this.expression = expression;
    }
    
    /**
     * Cache configuration.
     */
    public static class Cache {
        /**
         * Whether caching is enabled.
         */
        private boolean enabled = true;
        
        /**
         * Cache TTL in hours.
         */
        private int ttlHours = 24;
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public int getTtlHours() {
            return ttlHours;
        }
        
        public void setTtlHours(int ttlHours) {
            this.ttlHours = ttlHours;
        }
    }
    
    /**
     * Script loading configuration.
     */
    public static class Script {
        /**
         * S3 configuration.
         */
        private S3 s3 = new S3();
        
        /**
         * Local file system base path.
         */
        private String localBasePath = "/scripts";
        
        public S3 getS3() {
            return s3;
        }
        
        public void setS3(S3 s3) {
            this.s3 = s3;
        }
        
        public String getLocalBasePath() {
            return localBasePath;
        }
        
        public void setLocalBasePath(String localBasePath) {
            this.localBasePath = localBasePath;
        }
    }
    
    /**
     * S3 configuration for script loading.
     */
    public static class S3 {
        /**
         * Whether S3 script loading is enabled.
         */
        private boolean enabled = false;
        
        /**
         * S3 bucket name.
         */
        private String bucket;
        
        /**
         * AWS region.
         */
        private String region = "us-east-1";
        
        /**
         * S3 key prefix.
         */
        private String keyPrefix = "scripts/";
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public String getBucket() {
            return bucket;
        }
        
        public void setBucket(String bucket) {
            this.bucket = bucket;
        }
        
        public String getRegion() {
            return region;
        }
        
        public void setRegion(String region) {
            this.region = region;
        }
        
        public String getKeyPrefix() {
            return keyPrefix;
        }
        
        public void setKeyPrefix(String keyPrefix) {
            this.keyPrefix = keyPrefix;
        }
    }
    
    /**
     * Expression evaluation configuration.
     */
    public static class Expression {
        /**
         * Type of expression evaluator to use (SPEL, MVEL, JEXL).
         * Default: SPEL
         */
        private ExpressionEvaluatorType evaluatorType = ExpressionEvaluatorType.SPEL;
        
        public ExpressionEvaluatorType getEvaluatorType() {
            return evaluatorType;
        }
        
        public void setEvaluatorType(ExpressionEvaluatorType evaluatorType) {
            this.evaluatorType = evaluatorType != null ? evaluatorType : ExpressionEvaluatorType.SPEL;
        }
    }
}

