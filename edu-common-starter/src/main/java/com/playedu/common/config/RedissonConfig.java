package com.playedu.common.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "edu.redisson", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RedissonConfig {
    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean(RedissonClient.class)
    public RedissonClient redissonClient(Environment environment) {
        String host = getProperty(environment, "spring.data.redis.host", "spring.redis.host", "127.0.0.1");
        int port =
                Integer.parseInt(
                        getProperty(environment, "spring.data.redis.port", "spring.redis.port", "6379"));
        int database =
                Integer.parseInt(
                        getProperty(environment, "spring.data.redis.database", "spring.redis.database", "0"));
        String password = getProperty(environment, "spring.data.redis.password", "spring.redis.password", "");
        boolean ssl =
                Boolean.parseBoolean(
                        getProperty(environment, "spring.data.redis.ssl.enabled", "spring.redis.ssl", "false"));

        String prefix = ssl ? "rediss://" : "redis://";
        Config config = new Config();
        config.useSingleServer()
                .setAddress(prefix + host + ":" + port)
                .setDatabase(database);
        if (StringUtils.hasText(password)) {
            config.useSingleServer().setPassword(password);
        }
        return Redisson.create(config);
    }

    private String getProperty(Environment environment, String primaryKey, String fallbackKey, String defaultValue) {
        String value = environment.getProperty(primaryKey);
        if (StringUtils.hasText(value)) {
            return value;
        }
        value = environment.getProperty(fallbackKey);
        return StringUtils.hasText(value) ? value : defaultValue;
    }
}
