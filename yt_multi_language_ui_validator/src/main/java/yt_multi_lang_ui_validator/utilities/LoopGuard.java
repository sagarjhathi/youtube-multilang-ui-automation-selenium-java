package main.java.yt_multi_lang_ui_validator.utilities;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.aventstack.extentreports.ExtentTest;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.openqa.selenium.NoSuchSessionException;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.remote.UnreachableBrowserException;
import org.openqa.selenium.remote.http.ConnectionFailedException;
import org.testng.asserts.SoftAssert;

import main.java.yt_multi_lang_ui_validator.logger.LoggerUtility;
import main.java.yt_multi_lang_ui_validator.reporting.ReportManager;

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
			"Timed out receiving message from renderer", "session deleted because of page crash", "tab crashed",
			"target window already closed", "cannot determine loading status", "unable to discover open pages",
			"Connection refused");

	/** Steps that bring the page back to the state the loop starts from. */
	public interface Recovery {
		void run() throws Exception;
	}

	private final SoftAssert softAssert;
	private boolean retrying = false;
	private boolean stop = false;
	private int environmentFailures = 0;
	private int otherFailures = 0;

	// counters for the summary line
	private int passedCount = 0;
	private int passedOnRetryCount = 0;
	private int failedCount = 0;
	private boolean itemMismatch = false;
	private final Map<String, Integer> counters = new LinkedHashMap<>();
	// counts of the item being checked: added to the summary when the item ends, dropped when it is run again
	private final Map<String, Integer> itemCounters = new LinkedHashMap<>();

	public LoopGuard(SoftAssert softAssert) {
		this.softAssert = softAssert;
	}

	public boolean shouldStop() {
		return stop;
	}

	/** Marks the current item as failed on a comparison. The soft assertion itself is made by the caller. */
	public void mismatch() {
		itemMismatch = true;
	}

	/** Counts a named event for the summary, e.g. count("detection skipped") or count("slot Live"). */
	public void count(String label) {
		itemCounters.merge(label, 1, Integer::sum);
	}

	private void commitItemCounters() {
		itemCounters.forEach((label, number) -> counters.merge(label, number, Integer::sum));
		itemCounters.clear();
	}

	public void passed(String item) {
		if (retrying) {
			log.warn("[{}] {} passed on the retry", ThreadContext.get("testName"), item);
		}
		if (itemMismatch) {
			failedCount++; // finished without an error, but a comparison failed
		} else {
			passedCount++;
			if (retrying) {
				passedOnRetryCount++;
			}
		}
		itemMismatch = false;
		commitItemCounters();
		retrying = false;
		environmentFailures = 0;
		otherFailures = 0;
	}

	/**
	 * Logs (and adds to the report) one line for the whole loop, e.g.
	 * "Summary | passed 53 of 53 | 2 passed on the retry | 1 failed | stopped early | detection skipped: 4".
	 * Only the non-zero parts are printed, except "passed X of Y".
	 *
	 * @param totalItems how many items the loop was meant to check
	 * @return the summary line
	 */
	public String logSummary(int totalItems) {
		StringBuilder summary = new StringBuilder("Summary | passed " + passedCount + " of " + totalItems);
		if (passedOnRetryCount > 0) {
			summary.append(" | ").append(passedOnRetryCount).append(" passed on the retry");
		}
		if (failedCount > 0) {
			summary.append(" | ").append(failedCount).append(" failed");
		}
		if (stop) {
			summary.append(" | stopped early");
		}
		for (Map.Entry<String, Integer> counter : counters.entrySet()) {
			summary.append(" | ").append(counter.getKey()).append(": ").append(counter.getValue());
		}
		log.info("[{}] {}", ThreadContext.get("testName"), summary);
		ExtentTest extentTest = ReportManager.getTest();
		if (extentTest != null) {
			extentTest.info(summary.toString());
		}
		return summary.toString();
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
			itemCounters.clear();
			return true;
		}

		// failed on the retry as well: record it and move on
		retrying = false;
		itemMismatch = false;
		itemCounters.clear();
		failedCount++;
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
		if (e instanceof NoSuchSessionException || e instanceof NoSuchWindowException
				|| e instanceof UnreachableBrowserException || e instanceof ConnectionFailedException) {
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
