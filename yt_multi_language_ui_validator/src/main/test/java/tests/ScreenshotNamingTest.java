package main.test.java.tests;

import org.testng.Assert;
import org.testng.annotations.Test;
import main.java.yt_multi_lang_ui_validator.utilities.ScreenshotUtilUpdated;

/**
 * Offline checks (no browser) of the pure parts of {@link ScreenshotUtilUpdated}: file names and the per-test limit.
 * Whether the picture shows the right page state can only be seen in a live run.
 * Run with {@code mvn test -Dsurefire.suiteXmlFiles=src/main/test/java/runners/offline.xml}.
 */
public class ScreenshotNamingTest {

	@Test
	public void fileNameHasNumberItemKindAndAttempt() {
		Assert.assertEquals(ScreenshotUtilUpdated.buildFileName(3, "Deutsch", "check-failed", 1), "s03_Deutsch_check-failed_a1");
		Assert.assertEquals(ScreenshotUtilUpdated.buildFileName(12, "Deutsch", "failed-before-recovery", 2), "s12_Deutsch_failed-before-recovery_a2");
	}

	@Test
	public void attemptIsLeftOutOutsideTheLoops() {
		Assert.assertEquals(ScreenshotUtilUpdated.buildFileName(1, "page", "test-failed", 0), "s01_page_test-failed");
	}

	@Test
	public void unsafeCharactersBecomeADash() {
		// \ / : * ? " < > | are not allowed in Windows file names
		Assert.assertEquals(ScreenshotUtilUpdated.cleanName("English (US)"), "English-US");
		Assert.assertEquals(ScreenshotUtilUpdated.cleanName("a\\b/c:d*e?f\"g<h>i|j"), "a-b-c-d-e-f-g-h-i-j");
	}

	@Test
	public void lettersOfOtherLanguagesAreKept() {
		String hindi = "हिन्दी"; // the vowel signs and the virama are marks, not letters: they must stay
		Assert.assertEquals(ScreenshotUtilUpdated.cleanName(hindi), hindi);
		Assert.assertEquals(ScreenshotUtilUpdated.cleanName("Azərbaycan"), "Azərbaycan");
	}

	@Test
	public void longOrEmptyNamesStayUsable() {
		Assert.assertEquals(ScreenshotUtilUpdated.cleanName("x".repeat(100)).length(), 30);
		Assert.assertEquals(ScreenshotUtilUpdated.cleanName("???"), "item");
		Assert.assertEquals(ScreenshotUtilUpdated.cleanName(null), "item");
	}

	@Test
	public void numbersCountPerTestAndStopAtTheLimit() {
		Assert.assertEquals(ScreenshotUtilUpdated.nextNumber("limit-test-a", 3), 1);
		Assert.assertEquals(ScreenshotUtilUpdated.nextNumber("limit-test-a", 3), 2);
		Assert.assertEquals(ScreenshotUtilUpdated.nextNumber("limit-test-a", 3), 3);
		Assert.assertEquals(ScreenshotUtilUpdated.nextNumber("limit-test-a", 3), 0, "the 4th picture is over the limit");
		Assert.assertEquals(ScreenshotUtilUpdated.nextNumber("limit-test-a", 3), 0, "it stays over the limit");
		Assert.assertEquals(ScreenshotUtilUpdated.nextNumber("limit-test-b", 3), 1, "another test starts at 1");
	}

	@Test
	public void captureNeverThrowsWithoutABrowser() {
		// no driver exists in the offline run: the call must only log and return null
		Assert.assertNull(ScreenshotUtilUpdated.capture("test-failed", "page", 0));
		Assert.assertNull(ScreenshotUtilUpdated.captureHeldOrNow("check-failed", "Deutsch", 1));
		ScreenshotUtilUpdated.hold();
		ScreenshotUtilUpdated.dropHeld();
	}
}
