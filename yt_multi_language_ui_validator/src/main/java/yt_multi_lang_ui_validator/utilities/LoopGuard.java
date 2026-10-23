package main.java.yt_multi_lang_ui_validator.utilities;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.openqa.selenium.NoSuchSessionException;
import org.openqa.selenium.remote.UnreachableBrowserException;
import org.testng.asserts.SoftAssert;

import main.java.yt_multi_lang_ui_validator.logger.LoggerUtility;

/**
 * Keeps a language/location loop going when one iteration throws.
 *
 * In the loop: call {@link #passed} at the end of the iteration and {@link #retryAfterFailure} in the catch
 * (true means run the same item again). The loop condition uses {@link #shouldStop}.
 *
 * Rules: a failed item is retried once, and recorded as a failure only if the retry fails too. The loop stops
 * after 3 environment failures in a row (browser/network/site broken) or 5 other failures in a row
 * (element, Selenium, code). A passing item resets the count.
 */
public class LoopGuard {

	private static final Logger log = LoggerUtility.getLogger(LoopGuard.class);

	private static final int MAX_ENVIRONMENT_FAILURES = 3;
	private static final int MAX_OTHER_FAILURES = 5;

	/** Error messages that mean the browser, network or site is broken, not just one element missing. */
	private static final List<String> ENVIRONMENT_MESSAGES = List.of(
			"net::ERR_", "chrome not reachable", "disconnected", "invalid session id",
			"Timed out receiving message from renderer");

	/** Steps that bring the page back to the state the loop starts from. */
	public interface Recovery {
		void run() throws Exception;
	}

	private final SoftAssert softAssert;
	private boolean retrying = false;
	private boolean stop = false;
	private int environmentFailures = 0;
	private int otherFailures = 0;

	public LoopGuard(SoftAssert softAssert) {
		this.softAssert = softAssert;
	}

	public boolean shouldStop() {
		return stop;
	}

	public void passed(String item) {
		if (retrying) {
			log.warn("[{}] {} passed on the retry", ThreadContext.get("testName"), item);
		}
		retrying = false;
		environmentFailures = 0;
		otherFailures = 0;
	}

	/** @return true if the same item should be run again, false to move on to the next one */
	public boolean retryAfterFailure(String item, Exception e, Recovery recovery) {
		log.error("[{}] {} failed | {}", ThreadContext.get("testName"), item, describe(e));

		try {
			recovery.run();
		} catch (Exception recoveryError) {
			log.error("[{}] could not recover the page | {}", ThreadContext.get("testName"), describe(recoveryError));
		}

		// first failure: run the same item once more
		if (!retrying) {
			retrying = true;
			return true;
		}

		// failed on the retry as well: record it and move on
		retrying = false;
		softAssert.fail("Failed at " + item + " (also failed on retry) | " + describe(e));
		countFailure(item, isEnvironmentFailure(e));
		return false;
	}

	private void countFailure(String item, boolean environment) {
		if (environment) {
			environmentFailures++;
		} else {
			otherFailures++;
		}

		if (environmentFailures >= MAX_ENVIRONMENT_FAILURES || otherFailures >= MAX_OTHER_FAILURES) {
			softAssert.fail("Aborted at " + item + " | failures in a row: environment=" + environmentFailures
					+ ", element/code=" + otherFailures + ", remaining items not checked");
			stop = true;
		}
	}

	private boolean isEnvironmentFailure(Exception e) {
		if (e instanceof NoSuchSessionException || e instanceof UnreachableBrowserException) {
			return true;
		}
		String message = e.getMessage();
		if (message == null) {
			return false;
		}
		for (String environmentMessage : ENVIRONMENT_MESSAGES) {
			if (message.contains(environmentMessage)) {
				return true;
			}
		}
		return false;
	}

	/** Exception type plus the first line of its message (Selenium adds long build/driver info after it). */
	private String describe(Exception e) {
		String message = e.getMessage();
		if (message == null) {
			message = "";
		}
		String firstLine = message.split("\\R", 2)[0];
		if (firstLine.length() > 200) {
			firstLine = firstLine.substring(0, 200) + "...";
		}
		return e.getClass().getSimpleName() + ": " + firstLine;
	}
}
