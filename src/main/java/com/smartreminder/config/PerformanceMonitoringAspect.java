package com.smartreminder.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Performance Monitoring Aspect (Spring AOP)
 * 
 * Automatically measures the execution time of all Service and Controller methods.
 * Provides structured telemetry and logs performance warnings if execution exceeds 250ms.
 */
@Aspect
@Component
public class PerformanceMonitoringAspect {

    private static final Logger log = LoggerFactory.getLogger(PerformanceMonitoringAspect.class);
    private static final long SLOW_QUERY_THRESHOLD_MS = 250;

    /**
     * Pointcut targeting all public methods in Controller and Service packages.
     */
    @Pointcut("within(com.smartreminder.controller..*) || within(com.smartreminder.service.impl..*)")
    public void applicationPackagePointcut() {
    }

    @Around("applicationPackagePointcut()")
    public Object profileMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            if (duration > SLOW_QUERY_THRESHOLD_MS) {
                log.warn("[PERF-SLOW] {}.{}() executed in {} ms (exceeded threshold of {} ms)",
                        className, methodName, duration, SLOW_QUERY_THRESHOLD_MS);
            } else {
                log.debug("[PERF] {}.{}() completed in {} ms", className, methodName, duration);
            }

            return result;
        } catch (Throwable ex) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[PERF-ERROR] {}.{}() failed with exception after {} ms: {}",
                    className, methodName, duration, ex.getMessage());
            throw ex;
        }
    }
}
