package main.test.java.tests;

import java.util.ArrayList;
import java.util.List;
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
import main.java.yt_multi_lang_ui_validator.utilities.Canary;

/**
 * Offline checks (no browser) of {@link Canary}. The mode is passed in, so the environment is not touched.
 * Run with {@code mvn test -Dsurefire.suiteXmlFiles=src/main/test/java/runners/offline.xml}.
 */
public class CanaryTest {

	private final List<String> logged = new ArrayList<>();
	private Logger canaryLogger;
	private AbstractAppender collector;

	@BeforeClass
	public void collectCanaryLog() {
		canaryLogger = (Logger) LogManager.getLogger(Canary.class);
		canaryLogger.setLevel(Level.INFO);
		collector = new AbstractAppender("canary-test", null, null, true, Property.EMPTY_ARRAY) {
			@Override
			public void append(LogEvent event) {
				logged.add(event.getMessage().getFormattedMessage());
			}
		};
		collector.start();
		canaryLogger.addAppender(collector);
	}

	@AfterClass(alwaysRun = true)
	public void stopCollecting() {
		canaryLogger.removeAppender(collector);
		collector.stop();
	}

	@Test
	public void canaryOffReturnsTheValueAndLogsNothing() {
		logged.clear();
		Assert.assertEquals(Canary.canary("abc", 3, 3, null, false), "abc");
		Assert.assertTrue(logged.isEmpty(), "nothing should be logged when canary is off: " + logged);
	}

	@Test
	public void canaryOnSpoilsTheSpoiledRow() {
		logged.clear();
		Assert.assertEquals(Canary.canary("abc", 3, 3, null, true), "abc CANARY");
		Assert.assertEquals(logged.size(), 1, "one line should be logged: " + logged);
		Assert.assertTrue(logged.get(0).contains("CANARY | row 3 | 'abc' spoiled on purpose to 'abc CANARY', this check has to fail"), logged.get(0));
	}

	@Test
	public void canaryOnLeavesOtherRowsAlone() {
		logged.clear();
		Assert.assertEquals(Canary.canary("abc", 2, 3, null, true), "abc");
		Assert.assertTrue(logged.isEmpty(), "nothing should be logged for another row: " + logged);
	}

	@Test
	public void customSpoiledValueIsUsedWhenGiven() {
		Assert.assertEquals(Canary.canary("abc", 3, 3, "xyz", true), "xyz");
	}
}
