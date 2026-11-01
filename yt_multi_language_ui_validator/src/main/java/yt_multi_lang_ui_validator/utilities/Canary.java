package main.java.yt_multi_lang_ui_validator.utilities;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import main.java.yt_multi_lang_ui_validator.config.ConfigManager;
import main.java.yt_multi_lang_ui_validator.logger.LoggerUtility;

/**
 * Canary mode: a green test only proves something if it can also go red. With CANARY=true one expected value in
 * every test is made wrong on purpose, so every test has to FAIL. A test that still passes is not checking what it
 * claims. Only expected values are spoiled, never what is read from the page. Normal runs are not changed.
 *
 * Use it on the expected side of a check: {@code Canary.canary(expected, row, spoiledRow)} returns the value
 * unchanged unless canary mode is on and {@code row == spoiledRow}.
 */
public class Canary {

	private static final Logger log = LoggerUtility.getLogger(Canary.class);

	/** The environment variable wins over the CANARY key in masterdata.properties. Read once. */
	private static final boolean CANARY = readFlag();

	private Canary() {}

	private static boolean readFlag() {
		String environment = System.getenv("CANARY");
		if (environment != null && !environment.isBlank()) {
			return Boolean.parseBoolean(environment.trim());
		}
		return ConfigManager.getBoolean("CANARY", false);
	}

	public static boolean isCanary() {
		return CANARY;
	}

	/** The value with " CANARY" added when canary mode is on and this is the row to spoil. */
	public static String canary(String value, int row, int spoiledRow) {
		return canary(value, row, spoiledRow, null, CANARY);
	}

	/** Same, with the wrong value written out instead of the value plus " CANARY". */
	public static String canary(String value, int row, int spoiledRow, String spoiledValue) {
		return canary(value, row, spoiledRow, spoiledValue, CANARY);
	}

	/** The same with the mode given, so a test can try both without touching the environment. A null spoiledValue means "value CANARY". */
	public static String canary(String value, int row, int spoiledRow, String spoiledValue, boolean canaryOn) {
		if (!canaryOn || row != spoiledRow) {
			return value;
		}
		String spoiled = spoiledValue != null ? spoiledValue : value + " CANARY";
		log.info("[{}] CANARY | row {} | '{}' spoiled on purpose to '{}', this check has to fail", ThreadContext.get("testName"), row, value, spoiled);
		return spoiled;
	}
}
