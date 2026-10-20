package main.java.yt_multi_lang_ui_validator.pages;

import java.time.Duration; 
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import main.java.yt_multi_lang_ui_validator.base.BasePage;
import main.java.yt_multi_lang_ui_validator.config.ConfigManager;
import main.java.yt_multi_lang_ui_validator.lingua.LinguaHelper;
import main.java.yt_multi_lang_ui_validator.logger.LoggerUtility;
import main.java.yt_multi_lang_ui_validator.safeActions.SafeActions;
import main.java.yt_multi_lang_ui_validator.utilities.WaitUtility;

public class YtLandingPage  extends BasePage{

	private static final Logger log = LoggerUtility.getLogger(YtLandingPage.class);
	private final SafeActions safeAct;

	public YtLandingPage() {
		super();
		this.safeAct = new SafeActions(driver);
		log.info("Initialized YtLandingPage with driver: {}", driver);
	}


	public By sideMenu=By.xpath("//div[@class='style-scope ytd-mini-guide-renderer']");

	public By sideMenuExpandedList=By.xpath("//ytd-guide-entry-renderer[@class='style-scope ytd-guide-section-renderer']");

	public By settingEllipsesButton=By.xpath("//ytd-topbar-menu-button-renderer[@class='style-scope ytd-masthead style-default']");

	public By languageDropdownUnderSettings=By.xpath("(//div[@id='content-icon'])[2]");

	public By languageList=By.xpath("//yt-multi-page-menu-section-renderer[@class='style-scope ytd-multi-page-menu-renderer']//ytd-compact-link-renderer");

	public By ellipsesYtLandingPageTopLeft=By.xpath("//ytd-topbar-logo-renderer[@id='logo']/preceding-sibling::*[@id='guide-button']");

	public By sideMenuCollapsedList=By.xpath("//ytd-mini-guide-renderer[@class='style-scope ytd-app']//ytd-mini-guide-entry-renderer");

	public By countryCodeOnYtLogoLandingPage=By.xpath("(//span[@id='country-code'])[1]");

	public By settingEllipsesOptionsListLandingPage=By.xpath("//ytd-multi-page-menu-renderer");

	public By languageListUpdated=By.xpath("//ytd-multi-page-menu-renderer[@slot='dropdown-content']");

	public By searchInputLandinfPage=By.xpath("//input[@class='ytSearchboxComponentInput yt-searchbox-input title']");

	//Cannot use this directly as this banner wouldnt show if yt does not have a search histroy so to show these recommendation banner
	public By horizontalScrollBarLandingPage=By.xpath("//div[@id='scroll-container']");

	public By locationDropdownUnderSettings=By.xpath("(//div[@id='content-icon'])[4]");

	public By locationList=By.xpath("//yt-multi-page-menu-section-renderer[@class='style-scope ytd-multi-page-menu-renderer']//ytd-compact-link-renderer");

	public By globalFilterButton = By.xpath("//div[@id='filter-button']");

	public By globalFilterData=By.xpath("//ytd-search-filter-options-dialog-renderer[@class='style-scope ytd-popup-container']");

	public By closeButtonGlobalFilterPopup=By.xpath("//yt-icon[@icon='close']");





	public WebElement getLanguageElementByName(String name) {
		By locator=By.xpath("//yt-multi-page-menu-section-renderer[@class='style-scope ytd-multi-page-menu-renderer']//yt-formatted-string[@id='label' and text()='" + name + "']");
		log.info("Looking for language element with name: {}", name);
		WebElement safeElement = safeAct.safeFindElement(locator);
		safeElement = safeAct.safeFindElement(locator);
		System.out.println(locator);
		return safeElement;
	}
	
	
	public By getLanguageElementByNameBy(String name) {
		By locator=By.xpath("//yt-multi-page-menu-section-renderer[@class='style-scope ytd-multi-page-menu-renderer']//yt-formatted-string[@id='label' and text()='" + name + "']");
		WebElement safeElement = safeAct.safeFindElement(locator);
		log.info("Looking for language element with name: {}", name);
		return locator;
	}
	
	

		

	public void clickingUnderSearchInput() {
		log.info("Clicking into the search bar...");
		safeAct.safeFindElement(searchInputLandinfPage);
		safeAct.safeClick(searchInputLandinfPage);
	}

	public void givingInputUnderSearchBar(String input) {
		log.info("Giving input into the search bar with input as  = "+input);
		safeAct.safeClick(searchInputLandinfPage);
		safeAct.safeFindElement(searchInputLandinfPage).sendKeys(input);
	}


