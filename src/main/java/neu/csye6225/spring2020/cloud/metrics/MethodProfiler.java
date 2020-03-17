package neu.csye6225.spring2020.cloud.metrics;

import org.apache.logging.log4j.Logger;
import com.timgroup.statsd.StatsDClient;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

import static org.apache.logging.log4j.LogManager.*;
import com.google.common.base.Stopwatch;

import java.util.concurrent.TimeUnit;

@Aspect
public class MethodProfiler {

    private final StatsDClient statsDClient;

    private static final Logger logger = getLogger();

    public MethodProfiler(StatsDClient statsDClient) {
        this.statsDClient = statsDClient;
    }

    @Pointcut("execution(* neu.csye6225.spring2020.cloud.controller.EntryController.*(..))")
    public void restServiceMethods() {
    }

    @Around("restServiceMethods()")
    public Object loggerProfile(ProceedingJoinPoint pjp) throws Throwable {

        // execute the method, record the result and measure the time
        logger.info("Method Entered: " + pjp.getSignature().getName());
        Object output = pjp.proceed();

        logger.info("Method Exited: " + pjp.getSignature().getName());
        // return the recorded result
        return output;
    }

    @Around("restServiceMethods()")
    public Object profile(ProceedingJoinPoint pjp) throws Throwable {
        // execute the method, record the result and measure the time
        Stopwatch stopwatch = Stopwatch.createStarted();
        Object output = pjp.proceed();
        stopwatch.stop();
        // send the recorded time to statsd
        statsDClient.recordExecutionTime(pjp.getSignature().getName(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
        // return the recorded result
        return output;
    }

    @Before("restServiceMethods()")
    public void countEndpointCall(JoinPoint joinPoint) {
        // Calling the statsDClient and incrementing count by 1 for respective endpoint.
        // joinPoint.getSignature().getName() returns the name of the method for which this AOP method is called
        statsDClient.increment(joinPoint.getSignature().getName());
    }
}
