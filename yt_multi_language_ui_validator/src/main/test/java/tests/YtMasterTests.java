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
import main.java.yt_multi_lang_ui_validator.fileReader.FileReader;
import main.java.yt_multi_lang_ui_validator.lingua.LinguaHelper;
import main.java.yt_multi_lang_ui_validator.logger.LoggerUtility;
import main.java.yt_multi_lang_ui_validator.pages.YtLandingPage;
import main.java.yt_multi_lang_ui_validator.utilities.GenericUtility;
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


		FileReader verifyingSideMenuLanguageAsInSettingsDataReader=new FileReader();
		verifyingSideMenuLanguageAsInSettingsDataReader.loadWorkbook("data/verifyingSideMenuLanguageAsInSettings.xlsx");
		verifyingSideMenuLanguageAsInSettingsDataReader.loadSheet("verifyingSideMenuLanguageAsInSe");


		landingPage.openingLandingPage();


		landingPage.clickingSettingEllipsesButton();

		landingPage.clickingLanguageDropdownButton();

		String testName = ThreadContext.get("logFileName");
		int LanguagesRowCount= verifyingSideMenuLanguageAsInSettingsDataReader.getRowCount();


		LanguagesRowCount = genericUtility.resolveLanguageIterationCount(LanguagesRowCount);


		List<String[]> hypeLiveWordRows=GenericUtility.loadHypeLiveWordRows();

		LoopGuard guard=new LoopGuard(softAssert);
		for(int languageIndex=1;languageIndex<LanguagesRowCount && !guard.shouldStop();languageIndex++) {

			String langText=verifyingSideMenuLanguageAsInSettingsDataReader.getCellValue(languageIndex, 0);
			try {
				System.out.println(langText+" lang text from the sheet   "+languageIndex);

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
				// YouTube fills one slot with Hype, Live or nothing: the expected text is aligned to it only for the known words (see docs)
				GenericUtility.HypeLiveAlignment alignment=GenericUtility.alignExpectedHypeLive(hypeLiveWordRows, langText, String.join(" ", sideMenuTexts), verifyingSideMenuLanguageAsInSettingsDataReader.getCellValue(languageIndex, 1));
				String expectedData=alignment.expected();
				String actualData=GenericUtility.comparableSideMenuText(String.join(" ", sideMenuTexts));


				System.out.println("========="+expectedData+" =====  Expected data from the sheet");
				GenericUtility.logComparison("Side menu", langText, actualData.trim().equals(expectedData.trim()), actualData.trim().length(), "slot: "+alignment.detail());
				guard.count("slot "+alignment.kind());
				GenericUtility.softAssertEquals(softAssert, guard, actualData.trim(),expectedData.trim(),"Side menu text differs | language="+langText);


				landingPage.clickingSettingEllipsesButton();

				landingPage.clickingLanguageDropdownButton();


				guard.passed(langText);
			} catch (Exception e) {
				if (guard.retryAfterFailure(langText, e, () -> reopenLanguageDropdown(landingPage))) {
					languageIndex--; // run the same language again
				}
			}
		}

		verifyingSideMenuLanguageAsInSettingsDataReader.closeWorkbook();


		guard.logSummary(LanguagesRowCount-1);

		softAssert.assertAll();
	}









	@Test(groups = {"lingua", "side-menu-expanded"})
	public void verifyingSideMenuLanguageAsInSettingsLingua() throws InterruptedException {



		YtLandingPage landingPage=new YtLandingPage();
		GenericUtility genericUtility=new GenericUtility();
		SoftAssert softAssert = new SoftAssert();


		FileReader applicableExpectedLangReader=new FileReader();
		applicableExpectedLangReader.loadWorkbook("data/ApplicableLanguageExpectedLanguage.xlsx");
		applicableExpectedLangReader.loadSheet("AppVExpectLanguages");

		FileReader applicableExpectedAttributeReader=new FileReader();
		applicableExpectedAttributeReader.loadWorkbook("data/ApplicableLanguageExpectedAttibute.xlsx");
		applicableExpectedAttributeReader.loadSheet("Attribute");




		landingPage.openingLandingPage();




		landingPage.clickingSettingEllipsesButton();

		landingPage.clickingLanguageDropdownButton();

		String testName = ThreadContext.get("logFileName");
		int LanguagesRowCount= applicableExpectedLangReader.getRowCount();


		LanguagesRowCount = genericUtility.resolveLanguageIterationCount(LanguagesRowCount);


		LoopGuard guard=new LoopGuard(softAssert);
		for(int languageIndex=1;languageIndex<LanguagesRowCount && !guard.shouldStop();languageIndex++) {

			String langText=applicableExpectedLangReader.getCellValue(languageIndex, 0);
			try {
				System.out.println(langText+" lang text from the sheet   "+languageIndex);






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




				String applicableLanguage=applicableExpectedLangReader.getCellValue(languageIndex, 0);
				String detectedLanguage=LinguaHelper.detectLanguage(genericUtility.removeEnglishWordsForNonEnglish(sideMenuItems.toString(), langText));
				String expectedLanguage=applicableExpectedLangReader.getCellValue(languageIndex, 1);


				String expectedLanguageAttribute=applicableExpectedAttributeReader.getCellValue(languageIndex, 1);
				GenericUtility.softAssertEquals(softAssert, guard, applicableExpectedAttributeReader.getCellValue(languageIndex, 0), langText, "Sheet rows out of sync | attribute sheet row "+languageIndex+" is not "+langText);
				String detectedLanguageAttribute=genericUtility.getLangAttribute();

				System.out.println("ApplicableLanguage  "+applicableLanguage+"     "+"Detected Language   "+detectedLanguage+"  "+"Expected Language  "+expectedLanguage);
				System.out.println("ApplicableLanguage  "+applicableLanguage+"     "+"Detected Language attribute   "+detectedLanguageAttribute+"  "+"Expected Language attribute "+expectedLanguageAttribute);

				log.info("[{}] ApplicableLanguage==  "+applicableLanguage+"     "+"Detected Language ==  "+detectedLanguage+"  "+"Expected Language == "+expectedLanguage, ThreadContext.get("testName"));

				genericUtility.assertLanguageDetectedAndAttributeMatch(softAssert, guard, langText, detectedLanguage, expectedLanguage, detectedLanguageAttribute, expectedLanguageAttribute);


				landingPage.clickingSettingEllipsesButton();

				landingPage.clickingLanguageDropdownButton();


				guard.passed(langText);
			} catch (Exception e) {
				if (guard.retryAfterFailure(langText, e, () -> reopenLanguageDropdown(landingPage))) {
					languageIndex--; // run the same language again
				}
			}
		}
		applicableExpectedLangReader.closeWorkbook();
		applicableExpectedAttributeReader.closeWorkbook();

		guard.logSummary(LanguagesRowCount-1);

		softAssert.assertAll();
	}





	@Test(groups = {"non-lingua", "side-menu-collapsed"})
	public void verifyingSideMenuCollapsedLangAsInSettings() throws InterruptedException, InvalidFormatException, IOException {


		YtLandingPage landingPage=new YtLandingPage();
		GenericUtility genericUtility=new GenericUtility();
		WaitUtility waitUtility=new WaitUtility();
		SoftAssert softAssert = new SoftAssert();



		FileReader verifyingSideMenuCollapsedLangAsInSettingsDataReader=new FileReader();
		verifyingSideMenuCollapsedLangAsInSettingsDataReader.loadWorkbook("data/verifyingSideMenuCollapsedLangAsInSettings.xlsx");
		verifyingSideMenuCollapsedLangAsInSettingsDataReader.loadSheet("verifyingSideMenuCollapsedLangA");


		FileReader applicableExpectedReader=new FileReader();
		applicableExpectedReader.loadWorkbook("data/ApplicableLanguageExpectedLanguage.xlsx");
		applicableExpectedReader.loadSheet("AppVExpectLanguages");


		int LanguagesRowCount= verifyingSideMenuCollapsedLangAsInSettingsDataReader.getRowCount();
		String testName = ThreadContext.get("logFileName");

		landingPage.openingLandingPage();
		landingPage.clickingLeftEllipses();



		landingPage.clickingSettingEllipsesButton();

		landingPage.clickingLanguageDropdownButton();



		LanguagesRowCount = genericUtility.resolveLanguageIterationCount(LanguagesRowCount);


		LoopGuard guard=new LoopGuard(softAssert);
		for(int languageIndex=1;languageIndex<LanguagesRowCount && !guard.shouldStop();languageIndex++) {

			String langText=verifyingSideMenuCollapsedLangAsInSettingsDataReader.getCellValue(languageIndex, 0);
			try {
				System.out.println(langText+"    "+languageIndex);


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

				String expectedData =	verifyingSideMenuCollapsedLangAsInSettingsDataReader.getCellValue(languageIndex, 1).trim();
				String actualData=sideMenuItems.toString();


				GenericUtility.logComparison("Collapsed side menu", langText, actualData.trim().equals(expectedData.trim()), actualData.trim().length(), null);
				GenericUtility.softAssertEquals(softAssert, guard, actualData.trim(),expectedData.trim(),"Side menu text differs | language="+langText);


				landingPage.clickingSettingEllipsesButton();

				landingPage.clickingLanguageDropdownButton();

				guard.passed(langText);
			} catch (Exception e) {
				if (guard.retryAfterFailure(langText, e, () -> reopenCollapsedLanguageDropdown(landingPage))) {
					languageIndex--; // run the same language again
				}
			}
		}

		verifyingSideMenuCollapsedLangAsInSettingsDataReader.closeWorkbook();
		applicableExpectedReader.closeWorkbook();


		guard.logSummary(LanguagesRowCount-1);

		softAssert.assertAll();	
	}









	@Test(groups = {"lingua", "side-menu-collapsed"})
	public void verifyingSideMenuCollapsedLangAsInSettingsLingua() throws InterruptedException, InvalidFormatException, IOException {


		YtLandingPage landingPage=new YtLandingPage();
		GenericUtility genericUtility=new GenericUtility();
		SoftAssert softAssert = new SoftAssert();




		FileReader applicableExpectedReader=new FileReader();
		applicableExpectedReader.loadWorkbook("data/ApplicableLanguageExpectedLanguage.xlsx");
		applicableExpectedReader.loadSheet("AppVExpectLanguages");



		FileReader applicableExpectedAttributeReader=new FileReader();
		applicableExpectedAttributeReader.loadWorkbook("data/ApplicableLanguageExpectedAttibute.xlsx");
		applicableExpectedAttributeReader.loadSheet("Attribute");

		int LanguagesRowCount= applicableExpectedReader.getRowCount();
		String testName = ThreadContext.get("logFileName");
		landingPage.openingLandingPage();





		genericUtility.getWindowHeightWidth();
		landingPage.clickingLeftEllipses();


		landingPage.clickingSettingEllipsesButton();


		landingPage.clickingLanguageDropdownButton();




		LanguagesRowCount = genericUtility.resolveLanguageIterationCount(LanguagesRowCount);

		LoopGuard guard=new LoopGuard(softAssert);
		for(int languageIndex=1;languageIndex<LanguagesRowCount && !guard.shouldStop();languageIndex++) {

			String langText=applicableExpectedReader.getCellValue(languageIndex, 0);
			try {
				System.out.println(langText+"    "+languageIndex);


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



				String applicableLanguage=applicableExpectedReader.getCellValue(languageIndex, 0);
				String textForLingua=genericUtility.removeEnglishWordsForNonEnglish(sideMenuItems.toString(), langText);
				String detectedLanguage=LinguaHelper.detectLanguage(textForLingua);
				log.info("text sent to lingua is "+"      "+textForLingua);
				String expectedLanguage=applicableExpectedReader.getCellValue(languageIndex, 1);


				String expectedLanguageAttribute=applicableExpectedAttributeReader.getCellValue(languageIndex, 1);
				GenericUtility.softAssertEquals(softAssert, guard, applicableExpectedAttributeReader.getCellValue(languageIndex, 0), langText, "Sheet rows out of sync | attribute sheet row "+languageIndex+" is not "+langText);

				String detectedLanguageAttribute=genericUtility.getLangAttribute();

				System.out.println("ApplicableLanguage  "+applicableLanguage+"     "+"Detected Language   "+detectedLanguage+"  "+"Expected Language  "+expectedLanguage);
				System.out.println("ApplicableLanguage  "+applicableLanguage+"     "+"Detected Language attribute   "+detectedLanguageAttribute+"  "+"Expected Language attribute "+expectedLanguageAttribute);



				genericUtility.assertLanguageDetectedAndAttributeMatch(softAssert, guard, langText, detectedLanguage, expectedLanguage, detectedLanguageAttribute, expectedLanguageAttribute);


				landingPage.clickingSettingEllipsesButton();

				landingPage.clickingLanguageDropdownButton();

				guard.passed(langText);
			} catch (Exception e) {
				if (guard.retryAfterFailure(langText, e, () -> reopenCollapsedLanguageDropdown(landingPage))) {
					languageIndex--; // run the same language again
				}
			}
		}
		applicableExpectedReader.closeWorkbook();
		applicableExpectedAttributeReader.closeWorkbook();



		guard.logSummary(LanguagesRowCount-1);

		softAssert.assertAll();	
	}


	@Test(groups = {"non-lingua", "settings-region"})
	public void verifyingSettingOptionsLang() throws InterruptedException {



		YtLandingPage landingPage=new YtLandingPage();
		GenericUtility genericUtility=new GenericUtility();
		SoftAssert softAssert=new SoftAssert();



		FileReader applicableExpectedLangReader=new FileReader();
		applicableExpectedLangReader.loadWorkbook("data/ApplicableLanguageExpectedLanguage.xlsx");
		applicableExpectedLangReader.loadSheet("AppVExpectLanguages");

		FileReader applicableExpectedAttributeReader=new FileReader();
		applicableExpectedAttributeReader.loadWorkbook("data/ApplicableLanguageExpectedAttibute.xlsx");
		applicableExpectedAttributeReader.loadSheet("Attribute");

		String testName= ThreadContext.get("testName");


		int LanguagesRowCount= applicableExpectedLangReader.getRowCount();

		landingPage.openingLandingPage();
		landingPage.clickingSettingEllipsesButton();

		landingPage.clickingLanguageDropdownButton();





		LanguagesRowCount = genericUtility.resolveLanguageIterationCount(LanguagesRowCount);

		LoopGuard guard=new LoopGuard(softAssert);
		for(int languageIndex=1;languageIndex<LanguagesRowCount && !guard.shouldStop();languageIndex++) {

			String langText=applicableExpectedLangReader.getCellValue(languageIndex, 0);
			try {
				System.out.println(langText+"    "+languageIndex);


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


				String applicableLanguage=applicableExpectedLangReader.getCellValue(languageIndex, 0);
				String detectedLanguage=LinguaHelper.detectLanguage(genericUtility.repeatTextForDetection(languageFromSettings, 4));
				String expectedLanguage=applicableExpectedLangReader.getCellValue(languageIndex, 1);


				String expectedLanguageAttribute=applicableExpectedAttributeReader.getCellValue(languageIndex, 1);
				GenericUtility.softAssertEquals(softAssert, guard, applicableExpectedAttributeReader.getCellValue(languageIndex, 0), langText, "Sheet rows out of sync | attribute sheet row "+languageIndex+" is not "+langText);
				String detectedLanguageAttribute=genericUtility.getLangAttribute();

				System.out.println("ApplicableLanguage  "+applicableLanguage+"     "+"Detected Language   "+detectedLanguage+"  "+"Expected Language  "+expectedLanguage);
				System.out.println("ApplicableLanguage  "+applicableLanguage+"     "+"Detected Language attribute   "+detectedLanguageAttribute+"  "+"Expected Language attribute "+expectedLanguageAttribute);

				genericUtility.assertLanguageDetectedAndAttributeMatch(softAssert, guard, langText, detectedLanguage, expectedLanguage, detectedLanguageAttribute, expectedLanguageAttribute);


				// landingPage.clickingSettingEllipsesButton();
				landingPage.openSettingsMenu();
				landingPage.clickingLanguageDropdownButton();


				guard.passed(langText);
			} catch (Exception e) {
				if (guard.retryAfterFailure(langText, e, () -> reopenLanguageDropdown(landingPage))) {
					languageIndex--; // run the same language again
				}
			}
		}

		applicableExpectedLangReader.closeWorkbook();
		applicableExpectedAttributeReader.closeWorkbook();

		guard.logSummary(LanguagesRowCount-1);

		softAssert.assertAll();	
	}




	@Test(groups = {"non-lingua", "settings-region"})
	public void verifyCountryCodeAsBasedOnRegion() throws InterruptedException {


		FileReader reader=new FileReader();
		YtLandingPage landingPage=new YtLandingPage();
		SoftAssert softAssert=new SoftAssert();
		GenericUtility genericUtility = new GenericUtility();

		reader.loadWorkbook("data/Country_Name_Code.xlsx");
		reader.loadSheet("CountryNameCode");



		landingPage.openingLandingPage();

		landingPage.clickingSettingEllipsesButton();

		landingPage.clickingLocationDropdownUnderSettings();




		List<WebElement> locationList=landingPage.getLocationList();
		int locationListSize=locationList.size();
		if(reader.getRowCount()-1!=locationListSize) {
			log.warn("[{}] Country sheet has {} rows but the page lists {} locations", ThreadContext.get("testName"), reader.getRowCount()-1, locationListSize);
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

				String locationTextFromSheet=reader.getCellValue(locationIndex+1, 0);
				System.out.println(locationTextFromSheet+"  location text from sheet  "+locationIndex);
				GenericUtility.softAssertEquals(softAssert, guard, locationTextFromSheet, locationText, "Sheet rows out of sync | country sheet row "+(locationIndex+1)+" is not "+locationText);




				locationList.get(locationIndex).click();

				landingPage.clickingSettingEllipsesButton();
				genericUtility.isElementInViewport(landingPage.locationDropdownUnderSettings);
				ScreenshotUtilUpdated.capture(testName, locationText);
				landingPage.clickingSettingEllipsesButton();



				String applicableLocation=locationText;
				String expectedCountryCode=reader.getCellValue(locationIndex+1, 1);
				String detectedCountryCode=landingPage.getCountryCode();

				System.out.println("applicable location is ==  "+applicableLocation+" Expected location  =="+expectedCountryCode+"  detected country code"+detectedCountryCode);
				boolean codeMatches=java.util.Objects.equals(detectedCountryCode, expectedCountryCode);
				GenericUtility.logComparison("Country code", locationText, codeMatches, detectedCountryCode==null ? 0 : detectedCountryCode.length(), "code: '"+detectedCountryCode+"'");
				GenericUtility.softAssertEquals(softAssert, guard, detectedCountryCode, expectedCountryCode,"Country code differs | location="+locationText);
				if(expectedCountryCode.isEmpty()) {
					// only the United States has no code, an empty expected code on any other row would pass a failed read
					guard.count("no code expected");
					GenericUtility.softAssertEquals(softAssert, guard, locationTextFromSheet, "United States", "Expected country code is empty for \""+locationText+"\", only \"United States\" has none.");
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
		reader.closeWorkbook();
		guard.logSummary(locationListSize);

		softAssert.assertAll();
	}



	@Test(groups = {"non-lingua", "global-filter"})
	public void verifyingGlobalFilterLandingPage() throws InterruptedException {


		GenericUtility genericUtility=new GenericUtility();
		YtLandingPage landingPage=new YtLandingPage();
		SoftAssert softAssert =new SoftAssert();



		FileReader verifyingGlobalFilterLandingPageDataReader=new FileReader();
		verifyingGlobalFilterLandingPageDataReader.loadWorkbook("data/verifyingGlobalFilterLandingPage.xlsx");
		verifyingGlobalFilterLandingPageDataReader.loadSheet("verifyingGlobalFilterLandingPag");


		landingPage.openingLandingPage();
		landingPage.givingInputUnderSearchBar("youtube");
		genericUtility.clickEnter(landingPage.searchInputLandinfPage);

		int LanguagesRowCount= verifyingGlobalFilterLandingPageDataReader.getRowCount();



		String testName= ThreadContext.get("testName");



		LanguagesRowCount = genericUtility.resolveLanguageIterationCount(LanguagesRowCount);

		LoopGuard guard=new LoopGuard(softAssert);
		for(int languageIndex=1;languageIndex<LanguagesRowCount && !guard.shouldStop();languageIndex++) {

			String langText=verifyingGlobalFilterLandingPageDataReader.getCellValue(languageIndex, 0);
			try {
				landingPage.clickingSettingEllipsesButton();

				landingPage.clickingLanguageDropdownButton();


				System.out.println(langText+"    "+languageIndex);


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


				String expectedData=verifyingGlobalFilterLandingPageDataReader.getCellValue(languageIndex, 1).trim();
				String actualValue=globalSettingText;


				String actualNormalized=GenericUtility.normalizeText(actualValue);
				String expectedNormalized=GenericUtility.normalizeText(expectedData);
				GenericUtility.logComparison("Search filter", langText, actualNormalized.equals(expectedNormalized), actualNormalized.length(), null);
				GenericUtility.softAssertEquals(softAssert, guard, actualNormalized, expectedNormalized, "Global filter text differs | language="+langText);

				guard.passed(langText);
			} catch (Exception e) {
				if (guard.retryAfterFailure(langText, e, () -> reopenSearchResults(landingPage, genericUtility))) {
					languageIndex--; // run the same language again
				}
			}
		}

		verifyingGlobalFilterLandingPageDataReader.closeWorkbook();

		guard.logSummary(LanguagesRowCount-1);

		softAssert.assertAll();	



	}






	@Test(groups = {"lingua", "global-filter"})
	public void verifyingGlobalFilterLandingPageLingua() throws InterruptedException {


		GenericUtility genericUtility=new GenericUtility();
		YtLandingPage landingPage=new YtLandingPage();
		SoftAssert softAssert =new SoftAssert();


		FileReader applicableExpectedLangReader=new FileReader();
		applicableExpectedLangReader.loadWorkbook("data/ApplicableLanguageExpectedLanguage.xlsx");
		applicableExpectedLangReader.loadSheet("AppVExpectLanguages");

		FileReader applicableExpectedAttributeReader=new FileReader();
		applicableExpectedAttributeReader.loadWorkbook("data/ApplicableLanguageExpectedAttibute.xlsx");
		applicableExpectedAttributeReader.loadSheet("Attribute");

		int LanguagesRowCount= applicableExpectedLangReader.getRowCount();


		String testName= ThreadContext.get("testName");



		landingPage.openingLandingPage();
		landingPage.givingInputUnderSearchBar("youtube");
		genericUtility.clickEnter(landingPage.searchInputLandinfPage);



		LanguagesRowCount = genericUtility.resolveLanguageIterationCount(LanguagesRowCount);


		LoopGuard guard=new LoopGuard(softAssert);
		for(int languageIndex=1;languageIndex<LanguagesRowCount && !guard.shouldStop();languageIndex++) {

			String langText=applicableExpectedLangReader.getCellValue(languageIndex, 0);
			try {
				landingPage.clickingSettingEllipsesButton();

				landingPage.clickingLanguageDropdownButton();


				System.out.println(langText+"    "+languageIndex);


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




				String applicableLanguage=applicableExpectedLangReader.getCellValue(languageIndex, 0);
				String detectedLanguage=LinguaHelper.detectLanguage(genericUtility.removeEnglishWordsForNonEnglish(globalSettingText, langText));
				String expectedLanguage=applicableExpectedLangReader.getCellValue(languageIndex, 1);


				String expectedLanguageAttribute=applicableExpectedAttributeReader.getCellValue(languageIndex, 1);
				GenericUtility.softAssertEquals(softAssert, guard, applicableExpectedAttributeReader.getCellValue(languageIndex, 0), langText, "Sheet rows out of sync | attribute sheet row "+languageIndex+" is not "+langText);
				String detectedLanguageAttribute=genericUtility.getLangAttribute();

				System.out.println("ApplicableLanguage  "+applicableLanguage+"     "+"Detected Language   "+detectedLanguage+"  "+"Expected Language  "+expectedLanguage);
				System.out.println("ApplicableLanguage  "+applicableLanguage+"     "+"Detected Language attribute   "+detectedLanguageAttribute+"  "+"Expected Language attribute "+expectedLanguageAttribute);

				genericUtility.assertLanguageDetectedAndAttributeMatch(softAssert, guard, langText, detectedLanguage, expectedLanguage, detectedLanguageAttribute, expectedLanguageAttribute);

				guard.passed(langText);
			} catch (Exception e) {
				if (guard.retryAfterFailure(langText, e, () -> reopenSearchResults(landingPage, genericUtility))) {
					languageIndex--; // run the same language again
				}
			}
		}

		applicableExpectedLangReader.closeWorkbook();
		applicableExpectedAttributeReader.closeWorkbook();


		guard.logSummary(LanguagesRowCount-1);

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
