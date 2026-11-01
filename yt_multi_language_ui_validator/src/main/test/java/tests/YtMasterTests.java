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
	public void verifyingSideMenuLanguageAsInSettings() throws InterruptedException {



		YtLandingPage landingPage=new YtLandingPage();
		SoftAssert softAssert = new SoftAssert();
		GenericUtility genericUtility= new GenericUtility();


		List<String> languages=DataSheets.languages().names();


		landingPage.openingLandingPage();


		landingPage.clickingSettingEllipsesButton();

		landingPage.clickingLanguageDropdownButton();

		String testName = ThreadContext.get("logFileName");
		int languageCount = genericUtility.resolveLanguageCount(languages.size());


		LoopGuard guard=new LoopGuard(softAssert);
		for(int i=0;i<languageCount && !guard.shouldStop();i++) {

			String langText=languages.get(i);
			try {
				System.out.println(langText+" lang text from the sheet   "+(i+1));

				ScreenshotUtilUpdated.capture(testName, langText);


				landingPage.getLanguageElementByName(langText).click();

				landingPage.clickingSettingEllipsesButton();
				landingPage.recoverSettingsMenuIfClosed();
				genericUtility.isElementInViewport(landingPage.languageDropdownUnderSettings);
				ScreenshotUtilUpdated.capture(testName, langText);
				landingPage.clickingSettingEllipsesButton();


				List<WebElement> listOfSideMenu=landingPage.gettingSideMenuExpandedList();

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
				String baseText=Canary.canary(sideMenuRow.get("Expected Data"), i, 0);
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


				landingPage.clickingSettingEllipsesButton();

				landingPage.clickingLanguageDropdownButton();


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
	public void verifyingSideMenuLanguageAsInSettingsLingua() throws InterruptedException {



		YtLandingPage landingPage=new YtLandingPage();
		GenericUtility genericUtility=new GenericUtility();
		SoftAssert softAssert = new SoftAssert();


		List<String> languages=DataSheets.languages().names();




		landingPage.openingLandingPage();




		landingPage.clickingSettingEllipsesButton();

		landingPage.clickingLanguageDropdownButton();

		String testName = ThreadContext.get("logFileName");
		int languageCount = genericUtility.resolveLanguageCount(languages.size());




		LoopGuard guard=new LoopGuard(softAssert);
		for(int i=0;i<languageCount && !guard.shouldStop();i++) {

			String langText=languages.get(i);
			try {
				System.out.println(langText+" lang text from the sheet   "+(i+1));






				landingPage.getLanguageElementByName(langText).click();	

				landingPage.clickingSettingEllipsesButton();
				landingPage.recoverSettingsMenuIfClosed();
				genericUtility.isElementInViewport(landingPage.languageDropdownUnderSettings);
				ScreenshotUtilUpdated.capture(testName, langText);
				landingPage.clickingSettingEllipsesButton();


				List<WebElement> listOfSideMenu=landingPage.gettingSideMenuExpandedList();

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
				String detectedLanguageAttribute=genericUtility.getLangAttribute();

				System.out.println("ApplicableLanguage  "+applicableLanguage+"     "+"Detected Language   "+detectedLanguage+"  "+"Expected Language  "+expectedLanguage);
				System.out.println("ApplicableLanguage  "+applicableLanguage+"     "+"Detected Language attribute   "+detectedLanguageAttribute+"  "+"Expected Language attribute "+expectedLanguageAttribute);

				log.info("[{}] ApplicableLanguage==  "+applicableLanguage+"     "+"Detected Language ==  "+detectedLanguage+"  "+"Expected Language == "+expectedLanguage, ThreadContext.get("testName"));

				genericUtility.assertLanguageDetectedAndAttributeMatch(softAssert, guard, langText, detectedLanguage, expectedLanguage, detectedLanguageAttribute, expectedLanguageAttribute, true, false);


				landingPage.clickingSettingEllipsesButton();

				landingPage.clickingLanguageDropdownButton();


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
	public void verifyingSideMenuCollapsedLangAsInSettings() throws InterruptedException, InvalidFormatException, IOException {


		YtLandingPage landingPage=new YtLandingPage();
		GenericUtility genericUtility=new GenericUtility();
		WaitUtility waitUtility=new WaitUtility();
		SoftAssert softAssert = new SoftAssert();



		List<String> languages=DataSheets.languages().names();




		int languageCount = genericUtility.resolveLanguageCount(languages.size());
		String testName = ThreadContext.get("logFileName");

		landingPage.openingLandingPage();
		landingPage.clickingLeftEllipses();



		landingPage.clickingSettingEllipsesButton();

		landingPage.clickingLanguageDropdownButton();





		LoopGuard guard=new LoopGuard(softAssert);
		for(int i=0;i<languageCount && !guard.shouldStop();i++) {

			String langText=languages.get(i);
			try {
				System.out.println(langText+"    "+(i+1));


				landingPage.getLanguageElementByName(langText).click();
				waitUtility.waitForElementReady(landingPage.sideMenuCollapsedList);
				landingPage.clickingSettingEllipsesButton();
				landingPage.recoverSettingsMenuIfClosed();
				genericUtility.isElementInViewport(landingPage.languageDropdownUnderSettings);
				ScreenshotUtilUpdated.capture(testName, langText);
				landingPage.clickingSettingEllipsesButton();

				List<WebElement> listOfSideMenu=landingPage.gettingSideMenuCollapsedList();

				StringBuilder sideMenuItems=new StringBuilder();
				for(int sideMenuItem=0;sideMenuItem<listOfSideMenu.size();sideMenuItem++) {
					System.out.println(listOfSideMenu.get(sideMenuItem).getText());
					sideMenuItems.append(listOfSideMenu.get(sideMenuItem).getText());

					if(sideMenuItem!=listOfSideMenu.size()-1) {
						sideMenuItems.append(" ");
					}
				}

				String expectedData =	Canary.canary(DataSheets.sideMenuCollapsed().get(langText).get("Expected Data"), i, 0);
				String actualData=sideMenuItems.toString();


				GenericUtility.logComparison("Collapsed side menu", langText, actualData.trim().equals(expectedData.trim()), actualData.trim().length(), null);
				GenericUtility.softAssertEquals(softAssert, guard, actualData.trim(),expectedData.trim(),"Side menu text differs | language="+langText);


				landingPage.clickingSettingEllipsesButton();

				landingPage.clickingLanguageDropdownButton();

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
	public void verifyingSideMenuCollapsedLangAsInSettingsLingua() throws InterruptedException, InvalidFormatException, IOException {


		YtLandingPage landingPage=new YtLandingPage();
		GenericUtility genericUtility=new GenericUtility();
		SoftAssert softAssert = new SoftAssert();




		List<String> languages=DataSheets.languages().names();




		int languageCount = genericUtility.resolveLanguageCount(languages.size());
		String testName = ThreadContext.get("logFileName");
		landingPage.openingLandingPage();





		genericUtility.getWindowHeightWidth();
		landingPage.clickingLeftEllipses();


		landingPage.clickingSettingEllipsesButton();


		landingPage.clickingLanguageDropdownButton();





		LoopGuard guard=new LoopGuard(softAssert);
		for(int i=0;i<languageCount && !guard.shouldStop();i++) {

			String langText=languages.get(i);
			try {
				System.out.println(langText+"    "+(i+1));


				landingPage.getLanguageElementByName(langText).click();

				landingPage.clickingSettingEllipsesButton();
				landingPage.recoverSettingsMenuIfClosed();
				genericUtility.isElementInViewport(landingPage.languageDropdownUnderSettings);
				ScreenshotUtilUpdated.capture(testName, langText);
				landingPage.clickingSettingEllipsesButton();

				List<WebElement> listOfSideMenu=landingPage.gettingSideMenuCollapsedList();

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

				String detectedLanguageAttribute=genericUtility.getLangAttribute();

				System.out.println("ApplicableLanguage  "+applicableLanguage+"     "+"Detected Language   "+detectedLanguage+"  "+"Expected Language  "+expectedLanguage);
				System.out.println("ApplicableLanguage  "+applicableLanguage+"     "+"Detected Language attribute   "+detectedLanguageAttribute+"  "+"Expected Language attribute "+expectedLanguageAttribute);



				genericUtility.assertLanguageDetectedAndAttributeMatch(softAssert, guard, langText, detectedLanguage, expectedLanguage, detectedLanguageAttribute, expectedLanguageAttribute, true, true);


				landingPage.clickingSettingEllipsesButton();

				landingPage.clickingLanguageDropdownButton();

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
	public void verifyingSettingOptionsLang() throws InterruptedException {



		YtLandingPage landingPage=new YtLandingPage();
		GenericUtility genericUtility=new GenericUtility();
		SoftAssert softAssert=new SoftAssert();



		List<String> languages=DataSheets.languages().names();


		String testName= ThreadContext.get("testName");


		int languageCount = genericUtility.resolveLanguageCount(languages.size());

		landingPage.openingLandingPage();
		landingPage.clickingSettingEllipsesButton();

		landingPage.clickingLanguageDropdownButton();






		LoopGuard guard=new LoopGuard(softAssert);
		for(int i=0;i<languageCount && !guard.shouldStop();i++) {

			String langText=languages.get(i);
			try {
				System.out.println(langText+"    "+(i+1));


				landingPage.getLanguageElementByName(langText).click();

				// landingPage.clickingSettingEllipsesButton();
				landingPage.openSettingsMenu();
				genericUtility.isElementInViewport(landingPage.languageDropdownUnderSettings);
				ScreenshotUtilUpdated.capture(testName, langText);
				// landingPage.clickingSettingEllipsesButton();
				//
				// landingPage.clickingSettingEllipsesButton();

				String languageFromSettings=landingPage.getsettingEllipsesOptionsListLandingPage();
				// landingPage.clickingSettingEllipsesButton();
				landingPage.closeSettingsMenu();


				String applicableLanguage=langText;
				String detectedLanguage=LinguaHelper.detectLanguage(genericUtility.repeatTextForDetection(languageFromSettings, 4));
				String expectedLanguage=Canary.canary(DataSheets.expectedLanguage().get(langText).get("Expected Language"), i, 1);


				String expectedLanguageAttribute=Canary.canary(DataSheets.expectedAttribute().get(Canary.canary(langText, i, 4)).get("Expected Attribute"), i, 2);
				String detectedLanguageAttribute=genericUtility.getLangAttribute();

				System.out.println("ApplicableLanguage  "+applicableLanguage+"     "+"Detected Language   "+detectedLanguage+"  "+"Expected Language  "+expectedLanguage);
				System.out.println("ApplicableLanguage  "+applicableLanguage+"     "+"Detected Language attribute   "+detectedLanguageAttribute+"  "+"Expected Language attribute "+expectedLanguageAttribute);

				genericUtility.assertLanguageDetectedAndAttributeMatch(softAssert, guard, langText, detectedLanguage, expectedLanguage, detectedLanguageAttribute, expectedLanguageAttribute, false, false);


				// landingPage.clickingSettingEllipsesButton();
				landingPage.openSettingsMenu();
				landingPage.clickingLanguageDropdownButton();


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




	@Test(groups = {"non-lingua", "settings-region"})
	public void verifyCountryCodeAsBasedOnRegion() throws InterruptedException {


		YtLandingPage landingPage=new YtLandingPage();
		SoftAssert softAssert=new SoftAssert();
		GenericUtility genericUtility = new GenericUtility();




		landingPage.openingLandingPage();

		landingPage.clickingSettingEllipsesButton();

		landingPage.clickingLocationDropdownUnderSettings();




		List<WebElement> locationList=landingPage.getLocationList();
		int locationListSize=locationList.size();
		int sheetCount=DataSheets.countries().size();
		if(sheetCount!=locationListSize) {
			log.warn("[{}] Country sheet has {} rows but the page lists {} locations", ThreadContext.get("testName"), sheetCount, locationListSize);
		}



		String testName= ThreadContext.get("testName");



		locationListSize = genericUtility.resolveCountryIterationCount(locationListSize);





		LoopGuard guard=new LoopGuard(softAssert);
		for(int locationIndex=0;locationIndex<locationListSize && !guard.shouldStop();locationIndex++) {
			String locationText = "location #"+locationIndex;
			try {
				locationList=landingPage.getLocationList();
				locationText = locationList.get(locationIndex).getText();


				System.out.println(locationText+"    "+locationIndex);





				locationList.get(locationIndex).click();

				landingPage.clickingSettingEllipsesButton();
				genericUtility.isElementInViewport(landingPage.locationDropdownUnderSettings);
				ScreenshotUtilUpdated.capture(testName, locationText);
				landingPage.clickingSettingEllipsesButton();



				String applicableLocation=locationText;
				String sheetCountryCode=DataSheets.countries().get(locationText).get("Country Code");
				String expectedCountryCode=Canary.canary(Canary.canary(sheetCountryCode, locationIndex, 0), locationIndex, 1);
				String detectedCountryCode=landingPage.getCountryCode();

				System.out.println("applicable location is ==  "+applicableLocation+" Expected location  =="+expectedCountryCode+"  detected country code"+detectedCountryCode);
				boolean codeMatches=java.util.Objects.equals(detectedCountryCode, expectedCountryCode);
				GenericUtility.logComparison("Country code", locationText, codeMatches, detectedCountryCode==null ? 0 : detectedCountryCode.length(), "code: '"+detectedCountryCode+"'");
				GenericUtility.softAssertEquals(softAssert, guard, detectedCountryCode, expectedCountryCode,"Country code differs | location="+locationText);
				if(sheetCountryCode.isEmpty()) {
					// only the United States has no code, an empty expected code on any other row would pass a failed read
					guard.count("no code expected");
					GenericUtility.softAssertEquals(softAssert, guard, locationText, "United States", "Expected country code is empty for \""+locationText+"\", only \"United States\" has none.");
				}

				landingPage.clickingSettingEllipsesButton();

				landingPage.clickingLocationDropdownUnderSettings();

				guard.passed(locationText);
			} catch (Exception e) {
				if (guard.retryAfterFailure(locationText, e, () -> reopenLocationDropdown(landingPage))) {
					locationIndex--; // run the same location again
				}
			}
		}
		guard.logSummary(locationListSize);

		softAssert.assertAll();
	}



	@Test(groups = {"non-lingua", "global-filter"})
	public void verifyingGlobalFilterLandingPage() throws InterruptedException {


		GenericUtility genericUtility=new GenericUtility();
		YtLandingPage landingPage=new YtLandingPage();
		SoftAssert softAssert =new SoftAssert();



		List<String> languages=DataSheets.languages().names();


		landingPage.openingLandingPage();
		landingPage.givingInputUnderSearchBar("youtube");
		genericUtility.clickEnter(landingPage.searchInputLandinfPage);

		int languageCount = genericUtility.resolveLanguageCount(languages.size());



		String testName= ThreadContext.get("testName");




		LoopGuard guard=new LoopGuard(softAssert);
		for(int i=0;i<languageCount && !guard.shouldStop();i++) {

			String langText=languages.get(i);
			try {
				landingPage.clickingSettingEllipsesButton();

				landingPage.clickingLanguageDropdownButton();


				System.out.println(langText+"    "+(i+1));


				genericUtility.smoothScrollToElement(landingPage.getLanguageElementByNameBy(langText));
				landingPage.getLanguageElementByName(langText).click();	

				landingPage.clickingSettingEllipsesButton();
				landingPage.recoverSettingsMenuIfClosed();
				genericUtility.isElementInViewport(landingPage.languageDropdownUnderSettings);
				ScreenshotUtilUpdated.capture(testName, langText);
				landingPage.clickingSettingEllipsesButton();



				landingPage.clickingGlobalFilterButton();
				genericUtility.isElementInViewport(landingPage.globalFilterData);
				ScreenshotUtilUpdated.capture(testName, langText);
				String globalSettingText=landingPage.getDataFromGlobalFilterPopup();
				landingPage.closeGlobalFilterPopup();


				String expectedData=Canary.canary(DataSheets.searchFilter().get(langText).get("Expected Data"), i, 0);
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
	public void verifyingGlobalFilterLandingPageLingua() throws InterruptedException {


		GenericUtility genericUtility=new GenericUtility();
		YtLandingPage landingPage=new YtLandingPage();
		SoftAssert softAssert =new SoftAssert();


		List<String> languages=DataSheets.languages().names();


		int languageCount = genericUtility.resolveLanguageCount(languages.size());


		String testName= ThreadContext.get("testName");



		landingPage.openingLandingPage();
		landingPage.givingInputUnderSearchBar("youtube");
		genericUtility.clickEnter(landingPage.searchInputLandinfPage);





		LoopGuard guard=new LoopGuard(softAssert);
		for(int i=0;i<languageCount && !guard.shouldStop();i++) {

			String langText=languages.get(i);
			try {
				landingPage.clickingSettingEllipsesButton();

				landingPage.clickingLanguageDropdownButton();


				System.out.println(langText+"    "+(i+1));


				landingPage.getLanguageElementByName(langText).click();	

				landingPage.clickingSettingEllipsesButton();
				landingPage.recoverSettingsMenuIfClosed();
				genericUtility.isElementInViewport(landingPage.languageDropdownUnderSettings);
				ScreenshotUtilUpdated.capture(testName, langText);
				landingPage.clickingSettingEllipsesButton();



				landingPage.clickingGlobalFilterButton();
				genericUtility.isElementInViewport(landingPage.globalFilterData);
				ScreenshotUtilUpdated.capture(testName, langText);
				String globalSettingText=landingPage.getDataFromGlobalFilterPopup();
				landingPage.closeGlobalFilterPopup();




				String applicableLanguage=langText;
				String detectedLanguage=LinguaHelper.detectLanguage(genericUtility.removeEnglishWordsForNonEnglish(globalSettingText, langText));
				String expectedLanguage=Canary.canary(DataSheets.expectedLanguage().get(langText).get("Expected Language"), i, 1);


				String expectedLanguageAttribute=Canary.canary(DataSheets.expectedAttribute().get(langText).get("Expected Attribute"), i, 2);
				String detectedLanguageAttribute=genericUtility.getLangAttribute();

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
		landingPage.openingLandingPage();
		landingPage.clickingSettingEllipsesButton();
		landingPage.clickingLanguageDropdownButton();
	}

	private void reopenCollapsedLanguageDropdown(YtLandingPage landingPage) throws InterruptedException {
		landingPage.openingLandingPage();
		landingPage.clickingLeftEllipses();
		landingPage.clickingSettingEllipsesButton();
		landingPage.clickingLanguageDropdownButton();
	}

	private void reopenLocationDropdown(YtLandingPage landingPage) throws InterruptedException {
		landingPage.openingLandingPage();
		landingPage.clickingSettingEllipsesButton();
		landingPage.clickingLocationDropdownUnderSettings();
	}

	private void reopenSearchResults(YtLandingPage landingPage, GenericUtility genericUtility) throws InterruptedException {
		landingPage.openingLandingPage();
		landingPage.givingInputUnderSearchBar("youtube");
		genericUtility.clickEnter(landingPage.searchInputLandinfPage);
	}

}
