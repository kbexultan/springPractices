package com.example.demo.practice_5.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;


@Aspect
@Component
public class ServiceLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(ServiceLoggingAspect.class);

    @Pointcut("within(@org.springframework.stereotype.Service *)")
    public void serviceLayer() {
    }

    @Around("serviceLayer()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().getDeclaringType().getSimpleName()
                + "." + joinPoint.getSignature().getName();
        String args = Arrays.toString(joinPoint.getArgs());

        log.info("--> {} args={}", method, args);
        long start = System.nanoTime();
        try {
            Object result = joinPoint.proceed();
            log.info("<-- {} finished in {} ms", method, elapsedMillis(start));
            return result;
        } catch (Throwable ex) {
            log.warn("<-- {} threw {} after {} ms: {}",
                    method, ex.getClass().getSimpleName(), elapsedMillis(start), ex.getMessage());
            throw ex;
        }
    }

    private static long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }
}
