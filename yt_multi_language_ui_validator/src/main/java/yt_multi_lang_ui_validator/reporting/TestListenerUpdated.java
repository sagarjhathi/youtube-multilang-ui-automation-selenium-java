package main.java.yt_multi_lang_ui_validator.reporting;


import org.apache.logging.log4j.ThreadContext; 
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;


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
        Attachers.attachAllLogFolders(ThreadContext.get("testName"));
        Attachers.attachAllImageFolders(ThreadContext.get("testName"));

    }

    @Override
    public void onTestSkipped(ITestResult result) {

        ReportManager.getTest().skip("Test Skipped");
        ReportManager.getTest().fail(result.getThrowable());
        Attachers.attachAllLogFolders(ThreadContext.get("testName"));
        Attachers.attachAllImageFolders(ThreadContext.get("testName"));
    }
    
    @Override
    public void onFinish(ITestContext context) {
        ReportManager.flush();
    }
}

