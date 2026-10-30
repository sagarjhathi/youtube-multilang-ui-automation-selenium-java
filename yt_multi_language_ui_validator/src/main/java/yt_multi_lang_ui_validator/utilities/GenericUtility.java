package main.java.yt_multi_lang_ui_validator.utilities;

import java.time.Duration; 
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
        el.sendKeys(Keys.ENTER);
        log.info("Sent ENTER to {}", locator);
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

            ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({ behavior: 'smooth', block: 'center' });",
                element
            );
            wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            log.info("[{}] Successfully scrolled to element: {}", ThreadContext.get("testName"), locator);

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

		// the override is a number of languages and row 0 of the sheets is the header, so one more row is read
		return resolveIterationCount(isCron, runForAllLanguages, overrideLanguageCount + 1, fullRowCount);
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

		int rowCount = Math.min(overrideCount, fullRowCount);
		log.info("[{}] Row count is == {}", ThreadContext.get("testName"), rowCount);
		return rowCount;
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
	 * Result of {@link #alignExpectedHypeLive}: the expected text to compare with, and what happened to the
	 * Hype/Live slot, for the comparison log line and the summary.
	 *
	 * @param expected the expected text to compare the page text with
	 * @param kind     "Hype", "Live", "missing", "equal", "unknown language" or "not adjusted"
	 * @param detail   readable form of the slot, e.g. "Live 'Regstreeks'" or "entry missing"
	 */
	public record HypeLiveAlignment(String expected, String kind, String detail) {
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
	 * @return the expected text to compare {@link #comparableSideMenuText} of the page text with, and the slot
	 */
	private static HypeLiveAlignment alignHypeLiveSlot(List<String[]> wordRows, String langText, String actualText, String expectedText) {
		String actual = comparableSideMenuText(actualText);
		String expected = comparableSideMenuText(expectedText);

		String language = normalizeText(langText).trim();
		String[] words = null;
		for (String[] row : wordRows) {
			if (row[0].equals(language)) {
				words = row;
				break;
			}
		}
		if (words == null) {
			return new HypeLiveAlignment(expected, "unknown language",
					actual.equals(expected) ? "texts equal, language not in the word sheet" : "language not in the word sheet");
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

		if (actual.equals(expected)) {
			if (inPage.size() == 1) {
				String pageWord = firstWordMatch(actual, inPage.get(0));
				String kind = slotKind(words, inPage.get(0));
				return new HypeLiveAlignment(expected, kind, kind + " '" + pageWord + "'");
			}
			return new HypeLiveAlignment(expected, inPage.isEmpty() ? "missing" : "equal", inPage.isEmpty() ? "entry missing, texts equal" : "texts equal");
		}
		if (inExpected.size() != 1) {
			return new HypeLiveAlignment(expected, "not adjusted", "not adjusted, the expected text holds " + inExpected.size() + " of the 2 words");
		}
		if (inPage.size() > 1) {
			return new HypeLiveAlignment(expected, "not adjusted", "not adjusted, the page shows both words " + inPage);
		}

		String expectedWord = inExpected.get(0);
		if (inPage.isEmpty()) {
			return new HypeLiveAlignment(replaceWord(expected, expectedWord, ""), "missing", "entry missing, '" + expectedWord + "' removed from the expected text");
		}
		if (inPage.get(0).equalsIgnoreCase(expectedWord)) {
			return new HypeLiveAlignment(expected, "not adjusted", "not adjusted, the page shows the expected word '" + expectedWord + "' and differs elsewhere");
		}
		String pageWord = firstWordMatch(actual, inPage.get(0));
		String kind = slotKind(words, inPage.get(0));
		return new HypeLiveAlignment(replaceWord(expected, expectedWord, pageWord), kind,
				kind + " '" + pageWord + "', expected '" + expectedWord + "' replaced by it");
	}

	/** The plain label of the Premium entry; YouTube swaps it for a localized trial offer ("Try Premium for $0") for some regions and accounts. */
	private static final String PREMIUM_LABEL = "YouTube Premium";
	/** Longest trial offer, in words, that is accepted in place of the plain label. */
	private static final int PREMIUM_OFFER_MAX_WORDS = 10;

	/**
	 * {@link #alignHypeLiveSlot} plus the Premium entry. The entry reads either "YouTube Premium" or a localized
	 * trial offer, depending on the region and account of the browser, so the sheet and the page can legitimately
	 * differ there. When that entry is the ONLY difference, the expected text is aligned to the page; any other
	 * difference still fails with the full texts.
	 */
	public static HypeLiveAlignment alignExpectedHypeLive(List<String[]> wordRows, String langText, String actualText, String expectedText) {
		HypeLiveAlignment slot = alignHypeLiveSlot(wordRows, langText, actualText, expectedText);
		String aligned = alignPremiumEntry(comparableSideMenuText(actualText), slot.expected());
		if (aligned.equals(slot.expected())) {
			return slot;
		}
		return new HypeLiveAlignment(aligned, slot.kind(), slot.detail() + " | Premium entry differs only in the offer text, page text used");
	}

	/**
	 * Returns {@code actual} when the two texts are identical except for the Premium entry, where one side holds the
	 * plain label and the other a short offer of at most {@value #PREMIUM_OFFER_MAX_WORDS} words; otherwise returns
	 * {@code expected} unchanged. The label must occur exactly once on the side that holds it.
	 */
	static String alignPremiumEntry(String actual, String expected) {
		if (actualHoldsOnlyLabelDifference(actual, expected) || actualHoldsOnlyLabelDifference(expected, actual)) {
			return actual;
		}
		return expected;
	}

	/** True when {@code withLabel} holds the plain label once and {@code other} has a short offer in its place and is equal around it. */
	private static boolean actualHoldsOnlyLabelDifference(String withLabel, String other) {
		List<String> label = Arrays.asList(PREMIUM_LABEL.split(" "));
		List<String> a = Arrays.asList(withLabel.trim().split("\s+"));
		List<String> o = Arrays.asList(other.trim().split("\s+"));
		int at = Collections.indexOfSubList(a, label);
		if (at < 0 || Collections.lastIndexOfSubList(a, label) != at) {
			return false;
		}
		List<String> before = a.subList(0, at);
		List<String> after = a.subList(at + label.size(), a.size());
		int offerWords = o.size() - before.size() - after.size();
		return offerWords >= 1 && offerWords <= PREMIUM_OFFER_MAX_WORDS
				&& o.subList(0, before.size()).equals(before)
				&& o.subList(o.size() - after.size(), o.size()).equals(after);
	}

	/** "Hype" or "Live", depending on which column of the word row {@code word} is in. */
	private static String slotKind(String[] words, String word) {
		return word.equalsIgnoreCase(words[1]) ? "Hype" : "Live";
	}

	/**
	 * One log line for a text comparison, e.g. "Side menu | Afrikaans | match | 123 chars | slot: Live 'Regstreeks'".
	 * A mismatch is logged as MISMATCH (warning) so a pass and a failure can be told apart in the report.
	 *
	 * @param what   what was compared ("Side menu", "Collapsed side menu", "Search filter", "Country code")
	 * @param item   language or location
	 * @param chars  length of the compared page text
	 * @param extra  optional text appended after the length, may be null
	 * @return {@code match}, so the call can be used inside a condition
	 */
	public static boolean logComparison(String what, String item, boolean match, int chars, String extra) {
		String line = what + " | " + item + " | " + (match ? "match" : "MISMATCH") + " | " + chars + " chars" + (extra == null ? "" : " | " + extra);
		if (match) {
			log.info("[{}] {}", ThreadContext.get("testName"), line);
		} else {
			log.warn("[{}] {}", ThreadContext.get("testName"), line);
		}
		return match;
	}

	/**
	 * Soft assertion that also tells the loop guard when the values differ, so the item is counted as failed in the
	 * summary instead of passed.
	 */
	public static void softAssertEquals(org.testng.asserts.SoftAssert softAssert, LoopGuard guard, Object actual, Object expected, String message) {
		softAssert.assertEquals(actual, expected, message);
		if (!java.util.Objects.equals(actual, expected)) {
			guard.mismatch();
		}
	}

	/** The text of the first whole-word match of {@code word} in {@code text}, as it is written there. */
	private static String firstWordMatch(String text, String word) {
		java.util.regex.Matcher matcher = wordPattern(word).matcher(text);
		return matcher.find() ? matcher.group() : word;
	}

	/** Side-menu text as compared: normalised, and without the entries YouTube shows inconsistently. */
	public static String comparableSideMenuText(String text) {
		// "YouTube Labs" is untranslated and shown only on some runs, like Music and TV; keep it optional until it is seen on every run
		return stripKeywords(text, "YouTube Music", "YouTube TV", "YouTube Labs");
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
	 * attribute is asserted, but only when the text came from the side menu ({@code sideMenuText}); the settings
	 * and filter texts are checked for every language.
	 */
	public void assertLanguageDetectedAndAttributeMatch(org.testng.asserts.SoftAssert softAssert, LoopGuard guard, String langText,
			String detectedLanguage, String expectedLanguage, String detectedAttribute, String expectedAttribute, boolean sideMenuText) {
		softAssertEquals(softAssert, guard, detectedAttribute, expectedAttribute, "Html lang attribute differs | language=" + langText);
		boolean attributeMatch = java.util.Objects.equals(detectedAttribute, expectedAttribute);
		if (sideMenuText && ConfigManager.getBoolean("skipFlakyLanguageDetection", true) && isFlakyDetectionLanguage(langText)) {
			log.info("[{}] Skipping language detection check for flaky language={}", ThreadContext.get("testName"), langText);
			guard.count("detection skipped");
			logLanguageCheck(langText, attributeMatch, detectedAttribute, expectedAttribute, "detection skipped (flaky), Lingua said " + detectedLanguage);
			return;
		}
		softAssertEquals(softAssert, guard, detectedLanguage, expectedLanguage, "Lingua detection differs | language=" + langText);
		boolean detectionMatch = java.util.Objects.equals(detectedLanguage, expectedLanguage);
		logLanguageCheck(langText, attributeMatch && detectionMatch, detectedAttribute, expectedAttribute,
				"detection " + detectedLanguage + (detectionMatch ? " = " : " != ") + expectedLanguage);
	}

	/** One log line per language for the Lingua tests, e.g. "Language check | Deutsch | match | lang 'de-DE' = 'de-DE' | detection GERMAN = GERMAN". */
	private static void logLanguageCheck(String langText, boolean match, String detectedAttribute, String expectedAttribute, String detection) {
		String line = "Language check | " + langText + " | " + (match ? "match" : "MISMATCH") + " | lang '" + detectedAttribute
				+ "' " + (java.util.Objects.equals(detectedAttribute, expectedAttribute) ? "=" : "!=") + " '" + expectedAttribute + "' | " + detection;
		if (match) {
			log.info("[{}] {}", ThreadContext.get("testName"), line);
		} else {
			log.warn("[{}] {}", ThreadContext.get("testName"), line);
		}
	}


	/**
	 * Languages whose Lingua detection result is unreliable on the side-menu texts (short, mixed with English words);
	 * the detection assertion is skipped for these in the two side-menu tests only. The other languages matched in every run.
	 */
	// non-ASCII chars written as (char) codes so the source compiles regardless of platform encoding
	private static final List<String> FLAKY_DETECTION_LANGUAGES = List.of(
			"Az" + (char) 0x259 + "rbaycan", // detector returns TURKISH on the collapsed side menu
			"Bahasa Indonesia", // detector returns ENGLISH on the side-menu texts
			"Dansk", // detector returns DUTCH on the collapsed side menu
			"Hrvatski"); // detector returns BOSNIAN on the collapsed side menu

	public boolean isFlakyDetectionLanguage(String language) {
		return language != null && FLAKY_DETECTION_LANGUAGES.contains(language.trim());
	}

}
