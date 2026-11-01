package main.java.yt_multi_lang_ui_validator.pages;

import java.time.Duration;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import main.java.yt_multi_lang_ui_validator.base.BasePage;
import main.java.yt_multi_lang_ui_validator.config.ConfigManager;
import main.java.yt_multi_lang_ui_validator.logger.LoggerUtility;
import main.java.yt_multi_lang_ui_validator.safeActions.SafeActions;

public class YtLandingPage  extends BasePage{

	private static final Logger log = LoggerUtility.getLogger(YtLandingPage.class);
	private final SafeActions safeAct;

	public YtLandingPage() {
		super();
		this.safeAct = new SafeActions(driver);
		log.info("Initialized YtLandingPage with driver: {}", driver);
	}


	// ------------------------------------------------------------------
	// Locators, grouped by place on the page
	// ------------------------------------------------------------------

	// top bar
	public By settingsButton=By.xpath("//ytd-topbar-menu-button-renderer[@class='style-scope ytd-masthead style-default']");

	public By sideMenuButton=By.xpath("//ytd-topbar-logo-renderer[@id='logo']/preceding-sibling::*[@id='guide-button']");

	public By searchBox=By.xpath("//input[@class='ytSearchboxComponentInput yt-searchbox-input title']");

	public By countryCodeLabel=By.xpath("(//span[@id='country-code'])[1]");

	// side menu
	public By expandedSideMenuEntries=By.xpath("//ytd-guide-entry-renderer[@class='style-scope ytd-guide-section-renderer']");

	public By collapsedSideMenuEntries=By.xpath("//ytd-mini-guide-renderer[@class='style-scope ytd-app']//ytd-mini-guide-entry-renderer");

	// settings menu
	public By settingsMenu=By.xpath("//ytd-multi-page-menu-renderer");

	/** The rows you see inside the open settings menu (Appearance, Language, Location...). Language independent. */
	public By settingsMenuRows=By.xpath("//ytd-multi-page-menu-renderer//ytd-compact-link-renderer");

	public By languageRow=By.xpath("(//div[@id='content-icon'])[2]");

	public By locationRow=By.xpath("(//div[@id='content-icon'])[4]");

	public By locationRows=By.xpath("//yt-multi-page-menu-section-renderer[@class='style-scope ytd-multi-page-menu-renderer']//ytd-compact-link-renderer");

	// search filter
	public By searchFilterButton = By.xpath("//div[@id='filter-button']");

	public By searchFilterPopup=By.xpath("//ytd-search-filter-options-dialog-renderer[@class='style-scope ytd-popup-container']");

	public By searchFilterCloseButton=By.xpath("//yt-icon[@icon='close']");


	// ------------------------------------------------------------------
	// Open and close
	// ------------------------------------------------------------------

	public void openLandingPage() {

		String url=ConfigManager.get("Url");
		driver.get(url);
		log.info("Opened YouTube landing page.");
	}


	/** One plain click on the settings button: opens the menu when it is closed and closes it when it is open. */
	public void toggleSettingsMenu() throws InterruptedException {
		log.info("Clicking settings ellipses button...");
		safeAct.safeFindElement(settingsButton).click();
	}

	/** Opens the settings menu only if it is closed, so repeated calls never toggle it shut. */
	public void openSettingsMenu() {
		if (!isSettingsMenuOpen()) {
			log.info("Settings menu is closed, opening it...");
			safeAct.safeFindElement(settingsButton).click();
		}
		wait.until(d -> isSettingsMenuOpen());
	}

	/** Closes the settings menu only if it is open, so repeated calls never toggle it open. */
	public void closeSettingsMenu() {
		if (isSettingsMenuOpen()) {
			log.info("Settings menu is open, closing it...");
			safeAct.safeFindElement(settingsButton).click();
		}
		wait.until(d -> !isSettingsMenuOpen());
	}

	/**
	 * The dropdown exists only while the settings menu is open. The ellipses click just before this one can be missed,
	 * and a page refresh (the SafeActions retry) would only close the menu again. So the menu is given a moment to
	 * show and, if it still has not, opened here.
	 */
	public void openLanguageList() throws InterruptedException {
		log.info("Clicking language dropdown...");
		// Old code: clicked straight away, so a missed ellipses click ended in "Find element failed after 3 attempts"
		// safeAct.safeFindElement(languageRow).click();
		recoverSettingsMenuIfClosed();
		safeAct.safeFindElement(languageRow).click();
	}

