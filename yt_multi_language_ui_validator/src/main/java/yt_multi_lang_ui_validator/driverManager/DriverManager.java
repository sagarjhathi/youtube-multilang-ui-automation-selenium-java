package main.java.yt_multi_lang_ui_validator.driverManager;

import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;

import main.java.yt_multi_lang_ui_validator.config.ConfigManager;
import main.java.yt_multi_lang_ui_validator.logger.LoggerUtility;
import main.java.yt_multi_lang_ui_validator.seleniumGrid.GlobalGridUtility;

public class DriverManager {

	// Thread-local ensures driver is isolated per parallel thread/test
	private static final ThreadLocal<WebDriver> driver = new ThreadLocal<>();

	// Logger for this class
	private  static Logger log = LoggerUtility.getLogger(DriverManager.class);

	/** Get current thread's WebDriver instance */
	public static WebDriver getDriver() {
		return driver.get();
	}

	/** Initialize a new WebDriver instance if not already present for this thread */
	public static void initDriver() {
		if (driver.get() == null) {
			log.info("No existing WebDriver found for current thread. Initializing a new ChromeDriver...");



			try {            	
				String browser = ConfigManager.get("browser", "chrome");
				if (browser != null) browser = browser.trim().toLowerCase(Locale.ENGLISH);

				switch (browser) {
				case "firefox": {

					org.openqa.selenium.firefox.FirefoxOptions firefoxOptions = new org.openqa.selenium.firefox.FirefoxOptions();


					String windowSize= ConfigManager.get("firefox.window-size");
					if(windowSize!=null) {
						firefoxOptions.addArguments(windowSize);
					}
					if (ConfigManager.getBoolean("firefox.arg.disable_gpu", true)) {
						firefoxOptions.addArguments("--disable-gpu");
					}
					if (ConfigManager.getBoolean("firefox.arg.disable_dev_shm_usage", true)) {
						firefoxOptions.addArguments("--disable-dev-shm-usage");
					}
					if (ConfigManager.getBoolean("firefox.arg.no_sandbox", true)) {
						firefoxOptions.addArguments("--no-sandbox");
					}
					if (ConfigManager.getBoolean("firefox.arg.disable_extensions", true)) {
						firefoxOptions.addArguments("--disable-extensions");
					}
					if (ConfigManager.getBoolean("firefox.headless", false)) {
						firefoxOptions.addArguments("-headless");
					}

					try {
						io.github.bonigarcia.wdm.WebDriverManager.firefoxdriver().setup();
						log.info("WebDriverManager: geckodriver setup OK");
					} catch (Exception e) {
						log.warn("WDM geckodriver setup failed: {}. Will try local fallback if configured.", e.getMessage());
						String local = ConfigManager.get("webdriver.firefox.local.path", "");
						if (!local.isBlank()) System.setProperty("webdriver.gecko.driver", local);
					}

					WebDriver firefoxDriver = new org.openqa.selenium.firefox.FirefoxDriver(firefoxOptions);

					if(ConfigManager.getBoolean("firefox.runOnGrid", false)) {
						firefoxDriver=GlobalGridUtility.createRemoteFirefoxDriver(ConfigManager.get("gridHubUrl"), firefoxOptions);
					}
					driver.set(firefoxDriver);
					try { firefoxDriver.manage().deleteAllCookies(); } catch (Exception ignored) {}

					if (ConfigManager.getBoolean("firefox.arg.start_maximized", true)) {
						try { firefoxDriver.manage().window().maximize(); } catch (Exception ignored) {}
					}
					log.info("FirefoxDriver initialized successfully for thread: {}", Thread.currentThread().threadId());
					break;
				}

				case "edge": {

					org.openqa.selenium.edge.EdgeOptions edgeOptions = new org.openqa.selenium.edge.EdgeOptions();

					String windowSize= ConfigManager.get("edge.window-size");
					if(windowSize!=null) {
						edgeOptions.addArguments(windowSize);
					}
					if (ConfigManager.getBoolean("edge.arg.start_maximized", true)) {
						edgeOptions.addArguments("--start-maximized");
					}
					if (ConfigManager.getBoolean("edge.arg.disable_gpu", true)) {
						edgeOptions.addArguments("--disable-gpu");
					}
					if (ConfigManager.getBoolean("edge.arg.disable_dev_shm_usage", true)) {
						edgeOptions.addArguments("--disable-dev-shm-usage");
					}
					if (ConfigManager.getBoolean("edge.arg.no_sandbox", true)) {
						edgeOptions.addArguments("--no-sandbox");
					}
					if (ConfigManager.getBoolean("edge.arg.disable_extensions", true)) {
						edgeOptions.addArguments("--disable-extensions");
					}

					boolean useExtEdge = ConfigManager.getBoolean("edge.use.automation.extension", false);
					edgeOptions.setExperimentalOption("useAutomationExtension", useExtEdge);

					try {

						io.github.bonigarcia.wdm.WebDriverManager.edgedriver().browserVersion("17134").setup();
						log.info("WebDriverManager: edgedriver setup OK");
					} catch (Exception e) {
						log.warn("WDM edgedriver setup failed: {}. Will try local fallback if configured.", e.getMessage());
						String local = ConfigManager.get("webdriver.edge.local.path", "");
						if (!local.isBlank()) System.setProperty("webdriver.edge.driver", local);
					}
					System.setProperty("SE_MSEDGEDRIVER_MIRROR_URL", "https://msedgedriver.microsoft.com");
					log.info("Trying local fallback with == System.setProperty(\"SE_MSEDGEDRIVER_MIRROR_URL\", \"https://msedgedriver.microsoft.com\")");


					WebDriver edgeDriver = new org.openqa.selenium.edge.EdgeDriver(edgeOptions);
					if(ConfigManager.getBoolean("edge.runOnGrid", false)) {
						edgeDriver=GlobalGridUtility.createRemoteEdgeDriver(ConfigManager.get("gridHubUrl"), edgeOptions);
					}
					driver.set(edgeDriver);
					try { edgeDriver.manage().deleteAllCookies(); } catch (Exception ignored) {}
					log.info("EdgeDriver initialized successfully for thread: {}", Thread.currentThread().threadId());
					break;
				}


				default: {

					org.openqa.selenium.chrome.ChromeOptions options = new org.openqa.selenium.chrome.ChromeOptions();


					String windowSize= ConfigManager.get("chrome.window-size");
					if(windowSize!=null) {
						options.addArguments(windowSize);
					}

//					if (ConfigManager.getBoolean("chrome.arg.start_maximized", true)) {
//						options.addArguments("--start-maximized");
//					}
					if (ConfigManager.getBoolean("chrome.arg.disable_gpu", true)) {
						options.addArguments("--disable-gpu");
					}
					if (ConfigManager.getBoolean("chrome.arg.disable_blink_features_automation_controlled", true)) {
						options.addArguments("--disable-blink-features=AutomationControlled");
					}
					if (ConfigManager.getBoolean("chrome.arg.disable_dev_shm_usage", true)) {
						options.addArguments("--disable-dev-shm-usage");
					}
					if (ConfigManager.getBoolean("chrome.arg.no_sandbox", true)) {
						options.addArguments("--no-sandbox");
					}
					if (ConfigManager.getBoolean("chrome.arg.disable_extensions", true)) {
						options.addArguments("--disable-extensions");

					}
					if (ConfigManager.getBoolean("chrome.headless", false)) {
						options.addArguments("--headless");
						options.addArguments("--window-size=1920,1080");
						
					}
					
					


					try {
						io.github.bonigarcia.wdm.WebDriverManager.chromedriver().setup();
						log.info("WebDriverManager: chromedriver setup OK");
					} catch (Exception e) {
						log.warn("WDM chromedriver setup failed: {}. Will try local fallback if configured.", e.getMessage());
						String local = ConfigManager.get("webdriver.chrome.local.path", "");
						if (!local.isBlank()) System.setProperty("webdriver.chrome.driver", local);
					}

					WebDriver chromeDriver = new org.openqa.selenium.chrome.ChromeDriver(options);

					if(ConfigManager.getBoolean("chrome.runOnGrid", false)) {
						chromeDriver=GlobalGridUtility.createRemoteChromeDriver(ConfigManager.get("gridHubUrl"), options);
					}

					driver.set(chromeDriver);
					try { chromeDriver.manage().deleteAllCookies(); } catch (Exception ignored) {}
					log.info("ChromeDriver initialized successfully for thread: {}", Thread.currentThread().threadId());
					break;
				}
				}



			}catch(Exception e) {
				log.error("Failed to initialize WebDriver: {}", e.getMessage(), e);
				throw new RuntimeException("WebDriver initialization failed", e);
			}

		} 
		else {
			log.info("Reusing existing WebDriver instance for thread: {}", Thread.currentThread().threadId());
		}
	}


	/** Quit and clean up WebDriver for current thread */
	public static void quitDriver() {
		WebDriver currentDriver = driver.get();

		if (currentDriver != null) {
			try {
		//		log.info("Closing WebDriver for thread: {}", Thread.currentThread().threadId());
				currentDriver.quit();
			} catch (Exception e) {
			//	log.error("Error while quitting WebDriver for thread {}: {}", Thread.currentThread().threadId(), e.getMessage());
			} finally {
				driver.remove();
		//		log.info("WebDriver instance removed from ThreadLocal for thread: {}", Thread.currentThread().threadId());
			}
		} else {
		//	log.warn("quitDriver() called but no WebDriver was found for thread: {}", Thread.currentThread().threadId());
		}
	}



}
