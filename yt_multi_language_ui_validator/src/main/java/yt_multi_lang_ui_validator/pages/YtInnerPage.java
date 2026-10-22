package main.java.yt_multi_lang_ui_validator.pages;

import org.openqa.selenium.By;

import main.java.yt_multi_lang_ui_validator.base.BasePage;

public class YtInnerPage  extends BasePage{


	public By youtuberMainInfoInnerPage=By.xpath("//div[@id='above-the-fold']//div[@id='top-row']");
	
	By settingButtonUnderVideoPlayerInnerPage=By.xpath("//button[@class='ytp-button ytp-settings-button']");
	
}
