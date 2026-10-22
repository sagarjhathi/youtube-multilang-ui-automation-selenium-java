package main.java.yt_multi_lang_ui_validator.utilities;

import java.io.File; 

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;


import main.java.yt_multi_lang_ui_validator.config.ConfigManager;
import main.java.yt_multi_lang_ui_validator.driverManager.DriverManager;
import main.java.yt_multi_lang_ui_validator.logger.LoggerUtility;
import main.java.yt_multi_lang_ui_validator.pathManager.PathManager;

public class ScreenshotUtilUpdated {

	
	
	
	private  final static Logger log = LoggerUtility.getLogger(ScreenshotUtilUpdated.class);

	
	WebDriver driver;
	
	 public static String capture(String... names) {
	        WebDriver driver = DriverManager.getDriver(); // ✅ get from ThreadLocal
	     //   String inputNames=String.join("_", names);
	        try {
	
	        	String testName = ThreadContext.get("testName");

	        	String screenShotPath = PathManager.getScreenshotPath(testName);

	        	// create folder
	        	File folder = new File(screenShotPath);
	        	folder.mkdirs();
	
	        	String finalPath= screenShotPath + File.separator + ThreadContext.get("testNameShort")+System.nanoTime()+".png";
	        		System.out.println(finalPath+"   -->> final path if param exist");
	        	
	
	        	// capture
	        	File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
	       
				 if(ConfigManager.getBoolean("compressImage", false)) {
	                  	try{
	                  		String imageQuality=ConfigManager.get("imageCompressionQuality");
	                  		System.out.println(imageQuality);
	                  		
	                  		double parseImageQuality=Double.parseDouble(imageQuality);
	                  		System.out.println(parseImageQuality);
	                  		ImageCompressor.compressImage(src, finalPath,parseImageQuality);
	                  	  }catch(Exception e) {
	                  		  log.warn("[{}] compression quality / compressImage method failed, handling it the default way[No compression].", ThreadContext.get("testName"));
	                  		  FileUtils.copyFile(src, new File(finalPath));
	                  	  }
	                	  
	                  }else {
	                	  log.warn("[{}] handling it the default way[No compression].", ThreadContext.get("testName"));
	            		  FileUtils.copyFile(src, new File(finalPath));
	                  }


	        	return finalPath;

	        } catch (Exception e) {
	            System.out.println("Screenshot failed: " + e.getMessage()+ThreadContext.get("testName"));
	            return null;
	        }
	        
	    }
}
