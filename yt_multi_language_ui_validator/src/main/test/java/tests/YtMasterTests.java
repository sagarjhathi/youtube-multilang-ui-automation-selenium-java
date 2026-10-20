package main.test.java.tests;

import java.io.IOException;
import java.util.List;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import main.test.java.retry.*;
import main.java.yt_multi_lang_ui_validator.base.BaseTest;
import main.java.yt_multi_lang_ui_validator.fileReader.FileReader;
import main.java.yt_multi_lang_ui_validator.fileWriter.ExcelFileWriter;
import main.java.yt_multi_lang_ui_validator.lingua.LinguaHelper;
import main.java.yt_multi_lang_ui_validator.logger.LoggerUtility;
import main.java.yt_multi_lang_ui_validator.pages.YtLandingPage;
import main.java.yt_multi_lang_ui_validator.utilities.GenericUtility;
import main.java.yt_multi_lang_ui_validator.utilities.ScreenshotUtilUpdated;
import main.java.yt_multi_lang_ui_validator.utilities.WaitUtility;



public class YtMasterTests extends BaseTest{

	private static final  Logger log=LoggerUtility.getLogger(YtMasterTests.class);

	@Test(groups = {"non-lingua"})
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


		for(int languageIndex=1;languageIndex<LanguagesRowCount;languageIndex++) {

			String langText=verifyingSideMenuLanguageAsInSettingsDataReader.getCellValue(languageIndex, 0);
			System.out.println(langText+" lang text from the sheet   "+languageIndex);

			ScreenshotUtilUpdated.capture(testName, langText);


			landingPage.getLanguageElementByName(langText).click();	

			landingPage.clickingSettingEllipsesButton();
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


			String expectedData=genericUtility.stripKeywords(verifyingSideMenuLanguageAsInSettingsDataReader.getCellValue(languageIndex, 1), "YouTube Music", "YouTube TV");
			String actualData=genericUtility.stripKeywords(sideMenuItems.toString(), "YouTube Music", "YouTube TV");


			if(sideMenuItems.toString().trim().equals(expectedData.trim())) {
				System.out.println("Checking if both data are true here    ");
			}

			System.out.println("========="+expectedData+" =====  Expected data from the sheet");
			softAssert.assertEquals(actualData.trim(),expectedData.trim(),"Mismatch in the Actual and Expected data for language="+langText);


			landingPage.clickingSettingEllipsesButton();

			landingPage.clickingLanguageDropdownButton();


		}

		verifyingSideMenuLanguageAsInSettingsDataReader.closeWorkbook();


