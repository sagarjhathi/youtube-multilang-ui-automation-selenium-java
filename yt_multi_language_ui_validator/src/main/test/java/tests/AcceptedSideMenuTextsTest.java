package main.test.java.tests;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import main.java.yt_multi_lang_ui_validator.fileReader.FileReader;
import main.java.yt_multi_lang_ui_validator.utilities.GenericUtility;

/**
 * Offline checks (no browser) of the allowed side-menu versions kept in data/verifyingSideMenuLanguageAsInSettings.xlsx.
 * Not part of the suite xml files; run with {@code mvn test -Dsurefire.suiteXmlFiles=src/main/test/java/runners/offline.xml}.
 */
public class AcceptedSideMenuTextsTest {

	private static final String SHEET_FILE = "data/verifyingSideMenuLanguageAsInSettings.xlsx";
	private static final String SHEET_NAME = "verifyingSideMenuLanguageAsInSe";

	private static final String AFRIKAANS_START = "Tuis Kortvideo's Intekeninge Myne Geskiedenis Musiek Flieks en TV";
	private static final String AFRIKAANS_END = "YouTube Kids Aangeegeskiedenis";

	private final List<String> languages = new ArrayList<>();
	private final List<String> bases = new ArrayList<>();
	private final List<String> slotCells = new ArrayList<>();
	private final List<String> premiumCells = new ArrayList<>();
	private final Map<String, List<String[]>> acceptedByLanguage = new LinkedHashMap<>();

	@BeforeClass
	public void readSheet() {
		FileReader reader = new FileReader();
		reader.loadWorkbook(SHEET_FILE);
		reader.loadSheet(SHEET_NAME);
		for (int row = 1; row < reader.getRowCount(); row++) {
			String language = reader.getCellValue(row, 0);
			languages.add(language);
			bases.add(reader.getCellValue(row, 1));
			slotCells.add(reader.getCellValue(row, 2));
			premiumCells.add(reader.getCellValue(row, 3));
			acceptedByLanguage.put(language, GenericUtility.loadAcceptedSideMenuTexts(reader, row));
		}
		reader.closeWorkbook();
	}

	/** True when the page text equals one of the allowed texts of the language, the way the test decides it. */
	private boolean allowed(String language, String pageText) {
		List<String[]> accepted = acceptedByLanguage.get(language);
		Assert.assertNotNull(accepted, "language not in the sheet: " + language);
		return GenericUtility.indexOfAcceptedText(accepted, GenericUtility.comparableSideMenuText(pageText)) >= 0;
	}

	private void assertAllowed(String language, String pageText) {
		Assert.assertTrue(allowed(language, pageText), language + " should be allowed: " + pageText);
	}

	private void assertRejected(String language, String pageText) {
		Assert.assertFalse(allowed(language, pageText), language + " should be rejected: " + pageText);
	}

	private static String afrikaans(String slotAndPremium) {
		return AFRIKAANS_START + " " + slotAndPremium + " " + AFRIKAANS_END;
	}

	// A) generation

	@Test
	public void sixTextsInOrderWithoutDoubleSpaces() {
		Map<String, List<String>> options = new LinkedHashMap<>();
		options.put("slot", GenericUtility.splitOptions("Live || Hype || (none)"));
		options.put("premium", GenericUtility.splitOptions("YouTube Premium || Try Premium for $0"));
		List<String[]> accepted = GenericUtility.acceptedSideMenuTexts("Home Shorts {slot} {premium} Kids", options);

		List<String> texts = new ArrayList<>();
		for (String[] entry : accepted) {
			texts.add(entry[0]);
		}
		Assert.assertEquals(texts, List.of(
				"Home Shorts Live YouTube Premium Kids",
				"Home Shorts Live Try Premium for $0 Kids",
				"Home Shorts Hype YouTube Premium Kids",
				"Home Shorts Hype Try Premium for $0 Kids",
				"Home Shorts YouTube Premium Kids",
				"Home Shorts Try Premium for $0 Kids"));
		Assert.assertEquals(accepted.get(0)[1], "slot=Live premium=YouTube Premium");
		Assert.assertEquals(accepted.get(4)[1], "slot=(none) premium=YouTube Premium");
	}

	@Test
	public void optionsKeepSingleBarsAndCommas() {
		Assert.assertEquals(GenericUtility.splitOptions("A, B | C || D ||  (none) "), List.of("A, B | C", "D", ""));
	}

	@Test
	public void matcherReturnsTheIndexOfTheEqualText() {
		List<String[]> accepted = acceptedByLanguage.get("Afrikaans");
		Assert.assertEquals(GenericUtility.indexOfAcceptedText(accepted, GenericUtility.comparableSideMenuText(afrikaans("Regstreeks YouTube Premium"))), 0);
		Assert.assertTrue(GenericUtility.indexOfAcceptedText(accepted, GenericUtility.comparableSideMenuText(afrikaans("Ophef YouTube Premium"))) > 0);
		Assert.assertEquals(GenericUtility.indexOfAcceptedText(accepted, GenericUtility.comparableSideMenuText(afrikaans("Gaming YouTube Premium"))), -1);
	}

	// B) the texts really seen are allowed

