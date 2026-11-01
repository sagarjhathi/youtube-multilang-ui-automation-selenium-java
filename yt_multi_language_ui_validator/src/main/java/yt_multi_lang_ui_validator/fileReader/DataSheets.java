package main.java.yt_multi_lang_ui_validator.fileReader;

/**
 * The data sheets, each read once and looked up by name (see {@link SheetLookup}). {@link #languages()} is the one
 * list of languages every language test loops over; the other sheets are asked for a row by that language name.
 * Column headers are used as they are written in the sheets.
 */
public class DataSheets {

	private DataSheets() {}

	/** The master list: every language, in the order the tests run them. */
	public static SheetLookup languages() {
		return SheetLookup.load("data/LanguagesList.xlsx", "LanguagesList", "LanguagesList");
	}

	/** Column "Expected Language", the language Lingua should detect. */
	public static SheetLookup expectedLanguage() {
		return SheetLookup.load("data/ApplicableLanguageExpectedLanguage.xlsx", "AppVExpectLanguages", "Language");
	}

	/** Column "Expected Attribute", the expected html lang. */
	public static SheetLookup expectedAttribute() {
		return SheetLookup.load("data/ApplicableLanguageExpectedAttibute.xlsx", "Attribute", "Language");
	}

	/** Columns "Expected Text" (base text with {slot} and {premium}), "slot" and "premium" (their options). */
	public static SheetLookup sideMenuExpanded() {
		return SheetLookup.load("data/expectedSideMenuExpanded.xlsx", "ExpectedSideMenuExpanded", "Language");
	}

	/** Column "Expected Text", the collapsed side menu text. */
	public static SheetLookup sideMenuCollapsed() {
		return SheetLookup.load("data/expectedSideMenuCollapsed.xlsx", "ExpectedSideMenuCollapsed", "Language");
	}

	/** Column "Expected Text", the search filter popup text. */
	public static SheetLookup searchFilter() {
		return SheetLookup.load("data/expectedSearchFilter.xlsx", "ExpectedSearchFilter", "Language");
	}

	/** Keyed by country name, column "Country Code" (empty for the one country without a code). */
	public static SheetLookup countries() {
		return SheetLookup.load("data/countryCodes.xlsx", "CountryNameCode", "Country Name");
	}
}
