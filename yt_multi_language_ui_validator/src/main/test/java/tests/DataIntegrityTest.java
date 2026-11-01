package main.test.java.tests;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.Test;
import main.java.yt_multi_lang_ui_validator.fileReader.DataSheets;
import main.java.yt_multi_lang_ui_validator.fileReader.SheetLookup;

/**
 * Offline checks (no browser) that the data sheets agree with each other. LanguagesList.xlsx is the master list;
 * every language sheet must have each of its languages exactly once. A sheet that cannot be read (a duplicate name,
 * a blank name, a missing column) fails here with a message naming the file.
 * Run with {@code mvn test -Dsurefire.suiteXmlFiles=src/main/test/java/runners/offline.xml}.
 */
public class DataIntegrityTest {

	/** The sheets with one row per language, and the columns that must be filled in for every language. */
	private static Map<SheetLookup, List<String>> languageSheets() {
		Map<SheetLookup, List<String>> sheets = new LinkedHashMap<>();
		sheets.put(DataSheets.expectedLanguage(), Arrays.asList("Expected Language"));
		sheets.put(DataSheets.expectedAttribute(), Arrays.asList("Expected Attribute"));
		sheets.put(DataSheets.sideMenuExpanded(), Arrays.asList("Expected Text", "slot", "premium"));
		sheets.put(DataSheets.sideMenuCollapsed(), Arrays.asList("Expected Text"));
		sheets.put(DataSheets.searchFilter(), Arrays.asList("Expected Text"));
		return sheets;
	}

	/** D1: every language of the master list is in each sheet exactly once, and the sheet has no language the master lacks. */
	@Test
	public void everyLanguageIsInEachSheetOnce() {
		List<String> master = DataSheets.languages().names();
		for (SheetLookup sheet : languageSheets().keySet()) {
			List<String> names = sheet.names();
			List<String> missing = new ArrayList<>(master);
			missing.removeAll(names);
			List<String> extra = new ArrayList<>(names);
			extra.removeAll(master);
			Assert.assertTrue(missing.isEmpty(), sheet.source() + " has no row for " + missing);
			Assert.assertTrue(extra.isEmpty(), sheet.source() + " has languages that LanguagesList.xlsx lacks: " + extra);
		}
	}

	/** D2: no blank cell in a column the tests need. */
	@Test
	public void requiredCellsAreFilled() {
		for (Map.Entry<SheetLookup, List<String>> sheet : languageSheets().entrySet()) {
			for (String language : sheet.getKey().names()) {
				for (String column : sheet.getValue()) {
					Assert.assertFalse(sheet.getKey().get(language).get(column).isEmpty(),
							sheet.getKey().source() + " has a blank '" + column + "' for '" + language + "'");
				}
			}
		}
	}

	private static List<List<String>> rows(String[]... rows) {
		List<List<String>> cellRows = new ArrayList<>();
		for (String[] row : rows) {
			cellRows.add(Arrays.asList(row));
		}
		return cellRows;
	}

	/** D3: a missing name, a missing column and a duplicate name each throw an error that names the file. */
	@Test
	public void errorsNameTheFileAndTheName() {
		SheetLookup sheet = SheetLookup.fromRows("expected.xlsx", "Sheet1", "Language",
				rows(new String[] { "Language", "Expected" }, new String[] { "Afrikaans", "a" }, new String[] { "Dansk", "d" }));

		IllegalStateException missingName = Assert.expectThrows(IllegalStateException.class, () -> sheet.get("Deutsch"));
		Assert.assertTrue(missingName.getMessage().contains("expected.xlsx") && missingName.getMessage().contains("'Deutsch'"), missingName.getMessage());

		IllegalStateException missingColumn = Assert.expectThrows(IllegalStateException.class, () -> sheet.get("Dansk").get("Slot"));
		Assert.assertTrue(missingColumn.getMessage().contains("expected.xlsx") && missingColumn.getMessage().contains("'Slot'"), missingColumn.getMessage());

		IllegalStateException duplicate = Assert.expectThrows(IllegalStateException.class, () -> SheetLookup.fromRows("expected.xlsx", "Sheet1", "Language",
				rows(new String[] { "Language", "Expected" }, new String[] { "Dansk", "d" }, new String[] { "Deutsch", "g" }, new String[] { "Dansk", "d2" })));
		Assert.assertTrue(duplicate.getMessage().contains("expected.xlsx") && duplicate.getMessage().contains("'Dansk'")
				&& duplicate.getMessage().contains("rows 2 and 4"), duplicate.getMessage());

		IllegalStateException blankName = Assert.expectThrows(IllegalStateException.class, () -> SheetLookup.fromRows("expected.xlsx", "Sheet1", "Language",
				rows(new String[] { "Language", "Expected" }, new String[] { "Dansk", "d" }, new String[] { " ", "x" })));
		Assert.assertTrue(blankName.getMessage().contains("expected.xlsx") && blankName.getMessage().contains("row 3"), blankName.getMessage());

		IllegalStateException blankHeader = Assert.expectThrows(IllegalStateException.class, () -> SheetLookup.fromRows("expected.xlsx", "Sheet1", "Language",
				rows(new String[] { "Language", "" }, new String[] { "Dansk", "d" })));
		Assert.assertTrue(blankHeader.getMessage().contains("expected.xlsx"), blankHeader.getMessage());
	}

	/** D4: each country once (a duplicate stops at load), and every country has a code except United States. */
	@Test
	public void everyCountryHasACodeExceptUnitedStates() {
		SheetLookup countries = DataSheets.countries();
		for (String country : countries.names()) {
			String code = countries.get(country).get("Country Code");
			if (country.equals("United States")) {
				Assert.assertTrue(code.isEmpty(), countries.source() + " should have no code for 'United States', it has '" + code + "'");
			} else {
				Assert.assertFalse(code.isEmpty(), countries.source() + " has no code for '" + country + "'");
			}
		}
	}

	/** D5: the master list is not empty, and each language sheet has as many rows as the master has languages. */
	@Test
	public void rowCountsMatchTheMasterList() {
		SheetLookup master = DataSheets.languages();
		Assert.assertTrue(master.size() > 0, master.source() + " has no languages");
		for (SheetLookup sheet : languageSheets().keySet()) {
			Assert.assertEquals(sheet.size(), master.size(), sheet.source() + " has " + sheet.size() + " languages, LanguagesList.xlsx has " + master.size());
		}
	}

	/**
	 * D6: the two country tests are named after the first and last country of their half of the sheet (the first half is
	 * ceil(n / 2) countries). When the sheet or the middle of the list changes, the names must be changed with it.
	 */
	@Test
	public void countryTestNamesMatchTheSheet() throws IOException {
		List<String> countries = DataSheets.countries().names();
		int middle = (countries.size() + 1) / 2;
		String[] expectedNames = {
				"verifyingCountryCodeOnLogo_" + methodPart(countries.get(0)) + "_to_" + methodPart(countries.get(middle - 1)),
				"verifyingCountryCodeOnLogo_" + methodPart(countries.get(middle)) + "_to_" + methodPart(countries.get(countries.size() - 1)) };
		String source = new String(Files.readAllBytes(Paths.get("src/main/test/java/tests/YtMasterTests.java")), StandardCharsets.UTF_8);
		for (String name : expectedNames) {
			Assert.assertTrue(source.contains("public void " + name + "("),
					"YtMasterTests.java has no test '" + name + "': the country sheet (" + DataSheets.countries().source() + ") or the middle of the list changed, rename the country test");
		}
	}

	/** A country name as written in a method name: no spaces. */
	private static String methodPart(String country) {
		return country.replace(" ", "");
	}
}
