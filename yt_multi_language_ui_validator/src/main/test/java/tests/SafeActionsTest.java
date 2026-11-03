package main.test.java.tests;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.sun.net.httpserver.HttpServer;
import main.java.yt_multi_lang_ui_validator.driverManager.DriverManager;
import main.java.yt_multi_lang_ui_validator.safeActions.SafeActions;

/**
 * Offline checks of {@link SafeActions} on small fake pages served from a local HttpServer (no YouTube, no network).
 * It needs headless Chrome, started through DriverManager like the real tests. The waits are short: explicit.wait is
 * 1 s and element.retry.attempts is 3 (set as system properties, which win over masterdata.properties).
 * Every test has its own path, so the request count of a path shows how often its page was loaded: a refresh is a second request.
 * Run with {@code mvn test -Dsurefire.suiteXmlFiles=src/main/test/java/runners/offline.xml}.
 */
public class SafeActionsTest {

	private static final List<String> PROPERTIES = List.of("explicit.wait", "element.retry.attempts", "browser", "chrome.headless");

	/** A button that counts its clicks in window.clicks. */
	private static final String CLICK_COUNTER = "window.clicks = 0; document.getElementById('b').addEventListener('click', function () { window.clicks++; });";

	private final Map<String, String> pages = new ConcurrentHashMap<>();
	private final Map<String, AtomicInteger> requests = new ConcurrentHashMap<>();
	private HttpServer server;
	private WebDriver driver;

	@BeforeClass
	public void startFakeSiteAndBrowser() throws IOException {
		System.setProperty("explicit.wait", "1");
		System.setProperty("element.retry.attempts", "3");
		System.setProperty("browser", "chrome");
		System.setProperty("chrome.headless", "true");

		server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		server.createContext("/", exchange -> {
			String path = exchange.getRequestURI().getPath();
			requests.computeIfAbsent(path, p -> new AtomicInteger()).incrementAndGet();
			String html = pages.get(path);
			byte[] body = (html == null ? "not found" : html).getBytes(StandardCharsets.UTF_8);
			exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
			exchange.sendResponseHeaders(html == null ? 404 : 200, body.length);
			try (OutputStream out = exchange.getResponseBody()) {
				out.write(body);
			}
		});
		server.start();

		DriverManager.initDriver();
		driver = DriverManager.getDriver();
	}

	@AfterClass(alwaysRun = true)
	public void stopBrowserAndFakeSite() {
		DriverManager.quitDriver();
		if (server != null) {
			server.stop(0);
		}
		PROPERTIES.forEach(System::clearProperty);
	}

	/** Serves the page at its own path and opens it, so the request count of the path starts at 1. */
	private String open(String path, String html) {
		pages.put(path, html);
		requests.remove(path);
		driver.get("http://127.0.0.1:" + server.getAddress().getPort() + path);
		return driver.getCurrentUrl();
	}

	private int requestsOf(String path) {
		AtomicInteger count = requests.get(path);
		return count == null ? 0 : count.get();
	}

	private long clicks() {
		return (Long) ((JavascriptExecutor) driver).executeScript("return window.clicks;");
	}

	/** S1: a button that shows up 1.5 s after the load is clicked on a later attempt, and the page is not reloaded. */
	@Test
	public void lateElementIsClickedWithoutRefresh() {
		open("/s1", "<button id='b' style='display:none'>go</button><script>" + CLICK_COUNTER
				+ "setTimeout(function () { document.getElementById('b').style.display = 'block'; }, 1500);</script>");

		new SafeActions(driver).safeClick(By.id("b"));

		Assert.assertEquals(clicks(), 1, "the button should be clicked once");
		Assert.assertEquals(requestsOf("/s1"), 1, "the page should be loaded once, no refresh");
	}

	/** S2: an element that never shows up: the page is refreshed ONCE (after attempt 2, not after the last attempt). */
	@Test
	public void pageIsRefreshedOnceAfterTheSecondAttemptOnly() {
		open("/s2", "<div>nothing here</div>");

		Assert.assertThrows(RuntimeException.class, () -> new SafeActions(driver).safeFindElement(By.id("nope")));

		Assert.assertEquals(requestsOf("/s2"), 2, "the first load plus one refresh");
	}

	/** S3: a button under a transparent overlay: the native click is blocked, the JS fallback clicks it. */
	@Test
	public void blockedClickFallsBackToJavascriptClick() {
		open("/s3", "<button id='b' style='position:absolute;left:10px;top:10px'>go</button>"
				+ "<div style='position:absolute;left:0;top:0;width:300px;height:200px;z-index:9'></div><script>" + CLICK_COUNTER + "</script>");

		new SafeActions(driver).safeClick(By.id("b"));

		Assert.assertEquals(clicks(), 1, "the JS fallback should have clicked the covered button");
	}

	/** S4: when every attempt fails the error is ONE line with the action, attempts, element, reason and page. */
	@Test
	public void finalErrorIsOneLineWithAllParts() {
		String url = open("/s4", "<div>no button</div>");

		RuntimeException error = Assert.expectThrows(RuntimeException.class, () -> new SafeActions(driver).safeFindElement(By.id("nope")));

		String message = error.getMessage();
		Assert.assertFalse(message.contains("\n"), "the message should be one line: " + message);
		Assert.assertTrue(message.contains("failed after 3 attempts"), message);
		Assert.assertTrue(message.contains("| element="), message);
		Assert.assertTrue(message.contains("| reason="), message);
		Assert.assertTrue(message.contains("| page=" + url), message);
	}

	/** S5: safeFindElements returns only the elements that are displayed and enabled, and throws when none is. */
	@Test
	public void onlyReadyElementsAreReturned() {
		open("/s5", "<button class='e' id='shown'>a</button><button class='e' style='display:none'>b</button>"
				+ "<button class='e' disabled>c</button><button class='none' disabled>d</button><button class='none' style='display:none'>e</button>");
		SafeActions safeActions = new SafeActions(driver);

		List<WebElement> ready = safeActions.safeFindElements(By.cssSelector(".e"));
		Assert.assertEquals(ready.size(), 1, "only the displayed and enabled button is ready");
		Assert.assertEquals(ready.get(0).getDomProperty("id"), "shown");

		RuntimeException error = Assert.expectThrows(RuntimeException.class, () -> safeActions.safeFindElements(By.cssSelector(".none")));
		Assert.assertTrue(error.getMessage().contains("Find elements failed after 3 attempts"), error.getMessage());
	}
}
