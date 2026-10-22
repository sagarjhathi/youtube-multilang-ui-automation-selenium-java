package main.java.yt_multi_lang_ui_validator.base;

import java.time.Duration;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import main.java.yt_multi_lang_ui_validator.config.ConfigManager;
import main.java.yt_multi_lang_ui_validator.driverManager.DriverManager;



public class BasePage {

	protected WebDriver driver;
	protected WebDriverWait wait;
	
	 public BasePage() {
	
	        this.driver = DriverManager.getDriver();
	        if (driver == null) {
	            throw new IllegalStateException("WebDriver not initialized!");
	        }
	        
	        int waitDuration= ConfigManager.getInt("explicit.wait", 10);
	        System.out.println(waitDuration+"   wait in the base page added");
	        this.wait = new WebDriverWait(driver, Duration.ofSeconds(waitDuration));
	    }

}
