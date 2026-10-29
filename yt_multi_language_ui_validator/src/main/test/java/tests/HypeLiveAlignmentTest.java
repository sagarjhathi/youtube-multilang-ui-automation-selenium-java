package main.test.java.tests;

import org.testng.Assert;
import org.testng.annotations.Test;
import main.java.yt_multi_lang_ui_validator.utilities.GenericUtility;
import main.java.yt_multi_lang_ui_validator.utilities.GenericUtility.HypeLiveAlignment;

/**
 * Offline checks (no browser) of {@link GenericUtility#alignExpectedHypeLive}. A comparison passes only when the aligned
 * expected text equals the page text, so each case asserts that, or the opposite for the cases that must fail.
 * Not part of the suite xml files; run with {@code mvn test -Dtest=HypeLiveAlignmentTest}.
 */
public class HypeLiveAlignmentTest {

	private static final java.util.List<String[]> WORDS = java.util.Collections.singletonList(new String[] { "Testish", "Boost", "Stream" });
	private static final String LANGUAGE = "Testish";

	private static HypeLiveAlignment align(String page, String expected) {
		return GenericUtility.alignExpectedHypeLive(WORDS, LANGUAGE, page, expected);
	}

	private static void assertPasses(HypeLiveAlignment alignment, String page) {
		Assert.assertEquals(alignment.expected(), GenericUtility.comparableSideMenuText(page));
	}

	private static void assertFails(HypeLiveAlignment alignment, String page) {
		Assert.assertNotEquals(alignment.expected(), GenericUtility.comparableSideMenuText(page));
	}

	@Test
	public void sameLiveWordOnBothSides() {
		String page = "Home Shorts Stream History";
		HypeLiveAlignment alignment = align(page, "Home Shorts Stream History");
		assertPasses(alignment, page);
		Assert.assertEquals(alignment.kind(), "Live");
	}

	@Test
	public void sameHypeWordOnBothSides() {
		String page = "Home Shorts Boost History";
		HypeLiveAlignment alignment = align(page, "Home Shorts Boost History");
		assertPasses(alignment, page);
		Assert.assertEquals(alignment.kind(), "Hype");
	}

	@Test
	public void sheetHoldsHypePageShowsLive() {
		String page = "Home Shorts Stream History";
		HypeLiveAlignment alignment = align(page, "Home Shorts Boost History");
		assertPasses(alignment, page);
		Assert.assertEquals(alignment.kind(), "Live");
	}

	@Test
	public void sheetHoldsLivePageShowsHype() {
		String page = "Home Shorts Boost History";
		HypeLiveAlignment alignment = align(page, "Home Shorts Stream History");
		assertPasses(alignment, page);
		Assert.assertEquals(alignment.kind(), "Hype");
	}

	@Test
	public void pageHasNoEntryExpectedWordIsRemoved() {
		String page = "Home Shorts History";
		HypeLiveAlignment alignment = align(page, "Home Shorts Boost History");
		assertPasses(alignment, page);
		Assert.assertEquals(alignment.kind(), "missing");
	}

	@Test
	public void pageShowsBothWordsIsNotAdjusted() {
		String page = "Home Boost Stream History";
		HypeLiveAlignment alignment = align(page, "Home Shorts Boost History");
		assertFails(alignment, page);
		Assert.assertEquals(alignment.kind(), "not adjusted");
	}

	@Test
	public void anotherEntryInTheSlotFails() {
		String page = "Home Shorts Gaming History";
		assertFails(align(page, "Home Shorts Boost History"), page);
	}

	@Test
	public void wrongWordOnThePageFails() {
		String page = "Home Shorts Strem History";
		assertFails(align(page, "Home Shorts Boost History"), page);
	}

	@Test
	public void wordInsideALongerWordDoesNotCount() {
		String page = "Home Shorts Streaming History";
		assertFails(align(page, "Home Shorts Boost History"), page);
	}

	@Test
	public void changedTextElsewhereStillFailsWhenTheSlotMatches() {
		String page = "Home Shorts Stream Playlists";
		assertFails(align(page, "Home Shorts Boost History"), page);
	}

	@Test
	public void caseOfThePageWordIsIgnored() {
		String page = "Home Shorts STREAM History";
		assertPasses(align(page, "Home Shorts Boost History"), page);
	}

	@Test
	public void youtubeLabsOnThePageOnlyIsOptional() {
		String page = "Home Shorts Stream YouTube Premium YouTube Kids YouTube Labs History";
		HypeLiveAlignment alignment = align(page, "Home Shorts Stream YouTube Premium YouTube Kids History");
		assertPasses(alignment, page);
		Assert.assertEquals(alignment.kind(), "Live");
	}

	@Test
	public void youtubeLabsMissingFromThePageIsOptional() {
		String page = "Home Shorts Stream YouTube Premium YouTube Kids History";
		HypeLiveAlignment alignment = align(page, "Home Shorts Stream YouTube Premium YouTube Kids History");
		assertPasses(alignment, page);
		Assert.assertEquals(alignment.kind(), "Live");
	}

	@Test
	public void languageMissingFromTheWordSheetFailsWhenTextsDiffer() {
		String page = "Home Shorts Stream History";
		HypeLiveAlignment alignment = GenericUtility.alignExpectedHypeLive(WORDS, "Unknown", page, "Home Shorts Boost History");
		assertFails(alignment, page);
		Assert.assertEquals(alignment.kind(), "unknown language");
	}
}