	public void clickingGlobalFilterButton() {
		log.info("Clicking on global filter button in landing page..");
		safeAct.safeClick(globalFilterButton);
	}


	public void clickingLocationDropdownUnderSettings() {
		log.info("Clicking on location dropdown under settings..");
		safeAct.safeClick(locationDropdownUnderSettings);

	}





	public void closeGlobalFilterPopup() {
		log.info("Closing the global filter pop-up in landing page..");
		safeAct.safeFindElement(closeButtonGlobalFilterPopup).click();
	}


	public List<WebElement> getLocationList() {
		log.info("Fetching the location list ...");
		List<WebElement>list=safeAct.safeFindElements(locationList);
		list.removeIf(el -> el.getText().trim().isEmpty());
		log.info("Fetched  the location list == "+list);
		return list;

	}


	public void openingLandingPage() {

		String url=ConfigManager.get("Url");
		driver.get(url);
		log.info("Opened YouTube landing page.");
	}


	public void clickingSettingEllipsesButton() throws InterruptedException {
		log.info("Clicking settings ellipses button...");
		safeAct.safeFindElement(settingEllipsesButton).click();
	}

	/** The rows you see inside the open settings menu (Appearance, Language, Location...). Language independent. */
	public By settingsMenuRows=By.xpath("//ytd-multi-page-menu-renderer//ytd-compact-link-renderer");

	/** The menu counts as open only while at least one of its rows is actually displayed on screen. */
	private boolean isSettingsMenuOpen() {
		try {
			// return driver.findElements(settingEllipsesOptionsListLandingPage).stream().anyMatch(WebElement::isDisplayed);
			return driver.findElements(settingsMenuRows).stream().anyMatch(WebElement::isDisplayed);
		} catch (StaleElementReferenceException e) {
			return false;
		}
	}

	/** Opens the settings menu only if it is closed, so repeated calls never toggle it shut. */
	public void openSettingsMenu() {
		if (!isSettingsMenuOpen()) {
			log.info("Settings menu is closed, opening it...");
			safeAct.safeFindElement(settingEllipsesButton).click();
		}
		wait.until(d -> isSettingsMenuOpen());
	}

	/** Closes the settings menu only if it is open, so repeated calls never toggle it open. */
	public void closeSettingsMenu() {
		if (isSettingsMenuOpen()) {
			log.info("Settings menu is open, closing it...");
			safeAct.safeFindElement(settingEllipsesButton).click();
		}
		wait.until(d -> !isSettingsMenuOpen());
	}

	public void clickingLanguageDropdownButton() throws InterruptedException {
		log.info("Clicking language dropdown...");
		safeAct.safeFindElement(languageDropdownUnderSettings).click();
	}

	public List<WebElement> gettingLanguageList() throws InterruptedException{
		log.info("Fetching available language list...");
		List<WebElement> langList=safeAct.safeFindElements(languageList);
		langList.removeIf(el -> el.getText().trim().isEmpty());
		log.info("Found {} languages.", langList.size());
		return langList;
	}


	public String getsettingEllipsesOptionsListLandingPage() {
		log.info("Getting the setting ellipses options");
		String str=safeAct.safeFindElement(settingEllipsesOptionsListLandingPage).getText().trim();
		return str;
	}


	public List<WebElement> gettingSideMenuExpandedList() throws InterruptedException{
		log.info("Fetching the side menu in expanded view");
		List<WebElement> sideMenuList=safeAct.safeFindElements(sideMenuExpandedList);
		return sideMenuList;
	}


	public List<WebElement> gettingSideMenuCollapsedList() throws InterruptedException{
		log.info("Fetching the side menu in collapsed view");
		List<WebElement> sideMenuList=safeAct.safeFindElements(sideMenuCollapsedList);
		return sideMenuList;
	}


	public void clickingLeftEllipses() {
		log.info("Clicking left ellipses in the landing page");
		safeAct.safeFindElement(ellipsesYtLandingPageTopLeft).click();
	}






	public String getDataFromGlobalFilterPopup() {
		log.info("Fetching the data from the global filter in landing page");
		String str=safeAct.safeFindElement(globalFilterData).getText().trim();
		return str;
	}





	public String getCountryCode() {
		log.info("Retrieving country code from YouTube logo...");
		String code= safeAct.safeFindElement(countryCodeOnYtLogoLandingPage).getText();
		log.info("Detected country code: {}", code);
		return code;
	}





}


