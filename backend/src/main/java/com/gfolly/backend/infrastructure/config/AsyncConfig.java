package com.gfolly.backend.infrastructure.config;

import com.gfolly.backend.infrastructure.multitenant.TenantContext;
import com.gfolly.backend.infrastructure.multitenant.TenantContextUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("Async-");
        executor.setTaskDecorator(new ContextCopyingDecorator());
        executor.initialize();
        return executor;
    }

    public static class ContextCopyingDecorator implements TaskDecorator {
        @Override
        public Runnable decorate(Runnable runnable) {
            RequestAttributes context = RequestContextHolder.getRequestAttributes();
            String tenantId = TenantContext.getCurrentTenant(); // This can be null
            SecurityContext securityContext = SecurityContextHolder.getContext();

            return () -> {
                if (tenantId != null) {
                    // If tenantId is present, propagate it using TenantContextUtils
                    TenantContextUtils.runInTenantContext(tenantId, () -> {
                        propagateRequestContext(context);
                        propagateSecurityContext(securityContext);
                        runnable.run();
                    });
                } else {
                    // If tenantId is null, do not propagate tenant context,
                    // but still propagate RequestAttributes and SecurityContext if they exist.
                    propagateRequestContext(context);
                    propagateSecurityContext(securityContext);
                    runnable.run();
                }
            };
        }

        private void propagateRequestContext(RequestAttributes context) {
            if (context != null) {
                RequestContextHolder.setRequestAttributes(context);
            }
        }

        private void propagateSecurityContext(SecurityContext securityContext) {
            if (securityContext != null) {
                SecurityContextHolder.setContext(securityContext);
            }
        }
    }
}

