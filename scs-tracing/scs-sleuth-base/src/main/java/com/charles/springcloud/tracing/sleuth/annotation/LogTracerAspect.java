package com.charles.springcloud.tracing.sleuth.annotation;

import com.charles.springcloud.tracing.constants.ActionByMultiStepsKeys;
import com.charles.springcloud.tracing.constants.AspectOrder;
import com.charles.springcloud.tracing.sleuth.service.CacheService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import static com.charles.springcloud.tracing.constants.CustomizedMdcKeys.ACTION;
import static com.charles.springcloud.tracing.constants.CustomizedMdcKeys.STEP;

@Aspect
@Component
@Order(AspectOrder.LOG_TRACER_ORDER)
public class LogTracerAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogTracerAspect.class);
    private final Tracer tracer;
    private final CacheService cacheService;

    @Autowired
    public LogTracerAspect(Tracer tracer, CacheService cacheService) {
        this.tracer = tracer;
        this.cacheService = cacheService;
    }

    @Around(value = "@annotation(tracer)")
    public Object process(ProceedingJoinPoint joinPoint, LogActionTracer tracer) {
        // 如果需要reentrant, 在第二次请求时本条日志的输出的traceId是新生成的
        LOGGER.info("************ one new tracer ************");
        try {
            MDC.put(ACTION, tracer.action());
            this.tracer.getCurrentSpan().setBaggageItem(ACTION, tracer.action());
            Object result = joinPoint.proceed();
            // 方法执行完从result中获取REENTRANT_KEY，如果需要，保存到缓存中
            if (tracer.continued()) {
                Span currentSpan = this.tracer.getCurrentSpan();
                cacheService.cache(ActionByMultiStepsKeys.REENTRANT_KEY, currentSpan);
            }
            LOGGER.info("result = {}", result);
            return result;
        } catch (Throwable e) {
            LOGGER.error("MDC标识添加异常 ", e);
            throw new RuntimeException(e.getCause());
        } finally {
            MDC.remove(ACTION);
        }
    }

    @Around(value = "@annotation(tracer)")
    public Object process(ProceedingJoinPoint joinPoint, LogActionStepTracer tracer) {
        // 如果需要reentrant, 在第二次请求时本条日志的输出的traceId是新生成的
        try {
            MDC.put(STEP, tracer.step());
            return joinPoint.proceed();
        } catch (Throwable e) {
            LOGGER.error("MDC标识添加异常 ", e);
            throw new RuntimeException("MDC标识添加异常");
        } finally {
            MDC.remove(STEP);
        }
    }
}