package main.test.java.tests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import main.java.yt_multi_lang_ui_validator.base.BaseTest;
import main.java.yt_multi_lang_ui_validator.fileReader.DataSheets;
import main.java.yt_multi_lang_ui_validator.fileReader.SheetLookup;
import main.java.yt_multi_lang_ui_validator.lingua.LinguaHelper;
import main.java.yt_multi_lang_ui_validator.logger.LoggerUtility;
import main.java.yt_multi_lang_ui_validator.pages.YtLandingPage;
import main.java.yt_multi_lang_ui_validator.utilities.GenericUtility;
import main.java.yt_multi_lang_ui_validator.utilities.Canary;
import main.java.yt_multi_lang_ui_validator.utilities.LoopGuard;
import main.java.yt_multi_lang_ui_validator.utilities.ScreenshotUtilUpdated;
import main.java.yt_multi_lang_ui_validator.utilities.WaitUtility;



public class YtMasterTests extends BaseTest{

	private static final  Logger log=LoggerUtility.getLogger(YtMasterTests.class);

	@Test(groups = {"non-lingua", "side-menu-expanded"})
	public void verifyingSideMenuText_Expanded() throws InterruptedException {



		YtLandingPage landingPage=new YtLandingPage();
		SoftAssert softAssert = new SoftAssert();
		GenericUtility genericUtility= new GenericUtility();


		List<String> languages=DataSheets.languages().names();


		landingPage.openLandingPage();


		landingPage.toggleSettingsMenu();

		landingPage.openLanguageList();

		String testName = ThreadContext.get("logFileName");
		int languageCount = genericUtility.languageCountToRun(languages.size());


		LoopGuard guard=new LoopGuard(softAssert);
		for(int i=0;i<languageCount && !guard.shouldStop();i++) {

			String langText=languages.get(i);
			try {
				System.out.println(langText+" lang text from the sheet   "+(i+1));

				ScreenshotUtilUpdated.capture(testName, langText);


				landingPage.languageEntry(langText).click();

				landingPage.toggleSettingsMenu();
				landingPage.recoverSettingsMenuIfClosed();
				genericUtility.isElementInViewport(landingPage.languageRow);
				ScreenshotUtilUpdated.capture(testName, langText);
				landingPage.toggleSettingsMenu();


				List<WebElement> listOfSideMenu=landingPage.readExpandedSideMenuEntries();

				StringBuilder sideMenuItems=new StringBuilder();
				for(int sideMenuItem=0;sideMenuItem<listOfSideMenu.size();sideMenuItem++) {
					System.out.println(listOfSideMenu.get(sideMenuItem).getText());
					sideMenuItems.append(listOfSideMenu.get(sideMenuItem).getText());

					if(sideMenuItem!=listOfSideMenu.size()-1) {
						sideMenuItems.append(" ");
					}

				}

				System.out.println(sideMenuItems.toString()+"        Append text from list");


				List<String> sideMenuTexts=new ArrayList<>();
				for(WebElement sideMenuElement:listOfSideMenu) {
					sideMenuTexts.add(sideMenuElement.getText());
				}
				// the sheet lists every version of the menu YouTube may show (slot and Premium entry options); the page must equal one of them
				SheetLookup.SheetRow sideMenuRow=DataSheets.sideMenuExpanded().get(langText);
				String baseText=Canary.canary(sideMenuRow.get("Expected Text"), i, 0);
				List<String[]> acceptedTexts=GenericUtility.loadAcceptedSideMenuTexts(sideMenuRow, baseText);
				String actualData=GenericUtility.comparableSideMenuText(String.join(" ", sideMenuTexts));
				int acceptedIndex=GenericUtility.indexOfAcceptedText(acceptedTexts, actualData);
				// no match: compare with the usual (first) version so the failure shows the usual expected / received difference
				String expectedData=acceptedTexts.get(acceptedIndex>=0?acceptedIndex:0)[0];
				String version=acceptedIndex>=0?acceptedTexts.get(acceptedIndex)[1]:"none of the "+acceptedTexts.size()+" matches";


				System.out.println("========="+expectedData+" =====  Expected data from the sheet");
				GenericUtility.logComparison("Side menu", langText, acceptedIndex>=0, actualData.trim().length(), "version: "+version);
				guard.count(acceptedIndex==0?"usual version":acceptedIndex>0?"other version":"unknown version");
				GenericUtility.softAssertEquals(softAssert, guard, actualData.trim(),expectedData.trim(),"Side menu text differs | language="+langText);


				landingPage.toggleSettingsMenu();

				landingPage.openLanguageList();


				guard.passed(langText);
			} catch (Exception e) {
				if (guard.retryAfterFailure(langText, e, () -> reopenLanguageDropdown(landingPage))) {
					i--; // run the same language again
				}
			}
		}

		guard.logSummary(languageCount);

		softAssert.assertAll();
	}