		softAssert.assertAll();
	}









	@Test(groups = {"lingua"})
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


		for(int languageIndex=1;languageIndex<LanguagesRowCount;languageIndex++) {

			String langText=applicableExpectedLangReader.getCellValue(languageIndex, 0);
			System.out.println(langText+" lang text from the sheet   "+languageIndex);






			landingPage.getLanguageElementByName(langText).click();	

			landingPage.clickingSettingEllipsesButton();
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
			String detectedLanguageAttribute=genericUtility.getLangAttribute();

			System.out.println("ApplicableLanguage  "+applicableLanguage+"     "+"Detected Language   "+detectedLanguage+"  "+"Expected Language  "+expectedLanguage);
			System.out.println("ApplicableLanguage  "+applicableLanguage+"     "+"Detected Language attribute   "+detectedLanguageAttribute+"  "+"Expected Language attribute "+expectedLanguageAttribute);

			log.info("[{}] ApplicableLanguage==  "+applicableLanguage+"     "+"Detected Language ==  "+detectedLanguage+"  "+"Expected Language == "+expectedLanguage, ThreadContext.get("testName"));

			genericUtility.assertLanguageDetectedOrAttributeMatches(softAssert, langText, detectedLanguage, expectedLanguage, detectedLanguageAttribute, expectedLanguageAttribute);


			landingPage.clickingSettingEllipsesButton();

			landingPage.clickingLanguageDropdownButton();


		}
		applicableExpectedLangReader.closeWorkbook();
		applicableExpectedAttributeReader.closeWorkbook();

		softAssert.assertAll();
	}





	@Test(groups = {"non-lingua"})
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


		for(int languageIndex=1;languageIndex<LanguagesRowCount;languageIndex++) {

			String langText=verifyingSideMenuCollapsedLangAsInSettingsDataReader.getCellValue(languageIndex, 0);
			System.out.println(langText+"    "+languageIndex);


			landingPage.getLanguageElementByName(langText).click();
			waitUtility.waitForElementReady(landingPage.sideMenuCollapsedList);
			landingPage.clickingSettingEllipsesButton();
			genericUtility.isElementInViewport(landingPage.languageDropdownUnderSettings);
			ScreenshotUtilUpdated.capture(testName, langText);
			landingPage.clickingSettingEllipsesButton();

			genericUtility.getLangAttribute();

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


			softAssert.assertEquals(actualData.trim(),expectedData.trim(),"Mismatch in the Actual and Expected data for language="+langText);


			landingPage.clickingSettingEllipsesButton();

			landingPage.clickingLanguageDropdownButton();

		}

		verifyingSideMenuCollapsedLangAsInSettingsDataReader.closeWorkbook();
		applicableExpectedReader.closeWorkbook();


		softAssert.assertAll();	
	}









	@Test(groups = {"lingua"})
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





		List<Integer> sizes=genericUtility.getWindowHeightWidth();
		landingPage.clickingLeftEllipses();


		landingPage.clickingSettingEllipsesButton();


		landingPage.clickingLanguageDropdownButton();




		LanguagesRowCount = genericUtility.resolveLanguageIterationCount(LanguagesRowCount);

		for(int languageIndex=1;languageIndex<LanguagesRowCount;languageIndex++) {

			String langText=applicableExpectedReader.getCellValue(languageIndex, 0);
			System.out.println(langText+"    "+languageIndex);


			landingPage.getLanguageElementByName(langText).click();

			landingPage.clickingSettingEllipsesButton();
			genericUtility.isElementInViewport(landingPage.languageDropdownUnderSettings);
			ScreenshotUtilUpdated.capture(testName, langText);
			landingPage.clickingSettingEllipsesButton();

			genericUtility.getLangAttribute();

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

			String detectedLanguageAttribute=genericUtility.getLangAttribute();

			System.out.println("ApplicableLanguage  "+applicableLanguage+"     "+"Detected Language   "+detectedLanguage+"  "+"Expected Language  "+expectedLanguage);
			System.out.println("ApplicableLanguage  "+applicableLanguage+"     "+"Detected Language attribute   "+detectedLanguageAttribute+"  "+"Expected Language attribute "+expectedLanguageAttribute);



			genericUtility.assertLanguageDetectedOrAttributeMatches(softAssert, langText, detectedLanguage, expectedLanguage, detectedLanguageAttribute, expectedLanguageAttribute);


			landingPage.clickingSettingEllipsesButton();

			landingPage.clickingLanguageDropdownButton();

		}
		applicableExpectedReader.closeWorkbook();
		applicableExpectedAttributeReader.closeWorkbook();



		softAssert.assertAll();	
	}


	@Test(groups = {"non-lingua"})
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

		for(int languageIndex=1;languageIndex<LanguagesRowCount;languageIndex++) {

			String langText=applicableExpectedLangReader.getCellValue(languageIndex, 0);
			System.out.println(langText+"    "+languageIndex);


			landingPage.getLanguageElementByName(langText).click();

			landingPage.clickingSettingEllipsesButton();
			genericUtility.isElementInViewport(landingPage.languageDropdownUnderSettings);
			ScreenshotUtilUpdated.capture(testName, langText);
			landingPage.clickingSettingEllipsesButton();



			landingPage.clickingSettingEllipsesButton();

			String languageFromSettings=landingPage.getsettingEllipsesOptionsListLandingPage();
			landingPage.clickingSettingEllipsesButton();


			String applicableLanguage=applicableExpectedLangReader.getCellValue(languageIndex, 0);
			String detectedLanguage=LinguaHelper.detectLanguage(languageFromSettings);
			String expectedLanguage=applicableExpectedLangReader.getCellValue(languageIndex, 1);


			String expectedLanguageAttribute=applicableExpectedAttributeReader.getCellValue(languageIndex, 1);
			String detectedLanguageAttribute=genericUtility.getLangAttribute();

			System.out.println("ApplicableLanguage  "+applicableLanguage+"     "+"Detected Language   "+detectedLanguage+"  "+"Expected Language  "+expectedLanguage);
			System.out.println("ApplicableLanguage  "+applicableLanguage+"     "+"Detected Language attribute   "+detectedLanguageAttribute+"  "+"Expected Language attribute "+expectedLanguageAttribute);

			if(genericUtility.isFlakyDetectionLanguage(langText)) {
				log.info("[{}] Skipping language detection check for flaky language={}", ThreadContext.get("testName"), langText);
			} else {
				softAssert.assertEquals(detectedLanguage, expectedLanguage, "Language detection mismatch for language="+langText);
			}
			softAssert.assertEquals(detectedLanguageAttribute, expectedLanguageAttribute, "Language attributr mismatch for language="+langText);


			landingPage.clickingSettingEllipsesButton();

			landingPage.clickingLanguageDropdownButton();


		}

		applicableExpectedLangReader.closeWorkbook();
		applicableExpectedAttributeReader.closeWorkbook();

		softAssert.assertAll();	
	}




	@Test(groups = {"non-lingua"})
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



		String testName= ThreadContext.get("testName");



		locationListSize = genericUtility.resolveCountryIterationCount(locationListSize);





		for(int locationIndex=0;locationIndex<locationListSize;locationIndex++) {
			locationList=landingPage.getLocationList();
			String locationText = locationList.get(locationIndex).getText();


			System.out.println(locationText+"    "+locationIndex);

			String locationTextFromSheet=reader.getCellValue(locationIndex+1, 0);
			System.out.println(locationTextFromSheet+"  location text from sheet  "+locationIndex);




			locationList.get(locationIndex).click();

			landingPage.clickingSettingEllipsesButton();
			genericUtility.isElementInViewport(landingPage.locationDropdownUnderSettings);
			ScreenshotUtilUpdated.capture(testName, locationText);
			landingPage.clickingSettingEllipsesButton();



			String applicableLocation=locationText;
			String expectedCountryCode=reader.getCellValue(locationIndex+1, 1);
			String detectedCountryCode=landingPage.getCountryCode();

			System.out.println("applicable location is ==  "+applicableLocation+" Expected location  =="+expectedCountryCode+"  detected country code"+detectedCountryCode);
			softAssert.assertEquals(detectedCountryCode, expectedCountryCode,"Country code mismacth for location="+locationText);

			landingPage.clickingSettingEllipsesButton();

			landingPage.clickingLocationDropdownUnderSettings();



		}
		reader.closeWorkbook();
		softAssert.assertAll();
	}



	@Test(groups = {"non-lingua"})
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

		for(int languageIndex=1;languageIndex<LanguagesRowCount;languageIndex++) {

			landingPage.clickingSettingEllipsesButton();

			landingPage.clickingLanguageDropdownButton();


			String langText=verifyingGlobalFilterLandingPageDataReader.getCellValue(languageIndex, 0);
			System.out.println(langText+"    "+languageIndex);


			genericUtility.smoothScrollToElement(landingPage.getLanguageElementByNameBy(langText));
			landingPage.getLanguageElementByName(langText).click();	

			landingPage.clickingSettingEllipsesButton();
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


			softAssert.assertEquals(genericUtility.normalizeText(actualValue), genericUtility.normalizeText(expectedData), "Mismatch in the Expected and Actual Data for language"+langText);



		}

		verifyingGlobalFilterLandingPageDataReader.closeWorkbook();

		softAssert.assertAll();	



	}






	@Test(groups = {"lingua"})
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


		for(int languageIndex=1;languageIndex<LanguagesRowCount;languageIndex++) {

			landingPage.clickingSettingEllipsesButton();

			landingPage.clickingLanguageDropdownButton();


			String langText=applicableExpectedLangReader.getCellValue(languageIndex, 0);
			System.out.println(langText+"    "+languageIndex);


			landingPage.getLanguageElementByName(langText).click();	

			landingPage.clickingSettingEllipsesButton();
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
			String detectedLanguageAttribute=genericUtility.getLangAttribute();

			System.out.println("ApplicableLanguage  "+applicableLanguage+"     "+"Detected Language   "+detectedLanguage+"  "+"Expected Language  "+expectedLanguage);
			System.out.println("ApplicableLanguage  "+applicableLanguage+"     "+"Detected Language attribute   "+detectedLanguageAttribute+"  "+"Expected Language attribute "+expectedLanguageAttribute);

			genericUtility.assertLanguageDetectedOrAttributeMatches(softAssert, langText, detectedLanguage, expectedLanguage, detectedLanguageAttribute, expectedLanguageAttribute);



		}

		applicableExpectedLangReader.closeWorkbook();
		applicableExpectedAttributeReader.closeWorkbook();


		softAssert.assertAll();	



	}






}
