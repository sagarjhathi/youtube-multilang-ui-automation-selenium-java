package main.java.yt_multi_lang_ui_validator.utilities;


import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import main.java.yt_multi_lang_ui_validator.base.BasePage;
import main.java.yt_multi_lang_ui_validator.logger.LoggerUtility;

import java.time.Duration;
import java.util.List;

public class WaitUtility extends BasePage {

	
	private static final Logger log = LoggerUtility.getLogger(WaitUtility.class);


	public WebElement waitForElementReady(By by) {
	    return waitForElementReady(by, Duration.ofSeconds(10), 3);
	}

	protected WebElement waitForElementReady(By by, Duration timeout, int maxRetries) {
	    int attempts = 0;
        log.info("[{}] within waitForElementReady method", ThreadContext.get("testName"));

	    while (attempts < maxRetries) {
	        try {
	            WebDriverWait w = new WebDriverWait(driver, timeout);
	            // 1) presence
	            w.until(ExpectedConditions.presenceOfElementLocated(by));
	            // 2) visibility
	            WebElement el = w.until(ExpectedConditions.visibilityOfElementLocated(by));
	            // 3) clickable
	            el = w.until(ExpectedConditions.elementToBeClickable(by));
	            // final sanity checks
	            if (el.isDisplayed() && el.isEnabled()) return el;
	           
	            // otherwise loop and retry
	        } catch (StaleElementReferenceException | org.openqa.selenium.TimeoutException ignored) {
	            // try again
	        	if(attempts>=1) {
	 	    	   driver.navigate().refresh();
	 	    	   
	 	    	   log.info("[{}] Refreshing the page catch section", ThreadContext.get("testName"));
	 	    	   System.out.println("Refreshing the page to avoid longer loading of pages");
	 	       }
	        }
	       
	        log.info("[{}]Attempts ==",+attempts+ "  "+ThreadContext.get("testName"));
	        attempts++;
	    }
	    throw new org.openqa.selenium.TimeoutException("Element not ready after retries: " + by);
	}

	/** Click by locator when element is ready. JS fallback included. */
	public void clickWhenReady(By by) {
	    WebElement el = waitForElementReady(by);
	    try {
	        el.click();
	    } catch (Throwable t) {
	        // JS fallback if native click fails (overlay/animation)
	        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
	    }
	}

	/** Convenience: get visible list (fresh find) */
	public List<WebElement> findVisibleElements(By by) {
	    wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(by));
	    List<WebElement> els = driver.findElements(by);
	    els.removeIf(e -> e.getText() == null || e.getText().trim().isEmpty());
	    return els;
	}
	
}