	@Test(groups = {"lingua", "side-menu-expanded"})
	public void verifyingSideMenuLanguage_Expanded() throws InterruptedException {



		YtLandingPage landingPage=new YtLandingPage();
		GenericUtility genericUtility=new GenericUtility();
		SoftAssert softAssert = new SoftAssert();


		List<String> languages=DataSheets.languages().names();




		landingPage.openLandingPage();




		landingPage.toggleSettingsMenu();

		landingPage.openLanguageList();

		String testName = ThreadContext.get("logFileName");
		int languageCount = genericUtility.languageCountToRun(languages.size());




		LoopGuard guard=new LoopGuard(softAssert);
		for(int i=0;i<languageCount && !guard.shouldStop();i++) {

			String langText=languages.get(i);
			try {
				System.out.println(langText+" lang text from the sheet   "+(i+1));






				landingPage.languageEntry(langText).click();	

				landingPage.toggleSettingsMenu();
				landingPage.recoverSettingsMenuIfClosed();
				genericUtility.isElementInViewport(landingPage.languageRow);
				ScreenshotUtilUpdated.capture(testName, langText);
				landingPage.toggleSettingsMenu();


				List<WebElement> listOfSideMenu=landingPage.readExpandedSideMenuEntries();

				StringBuilder sideMenuItems=new StringBuilder();
				for(int sideMenuItem=0;sideMenuItem<listOfSideMenu.size();sideMenuItem++) {
					System.out.println(listOfSideMenu.get(sideMenuItem).getText());
					sideMenuItems.append(listOfSideMenu.get(sideMenuItem).getText());
					if(sideMenuItem!=listOfSideMenu.size()-1) {
						sideMenuItems.append(" ");
					}

				}




				String applicableLanguage=langText;
				String detectedLanguage=LinguaHelper.detectLanguage(genericUtility.removeEnglishWordsForNonEnglish(sideMenuItems.toString(), langText));
				String expectedLanguage=Canary.canary(DataSheets.expectedLanguage().get(langText).get("Expected Language"), i, 1);


				String expectedLanguageAttribute=Canary.canary(DataSheets.expectedAttribute().get(langText).get("Expected Attribute"), i, 2);
				String detectedLanguageAttribute=genericUtility.readHtmlLang();

				System.out.println("ApplicableLanguage  "+applicableLanguage+"     "+"Detected Language   "+detectedLanguage+"  "+"Expected Language  "+expectedLanguage);
				System.out.println("ApplicableLanguage  "+applicableLanguage+"     "+"Detected Language attribute   "+detectedLanguageAttribute+"  "+"Expected Language attribute "+expectedLanguageAttribute);

				log.info("[{}] ApplicableLanguage==  "+applicableLanguage+"     "+"Detected Language ==  "+detectedLanguage+"  "+"Expected Language == "+expectedLanguage, ThreadContext.get("testName"));

				genericUtility.assertLanguageDetectedAndAttributeMatch(softAssert, guard, langText, detectedLanguage, expectedLanguage, detectedLanguageAttribute, expectedLanguageAttribute, true, false);


				landingPage.toggleSettingsMenu();

				landingPage.openLanguageList();


				guard.passed(langText);
			} catch (Exception e) {
				if (guard.retryAfterFailure(langText, e, () -> reopenLanguageDropdown(landingPage))) {
					i--; // run the same language again
				}
			}
		}

		guard.logSummary(languageCount);

		softAssert.assertAll();
	}





