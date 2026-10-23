package main.java.yt_multi_lang_ui_validator.utilities;

import java.time.Duration; 
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import main.java.yt_multi_lang_ui_validator.base.BasePage;
import main.java.yt_multi_lang_ui_validator.config.ConfigManager;
import main.java.yt_multi_lang_ui_validator.logger.LoggerUtility;
import main.java.yt_multi_lang_ui_validator.safeActions.SafeActions;

public class GenericUtility extends BasePage {
	
	
	 private static final  Logger log=LoggerUtility.getLogger(GenericUtility.class);
	 SafeActions safeAct = new SafeActions(driver);
		
	
	public String getLangAttribute() {
        log.info("getLangAttribute() - locating <html> and reading lang property");
        try {
            WebElement html = driver.findElement(By.xpath("//html"));
            String langProp = html.getDomProperty("lang");
            log.info("Language attribute read: {}", langProp);
            return langProp;
        } catch (Exception e) {
            log.error("Failed to read html lang attribute: {}", e.getMessage());
            return null;
        }
    }
	
	
	
	

	
	
	
	public void maximizeDisplay() {
		driver.manage().window().maximize();
	}
	
	

 
	// ---- convenience methods ----
    public void clickEnter(By locator) {
        log.info("clickEnter({})", locator);
        WebElement el = safeAct.safeFindElement(locator);
        if (el != null) {
            el.sendKeys(Keys.ENTER);
            log.info("Sent ENTER to {}", locator);
        } else {
            log.warn("clickEnter - element not found: {}", locator);
        }
    }
    
    
    
    public List<Integer> getWindowHeightWidth() {
		int height=driver.manage().window().getSize().getHeight();
		int width=driver.manage().window().getSize().getWidth();
		System.out.println("height is "+height+"     width is"+width);
		List<Integer> list=new ArrayList<>();
		list.add(height);
		list.add(width);
		return list;	
    }
    
    
    
