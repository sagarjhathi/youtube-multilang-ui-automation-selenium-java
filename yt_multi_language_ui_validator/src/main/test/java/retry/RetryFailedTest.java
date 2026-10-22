package main.test.java.retry;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.testng.IRetryAnalyzer; 
import org.testng.ITestResult;

import main.java.yt_multi_lang_ui_validator.logger.LoggerUtility;

import java.util.Arrays;
import java.util.List;

public class RetryFailedTest implements IRetryAnalyzer {

    private int retryCount = 0;
    private static final int maxRetryCount = 1; // Retry only once
    private static final  Logger log=LoggerUtility.getLogger(RetryFailedTest.class);
    
    // List of exceptions to retry on (excluding assertion errors)
    private static final List<Class<? extends Throwable>> nonRetryableExceptions =
            Arrays.asList(AssertionError.class);

    @Override
    public boolean retry(ITestResult result) {
        Throwable cause = result.getThrowable();

        if (retryCount < maxRetryCount && (cause == null || isRetryable(cause))) {
            retryCount++;
            System.out.println("Retrying test: " + result.getName() + " due to " + cause);
            log.warn("[{}] Retrying test '{}' (attempt {}) due to: {}", 
                    ThreadContext.get("testName"), result.getName(), retryCount, cause != null ? cause.getMessage() : "Unknown error");
          
            return true;
        }
        
        log.info("[{}] Not retrying test '{}'. Retry count: {}, Cause: {}", 
                ThreadContext.get("testName"), result.getName(), retryCount, cause != null ? cause.getMessage() : "None");
        
        return false;
    }

    private boolean isRetryable(Throwable cause) {
        for (Class<? extends Throwable> nonRetryable : nonRetryableExceptions) {
            if (nonRetryable.isInstance(cause)) {
                log.debug("[{}] Cause '{}' marked as non-retryable", ThreadContext.get("testName"), cause.getClass().getSimpleName());

                return false;
            }
        }
        return true;
    }
}