	@Test(groups = {"non-lingua", "side-menu-collapsed"})
	public void verifyingSideMenuText_Collapsed() throws InterruptedException, InvalidFormatException, IOException {


		YtLandingPage landingPage=new YtLandingPage();
		GenericUtility genericUtility=new GenericUtility();
		WaitUtility waitUtility=new WaitUtility();
		SoftAssert softAssert = new SoftAssert();



		List<String> languages=DataSheets.languages().names();




		int languageCount = genericUtility.languageCountToRun(languages.size());
		String testName = ThreadContext.get("logFileName");

		landingPage.openLandingPage();
		landingPage.collapseSideMenuIfExpanded();



		landingPage.toggleSettingsMenu();

		landingPage.openLanguageList();





		LoopGuard guard=new LoopGuard(softAssert);
		for(int i=0;i<languageCount && !guard.shouldStop();i++) {

			String langText=languages.get(i);
			try {
				System.out.println(langText+"    "+(i+1));


				landingPage.languageEntry(langText).click();
				waitUtility.waitForElementReady(landingPage.collapsedSideMenuEntries);
				landingPage.toggleSettingsMenu();
				landingPage.recoverSettingsMenuIfClosed();
				genericUtility.isElementInViewport(landingPage.languageRow);
				ScreenshotUtilUpdated.capture(testName, langText);
				landingPage.toggleSettingsMenu();

				List<WebElement> listOfSideMenu=landingPage.readCollapsedSideMenuEntries();

				StringBuilder sideMenuItems=new StringBuilder();
				for(int sideMenuItem=0;sideMenuItem<listOfSideMenu.size();sideMenuItem++) {
					System.out.println(listOfSideMenu.get(sideMenuItem).getText());
					sideMenuItems.append(listOfSideMenu.get(sideMenuItem).getText());

					if(sideMenuItem!=listOfSideMenu.size()-1) {
						sideMenuItems.append(" ");
					}
				}

				String expectedData =	Canary.canary(DataSheets.sideMenuCollapsed().get(langText).get("Expected Text"), i, 0);
				String actualData=sideMenuItems.toString();


				GenericUtility.logComparison("Collapsed side menu", langText, actualData.trim().equals(expectedData.trim()), actualData.trim().length(), null);
				GenericUtility.softAssertEquals(softAssert, guard, actualData.trim(),expectedData.trim(),"Side menu text differs | language="+langText);


				landingPage.toggleSettingsMenu();

				landingPage.openLanguageList();

				guard.passed(langText);
			} catch (Exception e) {
				if (guard.retryAfterFailure(langText, e, () -> reopenCollapsedLanguageDropdown(landingPage))) {
					i--; // run the same language again
				}
			}
		}



		guard.logSummary(languageCount);

		softAssert.assertAll();	
	}









