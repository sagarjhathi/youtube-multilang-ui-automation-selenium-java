package main.java.yt_multi_lang_ui_validator.utilities;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import main.java.yt_multi_lang_ui_validator.config.ConfigManager;
import main.java.yt_multi_lang_ui_validator.driverManager.DriverManager;
import main.java.yt_multi_lang_ui_validator.logger.LoggerUtility;
import main.java.yt_multi_lang_ui_validator.pathManager.PathManager;

/**
 * Screenshots as evidence of a failure. Rules, so the picture shows the step that went wrong:
 *   - A picture is taken only where something went wrong, never for a passing item.
 *   - It is taken right after the page was read (or right after the error), BEFORE the next click or reload.
 *   - It never fails a test: a closed or missing browser, or any other problem, is only logged and null is returned.
 *
 * File name: s<number>_<item>_<kind>_a<attempt>, for example s03_Deutsch_check-failed_a1.jpg
 * The number counts the pictures of one test, so the files sort in the order they were taken.
 * The log line written with every picture holds the url, title, readyState and html lang, which show which step it belongs to.
 */
public class ScreenshotUtilUpdated {

	private final static Logger log = LoggerUtility.getLogger(ScreenshotUtilUpdated.class);

	private static final int MAX_SHOTS = 15;        // per test, so a bad night cannot fill the report
	private static final int MAX_SHOTS_CANARY = 3;  // every test fails on purpose in a canary run

	private static final Map<String, Integer> shotsPerTest = new HashMap<>();   // how many pictures each test has taken so far

	// A picture taken earlier in the item (page state when it was read), attached only if a check of the item fails.
	// One per thread, because the tests run in parallel.
	private static final ThreadLocal<File> heldShot = new ThreadLocal<>();


	// ---------------------------------------------------------------- pure helpers (tested offline)

	/** Letters (with their marks) and digits of any language are kept, everything else becomes "-", at most 30 characters. */
	public static String cleanName(String text) {
		if (text == null) {
			return "item";
		}
		String name = text.replaceAll("[^\\p{L}\\p{M}\\p{N}]+", "-").replaceAll("^-+|-+$", "");
		if (name.length() > 30) {
			name = name.substring(0, 30);
		}
		return name.isEmpty() ? "item" : name;
	}

	/** File name without the extension. attempt: 1 = first try of the item, 2 = the retry, 0 = not part of an item loop (left out). */
	public static String buildFileName(int number, String item, String kind, int attempt) {
		return "s" + String.format("%02d", number) + "_" + cleanName(item) + "_" + kind + (attempt > 0 ? "_a" + attempt : "");
	}

	/** The next picture number of this test (1, 2, 3...), or 0 when the limit is reached. */
	public static synchronized int nextNumber(String testKey, int limit) {
		int number = shotsPerTest.getOrDefault(testKey, 0) + 1;
		if (number > limit) {
			return 0;
		}
		shotsPerTest.put(testKey, number);
		return number;
	}


	// ---------------------------------------------------------------- taking pictures

	/**
	 * Takes a picture of the page now and keeps it in the screenshot folder of the running test.
	 * @param kind    what went wrong: check-failed, failed-before-recovery, recovery-failed, loop-aborted, menu-not-open,
	 *                menu-collapse-failed, menu-not-stable, lang-attr-unreadable, test-failed
	 * @param item    the language or location, or "page" when the problem belongs to no item
	 * @param attempt 1 = first try, 2 = the retry, 0 = not part of an item loop
	 * @return the path of the file, or null when no picture was kept
	 */
	public static String capture(String kind, String item, int attempt) {
		return save(null, kind, item, attempt);
	}

	/** Same, but uses the picture kept by hold() when there is one (the page has changed since), otherwise takes one now. */
	public static String captureHeldOrNow(String kind, String item, int attempt) {
		File held = heldShot.get();
		heldShot.remove();
		return save(held, kind, item, attempt);
	}

	/** Takes a picture now and keeps it in memory for this thread, nothing is saved. Call it right after the page was read. */
	public static void hold() {
		dropHeld();
		try {
			heldShot.set(takePicture());
		} catch (Exception e) {
			System.out.println("Screenshot hold failed (not fatal): " + e.getMessage() + " " + ThreadContext.get("testName"));
		}
	}

	/** Forgets the held picture: the item passed, or the attempt it belongs to has ended. */
	public static void dropHeld() {
		File held = heldShot.get();
		heldShot.remove();
		if (held != null) {
			held.delete();
		}
	}

	private static File takePicture() {
		return ((TakesScreenshot) DriverManager.getDriver()).getScreenshotAs(OutputType.FILE);
	}

	private static String save(File held, String kind, String item, int attempt) {
		String testName = ThreadContext.get("testName");
		try {
			int limit = Canary.isCanary() ? MAX_SHOTS_CANARY : MAX_SHOTS;
			int number = nextNumber(String.valueOf(testName), limit);
			if (number == 0) {
				log.info("[{}] Screenshot limit of {} reached, no picture for {} | {}", testName, limit, kind, item);
				return null;
			}

			File src = held != null ? held : takePicture();    // first, so nothing else comes between the failure and the picture

			String screenShotPath = PathManager.getScreenshotPath(testName);
			new File(screenShotPath).mkdirs();

			// compressed screenshots are saved as .jpg (PNG ignores the quality setting), uncompressed ones stay .png
			String extension = ConfigManager.getBoolean("compressImage", false) ? ".jpg" : ".png";
			String fileName = buildFileName(number, item, kind, attempt);
			String finalPath = screenShotPath + File.separator + fileName + extension;

			if (ConfigManager.getBoolean("compressImage", false)) {
				try {
					double imageQuality = Double.parseDouble(ConfigManager.get("imageCompressionQuality"));
					ImageCompressor.compressImage(src, finalPath, imageQuality);
				} catch (Exception e) {
					log.warn("[{}] compression quality / compressImage method failed, handling it the default way[No compression].", testName);
					finalPath = finalPath.replaceAll("\\.jpg$", ".png"); // the raw file is a PNG
					FileUtils.copyFile(src, new File(finalPath));
				}
			} else {
				FileUtils.copyFile(src, new File(finalPath));
			}

			log.info("[{}] Screenshot kept | {} | {}{}", testName, fileName, readPageFacts(), held != null ? " | (picture taken earlier in the item)" : "");
			return finalPath;

		} catch (Exception e) {
			System.out.println("Screenshot failed (not fatal): " + e.getMessage() + " " + testName);
			return null;
		}
	}

	/** What the page says about itself right now, on one line. Never throws. */
	private static String readPageFacts() {
		try {
			Object facts = ((JavascriptExecutor) DriverManager.getDriver()).executeScript(
					"return 'url=' + location.href + ' | title=' + document.title + ' | readyState=' + document.readyState + ' | html lang=' + document.documentElement.lang;");
			return String.valueOf(facts);
		} catch (Exception e) {
			return "page facts not readable";
		}
	}
}
