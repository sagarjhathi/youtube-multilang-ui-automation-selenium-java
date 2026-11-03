package main.test.java.tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import main.java.yt_multi_lang_ui_validator.utilities.LoopGuard;

/**
 * Offline checks (no browser) of {@link LoopGuard}, driven by hand the way a test loop drives it.
 * What the guard recorded is read from the SoftAssert (assertAll inside a try/catch) and from the summary line it returns.
 * Run with {@code mvn test -Dsurefire.suiteXmlFiles=src/main/test/java/runners/offline.xml}.
 */
public class LoopGuardTest {

	private final List<String> logged = new ArrayList<>();
	private Logger guardLogger;
	private AbstractAppender collector;

	@BeforeClass
	public void collectGuardLog() {
		guardLogger = (Logger) LogManager.getLogger(LoopGuard.class);
		guardLogger.setLevel(Level.INFO);
		collector = new AbstractAppender("loopguard-test", null, null, true, Property.EMPTY_ARRAY) {
			@Override
			public void append(LogEvent event) {
				logged.add(event.getMessage().getFormattedMessage());
			}
		};
		collector.start();
		guardLogger.addAppender(collector);
	}

	@AfterClass(alwaysRun = true)
	public void stopCollecting() {
		guardLogger.removeAppender(collector);
		collector.stop();
	}

	/** One iteration of the loop body: attempt 1 is the first run of the item, attempt 2 its retry. */
	private interface Body {
		void run(String item, int attempt) throws Exception;
	}

	/** The loop of a test: run the item, report it passed, or ask the guard to run it again. Returns how often each item ran. */
	private Map<String, Integer> runLoop(LoopGuard guard, List<String> items, Body body, LoopGuard.Recovery recovery) {
		Map<String, Integer> ran = new LinkedHashMap<>();
		for (int i = 0; i < items.size() && !guard.shouldStop(); i++) {
			String item = items.get(i);
			ran.merge(item, 1, Integer::sum);
			try {
				body.run(item, ran.get(item));
				guard.passed(item);
			} catch (Exception e) {
				if (guard.retryAfterFailure(item, e, recovery)) {
					i--;
				}
			}
		}
		return ran;
	}

	private Map<String, Integer> runLoop(LoopGuard guard, List<String> items, Body body) {
		return runLoop(guard, items, body, () -> { });
	}

	/** The failures a SoftAssert has collected, one entry each; empty when there are none. */
	private List<String> failuresOf(SoftAssert softAssert) {
		try {
			softAssert.assertAll();
			return new ArrayList<>();
		} catch (AssertionError e) {
			List<String> failures = new ArrayList<>();
			for (String line : e.getMessage().split("\\R")) {
				String failure = line.trim();
				if (failure.endsWith(",")) {
					failure = failure.substring(0, failure.length() - 1);
				}
				if (!failure.isEmpty() && !failure.startsWith("The following asserts failed")) {
					failures.add(failure);
				}
			}
			return failures;
		}
	}

	private List<String> items(String... names) {
		return Arrays.asList(names);
	}

	/** L1: an item that fails once is run again after the recovery and passes on the retry, with no failure recorded. */
	@Test
	public void itemThatFailsOnceIsRetriedAndPasses() {
		SoftAssert softAssert = new SoftAssert();
		LoopGuard guard = new LoopGuard(softAssert);
		int[] recoveries = {0};

		Map<String, Integer> ran = runLoop(guard, items("a", "b", "c"), (item, attempt) -> {
			if (item.equals("b") && attempt == 1) {
				throw new RuntimeException("click timed out");
			}
		}, () -> recoveries[0]++);
		String summary = guard.logSummary(3);

		Assert.assertEquals(ran.toString(), "{a=1, b=2, c=1}");
		Assert.assertEquals(recoveries[0], 1, "the page is recovered once");
		Assert.assertEquals(summary, "Summary | passed 3 of 3 | 1 passed on the retry");
		Assert.assertTrue(failuresOf(softAssert).isEmpty(), "no failure should be recorded");
	}

	/** L2: an item that fails twice records ONE failure with its error, and the loop goes on. */
	@Test
	public void itemThatFailsTwiceIsRecordedOnceAndLoopGoesOn() {
		SoftAssert softAssert = new SoftAssert();
		LoopGuard guard = new LoopGuard(softAssert);

		Map<String, Integer> ran = runLoop(guard, items("a", "bad", "c"), (item, attempt) -> {
			if (item.equals("bad")) {
				throw new RuntimeException("Click failed after 3 attempts | element=x | reason=y | page=z");
			}
		});
		String summary = guard.logSummary(3);

		Assert.assertEquals(ran.toString(), "{a=1, bad=2, c=1}");
		Assert.assertEquals(failuresOf(softAssert), items(
				"Failed at bad (also failed on retry) | RuntimeException: Click failed after 3 attempts | element=x | reason=y | page=z"));
		Assert.assertEquals(summary, "Summary | passed 2 of 3 | 1 failed");
		Assert.assertFalse(guard.shouldStop(), "one failed item must not stop the loop");
	}

