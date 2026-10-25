package main.test.java.tests;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import main.java.yt_multi_lang_ui_validator.fileReader.FileReader;
import main.java.yt_multi_lang_ui_validator.pages.YtLandingPage;
import main.java.yt_multi_lang_ui_validator.utilities.GenericUtility;

/**
 * Checks GenericUtility.alignExpectedHypeLive over every language of the expected sheet, and the stable menu read
 * of YtLandingPage. No browser is needed: the page text is built from the expected text and the menu is faked.
 * Not part of any runner xml; run it on its own.
 */
public class HypeLiveAlignTests {

	private final List<String> languages = new ArrayList<>();
	private final List<String> expectedTexts = new ArrayList<>();
	private List<String[]> wordRows;

	@BeforeClass
	public void loadData() {
		FileReader reader = new FileReader();
		reader.loadWorkbook("data/verifyingSideMenuLanguageAsInSettings.xlsx");
		reader.loadSheet("verifyingSideMenuLanguageAsInSe");
		for (int row = 1; row < reader.getRowCount(); row++) {
			languages.add(reader.getCellValue(row, 0));
			expectedTexts.add(reader.getCellValue(row, 1));
		}
		reader.closeWorkbook();
		wordRows = GenericUtility.loadHypeLiveWordRows();
	}

	private String[] words(String language) {
		for (String[] row : wordRows) {
			if (row[0].equals(GenericUtility.normalizeText(language).trim())) {
				return row;
			}
		}
		return null;
	}

	/** True when the page text passes: the aligned expected text equals the page text. */
	private boolean passes(String language, String expected, String page) {
		String aligned = GenericUtility.alignExpectedHypeLive(wordRows, language, page, expected);
		return aligned.equals(GenericUtility.comparableSideMenuText(page));
	}

	/** Expected text with the Hype word replaced by {@code replacement} ("" removes it). */
	private String pageWith(String expected, String hype, String replacement) {
		String normalized = GenericUtility.normalizeText(expected);
		int at = normalized.toLowerCase().indexOf(hype.toLowerCase());
		return (normalized.substring(0, at) + " " + replacement + " " + normalized.substring(at + hype.length())).replaceAll("\\s+", " ").trim();
	}

	/** Expected text holding the Live word in place of the Hype word. */
	private String expectedWithLive(int i) {
		String[] w = words(languages.get(i));
		return pageWith(expectedTexts.get(i), w[1], w[2]);
	}

	@Test
	public void wordFileCoversEveryLanguageAndHypeIsInTheExpectedText() {
		SoftAssert soft = new SoftAssert();
		soft.assertEquals(wordRows.size(), languages.size(), "word file rows vs expected sheet rows");
		for (int i = 0; i < languages.size(); i++) {
			String[] w = words(languages.get(i));
			soft.assertNotNull(w, "language missing in the word file | language=" + languages.get(i));
			if (w == null) {
				continue;
			}
			soft.assertFalse(w[1].isEmpty(), "Hype word empty | language=" + languages.get(i));
			soft.assertFalse(w[2].isEmpty(), "Live word empty | language=" + languages.get(i));
			soft.assertTrue(GenericUtility.normalizeText(expectedTexts.get(i)).contains(w[1]), "Hype word not in expected text | language=" + languages.get(i));
		}
		soft.assertAll();
	}

	@Test
	public void dataHoldsHypeOrLive_pageShowsHypeLiveOrNothing_passes() {
		SoftAssert soft = new SoftAssert();
		for (int i = 0; i < languages.size(); i++) {
			String language = languages.get(i);
			String[] w = words(language);
			String[] expectedVariants = { expectedTexts.get(i), expectedWithLive(i) };
			String[] pageVariants = { pageWith(expectedTexts.get(i), w[1], w[1]), expectedWithLive(i), pageWith(expectedTexts.get(i), w[1], "") };
			for (int e = 0; e < expectedVariants.length; e++) {
				for (int p = 0; p < pageVariants.length; p++) {
					soft.assertTrue(passes(language, expectedVariants[e], pageVariants[p]),
							"should pass | language=" + language + " expected=" + (e == 0 ? "Hype" : "Live") + " page=" + (p == 0 ? "Hype" : p == 1 ? "Live" : "nothing"));
				}
			}
		}
		soft.assertAll();
	}

