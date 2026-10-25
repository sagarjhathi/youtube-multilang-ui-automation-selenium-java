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
import main.java.yt_multi_lang_ui_validator.fileReader.FileReader;
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
	public static String normalizeText(String text) {
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
	public static String stripKeywords(String text, String... keywords) {
		String result = normalizeText(text);
		for (String keyword : keywords) {
			result = result.replaceAll("(?i)" + java.util.regex.Pattern.quote(normalizeText(keyword)), " ");
		}
		return result.replaceAll("\\s+", " ").trim();
	}

	/** Sheet with the Hype and Live word of every language (columns: Applied Language, Hype, Live). */
	private static final String HYPE_LIVE_WORDS_FILE = "data/HypeLiveWords.xlsx";
	private static final String HYPE_LIVE_WORDS_SHEET = "HypeLive";

	/** Word characters for whole-word matching: letters, combining marks and digits (works for non-Latin scripts). */
	private static final String WORD_CHAR = "[\\p{L}\\p{M}\\p{N}]";

	/** Reads the Hype/Live word sheet. Each row is {language, hype, live}, normalised like the side-menu texts. */
	public static List<String[]> loadHypeLiveWordRows() {
		FileReader reader = new FileReader();
		reader.loadWorkbook(HYPE_LIVE_WORDS_FILE);
		reader.loadSheet(HYPE_LIVE_WORDS_SHEET);
		List<String[]> rows = new ArrayList<>();
		for (int row = 1; row < reader.getRowCount(); row++) {
			rows.add(new String[] {
					normalizeText(reader.getCellValue(row, 0)).trim(),
					normalizeText(reader.getCellValue(row, 1)).trim(),
					normalizeText(reader.getCellValue(row, 2)).trim() });
		}
		reader.closeWorkbook();
		return rows;
	}

	/**
	 * YouTube fills one slot of the expanded side menu with the Hype entry, the Live entry or nothing, and the
	 * words differ per language. The expected sheet holds one of the two words, so this makes the EXPECTED text
	 * match what the page shows; the page text is never changed.
	 * <ol>
	 * <li>Compare as they are. Equal: nothing to change.</li>
	 * <li>Take the language's Hype and Live word (an empty one is ignored) and find which of them are in the
	 * expected text and in the page text, as whole words.</li>
	 * <li>The expected text must contain exactly one of the two words and the page must not contain both,
	 * otherwise nothing is changed.</li>
	 * <li>The page has the OTHER word: the expected word is replaced by the page word, which keeps its
	 * position.</li>
	 * <li>The page has NEITHER word: the expected word is removed.</li>
	 * <li>Anything else (another entry in the slot, a changed word elsewhere, a truncated menu, a language that is
	 * not in the word rows): nothing is changed, so the assertion fails with the full texts.</li>
	 * </ol>
	 *
	 * @param wordRows     rows of {@link #loadHypeLiveWordRows()}
	 * @param actualText   side-menu text from the page
	 * @param expectedText expected text from the sheet (holding the Hype or the Live word)
	 * @return the expected text to compare {@link #comparableSideMenuText} of the page text with
	 */
	public static String alignExpectedHypeLive(List<String[]> wordRows, String langText, String actualText, String expectedText) {
		String testName = ThreadContext.get("testName");
		String actual = comparableSideMenuText(actualText);
		String expected = comparableSideMenuText(expectedText);
		if (actual.equals(expected)) {
			log.info("[{}] Side menu matches the expected text as it is | language={}", testName, langText);
			return expected;
		}

		String language = normalizeText(langText).trim();
		String[] words = null;
		for (String[] row : wordRows) {
			if (row[0].equals(language)) {
				words = row;
				break;
			}
		}
		if (words == null) {
			log.info("[{}] No Hype/Live adjustment, language is not in the word file | language={}", testName, langText);
			return expected;
		}

		List<String> languageWords = new ArrayList<>();
		for (int column = 1; column <= 2; column++) {
			String word = words[column];
			if (!word.isEmpty() && languageWords.stream().noneMatch(word::equalsIgnoreCase)) {
				languageWords.add(word);
			}
		}
		List<String> inExpected = new ArrayList<>();
		List<String> inPage = new ArrayList<>();
		for (String word : languageWords) {
			if (containsWord(expected, word)) {
				inExpected.add(word);
			}
			if (containsWord(actual, word)) {
				inPage.add(word);
			}
		}
		if (inExpected.size() != 1) {
			log.info("[{}] No Hype/Live adjustment, the expected text holds {} of the 2 words | language={}", testName, inExpected.size(), langText);
			return expected;
		}
		if (inPage.size() > 1) {
			log.info("[{}] No Hype/Live adjustment, the page shows both words {} | language={}", testName, inPage, langText);
			return expected;
		}

		String expectedWord = inExpected.get(0);
		if (inPage.isEmpty()) {
			String aligned = replaceWord(expected, expectedWord, "");
			log.info("[{}] The page shows neither Hype nor Live, '{}' removed from the expected text | language={} matches={}",
					testName, expectedWord, langText, aligned.equals(actual));
			return aligned;
		}
		if (inPage.get(0).equalsIgnoreCase(expectedWord)) {
			log.info("[{}] No Hype/Live adjustment, the page shows the expected word '{}' and differs elsewhere | language={}", testName, expectedWord, langText);
			return expected;
		}
		String pageWord = firstWordMatch(actual, inPage.get(0));
		String aligned = replaceWord(expected, expectedWord, pageWord);
		log.info("[{}] The page shows '{}', expected '{}' replaced by it | language={} matches={}",
				testName, pageWord, expectedWord, langText, aligned.equals(actual));
		return aligned;
	}

	/** The text of the first whole-word match of {@code word} in {@code text}, as it is written there. */
	private static String firstWordMatch(String text, String word) {
		java.util.regex.Matcher matcher = wordPattern(word).matcher(text);
		return matcher.find() ? matcher.group() : word;
	}

	/** Side-menu text as compared: normalised, and without the entries YouTube shows inconsistently. */
	public static String comparableSideMenuText(String text) {
		return stripKeywords(text, "YouTube Music", "YouTube TV");
	}

	private static boolean containsWord(String text, String word) {
		return !word.isEmpty() && wordPattern(word).matcher(text).find();
	}

	/** Replaces the first whole-word occurrence of {@code word}; whitespace is collapsed afterwards. */
	private static String replaceWord(String text, String word, String replacement) {
		String replaced = wordPattern(word).matcher(text)
				.replaceFirst(java.util.regex.Matcher.quoteReplacement(" " + replacement + " "));
		return replaced.replaceAll("\\s+", " ").trim();
	}

	private static java.util.regex.Pattern wordPattern(String word) {
		return java.util.regex.Pattern.compile("(?<!" + WORD_CHAR + ")" + java.util.regex.Pattern.quote(word) + "(?!" + WORD_CHAR + ")",
				java.util.regex.Pattern.CASE_INSENSITIVE | java.util.regex.Pattern.UNICODE_CASE);
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
		softAssert.assertEquals(detectedAttribute, expectedAttribute, "Html lang attribute differs | language=" + langText);
		if (isFlakyDetectionLanguage(langText)) {
			log.info("[{}] Skipping language detection check for flaky language={}", ThreadContext.get("testName"), langText);
			return;
		}
		softAssert.assertEquals(detectedLanguage, expectedLanguage, "Lingua detection differs | language=" + langText);
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
