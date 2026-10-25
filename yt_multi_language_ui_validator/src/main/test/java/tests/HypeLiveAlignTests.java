package main.test.java.tests;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import main.java.yt_multi_lang_ui_validator.fileReader.FileReader;
import main.java.yt_multi_lang_ui_validator.utilities.GenericUtility;

/**
 * Checks GenericUtility.alignExpectedHypeLive over every language of the expected sheet. No browser is needed:
 * the page text is built from the expected text. Not part of any runner xml; run it on its own.
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

	/** Page text with the Hype word replaced by {@code replacement} ("" removes it). */
	private String pageWith(String expected, String hype, String replacement) {
		String normalized = GenericUtility.normalizeText(expected);
		int at = normalized.toLowerCase().indexOf(hype.toLowerCase());
		return (normalized.substring(0, at) + " " + replacement + " " + normalized.substring(at + hype.length())).replaceAll("\s+", " ").trim();
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
			soft.assertTrue(GenericUtility.normalizeText(expectedTexts.get(i)).contains(w[1]), "Hype word not in expected text | language=" + languages.get(i));
		}
		soft.assertAll();
	}

	@Test
	public void case1_pageEqualToExpected_passes() {
		SoftAssert soft = new SoftAssert();
		for (int i = 0; i < languages.size(); i++) {
			soft.assertTrue(passes(languages.get(i), expectedTexts.get(i), expectedTexts.get(i)), "should pass | language=" + languages.get(i));
		}
		soft.assertAll();
	}

	@Test
	public void case2_pageWithLiveInPlaceOfHype_passes() {
		SoftAssert soft = new SoftAssert();
		int checked = 0;
		for (int i = 0; i < languages.size(); i++) {
			String[] w = words(languages.get(i));
			if (w[2].isEmpty()) {
				continue; // Live word not decided yet (Français (Canada))
			}
			checked++;
			soft.assertTrue(passes(languages.get(i), expectedTexts.get(i), pageWith(expectedTexts.get(i), w[1], w[2])), "should pass | language=" + languages.get(i));
		}
		System.out.println("case2 languages checked: " + checked);
		soft.assertAll();
	}

	@Test
	public void case3_pageWithEntryLeftOut_passes() {
		SoftAssert soft = new SoftAssert();
		for (int i = 0; i < languages.size(); i++) {
			String[] w = words(languages.get(i));
			soft.assertTrue(passes(languages.get(i), expectedTexts.get(i), pageWith(expectedTexts.get(i), w[1], "")), "should pass | language=" + languages.get(i));
		}
		soft.assertAll();
	}

	@Test
	public void case4_slotWithGaming_fails() {
		SoftAssert soft = new SoftAssert();
		for (int i = 0; i < languages.size(); i++) {
			String[] w = words(languages.get(i));
			soft.assertFalse(passes(languages.get(i), expectedTexts.get(i), pageWith(expectedTexts.get(i), w[1], "Gaming")), "should fail | language=" + languages.get(i));
		}
		soft.assertAll();
	}

	@Test
	public void case5_anyOtherWordChanged_fails() {
		SoftAssert soft = new SoftAssert();
		for (int i = 0; i < languages.size(); i++) {
			String[] w = words(languages.get(i));
			String expected = GenericUtility.normalizeText(expectedTexts.get(i));
			// change the first word that is not part of the Hype word
			String changed = null;
			for (String token : expected.split("\s+")) {
				if (!w[1].contains(token)) {
					changed = expected.replaceFirst(java.util.regex.Pattern.quote(token), java.util.regex.Matcher.quoteReplacement(token + "X"));
					break;
				}
			}
			soft.assertNotNull(changed, "no word to change | language=" + languages.get(i));
			if (changed == null) {
				continue;
			}
			soft.assertFalse(passes(languages.get(i), expectedTexts.get(i), changed), "Hype shown, other word changed, should fail | language=" + languages.get(i));
			if (!w[2].isEmpty()) {
				String liveChanged = changed.replace(w[1], w[2]);
				soft.assertFalse(passes(languages.get(i), expectedTexts.get(i), liveChanged), "Live shown, other word changed, should fail | language=" + languages.get(i));
			}
			soft.assertFalse(passes(languages.get(i), expectedTexts.get(i), pageWith(changed, w[1], "")), "entry left out, other word changed, should fail | language=" + languages.get(i));
		}
		soft.assertAll();
	}

	@Test
	public void case6_liveWordInAnotherPosition_fails() {
		SoftAssert soft = new SoftAssert();
		for (int i = 0; i < languages.size(); i++) {
			String[] w = words(languages.get(i));
			if (w[2].isEmpty()) {
				continue;
			}
			String withoutHype = pageWith(expectedTexts.get(i), w[1], "");
			String atEnd = withoutHype + " " + w[2];
			String atStart = w[2] + " " + withoutHype;
			String moved = GenericUtility.normalizeText(expectedTexts.get(i)).trim().endsWith(w[1]) ? atStart : atEnd;
			soft.assertFalse(passes(languages.get(i), expectedTexts.get(i), moved), "Live in another position, should fail | language=" + languages.get(i));
		}
		soft.assertAll();
	}

	@Test
	public void extra_unknownLanguageAndCanadaGaming_fail() {
		String expected = expectedTexts.get(0);
		SoftAssert soft = new SoftAssert();
		soft.assertFalse(passes("Klingon", expected, expected + " X"), "language not in the file should fail");
		int canada = languages.indexOf("Français (Canada)");
		String[] w = words("Français (Canada)");
		soft.assertFalse(passes("Français (Canada)", expectedTexts.get(canada), pageWith(expectedTexts.get(canada), w[1], "Jeux vidéo")), "Canada with Jeux vidéo should fail");
		soft.assertAll();
	}
}