	@Test
	public void slotWithGaming_fails() {
		SoftAssert soft = new SoftAssert();
		for (int i = 0; i < languages.size(); i++) {
			String[] w = words(languages.get(i));
			String gaming = pageWith(expectedTexts.get(i), w[1], "Gaming");
			soft.assertFalse(passes(languages.get(i), expectedTexts.get(i), gaming), "Hype expected, Gaming shown | language=" + languages.get(i));
			soft.assertFalse(passes(languages.get(i), expectedWithLive(i), gaming), "Live expected, Gaming shown | language=" + languages.get(i));
		}
		soft.assertAll();
	}

	@Test
	public void anyOtherWordChanged_fails() {
		SoftAssert soft = new SoftAssert();
		for (int i = 0; i < languages.size(); i++) {
			String[] w = words(languages.get(i));
			String expected = GenericUtility.normalizeText(expectedTexts.get(i));
			String changed = null;
			for (String token : expected.split("\\s+")) {
				if (!w[1].contains(token)) {
					changed = expected.replaceFirst(java.util.regex.Pattern.quote(token), java.util.regex.Matcher.quoteReplacement(token + "X"));
					break;
				}
			}
			soft.assertNotNull(changed, "no word to change | language=" + languages.get(i));
			if (changed == null) {
				continue;
			}
			String[] pages = { changed, changed.replace(w[1], w[2]), pageWith(changed, w[1], "") };
			for (int p = 0; p < pages.length; p++) {
				soft.assertFalse(passes(languages.get(i), expectedTexts.get(i), pages[p]), "Hype expected, other word changed (page " + p + ") | language=" + languages.get(i));
				soft.assertFalse(passes(languages.get(i), expectedWithLive(i), pages[p]), "Live expected, other word changed (page " + p + ") | language=" + languages.get(i));
			}
		}
		soft.assertAll();
	}

	@Test
	public void truncatedMenu_fails() {
		SoftAssert soft = new SoftAssert();
		for (int i = 0; i < languages.size(); i++) {
			String[] tokens = GenericUtility.normalizeText(expectedTexts.get(i)).split("\\s+");
			String truncated = String.join(" ", java.util.Arrays.copyOf(tokens, Math.max(1, tokens.length / 2)));
			soft.assertFalse(passes(languages.get(i), expectedTexts.get(i), truncated), "Hype expected, truncated page | language=" + languages.get(i));
			soft.assertFalse(passes(languages.get(i), expectedWithLive(i), truncated), "Live expected, truncated page | language=" + languages.get(i));
		}
		soft.assertAll();
	}

	@Test
	public void liveWordInAnotherPosition_fails() {
		SoftAssert soft = new SoftAssert();
		for (int i = 0; i < languages.size(); i++) {
			String[] w = words(languages.get(i));
			String withoutHype = pageWith(expectedTexts.get(i), w[1], "");
			String moved = GenericUtility.normalizeText(expectedTexts.get(i)).trim().endsWith(w[1]) ? w[2] + " " + withoutHype : withoutHype + " " + w[2];
			soft.assertFalse(passes(languages.get(i), expectedTexts.get(i), moved), "Hype expected, Live moved | language=" + languages.get(i));
			soft.assertFalse(passes(languages.get(i), expectedWithLive(i), moved), "Live expected, Live moved | language=" + languages.get(i));
		}
		soft.assertAll();
	}

	@Test
	public void bothWordsOnThePage_fails() {
		SoftAssert soft = new SoftAssert();
		for (int i = 0; i < languages.size(); i++) {
			String[] w = words(languages.get(i));
			String both = pageWith(expectedTexts.get(i), w[1], w[1] + " " + w[2]);
			soft.assertFalse(passes(languages.get(i), expectedTexts.get(i), both), "Hype expected, both on the page | language=" + languages.get(i));
			soft.assertFalse(passes(languages.get(i), expectedWithLive(i), both), "Live expected, both on the page | language=" + languages.get(i));
		}
		soft.assertAll();
	}