	public void openLocationList() {
		log.info("Clicking on location dropdown under settings..");
		// Old code: clicked straight away, even if the settings menu had not opened
		// safeAct.safeClick(locationRow);
		recoverSettingsMenuIfClosed();
		safeAct.safeClick(locationRow);

	}

	/** Collapses the side menu. Does nothing if it is already collapsed; clicks once more if the first click was missed. */
	public void collapseSideMenuIfExpanded() {
		log.info("Clicking left ellipses in the landing page");
		// Old code: always clicked once, with no check that the side menu really collapsed
		// safeAct.safeFindElement(sideMenuButton).click();
		if (isSideMenuCollapsed()) {
			log.info("Side menu is already collapsed, no click needed");
			return;
		}
		safeAct.safeFindElement(sideMenuButton).click();
		if (!sideMenuCollapsesWithin(3)) {
			log.warn("Side menu did not collapse, clicking left ellipses once more");
			safeAct.safeFindElement(sideMenuButton).click();
		}
	}

	/** The side menu is collapsed while its mini-guide entries are displayed. */
	private boolean isSideMenuCollapsed() {
		try {
			return driver.findElements(collapsedSideMenuEntries).stream().anyMatch(WebElement::isDisplayed);
		} catch (StaleElementReferenceException e) {
			return false;
		}
	}

	private boolean sideMenuCollapsesWithin(int seconds) {
		try {
			new WebDriverWait(driver, Duration.ofSeconds(seconds)).until(d -> isSideMenuCollapsed());
			return true;
		} catch (TimeoutException e) {
			return false;
		}
	}

	public void typeInSearchBox(String input) {
		log.info("Giving input into the search bar with input as  = "+input);
		safeAct.safeClick(searchBox);
		safeAct.safeFindElement(searchBox).sendKeys(input);
	}

	public void openSearchFilter() {
		log.info("Clicking on global filter button in landing page..");
		safeAct.safeClick(searchFilterButton);
	}

	public void closeSearchFilter() {
		log.info("Closing the global filter pop-up in landing page..");
		safeAct.safeFindElement(searchFilterCloseButton).click();
	}


	// ------------------------------------------------------------------
	// Pick
	// ------------------------------------------------------------------

	public WebElement languageEntry(String name) {
		By locator=By.xpath("//yt-multi-page-menu-section-renderer[@class='style-scope ytd-multi-page-menu-renderer']//yt-formatted-string[@id='label' and text()='" + name + "']");
		log.info("Looking for language element with name: {}", name);
		WebElement safeElement = safeAct.safeFindElement(locator);
		System.out.println(locator);
		return safeElement;
	}


	public By languageEntryLocator(String name) {
		By locator=By.xpath("//yt-multi-page-menu-section-renderer[@class='style-scope ytd-multi-page-menu-renderer']//yt-formatted-string[@id='label' and text()='" + name + "']");
		safeAct.safeFindElement(locator);
		log.info("Looking for language element with name: {}", name);
		return locator;
	}


	// ------------------------------------------------------------------
	// Read
	// ------------------------------------------------------------------

	public List<WebElement> readLocationRows() {
		log.info("Fetching the location list ...");
		List<WebElement>list=safeAct.safeFindElements(locationRows);
		list.removeIf(el -> el.getText().trim().isEmpty());
		log.info("Fetched  the location list == "+list);
		return list;

	}

	public List<WebElement> readExpandedSideMenuEntries() throws InterruptedException{
		log.info("Fetching the side menu in expanded view");
		List<WebElement> sideMenuList=readUntilStable(() -> safeAct.safeFindElements(expandedSideMenuEntries));
		return sideMenuList;
	}


	public List<WebElement> readCollapsedSideMenuEntries() throws InterruptedException{
		log.info("Fetching the side menu in collapsed view");
		List<WebElement> sideMenuList=readUntilStable(() -> safeAct.safeFindElements(collapsedSideMenuEntries));
		return sideMenuList;
	}


	/** Extra reads allowed after the first one while the menu is still changing. */
	private static final int MENU_MAX_EXTRA_READS = 10;
	/** Identical reads in a row (the first one included) that count as a settled menu. */
	private static final int MENU_STABLE_READS = 3;
	private static final long MENU_READ_INTERVAL_MS = 500;