	@Test(groups = {"lingua", "side-menu-collapsed"})
	public void verifyingSideMenuLanguage_Collapsed() throws InterruptedException, InvalidFormatException, IOException {


		YtLandingPage landingPage=new YtLandingPage();
		GenericUtility genericUtility=new GenericUtility();
		SoftAssert softAssert = new SoftAssert();




		List<String> languages=DataSheets.languages().names();




		int languageCount = genericUtility.languageCountToRun(languages.size());
		String testName = ThreadContext.get("logFileName");
		landingPage.openLandingPage();





		genericUtility.getWindowHeightWidth();
		landingPage.collapseSideMenuIfExpanded();


		landingPage.toggleSettingsMenu();


		landingPage.openLanguageList();





		LoopGuard guard=new LoopGuard(softAssert);
		for(int i=0;i<languageCount && !guard.shouldStop();i++) {

			String langText=languages.get(i);
			try {
				System.out.println(langText+"    "+(i+1));


				landingPage.languageEntry(langText).click();

				landingPage.toggleSettingsMenu();
				landingPage.recoverSettingsMenuIfClosed();
				genericUtility.isElementInViewport(landingPage.languageRow);
				ScreenshotUtilUpdated.capture(testName, langText);
				landingPage.toggleSettingsMenu();

				List<WebElement> listOfSideMenu=landingPage.readCollapsedSideMenuEntries();

				StringBuilder sideMenuItems=new StringBuilder();
				for(int sideMenuItem=0;sideMenuItem<listOfSideMenu.size();sideMenuItem++) {
					System.out.println(listOfSideMenu.get(sideMenuItem).getText());
					sideMenuItems.append(listOfSideMenu.get(sideMenuItem).getText());
					if(sideMenuItem!=listOfSideMenu.size()-1) {
						sideMenuItems.append(" ");
					}
				}



				String applicableLanguage=langText;
				String textForLingua=genericUtility.removeEnglishWordsForNonEnglish(sideMenuItems.toString(), langText);
				String detectedLanguage=LinguaHelper.detectLanguage(textForLingua);
				log.info("text sent to lingua is "+"      "+textForLingua);
				String expectedLanguage=Canary.canary(DataSheets.expectedLanguage().get(langText).get("Expected Language"), i, 3);


				String expectedLanguageAttribute=Canary.canary(DataSheets.expectedAttribute().get(langText).get("Expected Attribute"), i, 2);

				String detectedLanguageAttribute=genericUtility.readHtmlLang();

				System.out.println("ApplicableLanguage  "+applicableLanguage+"     "+"Detected Language   "+detectedLanguage+"  "+"Expected Language  "+expectedLanguage);
				System.out.println("ApplicableLanguage  "+applicableLanguage+"     "+"Detected Language attribute   "+detectedLanguageAttribute+"  "+"Expected Language attribute "+expectedLanguageAttribute);



				genericUtility.assertLanguageDetectedAndAttributeMatch(softAssert, guard, langText, detectedLanguage, expectedLanguage, detectedLanguageAttribute, expectedLanguageAttribute, true, true);


				landingPage.toggleSettingsMenu();

				landingPage.openLanguageList();

				guard.passed(langText);
			} catch (Exception e) {
				if (guard.retryAfterFailure(langText, e, () -> reopenCollapsedLanguageDropdown(landingPage))) {
					i--; // run the same language again
				}
			}
		}



		guard.logSummary(languageCount);

		softAssert.assertAll();	
	}


	@Test(groups = {"non-lingua", "settings-region"})
	public void verifyingSettingsMenuLanguage() throws InterruptedException {



		YtLandingPage landingPage=new YtLandingPage();
		GenericUtility genericUtility=new GenericUtility();
		SoftAssert softAssert=new SoftAssert();



		List<String> languages=DataSheets.languages().names();


		String testName= ThreadContext.get("testName");


		int languageCount = genericUtility.languageCountToRun(languages.size());

		landingPage.openLandingPage();
		landingPage.toggleSettingsMenu();

		landingPage.openLanguageList();






		LoopGuard guard=new LoopGuard(softAssert);
		for(int i=0;i<languageCount && !guard.shouldStop();i++) {

			String langText=languages.get(i);
			try {
				System.out.println(langText+"    "+(i+1));


				landingPage.languageEntry(langText).click();

				// landingPage.toggleSettingsMenu();
				landingPage.openSettingsMenu();
				genericUtility.isElementInViewport(landingPage.languageRow);
				ScreenshotUtilUpdated.capture(testName, langText);
				// landingPage.toggleSettingsMenu();
				//
				// landingPage.toggleSettingsMenu();

				String languageFromSettings=landingPage.readSettingsMenuText();
				// landingPage.toggleSettingsMenu();
				landingPage.closeSettingsMenu();


				String applicableLanguage=langText;
				String detectedLanguage=LinguaHelper.detectLanguage(genericUtility.repeatTextForDetection(languageFromSettings, 4));
				String expectedLanguage=Canary.canary(DataSheets.expectedLanguage().get(langText).get("Expected Language"), i, 1);


				String expectedLanguageAttribute=Canary.canary(DataSheets.expectedAttribute().get(Canary.canary(langText, i, 4)).get("Expected Attribute"), i, 2);
				String detectedLanguageAttribute=genericUtility.readHtmlLang();

				System.out.println("ApplicableLanguage  "+applicableLanguage+"     "+"Detected Language   "+detectedLanguage+"  "+"Expected Language  "+expectedLanguage);
				System.out.println("ApplicableLanguage  "+applicableLanguage+"     "+"Detected Language attribute   "+detectedLanguageAttribute+"  "+"Expected Language attribute "+expectedLanguageAttribute);

				genericUtility.assertLanguageDetectedAndAttributeMatch(softAssert, guard, langText, detectedLanguage, expectedLanguage, detectedLanguageAttribute, expectedLanguageAttribute, false, false);


				// landingPage.toggleSettingsMenu();
				landingPage.openSettingsMenu();
				landingPage.openLanguageList();


				guard.passed(langText);
			} catch (Exception e) {
				if (guard.retryAfterFailure(langText, e, () -> reopenLanguageDropdown(landingPage))) {
					i--; // run the same language again
				}
			}
		}


		guard.logSummary(languageCount);

		softAssert.assertAll();	
	}