	@Test
	public void liveEntryTwice_fails() {
		SoftAssert soft = new SoftAssert();
		for (int i = 0; i < languages.size(); i++) {
			String[] w = words(languages.get(i));
			String twice = pageWith(expectedTexts.get(i), w[1], w[2] + " " + w[2]);
			soft.assertFalse(passes(languages.get(i), expectedTexts.get(i), twice), "Hype expected, Live twice | language=" + languages.get(i));
			soft.assertFalse(passes(languages.get(i), expectedWithLive(i), twice), "Live expected, Live twice | language=" + languages.get(i));
		}
		soft.assertAll();
	}

	@Test
	public void canadaJeuxVideo_passesOnceAndFailsTwice() {
		int canada = languages.indexOf("Français (Canada)");
		String[] w = words("Français (Canada)");
		SoftAssert soft = new SoftAssert();
		soft.assertEquals(w[2], "Jeux vidéo", "Canada Live word");
		soft.assertTrue(passes("Français (Canada)", expectedTexts.get(canada), pageWith(expectedTexts.get(canada), w[1], "Jeux vidéo")), "Jeux vidéo once should pass");
		soft.assertFalse(passes("Français (Canada)", expectedTexts.get(canada), pageWith(expectedTexts.get(canada), w[1], "Jeux vidéo Jeux vidéo")), "Jeux vidéo twice should fail");
		soft.assertFalse(passes("Klingon", expectedTexts.get(0), expectedTexts.get(0) + " X"), "language not in the file should fail");
		soft.assertAll();
	}

	// ---------- stable menu read ----------

	private static WebElement fakeEntry(String text) {
		return (WebElement) java.lang.reflect.Proxy.newProxyInstance(WebElement.class.getClassLoader(), new Class<?>[] { WebElement.class },
				(proxy, method, args) -> method.getName().equals("getText") ? text : null);
	}

	private static String joined(List<WebElement> menu) {
		StringBuilder text = new StringBuilder();
		for (WebElement entry : menu) {
			text.append(entry.getText()).append(' ');
		}
		return text.toString().trim();
	}

	@Test
	public void stableRead_menuGrowingIn300msSteps_isReadInFull() throws InterruptedException {
		long start = System.currentTimeMillis();
		int[] reads = { 0 };
		List<WebElement> menu = YtLandingPage.readUntilStable(() -> {
			reads[0]++;
			int entries = (int) Math.min(11, 1 + (System.currentTimeMillis() - start) / 300);
			List<WebElement> list = new ArrayList<>();
			for (int n = 1; n <= entries; n++) {
				list.add(fakeEntry("Entry" + n));
			}
			return list;
		});
		System.out.println("growing menu: " + menu.size() + " entries after " + reads[0] + " reads, " + (System.currentTimeMillis() - start) + " ms");
		Assert.assertEquals(menu.size(), 11, "the menu should be read in full");
	}

	@Test
	public void stableRead_settledMenu_takesThreeReads() throws InterruptedException {
		long start = System.currentTimeMillis();
		int[] reads = { 0 };
		List<WebElement> menu = YtLandingPage.readUntilStable(() -> {
			reads[0]++;
			return List.of(fakeEntry("A"), fakeEntry("B"));
		});
		long took = System.currentTimeMillis() - start;
		System.out.println("settled menu: " + reads[0] + " reads, " + took + " ms");
		Assert.assertEquals(reads[0], 3, "a settled menu is read 3 times");
		Assert.assertEquals(joined(menu), "A B");
		Assert.assertTrue(took >= 1000 && took < 2000, "settled read should take about 1 s, took " + took);
	}

	@Test
	public void stableRead_neverSettles_returnsLastReadAfterTenExtraReads() throws InterruptedException {
		int[] reads = { 0 };
		List<WebElement> menu = YtLandingPage.readUntilStable(() -> {
			reads[0]++;
			List<WebElement> list = new ArrayList<>();
			for (int n = 0; n < reads[0]; n++) {
				list.add(fakeEntry("E" + n));
			}
			return list;
		});
		Assert.assertEquals(reads[0], 11, "1 read + 10 extra reads");
		Assert.assertEquals(menu.size(), 11, "the last read is returned");
	}
}