	@Test
	public void afrikaansEveryObservedVersionIsAllowed() {
		assertAllowed("Afrikaans", afrikaans("Ophef YouTube Premium"));
		assertAllowed("Afrikaans", afrikaans("Regstreeks YouTube Premium"));
		assertAllowed("Afrikaans", AFRIKAANS_START + " Regstreeks YouTube Premium YouTube Kids YouTube Labs Aangeegeskiedenis");
		assertAllowed("Afrikaans", AFRIKAANS_START + " Regstreeks Probeer Premium vir $0 YouTube Kids YouTube Labs Aangeegeskiedenis");
		assertAllowed("Afrikaans", afrikaans("Regstreeks Probeer Premium vir $0"));
		assertAllowed("Afrikaans", afrikaans("YouTube Premium"));
	}

	@Test
	public void englishUsEveryObservedVersionIsAllowed() {
		String start = "Home Shorts Subscriptions You History Music Movies & TV";
		assertAllowed("English (US)", start + " Hype YouTube Premium YouTube Kids Report history");
		assertAllowed("English (US)", start + " Live YouTube Premium YouTube Kids Report history");
		assertAllowed("English (US)", start + " Live YouTube Premium YouTube Kids YouTube Labs Report history");
		assertAllowed("English (US)", start + " Live Try Premium for $0 YouTube Kids YouTube Labs Report history");
	}

	@Test
	public void frenchCanadaEveryObservedVersionIsAllowed() {
		String start = "Accueil Shorts Abonnements Vous Historique Musique Films et émissions de télévision";
		assertAllowed("Français (Canada)", start + " Applaudissement YouTube Premium YouTube Kids Historique des rapports");
		assertAllowed("Français (Canada)", start + " Jeux vidéo YouTube Premium YouTube Kids Historique des rapports");
		assertAllowed("Français (Canada)", start + " Jeux vidéo YouTube Premium YouTube Kids YouTube Labs Historique des rapports");
		assertAllowed("Français (Canada)", start + " Jeux vidéo Essayez Premium pour 0 USD YouTube Kids Historique des rapports");
		assertAllowed("Français (Canada)", start + " YouTube Premium YouTube Kids Historique des rapports");
	}

	// C) texts that must be rejected

	@Test
	public void aNewPromoWordingIsRejected() {
		assertRejected("Afrikaans", afrikaans("Regstreeks Probeer Premium vir $1"));
	}

	@Test
	public void anotherEntryInTheSlotIsRejected() {
		assertRejected("Afrikaans", afrikaans("Gaming YouTube Premium"));
	}

	@Test
	public void bothSlotWordsTogetherAreRejected() {
		assertRejected("Afrikaans", afrikaans("Regstreeks Ophef YouTube Premium"));
	}

	@Test
	public void aChangedWordElsewhereIsRejected() {
		assertRejected("Afrikaans", afrikaans("Regstreeks YouTube Premium").replace("Tuis", "Home"));
	}

	@Test
	public void aCutMenuIsRejected() {
		assertRejected("Afrikaans", "Tuis Kortvideo's Intekeninge Myne Geskiedenis");
	}

	@Test
	public void theSlotWordAfterThePremiumEntryIsRejected() {
		assertRejected("Afrikaans", afrikaans("YouTube Premium Regstreeks"));
	}

	// D) the data of all rows

	@Test
	public void everyRowIsWellFormed() {
		Assert.assertFalse(languages.isEmpty(), "no rows read from the sheet");
		for (int row = 0; row < languages.size(); row++) {
			String language = languages.get(row);
			Assert.assertEquals(count(bases.get(row), "{slot}"), 1, language + ": {slot} must appear once in the base");
			Assert.assertEquals(count(bases.get(row), "{premium}"), 1, language + ": {premium} must appear once in the base");
			Assert.assertFalse(slotCells.get(row).isEmpty(), language + ": slot options are empty");
			Assert.assertFalse(premiumCells.get(row).isEmpty(), language + ": premium options are empty");
			for (String option : GenericUtility.splitOptions(slotCells.get(row))) {
				Assert.assertFalse(option.contains("{"), language + ": slot option holds a placeholder: " + option);
			}
			for (String option : GenericUtility.splitOptions(premiumCells.get(row))) {
				Assert.assertFalse(option.contains("{"), language + ": premium option holds a placeholder: " + option);
			}

			List<String[]> accepted = acceptedByLanguage.get(language);
			int expectedCount = GenericUtility.splitOptions(slotCells.get(row)).size() * GenericUtility.splitOptions(premiumCells.get(row)).size();
			Assert.assertEquals(accepted.size(), expectedCount, language + ": number of allowed texts");
			for (int first = 0; first < accepted.size(); first++) {
				Assert.assertFalse(accepted.get(first)[0].contains("{"), language + ": placeholder left unfilled in " + accepted.get(first)[0]);
				Assert.assertFalse(accepted.get(first)[0].contains("  "), language + ": double space in " + accepted.get(first)[0]);
				for (int second = first + 1; second < accepted.size(); second++) {
					Assert.assertNotEquals(accepted.get(first)[0], accepted.get(second)[0],
							language + ": two allowed texts are equal (" + accepted.get(first)[1] + " / " + accepted.get(second)[1] + ")");
				}
			}
		}
	}

	private static int count(String text, String part) {
		int found = 0;
		int at = text.indexOf(part);
		while (at >= 0) {
			found++;
			at = text.indexOf(part, at + part.length());
		}
		return found;
	}
}