	// The two halves run in parallel in separate browsers. The names are written out by hand: first and last country of the half
	// (111 countries: 56 and 55). DataIntegrityTest fails when the country sheet no longer matches them.
	@Test(groups = {"non-lingua", "settings-region"})
	public void verifyingCountryCodeOnLogo_Algeria_to_Liechtenstein() throws InterruptedException {
		checkCountryCodes(true);
	}

	@Test(groups = {"non-lingua", "settings-region"})
	public void verifyingCountryCodeOnLogo_Lithuania_to_Zimbabwe() throws InterruptedException {
		checkCountryCodes(false);
	}

	/** Checks the country code on the logo for one half of the locations: [0, middle) or [middle, total), middle = ceil(total / 2). */
	private void checkCountryCodes(boolean firstHalf) throws InterruptedException {


		YtLandingPage landingPage=new YtLandingPage();
		SoftAssert softAssert=new SoftAssert();
		GenericUtility genericUtility = new GenericUtility();




		landingPage.openLandingPage();

		landingPage.toggleSettingsMenu();

		landingPage.openLocationList();




		List<WebElement> locationRows=landingPage.readLocationRows();
		int locationListSize=locationRows.size();
		int sheetCount=DataSheets.countries().size();
		if(sheetCount!=locationListSize) {
			log.warn("[{}] Country sheet has {} rows but the page lists {} locations", ThreadContext.get("testName"), sheetCount, locationListSize);
		}



		String testName= ThreadContext.get("testName");



		int total = genericUtility.locationCountToRun(locationListSize);
		int middle = (total + 1) / 2;
		int start = firstHalf ? 0 : middle;
		int end = firstHalf ? middle : total;
		if(start >= end) {
			log.info("[{}] Country codes, {} half: no locations ({} in total)", testName, firstHalf ? "first" : "second", total);
			return;
		}
		log.info("[{}] Country codes, {} half: locations {} to {} of {}", testName, firstHalf ? "first" : "second", start, end-1, total);





		LoopGuard guard=new LoopGuard(softAssert);
		for(int locationIndex=start;locationIndex<end && !guard.shouldStop();locationIndex++) {
			String locationText = "location #"+locationIndex;
			try {
				locationRows=landingPage.readLocationRows();
				locationText = locationRows.get(locationIndex).getText();


				System.out.println(locationText+"    "+locationIndex);





				locationRows.get(locationIndex).click();

				landingPage.toggleSettingsMenu();
				genericUtility.isElementInViewport(landingPage.locationRow);
				ScreenshotUtilUpdated.capture(testName, locationText);
				landingPage.toggleSettingsMenu();



				String applicableLocation=locationText;
				String sheetCountryCode=DataSheets.countries().get(locationText).get("Country Code");
				String expectedCountryCode=Canary.canary(Canary.canary(sheetCountryCode, locationIndex-start, 0), locationIndex-start, 1);
				String detectedCountryCode=landingPage.readCountryCode();

				System.out.println("applicable location is ==  "+applicableLocation+" Expected location  =="+expectedCountryCode+"  detected country code"+detectedCountryCode);
				boolean codeMatches=java.util.Objects.equals(detectedCountryCode, expectedCountryCode);
				GenericUtility.logComparison("Country code", locationText, codeMatches, detectedCountryCode==null ? 0 : detectedCountryCode.length(), "code: '"+detectedCountryCode+"'");
				GenericUtility.softAssertEquals(softAssert, guard, detectedCountryCode, expectedCountryCode,"Country code differs | location="+locationText);
				if(sheetCountryCode.isEmpty()) {
					// only the United States has no code, an empty expected code on any other row would pass a failed read
					guard.count("no code expected");
					GenericUtility.softAssertEquals(softAssert, guard, locationText, "United States", "Expected country code is empty for \""+locationText+"\", only \"United States\" has none.");
				}

				landingPage.toggleSettingsMenu();

				landingPage.openLocationList();

				guard.passed(locationText);
			} catch (Exception e) {
				if (guard.retryAfterFailure(locationText, e, () -> reopenLocationDropdown(landingPage))) {
					locationIndex--; // run the same location again
				}
			}
		}
		guard.logSummary(end - start);

		softAssert.assertAll();
	}



