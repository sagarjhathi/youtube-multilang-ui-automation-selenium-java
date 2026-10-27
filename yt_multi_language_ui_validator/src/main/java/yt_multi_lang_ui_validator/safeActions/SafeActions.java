package main.java.yt_multi_lang_ui_validator.safeActions;


import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import main.java.yt_multi_lang_ui_validator.config.ConfigManager;
import main.java.yt_multi_lang_ui_validator.logger.LoggerUtility;

import java.time.Duration;
import java.util.List;

public class SafeActions{
   
	private  static final Logger log = LoggerUtility.getLogger(SafeActions.class);


	    private final WebDriver driver;
	    private final WebDriverWait wait;
	    

	    int timeoutSec;
	    int attempts;
	    
	    public SafeActions(WebDriver driver) {
	        this.driver = driver;
	         timeoutSec = ConfigManager.getInt("explicit.wait", 10); // seconds
	         attempts = ConfigManager.getInt("element.retry.attempts", 2);
	        
	        this.wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSec));
	        log.info("SafeActions initialized with driver {}", driver);
	    }
	    
	    
	    
	    // ---------- CORE CHECKS ----------

	    private boolean isReady(WebElement element) {
	        try {
	            return element != null && element.isDisplayed() && element.isEnabled();
	        } catch (StaleElementReferenceException e) {
	            return false;
	        }
	    }

	    // ---------- FAILURE MESSAGE ----------

	    private String currentUrl() {
	        try {
	            return driver.getCurrentUrl();
	        } catch (Exception e) {
	            return "unknown"; // browser is gone
	        }
	    }

	    /** Selenium messages are long with build info after the first line, so keep only the first line. */
	    private String firstLine(String message) {
	        if (message == null) return "";
	        String line = message.split("\\R", 2)[0];
	        return line.length() > 200 ? line.substring(0, 200) : line;
	    }

	    private String reasonOf(Exception e) {
	        return e.getClass().getSimpleName() + ": " + firstLine(e.getMessage());
	    }

	    /** One line only: LoopGuard prints just the first line of an exception. */
	    private RuntimeException failure(String action, By locator, String reason) {
	        return new RuntimeException(action + " failed after " + attempts + " attempts | element=" + locator
	                + " | reason=" + reason + " | page=" + currentUrl());
	    }

	    // ---------- SAFE FIND ELEMENT ----------

	    public WebElement safeFindElement(By locator) {
	        log.info("[{}] Trying to find element: {}", ThreadContext.get("testName"), locator);
	        String reason = "element not visible or not enabled";

	        for (int attempt = 1; attempt <= attempts; attempt++) {
	            try {
	                WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
	                element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
	                element = wait.until(ExpectedConditions.elementToBeClickable(locator));

	                if (isReady(element)) {
	                    log.info("[{}] Element found and ready (attempt {}): {}", ThreadContext.get("testName"), attempt, locator);
	                    return element;
	                } else {
	                    log.warn("[{}] Element found but not ready (attempt {}): {}", ThreadContext.get("testName"), attempt, locator);
	                }
	            } catch (TimeoutException e) {
	                reason = reasonOf(e);
	                log.warn("[{}] Timeout finding element (attempt {}): {}", ThreadContext.get("testName"), attempt, locator);
	            } catch (Exception e) {
	                reason = reasonOf(e);
	                log.warn("[{}] Error while finding element (attempt {}): {} -> {}", ThreadContext.get("testName"), attempt, locator, e.toString());
	            }

	            refreshIfNeeded(attempt);
	        }

	        log.error("[{}] Element not found after {} attempts: {}", ThreadContext.get("testName"), attempts, locator);
	        throw failure("Find element", locator, reason);
	    }

	    // ---------- SAFE FIND ELEMENTS ----------

	    public List<WebElement> safeFindElements(By locator) {
	        log.info("[{}] Finding elements list: {}", ThreadContext.get("testName"), locator);
	        String reason = "element not visible or not enabled";

	        for (int attempt = 1; attempt <= attempts; attempt++) {
	            try {
	                List<WebElement> elements = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(locator));

	                if (!elements.isEmpty()) {
	                    elements.removeIf(el -> !isReady(el));
	                    if (!elements.isEmpty()) {
	                        log.info("[{}] Found {} ready elements (attempt {}): {}", ThreadContext.get("testName"), elements.size(), attempt, locator);
	                        return elements;
	                    }
	                }
	            } catch (Exception e) {
	                reason = reasonOf(e);
	                log.warn("[{}] Attempt {} failed to find elements: {} -> {}", ThreadContext.get("testName"), attempt, locator, e.toString());
	            }

	            refreshIfNeeded(attempt);
	        }

	        log.error("[{}] No elements ready after {} attempts: {}", ThreadContext.get("testName"), attempts, locator);
	        throw failure("Find elements", locator, reason);
	    }

	
	    
	    
	    public boolean safeClick(By locator) {
	        log.info("[{}] Trying to click element: {}", ThreadContext.get("testName"), locator);
	        String reason = "element not visible or not enabled";

	        for (int attempt = 1; attempt <= attempts; attempt++) {
	            try {
	                // Wait for the element to be present, visible, and clickable
	                WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
	                element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
	                element = wait.until(ExpectedConditions.elementToBeClickable(locator));

	                if (isReady(element)) {
	                    try {
	                        // Try the normal native click first
	                        element.click();
	                        log.info("[{}] Clicked successfully (attempt {}): {}", ThreadContext.get("testName"), attempt, locator);
	                     
	                        return true;

	                    } catch ( WebDriverException e) {
	                        // These include cases where browser driver times out internally
	                        log.warn("[{}] Native click failed (attempt {}): {} -> {}", 
	                                 ThreadContext.get("testName"), attempt, locator, e.toString());

	                        // Fallback to JS click (handles overlays / stalled drivers)
	                        try {
	                            log.info("[{}] Trying JS click fallback for: {}", ThreadContext.get("testName"), locator);
	                            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
	                        
	                            return true;
	                        } catch (Exception jsEx) {
	                            reason = reasonOf(jsEx);
	                            log.error("[{}] JS click fallback failed (attempt {}): {} -> {}", 
	                                      ThreadContext.get("testName"), attempt, locator, jsEx.toString());
	                        }
	                    }

	                } else {
	                    log.warn("[{}] Element not ready to click (attempt {}): {}", ThreadContext.get("testName"), attempt, locator);
	                }

	            } catch (StaleElementReferenceException stale) {
	                reason = reasonOf(stale);
	                log.warn("[{}] Stale element detected (attempt {}): {}", ThreadContext.get("testName"), attempt, locator);
	            } catch (org.openqa.selenium.TimeoutException timeout) {
	                reason = reasonOf(timeout);
	                log.warn("[{}] Timeout waiting for element (attempt {}): {}", ThreadContext.get("testName"), attempt, locator);
	            } catch (Exception e) {
	                reason = reasonOf(e);
	                // This block also catches java.net.http.HttpTimeoutException indirectly
	                log.warn("[{}] Unexpected WebDriver exception (attempt {}): {} -> {}", 
	                         ThreadContext.get("testName"), attempt, locator, e.toString());
	            }

	            // Refresh the page only after multiple failed attempts
	            refreshIfNeeded(attempt);
	        }

	        log.error("[{}] Failed to click element after {} attempts: {}", ThreadContext.get("testName"), attempts, locator);
	        throw failure("Click", locator, reason);
	    }

	    
	    
	    protected void refreshIfNeeded(int attempt) {
	        // refresh only after multiple failed attempts
	        if (attempt >= 2) {
	            try {
	                log.warn("[{}] Attempt {} failed — refreshing the page to recover...", 
	                         ThreadContext.get("testName"), attempt);

	                // print current URL to help debug which page failed
	                String currentUrl = driver.getCurrentUrl();
	                log.info("[{}] Current URL before refresh: {}", ThreadContext.get("testName"), currentUrl);

	                // refresh the page
	                driver.navigate().refresh();
	                log.info("[{}] Page refreshed successfully.", ThreadContext.get("testName"));

	                // wait briefly for the DOM to reload and stabilize
	            

	                // optional: verify the page is ready again via JS readiness check
	                boolean pageReady = false;
	                try {
	                    WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(8));
	                    pageReady = shortWait.until(drv ->
	                        ((JavascriptExecutor) drv)
	                            .executeScript("return document.readyState")
	                            .toString()
	                            .equals("complete")
	                    );
	                } catch (Exception jsWaitEx) {
	                    log.warn("[{}] Page readiness check failed after refresh (non-fatal): {}", 
	                             ThreadContext.get("testName"), jsWaitEx.getMessage());
	                }

	                if (pageReady) {
	                    log.info("[{}] Page fully loaded after refresh.", ThreadContext.get("testName"));
	                } else {
	                    log.warn("[{}] Page not fully loaded even after refresh — proceeding with caution.", 
	                             ThreadContext.get("testName"));
	                }

	            } catch (Exception e) {
	                log.error("[{}] Unexpected error during refresh (attempt {}): {}", 
	                          ThreadContext.get("testName"), attempt, e.toString());
	            }
	        }
	    }

	    
	 		

}


