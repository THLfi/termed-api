package fi.thl.termed.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ProfileLoggerAspect {

  private Logger log = LoggerFactory.getLogger(getClass());

  @Around("@annotation(profile)")
  private Object profileMethod(ProceedingJoinPoint joinPoint, Profile profile)
      throws Throwable {

    Object returnValue;

    Signature signature = joinPoint.getSignature();

    long start = System.nanoTime();

    try {
      returnValue = joinPoint.proceed();
    } catch (Throwable t) {
      throw t;
    }

    log.info("{}.{} completed in {} ms. {}",
             signature.getDeclaringTypeName(),
             signature.getName(),
             String.valueOf((System.nanoTime() - start) / 1000000),
             profile.message());

    return returnValue;
  }

}