	@Test(groups = {"non-lingua", "global-filter"})
	public void verifyingSearchFilterText() throws InterruptedException {


		GenericUtility genericUtility=new GenericUtility();
		YtLandingPage landingPage=new YtLandingPage();
		SoftAssert softAssert =new SoftAssert();



		List<String> languages=DataSheets.languages().names();


		landingPage.openLandingPage();
		landingPage.typeInSearchBox("youtube");
		genericUtility.clickEnter(landingPage.searchBox);

		int languageCount = genericUtility.languageCountToRun(languages.size());



		String testName= ThreadContext.get("testName");




		LoopGuard guard=new LoopGuard(softAssert);
		for(int i=0;i<languageCount && !guard.shouldStop();i++) {

			String langText=languages.get(i);
			try {
				landingPage.toggleSettingsMenu();

				landingPage.openLanguageList();


				System.out.println(langText+"    "+(i+1));


				genericUtility.smoothScrollToElement(landingPage.languageEntryLocator(langText));
				landingPage.languageEntry(langText).click();	

				landingPage.toggleSettingsMenu();
				landingPage.recoverSettingsMenuIfClosed();
				genericUtility.isElementInViewport(landingPage.languageRow);
				ScreenshotUtilUpdated.capture(testName, langText);
				landingPage.toggleSettingsMenu();



				landingPage.openSearchFilter();
				genericUtility.isElementInViewport(landingPage.searchFilterPopup);
				ScreenshotUtilUpdated.capture(testName, langText);
				String globalSettingText=landingPage.readSearchFilterText();
				landingPage.closeSearchFilter();


				String expectedData=Canary.canary(DataSheets.searchFilter().get(langText).get("Expected Text"), i, 0);
				String actualValue=globalSettingText;


				String actualNormalized=GenericUtility.normalizeText(actualValue);
				String expectedNormalized=GenericUtility.normalizeText(expectedData);
				GenericUtility.logComparison("Search filter", langText, actualNormalized.equals(expectedNormalized), actualNormalized.length(), null);
				GenericUtility.softAssertEquals(softAssert, guard, actualNormalized, expectedNormalized, "Global filter text differs | language="+langText);

				guard.passed(langText);
			} catch (Exception e) {
				if (guard.retryAfterFailure(langText, e, () -> reopenSearchResults(landingPage, genericUtility))) {
					i--; // run the same language again
				}
			}
		}


		guard.logSummary(languageCount);

		softAssert.assertAll();	



	}