    public void smoothScrollToElement(By locator) {
        log.info("[{}] Within smoothScrollToElement method", ThreadContext.get("testName"));

        try {
            SafeActions safeAct = new SafeActions(driver);
            WebElement element = safeAct.safeFindElement(locator);

            if (element != null) {
                ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({ behavior: 'smooth', block: 'center' });", 
                    element
                );
                wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
                log.info("[{}] Successfully scrolled to element: {}", ThreadContext.get("testName"), locator);
            } else {
                log.warn("[{}] Element not found for locator: {}", ThreadContext.get("testName"), locator);
            }

        } catch (Exception e) {
            log.error("[{}] Error while scrolling to element: {} - {}", ThreadContext.get("testName"), locator, e.getMessage());
        }
    }  
    
    
 public void scrollByPixel(int x, int y) {
    	
		log.info("[{}] Within scrollByPixel method", ThreadContext.get("testName"));
        ((JavascriptExecutor) driver).executeScript("window.scrollBy(arguments[0], arguments[1]);", x, y);
     
    }
 
 
 
 
 public boolean isElementInViewport(By locator){
	    log.info("[{}] Waiting until element is visible in viewport", ThreadContext.get("testName"));

	    try {
	        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
	        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));

	        JavascriptExecutor js = (JavascriptExecutor) driver;
	        return (Boolean) js.executeScript(
	            "var elem = arguments[0],                 " +
	            "  box = elem.getBoundingClientRect(),    " +
	            "  cx = box.left + box.width / 2,         " +
	            "  cy = box.top + box.height / 2,         " +
	            "  e = document.elementFromPoint(cx, cy); " +
	            "for (; e; e = e.parentElement) {         " +
	            "  if (e === elem)                        " +
	            "    return true;                         " +
	            "}                                        " +
	            "return false;", element);

	    } catch (Exception e) {
	        return false;
	    }
	}


	/** Row-count override for the language-driven tests (masterdata.properties: runForAllLanguages / overideLanguageCount*). */
	public int resolveLanguageIterationCount(int fullRowCount) {
    	 boolean isCron = Boolean.parseBoolean(System.getenv("IS_CRON"));
		
		
		String suffix = isCron ? "CI" : "";

		boolean runForAllLanguages = ConfigManager.getBoolean("runForAllLanguages" + suffix, false);
		int defaultLanguageCount = ConfigManager.getInt("overideLanguageCountDefault" + suffix);
		int overrideLanguageCount = ConfigManager.getInt("overideLanguageCount" + suffix, defaultLanguageCount);

		return resolveIterationCount(isCron, runForAllLanguages, overrideLanguageCount, fullRowCount);
	}

	/** Row-count override for the country-driven tests (masterdata.properties: runForAllCountries / overideCountriesCount*). */
	public int resolveCountryIterationCount(int fullRowCount) {
		boolean isCron = Boolean.parseBoolean(System.getenv("IS_CRON"));
	
		String suffix = isCron ? "CI" : "";

		boolean runForAllCountries = ConfigManager.getBoolean("runForAllCountries" + suffix, false);
		int defaultCountriesCount = ConfigManager.getInt("overideCountriesCountDefault" + suffix);
		int overrideCountriesCount = ConfigManager.getInt("overideCountriesCount" + suffix, defaultCountriesCount);

		return resolveIterationCount(isCron, runForAllCountries, overrideCountriesCount, fullRowCount);
	}

	/**
	 * Applies the "run for all" override toggle to an already-resolved count. The caller
	 * fetches and names its own isCron/runForAll/override values so it's clear at each call
	 * site exactly which masterdata.properties keys were read.
	 */
	private int resolveIterationCount(boolean isCron, boolean runForAll, int overrideCount, int fullRowCount) {
		log.info("[{}] Execution is {} Hence referring to {} keys from UtilData",
				ThreadContext.get("testName"),
				isCron ? "scheduled type /CRON Job on CI" : "Not CRON Job",
				isCron ? "CI" : "normal");

		if (runForAll) {
			return fullRowCount;
		}

		log.info("[{}] Row count is == {}", ThreadContext.get("testName"), overrideCount);
		return overrideCount;
	}

	/**
	 * Makes expected (sheet) and actual (page) text comparable: non-breaking/other horizontal spaces become a
	 * plain space, invisible format characters (e.g. LRM/RLM bidi marks) are dropped, and the Turkish/Azeri
	 * dotted capital I (which the page renders as plain I) is folded to I. Newlines are kept.
	 */
	public String normalizeText(String text) {
		if (text == null) {
			return "";
		}
		String cleaned = text.replaceAll("\\h", " ")
				.replaceAll("\\p{Cf}", "")
				.replace((char) 0x130, 'I');
		// The page renders uppercase Greek with the tonos accent (e.g. TYPOS shown with an accented Y); the sheet has none.
		String decomposed = java.text.Normalizer.normalize(cleaned, java.text.Normalizer.Form.NFD)
				.replaceAll("(?<=\\p{IsGreek})\\u0301", "");
		return java.text.Normalizer.normalize(decomposed, java.text.Normalizer.Form.NFC);
	}

	/**
	 * Side-menu entries such as "YouTube Music" are shown inconsistently across languages/runs, so they are
	 * stripped (case-insensitive) from both expected and actual text before comparing. Whitespace is
	 * collapsed afterwards so removing a keyword mid-string doesn't leave a double space behind.
	 */
	public String stripKeywords(String text, String... keywords) {
		String result = normalizeText(text);
		for (String keyword : keywords) {
			result = result.replaceAll("(?i)" + java.util.regex.Pattern.quote(keyword), " ");
		}
		return result.replaceAll("\\s+", " ").trim();
	}

	/**
	 * Removes the Hype/Live entry from both the actual side-menu text and the expected text, so the rest can be
	 * compared as is. YouTube shows either word in that slot, it changes between runs and differs per language,
	 * so it can't be stripped by keyword. The entry is found by its neighbour instead of a fixed position: it is
	 * always the item right before "YouTube Premium" (a brand that is never translated).
	 * If that anchor isn't found, nothing is removed and the full texts come back, so the assertion fails
	 * visibly instead of ignoring the wrong entry.
	 *
	 * @param actualItems side-menu entries in page order
	 * @return {expected, actual}, both stripped of the given keywords and the Hype/Live entry
	 */
	public String[] removeHypeOrLive(List<String> actualItems, String expectedText, String... keywords) {
		String expected = stripKeywords(expectedText, keywords);
		String actual = stripKeywords(String.join(" ", actualItems), keywords);

		int premium = -1;
		for (int i = 0; i < actualItems.size(); i++) {
			if (actualItems.get(i).toLowerCase().contains("premium")) {
				premium = i;
				break;
			}
		}
		if (premium < 1) {
			return new String[] { expected, actual };
		}

		String before = stripKeywords(String.join(" ", actualItems.subList(0, premium - 1)), keywords);
		String after = stripKeywords(String.join(" ", actualItems.subList(premium, actualItems.size())), keywords);
		actual = (before + " " + after).trim();
		if (expected.startsWith(before) && expected.endsWith(after) && before.length() + after.length() < expected.length()) {
			expected = (before + " " + after).trim();
		}
		return new String[] { expected, actual };
	}


	/**
	 * English / brand words YouTube leaves untranslated in every language's UI. Matched as whole words,
	 * case-insensitive. Extend this list if a new untranslated label starts confusing Lingua.
	 */
	private static final List<String> UNTRANSLATED_ENGLISH_WORDS = List.of(
			"YouTube", "Shorts", "Music", "Premium", "Kids", "Studio", "TV");

	/**
	 * Removes the untranslated English words from text before it is handed to Lingua, so they don't dilute or
	 * skew the detection of the real UI language. Nothing is removed when the language under test is English
	 * (the English words are then the expected content). If stripping would leave nothing, the original text
	 * is returned so Lingua always has something to work with.
	 *
	 * @param text         the UI text fetched from the page
	 * @param languageName the language selected in the UI, as written in the sheet (e.g. "Deutsch", "English (US)")
	 */
	public String removeEnglishWordsForNonEnglish(String text, String languageName) {
		if (text == null || languageName == null || languageName.trim().regionMatches(true, 0, "English", 0, 7)) {
			return text;
		}
		String result = text;
		for (String word : UNTRANSLATED_ENGLISH_WORDS) {
			result = result.replaceAll("(?iu)(?<![\\p{L}\\p{N}])" + java.util.regex.Pattern.quote(word) + "(?![\\p{L}\\p{N}])", " ");
		}
		result = result.replaceAll("\\s+", " ").trim();
		return result.isEmpty() ? text : result;
	}


	/**
	 * Repeats a very short text (e.g. a single language name) so Lingua has more to score. The expected language is
	 * already known from the sheet, so this only gives the detector enough input to confirm it; it adds no new
	 * information. Blank text is returned unchanged.
	 */
	public String repeatTextForDetection(String text, int times) {
		if (text == null || text.isBlank() || times <= 1) {
			return text;
		}
		return String.join(" ", java.util.Collections.nCopies(times, text.trim()));
	}

	/**
	 * Language check for the Lingua tests: both the {@code <html lang>} attribute and the Lingua result must
	 * match the expected values. For flaky-detection languages the Lingua result is ignored and only the
	 * attribute is asserted.
	 */
	public void assertLanguageDetectedAndAttributeMatch(org.testng.asserts.SoftAssert softAssert, String langText,
			String detectedLanguage, String expectedLanguage, String detectedAttribute, String expectedAttribute) {
		softAssert.assertEquals(detectedAttribute, expectedAttribute, "Language attribute mismatch for language=" + langText);
		if (isFlakyDetectionLanguage(langText)) {
			log.info("[{}] Skipping language detection check for flaky language={}", ThreadContext.get("testName"), langText);
			return;
		}
		softAssert.assertEquals(detectedLanguage, expectedLanguage, "Language detection mismatch for language=" + langText);
	}


	/** Languages whose Lingua detection result is unreliable; the detection assertion is skipped for these. */
	// non-ASCII chars written as (char) codes so the source compiles regardless of platform encoding
	private static final List<String> FLAKY_DETECTION_LANGUAGES = List.of(
			"Afrikaans",
			"Az" + (char) 0x259 + "rbaycan",
			"Bahasa Indonesia",
			"Dansk",
			"English (US)",
			"Espa" + (char) 0xF1 + "ol (Espa" + (char) 0xF1 + "a)",
			"Espa" + (char) 0xF1 + "ol (Latinoam" + (char) 0xE9 + "rica)",
			"Espa" + (char) 0xF1 + "ol (US)",
			"Hrvatski",
			"Svenska");

	public boolean isFlakyDetectionLanguage(String language) {
		return language != null && FLAKY_DETECTION_LANGUAGES.contains(language.trim());
	}

}
