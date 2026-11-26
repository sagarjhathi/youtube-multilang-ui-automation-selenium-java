package main.java.yt_multi_lang_ui_validator.reporting;


import org.apache.logging.log4j.ThreadContext; 
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import main.java.yt_multi_lang_ui_validator.utilities.ScreenshotUtilUpdated;


public class TestListenerUpdated implements ITestListener{

	
	
	@Override
    public void onTestStart(ITestResult result) {

        ReportManager.createTest(ThreadContext.get("testName"));
    }
    

	
    @Override
    public void onTestSuccess(ITestResult result) {

        ReportManager.getTest().pass("Test Passed");
        Attachers.attachAllLogFolders(ThreadContext.get("testName"));
        Attachers.attachAllImageFolders(ThreadContext.get("testName"));
    }

    @Override
    public void onTestFailure(ITestResult result) {

        ReportManager.getTest().fail(result.getThrowable());
        captureIfFailedOutsideTheLoops(result);
        Attachers.attachAllLogFolders(ThreadContext.get("testName"));
        Attachers.attachAllImageFolders(ThreadContext.get("testName"));

    }

    /**
     * A test that ends with an error other than a failed assertion broke outside the loops (page did not open, list not found),
     * so no loop took a picture. This runs before @AfterMethod quits the browser (checked with TestNG 7.10.2).
     * Failed assertions are skipped: the loops already took a picture for every failed check.
     */
    private void captureIfFailedOutsideTheLoops(ITestResult result) {
        if (result.getThrowable() != null && !(result.getThrowable() instanceof AssertionError)) {
            ScreenshotUtilUpdated.capture("test-failed", "page", 0);
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {

        ReportManager.getTest().skip("Test Skipped");
        ReportManager.getTest().fail(result.getThrowable());
        captureIfFailedOutsideTheLoops(result); // TestNG reports the first try of a retried test as skipped
        Attachers.attachAllLogFolders(ThreadContext.get("testName"));
        Attachers.attachAllImageFolders(ThreadContext.get("testName"));
    }

    @Override
    public void onFinish(ITestContext context) {
        ReportManager.flush();
    }
}