	@Test(groups = {"lingua", "global-filter"})
	public void verifyingSearchFilterLanguage() throws InterruptedException {


		GenericUtility genericUtility=new GenericUtility();
		YtLandingPage landingPage=new YtLandingPage();
		SoftAssert softAssert =new SoftAssert();


		List<String> languages=DataSheets.languages().names();


		int languageCount = genericUtility.languageCountToRun(languages.size());


		String testName= ThreadContext.get("testName");



		landingPage.openLandingPage();
		landingPage.typeInSearchBox("youtube");
		genericUtility.clickEnter(landingPage.searchBox);





		LoopGuard guard=new LoopGuard(softAssert);
		for(int i=0;i<languageCount && !guard.shouldStop();i++) {

			String langText=languages.get(i);
			try {
				landingPage.toggleSettingsMenu();

				landingPage.openLanguageList();


				System.out.println(langText+"    "+(i+1));


				landingPage.languageEntry(langText).click();	

				landingPage.toggleSettingsMenu();
				landingPage.recoverSettingsMenuIfClosed();
				genericUtility.isElementInViewport(landingPage.languageRow);
				ScreenshotUtilUpdated.capture(testName, langText);
				landingPage.toggleSettingsMenu();



				landingPage.openSearchFilter();
				genericUtility.isElementInViewport(landingPage.searchFilterPopup);
				ScreenshotUtilUpdated.capture(testName, langText);
				String globalSettingText=landingPage.readSearchFilterText();
				landingPage.closeSearchFilter();




				String applicableLanguage=langText;
				String detectedLanguage=LinguaHelper.detectLanguage(genericUtility.removeEnglishWordsForNonEnglish(globalSettingText, langText));
				String expectedLanguage=Canary.canary(DataSheets.expectedLanguage().get(langText).get("Expected Language"), i, 1);


				String expectedLanguageAttribute=Canary.canary(DataSheets.expectedAttribute().get(langText).get("Expected Attribute"), i, 2);
				String detectedLanguageAttribute=genericUtility.readHtmlLang();

				System.out.println("ApplicableLanguage  "+applicableLanguage+"     "+"Detected Language   "+detectedLanguage+"  "+"Expected Language  "+expectedLanguage);
				System.out.println("ApplicableLanguage  "+applicableLanguage+"     "+"Detected Language attribute   "+detectedLanguageAttribute+"  "+"Expected Language attribute "+expectedLanguageAttribute);

				genericUtility.assertLanguageDetectedAndAttributeMatch(softAssert, guard, langText, detectedLanguage, expectedLanguage, detectedLanguageAttribute, expectedLanguageAttribute, false, false);

				guard.passed(langText);
			} catch (Exception e) {
				if (guard.retryAfterFailure(langText, e, () -> reopenSearchResults(landingPage, genericUtility))) {
					i--; // run the same language again
				}
			}
		}



		guard.logSummary(languageCount);

		softAssert.assertAll();



	}


	// Recovery steps used by LoopGuard after a failure: they bring the page back to the state each loop starts from.

	private void reopenLanguageDropdown(YtLandingPage landingPage) throws InterruptedException {
		landingPage.openLandingPage();
		landingPage.toggleSettingsMenu();
		landingPage.openLanguageList();
	}

	private void reopenCollapsedLanguageDropdown(YtLandingPage landingPage) throws InterruptedException {
		landingPage.openLandingPage();
		landingPage.collapseSideMenuIfExpanded();
		landingPage.toggleSettingsMenu();
		landingPage.openLanguageList();
	}

	private void reopenLocationDropdown(YtLandingPage landingPage) throws InterruptedException {
		landingPage.openLandingPage();
		landingPage.toggleSettingsMenu();
		landingPage.openLocationList();
	}

	private void reopenSearchResults(YtLandingPage landingPage, GenericUtility genericUtility) throws InterruptedException {
		landingPage.openLandingPage();
		landingPage.typeInSearchBox("youtube");
		genericUtility.clickEnter(landingPage.searchBox);
	}

}
