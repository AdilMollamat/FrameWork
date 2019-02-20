package com.jpmc.hlt.utils;

import java.io.File;

import org.apache.log4j.*;
import org.jsoup.helper.DescendableLinkedList;
import org.openqa.selenium.WebDriver;

import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;

public class ReportLog {
	static Logger log = Logger.getLogger(ReportLog.class);
	public static ExtentReports report;
	public static ExtentTest extLog;
	public static String Reportfolder;
	
	private static final boolean REALLY_CAPTURE_SCREENSHOTS = false;
	
	public static void CreateHtmlReport() {
		log.info("TimeStamp: " + CommonInit.sTimeStamp);
		
		report = new ExtentReports(CommonInit.projPath + "\\Reports\\" + ReportLog.Reportfolder
				+ "\\DW automation Report_" + CommonInit.sTimeStamp + ".html");
		report.loadConfig(new File(CommonInit.projPath + "\\extent-config.xml"));
	}
	
	public static void StartTestCase(String sTestCaseName) {
		extLog = report.startTest(sTestCaseName);
	}
	
	public static void EndTestCase() {
		report.endTest(extLog);
		report.flush();
	}
	
	public static void reportAStep(WebDriver driver, String status, String desc, String imageName, boolean imgCapture) {
		String image = null;
		if (imgCapture && REALLY_CAPTURE_SCREENSHOTS) {
			String scrnshot = UIControls.capturescreenshot(driver, imageName);
			image = extLog.addScreenCapture(scrnshot);
		}
		if (imgCapture && REALLY_CAPTURE_SCREENSHOTS) {
			switch (status.toLowerCase()) {
			case "pass":
				extLog.log(LogStatus.PASS, desc + image);
				break;
			case "fail":
				extLog.log(LogStatus.FAIL, desc + image);
				break;
			case "warning":
				extLog.log(LogStatus.WARNING, desc + image);
				break;
			case "info":
				extLog.log(LogStatus.INFO, desc + image);
				break;
			}
		} else if (!imgCapture) {
			switch (status.toLowerCase()) {
			case "pass":
				extLog.log(LogStatus.PASS, desc);
				break;
			case "fail":
				extLog.log(LogStatus.FAIL, desc);
				break;
			case "warning":
				extLog.log(LogStatus.WARNING, desc);
				break;
			case "info":
				extLog.log(LogStatus.INFO, desc);
				break;
			}
		}
		
	}
	
}