	/** L3: 5 items in a row that fail twice (not environment errors) stop the loop. */
	@Test
	public void fiveFailuresInARowStopTheLoop() {
		SoftAssert softAssert = new SoftAssert();
		LoopGuard guard = new LoopGuard(softAssert);

		Map<String, Integer> ran = runLoop(guard, items("1", "2", "3", "4", "5", "6", "7"), (item, attempt) -> {
			throw new RuntimeException("element not found");
		});
		String summary = guard.logSummary(7);

		Assert.assertTrue(guard.shouldStop());
		Assert.assertEquals(ran.keySet().toString(), "[1, 2, 3, 4, 5]", "items 6 and 7 are never tried");
		List<String> failures = failuresOf(softAssert);
		Assert.assertEquals(failures.size(), 6, "5 failed items and the abort: " + failures);
		Assert.assertEquals(failures.get(5), "Aborted at 5 | failures in a row: environment=0, element/code=5, remaining items not checked");
		Assert.assertEquals(summary, "Summary | passed 0 of 7 | 5 failed | stopped early");
	}

	/** L4: 3 environment failures in a row stop the loop earlier. */
	@Test
	public void threeEnvironmentFailuresInARowStopTheLoop() {
		SoftAssert softAssert = new SoftAssert();
		LoopGuard guard = new LoopGuard(softAssert);

		Map<String, Integer> ran = runLoop(guard, items("1", "2", "3", "4", "5"), (item, attempt) -> {
			throw new RuntimeException("page.goto: net::ERR_INTERNET_DISCONNECTED at https://www.youtube.com/");
		});
		guard.logSummary(5);

		Assert.assertTrue(guard.shouldStop());
		Assert.assertEquals(ran.keySet().toString(), "[1, 2, 3]");
		List<String> failures = failuresOf(softAssert);
		Assert.assertEquals(failures.get(failures.size() - 1),
				"Aborted at 3 | failures in a row: environment=3, element/code=0, remaining items not checked");
	}

	/** L5: a passing item resets the count, so 4 failures + 1 pass + 4 failures do not abort. */
	@Test
	public void passingItemResetsTheFailureCount() {
		SoftAssert softAssert = new SoftAssert();
		LoopGuard guard = new LoopGuard(softAssert);
		List<String> failing = items("f1", "f2", "f3", "f4", "g1", "g2", "g3", "g4");

		runLoop(guard, items("f1", "f2", "f3", "f4", "ok", "g1", "g2", "g3", "g4", "last"), (item, attempt) -> {
			if (failing.contains(item)) {
				throw new RuntimeException("element not found");
			}
		});
		String summary = guard.logSummary(10);

		Assert.assertFalse(guard.shouldStop());
		List<String> failures = failuresOf(softAssert);
		Assert.assertEquals(failures.size(), 8, "only the 8 failed items: " + failures);
		for (String failure : failures) {
			Assert.assertFalse(failure.startsWith("Aborted"), failure);
		}
		Assert.assertEquals(summary, "Summary | passed 2 of 10 | 8 failed");
	}

	/** L6: a recovery that throws is logged, and the item is still run again. */
	@Test
	public void failingRecoveryIsLoggedAndItemIsStillRetried() {
		SoftAssert softAssert = new SoftAssert();
		LoopGuard guard = new LoopGuard(softAssert);
		logged.clear();

		runLoop(guard, items("a"), (item, attempt) -> {
			if (attempt == 1) {
				throw new RuntimeException("first try fails");
			}
		}, () -> {
			throw new RuntimeException("reload failed too");
		});
		String summary = guard.logSummary(1);

		Assert.assertTrue(logged.stream().anyMatch(line -> line.contains("could not recover the page | RuntimeException: reload failed too")), logged.toString());
		Assert.assertEquals(summary, "Summary | passed 1 of 1 | 1 passed on the retry");
		Assert.assertTrue(failuresOf(softAssert).isEmpty());
	}

	/** L7: counters are kept per item: a retried item counts its label once, an item that fails twice adds nothing. */
	@Test
	public void countersAreKeptPerItem() {
		LoopGuard guard = new LoopGuard(new SoftAssert());

		runLoop(guard, items("a", "bad"), (item, attempt) -> {
			guard.count("detection skipped");
			if (item.equals("a") && attempt == 1) {
				throw new RuntimeException("first try fails");
			}
			if (item.equals("bad")) {
				throw new RuntimeException("always fails");
			}
		});

		Assert.assertEquals(guard.logSummary(2), "Summary | passed 1 of 2 | 1 passed on the retry | 1 failed | detection skipped: 1");
	}
}