	/**
	 * The menu can still be filling up after a language switch, so a single read may return only the first entries.
	 * Reads the menu, then reads it again every 500 ms until the joined text is identical on 3 reads in a row
	 * (at most 10 extra reads). Returns the last read if it never settles, so the comparison stays strict.
	 */
	public static List<WebElement> readUntilStable(java.util.function.Supplier<List<WebElement>> reader) throws InterruptedException {
		List<WebElement> elements = reader.get();
		String text = menuText(elements);
		int identicalReads = 1;
		for (int extraRead = 0; extraRead < MENU_MAX_EXTRA_READS && identicalReads < MENU_STABLE_READS; extraRead++) {
			Thread.sleep(MENU_READ_INTERVAL_MS);
			List<WebElement> reRead = reader.get();
			String reReadText = menuText(reRead);
			if (reReadText.equals(text)) {
				identicalReads++;
			} else {
				log.info("The menu was still changing ({} then {} characters), reading it again", text.length(), reReadText.length());
				identicalReads = 1;
			}
			elements = reRead;
			text = reReadText;
		}
		return elements;
	}

	/** Joined text of the menu entries; an entry that went stale while reading makes the text unequal, so it is read again. */
	private static String menuText(List<WebElement> elements) {
		StringBuilder text = new StringBuilder();
		for (WebElement element : elements) {
			try {
				text.append(element.getText()).append(' ');
			} catch (StaleElementReferenceException e) {
				text.append("<stale>").append(System.nanoTime());
			}
		}
		return text.toString();
	}

	public String readSettingsMenuText() {
		log.info("Getting the setting ellipses options");
		String str=safeAct.safeFindElement(settingsMenu).getText().trim();
		return str;
	}

	public String readSearchFilterText() {
		log.info("Fetching the data from the global filter in landing page");
		String str=safeAct.safeFindElement(searchFilterPopup).getText().trim();
		return str;
	}

	public String readCountryCode() {
		log.info("Retrieving country code from YouTube logo...");
		String code= safeAct.safeFindElement(countryCodeLabel).getText();
		log.info("Detected country code: {}", code);
		return code;
	}


	// ------------------------------------------------------------------
	// Recover
	// ------------------------------------------------------------------

	/** Recovery step: call right after the ellipses click and before using a dropdown that lives inside the settings menu. */
	public void recoverSettingsMenuIfClosed() {
		if (!settingsMenuOpensWithin(3)) {
			log.warn("Settings menu is not open, opening it before using a dropdown inside it");
			openSettingsMenu();
		}
	}

	private boolean settingsMenuOpensWithin(int seconds) {
		try {
			new WebDriverWait(driver, Duration.ofSeconds(seconds)).until(d -> isSettingsMenuOpen());
			return true;
		} catch (TimeoutException e) {
			return false;
		}
	}

	/** The menu counts as open only while at least one of its rows is actually displayed on screen. */
	private boolean isSettingsMenuOpen() {
		try {
			// return driver.findElements(settingsMenu).stream().anyMatch(WebElement::isDisplayed);
			return driver.findElements(settingsMenuRows).stream().anyMatch(WebElement::isDisplayed);
		} catch (StaleElementReferenceException e) {
			return false;
		}
	}


	// ------------------------------------------------------------------
	// Not used by any test (kept as it was, listed for a later cleanup)
	// ------------------------------------------------------------------

	public By sideMenu=By.xpath("//div[@class='style-scope ytd-mini-guide-renderer']");

	public By languageList=By.xpath("//yt-multi-page-menu-section-renderer[@class='style-scope ytd-multi-page-menu-renderer']//ytd-compact-link-renderer");

	public By languageListUpdated=By.xpath("//ytd-multi-page-menu-renderer[@slot='dropdown-content']");

	//Cannot use this directly as this banner wouldnt show if yt does not have a search histroy so to show these recommendation banner
	public By horizontalScrollBarLandingPage=By.xpath("//div[@id='scroll-container']");

	public void clickingUnderSearchInput() {
		log.info("Clicking into the search bar...");
		safeAct.safeFindElement(searchBox);
		safeAct.safeClick(searchBox);
	}

	public List<WebElement> gettingLanguageList() throws InterruptedException{
		log.info("Fetching available language list...");
		List<WebElement> langList=safeAct.safeFindElements(languageList);
		langList.removeIf(el -> el.getText().trim().isEmpty());
		log.info("Found {} languages.", langList.size());
		return langList;
	}

}
