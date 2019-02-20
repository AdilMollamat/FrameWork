package com.jpmc.hlt.utils;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.os.WindowsUtils;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.relevantcodes.extentreports.LogStatus;

public class UIControls {
	
	static Logger log = Logger.getLogger(UIControls.class);
	public static WebDriver driver = null;
	public static WebDriverWait wdWait;
	private static Select select;
	public static String ResultsFolderName;
	public static Timestamp timestamp = new Timestamp(System.currentTimeMillis());
	public static String sTimeStamp = timestamp.toString().replaceAll("-", "").replaceAll(":", "_").replaceAll(" ", "_").replaceAll(".", "_");
	public static String projPath = System.getProperty("user.dir");
	public static String scrnshot;
	public static String img;
	public static String applicationName;
	
	public static WebDriver createBrowser(String sbrowserType) throws IllegalAccessException {
		boolean runRemoteDriver = "remote".equalsIgnoreCase(System.getProperty("browser"));
		
		if(runRemoteDriver) {
			log.info("Building remote driver");
			driver = WebDriverFactory.getRemoteDriver();		
		} else {
			log.info("Building local driver");
			driver = WebDriverFactory.getDriver(sbrowserType);
		}
		
		ReportLog.extLog.log(LogStatus.PASS, "Launch Browser - " + sbrowserType);
		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
		maximizeBrowserWindow(runRemoteDriver);
		driver.manage().deleteAllCookies();
		wdWait = new WebDriverWait(driver, 60);
		return driver;
		
	}
	
	private static void maximizeBrowserWindow(boolean isRemoteDriver) {
		if (WindowsUtils.thisIsWindows() && driver instanceof FirefoxDriver && !isRemoteDriver) {
			try {
				Robot firefoxWindowMaximizerRobot = new Robot();
				firefoxWindowMaximizerRobot.keyPress(KeyEvent.VK_WINDOWS);
				firefoxWindowMaximizerRobot.keyPress(KeyEvent.VK_UP);
				firefoxWindowMaximizerRobot.keyRelease(KeyEvent.VK_WINDOWS);
				firefoxWindowMaximizerRobot.keyRelease(KeyEvent.VK_UP);
			} catch (AWTException awte) {
				throw new RuntimeException("Error when attempting to maximize the firefox window: ", awte);
			}
		} else {
			driver.manage().window().maximize();
		}
	}
	
	public static void delay(double d) {
		try {
			Thread.sleep((long)(d * 1000));
		} catch (InterruptedException e) {
			ReportLog.reportAStep(driver, "Fail", "Getting Exeption - " + e, "delay", true);
		}
	}
	
	public static By getByObject(String alias) {
		By by = null;
		String objProp;
		try {
			objProp = ObjectProp.getObjectProp(alias);
			String [] objTypeProp = objProp.split("@@");
			String objType = objTypeProp[0];
			String objProperty = objTypeProp[1];
			switch (objType.toLowerCase()) {
			case "id":
				by = By.id(objProperty);
				break;
			case "name":
				by = By.name(objProperty);
				break;
			case "class":
				by = By.className(objProperty);
				break;
			case "xpath":
				by = By.xpath(objProperty);
				break;
			case "css":
				by = By.cssSelector(objProperty);
				break;
			case "linktext":
				by = By.linkText(objProperty);
				break;
			case "partiallinktext":
				by = By.partialLinkText(objProperty);
				break;	
			}
			return by;
		} catch (Exception e) {
			ReportLog.reportAStep(driver, "Fail", "Getting Exeption for object " + alias, "delay", true);
			return null;
		}
	}
	
	public static void openUrl(String url) {
		driver.navigate().to(url);
		checkIfPageIsReady();
		ReportLog.reportAStep(driver, "Pass", "Launched URL - '" + url + "'.", "URL", true);
		
	}
	
	public static void checkIfPageIsReady() {
		JavascriptExecutor js = (JavascriptExecutor) driver;
		// Initially bellow given if condition will check ready stage of page.
		if (js.executeScript("return document.readyState").toString().equals("complete")) {
			log.info("Page is loaded");
			return;
		}
		try {
			for (int i = 0; i < 25; i++) {
				Thread.sleep(1000);
				// To check page ready state.
				if(js.executeScript("return document.readyState").toString().equals("complete")) {
					break;
				}
			}
		} catch (InterruptedException e) {
			log.error(Level.WARN, e);
			// Restore interrupted state...
			Thread.currentThread().interrupt();
		}
		log.info("Page is loaded.....end of code");
	}
	
	
	public static void refreshBrowser() {
		driver.navigate().refresh();
	}
	
	public static String getPageTitle() {
		String title = driver.getTitle();
		return title;
	}
	
	
	public static List<WebElement> getElements(String alias){
		By by;
		try {
			by = getByObject(alias);
			List<WebElement> eleList = null;
			if (driver != null)
				eleList = driver.findElements(by);
			else
				//ReportLog.reportAStep(driver, "Fail", "Driver object is null in");
			if (eleList.size() == 0) {
				//ReportLog.reportAStep(driver, "Fail", "Driver object is null in");
				log.error("element is null");
			}
			return eleList;
		} catch (Exception e) {
			ReportLog.reportAStep(driver, "Fail", "Getting Exeption for object " + alias + " - " +  e , alias, true);
			return null;
		}
	}
	
	public static WebElement getElement(String alias) {
		List<WebElement> list = getElements(alias);
		if (list.size() ==0) {
			return null;
		} else {
			return list.get(0);
		}
	}
	
	public static void click(String alias, boolean imageCap) {
		WebElement element;
		try {
			element = getElement(alias);
			if (element.isEnabled()) {
				element.click();
				delay(0.5);
				ReportLog.reportAStep(driver, "Pass", "Clicked - '" + alias + "'.", alias, imageCap);
				log.info("Clicked element '" + alias + "'.");
			} else {
				ReportLog.reportAStep(driver, "Fail", "Element not Eanbled with alias - " + alias, alias, true);
				log.error("Element not Enabled with alias - " + alias);
			}
		} catch (NullPointerException nle) {
			ReportLog.reportAStep(driver, "Fail", "Getting Null Pointer Exception for object" + alias + " - " + nle, alias,  true);
		} catch (Exception e) {
			ReportLog.reportAStep(driver, "Fail", "Getting Exception for object" + alias + " - " + e, alias,  true);
		}
	}
	
	
	public static void enterText(String alias, String text, boolean imageCap) {
		WebElement element;
		try {
			element = getElement(alias);
			if (!element.isEnabled()) {
				ReportLog.reportAStep(driver, "Info", "Entered password *******", alias,  imageCap);
				log.info("Entered text '" + text + "' in '");
			} else {
				ReportLog.reportAStep(driver, "Info", "Entered Text - '" + text + "' in - " , alias,  imageCap);
				log.info("Entered text '" + text + "' in '" + alias + "'.");
			}
		} catch (NullPointerException nle) {
			ReportLog.reportAStep(driver, "Fail", "Getting Null Pointer Exception for object" + alias + " - " + nle, alias,  true);
			log.info("Something went wrong when attempting to enter text: ", nle);
		} catch (Exception e) {
			ReportLog.reportAStep(driver, "Fail", "Getting Exception for object" + alias + " - " + e, alias,  true);
			log.info("Something went wrong when attempting to enter text: ", e);
		}
	}
	
	public static void enterEncryptText(String alias, String encryptText) {
		WebElement element;
		try {
			element = getElement(alias);
			if (!element.isEnabled()) {
				ReportLog.reportAStep(driver, "Fail", "Failed to enter text in - " + encryptText + "'in - " + alias, alias,  false);
				log.error("Element - " + alias + " not found");
			} else {
				element.sendKeys(CipherUtils.decrypt(encryptText));
				delay(0.5);
				ReportLog.reportAStep(driver, "Info", "Entered Text - '" + encryptText + "'in - " + alias, alias,  false);
				log.error("Entered Text in " + encryptText + "' in '" + alias + "'.");
			}
		} catch (NullPointerException nle) {
			ReportLog.reportAStep(driver, "Fail", "Getting Null Pointer Exception for object" + alias + " - " + nle, alias,  true);
			//log.info("Something went wrong when attempting to enter text: ", nle);
		} catch (Exception e) {
			ReportLog.reportAStep(driver, "Fail", "Getting Exception for object" + alias + " - " + e, alias,  true);
			//log.info("Something went wrong when attempting to enter text: ", e);
		}
	}
	
	public static void javaScriptEnterText(String alias, String text) {
		WebElement element;
		try {
			element = getElement(alias);
			JavascriptExecutor executor = (JavascriptExecutor) driver;
			executor.executeScript("argument[0].setAttribute('value', '" + text + ",)", element);
			
		} catch (NullPointerException nle) {
			ReportLog.reportAStep(driver, "Fail", "Getting Null Pointer Exception for object" + alias + " - " + nle, alias,  true);
			//log.info("Something went wrong when attempting to enter text: ", nle);
		} catch (Exception e) {
			ReportLog.reportAStep(driver, "Fail", "Getting Exception for object" + alias + " - " + e, alias,  true);
			//log.info("Something went wrong when attempting to enter text: ", e);
		}
	}
	
	public static void javaScriptClick(String alias) {
		WebElement element;
		try {
			element = getElement(alias);
			JavascriptExecutor executor = (JavascriptExecutor) driver;
			executor.executeScript("argument[0].click();", element);
		} catch (NullPointerException nle) {
			ReportLog.reportAStep(driver, "Fail", "Getting Null Pointer Exception for object" + alias + " - " + nle, alias,  true);
			//log.info("Something went wrong when attempting to enter text: ", nle);
		} catch (Exception e) {
			ReportLog.reportAStep(driver, "Fail", "Getting Exception for object" + alias + " - " + e, alias,  true);
			//log.info("Something went wrong when attempting to enter text: ", e);
		}
	}
	
	public static String javaScriptGetTextBoxValue(String alias) {
		WebElement element;
		String sTxt = null;
		try {
			element = getElement(alias);
			JavascriptExecutor executor = (JavascriptExecutor) driver;
			sTxt = (String) executor.executeScript("return argument[0].value", element);
			return sTxt;
		} catch (NullPointerException nle) {
			ReportLog.reportAStep(driver, "Fail", "Getting Null Pointer Exception for object" + alias + " - " + nle, alias,  true);
			//log.info("Something went wrong when attempting to enter text: ", nle);
			return null;
		} catch (Exception e) {
			ReportLog.reportAStep(driver, "Fail", "Getting Exception for object" + alias + " - " + e, alias,  true);
			//log.info("Something went wrong when attempting to enter text: ", e);
			return null;
		}
	}
	
	public static String getTextboxValue(String alias) {
		WebElement element = null;
		try {
			element = getElement(alias);
			return element.getAttribute("value");
		} catch (NullPointerException nle) {
			ReportLog.reportAStep(driver, "Fail", "Getting Null Pointer Exception for object" + alias + " - " + nle, alias,  true);
			//log.info("Something went wrong when attempting to enter text: ", nle);
			return null;
		} catch (Exception e) {
			ReportLog.reportAStep(driver, "Fail", "Getting Exception for object" + alias + " - " + e, alias,  true);
			//log.info("Something went wrong when attempting to enter text: ", e);
			return null;
		}
	}

	
	public static String getElementText(String alias) {
		WebElement element = null;
		try {
			element = getElement(alias);
			return element.getText();
		} catch (NullPointerException nle) {
			ReportLog.reportAStep(driver, "Fail", "Getting Null Pointer Exception for object" + alias + " - " + nle, alias,  true);
			//log.info("Something went wrong when attempting to enter text: ", nle);
			return null;
		} catch (Exception e) {
			ReportLog.reportAStep(driver, "Fail", "Getting Exception for object" + alias + " - " + e, alias,  true);
			//log.info("Something went wrong when attempting to enter text: ", e);
			return null;
		}
	}
	
	public static String getAttribute(String alias, String attVal) {
		WebElement element = null;
		try {
			element = getElement(alias);
			return element.getAttribute(attVal);
		} catch (NullPointerException nle) {
			ReportLog.reportAStep(driver, "Fail", "Getting Null Pointer Exception for object" + alias + " - " + nle, alias,  true);
			//log.info("Something went wrong when attempting to enter text: ", nle);
			return null;
		} catch (Exception e) {
			ReportLog.reportAStep(driver, "Fail", "Getting Exception for object" + alias + " - " + e, alias,  true);
			//log.info("Something went wrong when attempting to enter text: ", e);
			return null;
		}
	}
	
	public static boolean selectListOption(String alias, String value, boolean imageCap) {
		boolean bFlag = false;
		try {
			select = new Select(getElement(alias));
			List<WebElement> state_option = getDropdownListValues(alias);
			for (WebElement option : state_option) {
				if (option.getText().equals(value)) {
					select.selectByVisibleText(value);
					//select.selectByValue(value);
					delay(1);
					//bFlag = true;
					break;
				}
			}
			if (getDropdownSelectedValue(alias).equalsIgnoreCase(value)) {
				log.info("Selected '" + value + "' in " + alias + "' dropdown.");
				ReportLog.reportAStep(driver, "Pass", "Selected Value - '" + value + "' in '" + alias + "' List Box", alias,  imageCap);
				bFlag = true;
			} else {
				log.info(value + " not found " + alias + "' dropdown.");
				//ReportLog.reportAStep(driver, "Pass", "Selected Value - '" + value + "' in '" + alias + "' List Box", alias,  imageCap);
				bFlag = false;
			}
			select = null;
		} catch (NullPointerException nle) {
			ReportLog.reportAStep(driver, "Fail", "Getting Null Pointer Exception for object" + alias + " - " + nle, alias,  true);
			//log.info("Something went wrong when attempting to enter text: ", nle);
			return false;
		} catch (Exception e) {
			ReportLog.reportAStep(driver, "Fail", "Getting Exception for object" + alias + " - " + e, alias,  true);
			//log.info("Something went wrong when attempting to enter text: ", e);
			return false;
		}
		return bFlag;
	}
	
	public static List<WebElement> getDropdownListValues(String alias){
		List<WebElement> allOptions = null;
		try {
			select = new Select(getElement(alias));
			allOptions = select.getOptions();
			return allOptions;
		} catch (NullPointerException nle) {
			ReportLog.reportAStep(driver, "Fail", "Getting Null Pointer Exception for object" + alias + " - " + nle, alias,  true);
			//log.info("Something went wrong when attempting to enter text: ", nle);
			return null;
		} catch (Exception e) {
			ReportLog.reportAStep(driver, "Fail", "Getting Exception for object" + alias + " - " + e, alias,  true);
			//log.info("Something went wrong when attempting to enter text: ", e);
			return null;
		}
	}
	
	public static String getDropdownSelectedValue(String alias) {
		try {
			select = new Select(getElement(alias));
			return select.getFirstSelectedOption().getText();
		} catch (NullPointerException nle) {
			ReportLog.reportAStep(driver, "Fail", "Getting Null Pointer Exception for object" + alias + " - " + nle, alias,  true);
			//log.info("Something went wrong when attempting to enter text: ", nle);
			return null;
		} catch (Exception e) {
			ReportLog.reportAStep(driver, "Fail", "Getting Exception for object" + alias + " - " + e, alias,  true);
			//log.info("Something went wrong when attempting to enter text: ", e);
			return null;
		}
		
	}
	
	public static void waitForPageToLoad(WebDriver driver) {
		driver.manage().timeouts().pageLoadTimeout(80, TimeUnit.SECONDS);
	}
	
	public static boolean verifyObjectExists(String alias) {
		boolean exists = false;
		List<WebElement> elements = null;
		try {
			By by = getByObject(alias);
			elements = driver.findElements(by);
			if (!(elements.size() == 0)) {
				if (elements.get(0).isDisplayed()) {
					exists = true;
					//ReportLog.reportAStep(driver, "Fail", "Getting Exception for object" + alias + " - " + e, alias,  true);
					//log.info("Something went wrong when attempting to enter text: ", e);
				}
			}
			return exists;
		} catch (NullPointerException nle) {
			ReportLog.reportAStep(driver, "Fail", "Getting Null Pointer Exception for object" + alias + " - " + nle, alias,  true);
			//log.info("Something went wrong when attempting to enter text: ", nle);
			return false;
		} catch (Exception e) {
			ReportLog.reportAStep(driver, "Fail", "Getting Exception for object" + alias + " - " + e, alias,  true);
			//log.info("Something went wrong when attempting to enter text: ", e);
			return false;
		}
	}
	
	public static void browserBack() {
		driver.navigate().back();
		waitForPageToLoad(driver);
	}
	
	public static void enterKeys(String alias, String skey) {
		WebElement element;
		try {
			element = getElement(alias);
			switch(skey.toLowerCase()) {
			case "tab":
				element.sendKeys(Keys.TAB);
				//ReportLog.reportAStep(driver, "Fail", "Getting Exception for object" + alias + " - " + e, alias,  true);
				break;
			case "enter":
				element.sendKeys(Keys.ENTER);
				//ReportLog.reportAStep(driver, "Fail", "Getting Exception for object" + alias + " - " + e, alias,  true);
				break;
			}
		} catch (NullPointerException nle) {
			ReportLog.reportAStep(driver, "Fail", "Getting Null Pointer Exception for object" + alias + " - " + nle, alias,  true);
			//log.info("Something went wrong when attempting to enter text: ", nle);
		} catch (Exception e) {
			ReportLog.reportAStep(driver, "Fail", "Getting Exception for object" + alias + " - " + e, alias,  true);
			//log.info("Something went wrong when attempting to enter text: ", e);
		}
	}
	
	public static void closeBrowser() {
		driver.close();
		//ReportLog.reportAStep(driver, "Fail", "Getting Exception for object" + alias + " - " + e, alias,  true);
	}
	
	public static String capturescreenshot(WebDriver driver, String screenShotName) {
		String dest = null;
		try {
			System.currentTimeMillis();
			TakesScreenshot ts = (TakesScreenshot) driver;
			File source = ts.getScreenshotAs(OutputType.FILE);
			dest = System.getProperty("user.dir") + "//Reports//" + ReportLog.Reportfolder + "//Snapshots//"
					+ screenShotName + "_" + System.currentTimeMillis() + ".png";
			log.info("Dest folder" + dest);
			File destination = new File(dest);
			FileUtils.copyFile(source, destination);
			return dest;
		} catch (NullPointerException nle) {
			ReportLog.reportAStep(driver, "Fail", "Getting Null Pointer Exception for object", "Getting Exception for object", true);
			//log.info("Something went wrong when attempting to enter text: ", nle);
			return null;
		} catch (Exception e) {
			ReportLog.reportAStep(driver, "Fail", "Getting Exception for object" , "Getting Exception for object",  true);
			//log.info("Something went wrong when attempting to enter text: ", e);
			return null;
		}
	}
	
	
	public static String createReportFolders() {
		File dir = new File(projPath + "\\Reports\\Report_" + sTimeStamp);
		dir.mkdir();
		ResultsFolderName = dir.getName();
		File snapshots = new File(projPath + "\\Reports\\" + ResultsFolderName + "\\Snapshots");
		snapshots.mkdir();
		return ResultsFolderName;
	}
	
	public static boolean isElementDisplayed(String alias) {
		WebElement element = null;
		try {
			element = getElement(alias);
			return element.isDisplayed();
		} catch (NullPointerException nle) {
			ReportLog.reportAStep(driver, "Fail", "Getting Null Pointer Exception for object" + alias + " - " + nle, alias,  true);
			//log.info("Something went wrong when attempting to enter text: ", nle);
			return false;
		} catch (Exception e) {
			ReportLog.reportAStep(driver, "Fail", "Getting Exception for object" + alias + " - " + e, alias,  true);
			//log.info("Something went wrong when attempting to enter text: ", e);
			return false;
		}
	}
	
	
	public static boolean isElementEnabled(String alias) {
		WebElement element = null;
		try {
			element = getElement(alias);
			return element.isEnabled();
		} catch (NullPointerException nle) {
			ReportLog.reportAStep(driver, "Fail", "Getting Null Pointer Exception for object" + alias + " - " + nle, alias,  true);
			//log.info("Something went wrong when attempting to enter text: ", nle);
			return false;
		} catch (Exception e) {
			ReportLog.reportAStep(driver, "Fail", "Getting Exception for object" + alias + " - " + e, alias,  true);
			//log.info("Something went wrong when attempting to enter text: ", e);
			return false;
		}
	}
	
	public static String getCurrentWindowHandle() {
		return driver.getWindowHandle();
	}
	
	public static void switchToWindow(String windowHandle) {
		driver.switchTo().window(windowHandle);
		//ReportLog.reportAStep(driver, "Fail", "Getting Exception for object" + alias + " - " + e, alias,  true);
	}
	
	public static String switchToChildWindow() {
		String parentWindow = getCurrentWindowHandle();
		Set<String> handles = driver.getWindowHandles();
		
		while (handles.size() <= 1) {
			delay(1);
			handles = driver.getWindowHandles();
		}
		
		for (String windowHandle : handles) {
			if (!windowHandle.equals(parentWindow)) {
				driver.switchTo().window(windowHandle);
				//ReportLog.reportAStep(driver, "Fail", "Getting Exception for object" + alias + " - " + e, alias,  true);
				return windowHandle;
			}
		}
		return parentWindow;
	}
	
	public static String switchToChildWindow(String sTitle) {
		String parentWindow = getCurrentWindowHandle();
		Set<String> handles = driver.getWindowHandles();
		for (String windowHandle : handles) {
			if (!windowHandle.equals(parentWindow)) {
				driver.switchTo().window(windowHandle);
				if (getPageTitle().contains(sTitle)) {
					//ReportLog.reportAStep(driver, "Fail", "Getting Exception for object" + alias + " - " + e, alias,  true);
					return windowHandle;
				}
			}
		}
		return parentWindow;
	}
	
	public static Set<String> getWindowHandles(){
		Set<String> handles = driver.getWindowHandles();
		return handles;
	}
	
	public static void closeChildWindow(String parentWindow) {
		Set<String> handles = driver.getWindowHandles();
		for (String windowHandle : handles) {
			if (!windowHandle.equals(parentWindow)) {
				driver.switchTo().window(windowHandle);
				driver.close();
				delay(1);
				driver.switchTo().window(parentWindow);
				//ReportLog.reportAStep(driver, "Fail", "Getting Exception for object" + alias + " - " + e, alias,  true);
			}
		}
	}
	
	
	public static void switchToDefaultWindow() {
		driver.switchTo().defaultContent();
		//ReportLog.reportAStep(driver, "Fail", "Getting Exception for object" + alias + " - " + e, alias,  true);
	}
	
	public static void waitForElementToBeClickable(String alias) {
		try {
			By by = getByObject(alias);
			wdWait.until(ExpectedConditions.elementToBeClickable(by));
		} catch (NullPointerException nle) {
			ReportLog.reportAStep(driver, "Fail", "Getting Null Pointer Exception for object" + alias + " - " + nle, alias,  true);
			//log.info("Something went wrong when attempting to enter text: ", nle);
		} catch (Exception e) {
			ReportLog.reportAStep(driver, "Fail", "Getting Exception for object" + alias + " - " + e, alias,  true);
			//log.info("Something went wrong when attempting to enter text: ", e);
		}
	}
	
	
	public static void waitForElementToBeEnabled(String alias, int iWaitTime) {
		try {
			WebElement element = getElement(alias);
			if (element.isDisplayed()) {
				int iCount = 1;
				while (!element.isEnabled()) {
					delay(1);
					iCount++;
					if (iCount == iWaitTime) {
						break;
					}
				}
			} else {
				//ReportLog.reportAStep(driver, "Fail", "Getting Exception for object" + alias + " - " + e, alias,  true);
			}
		} catch (NullPointerException nle) {
			ReportLog.reportAStep(driver, "Fail", "Getting Null Pointer Exception for object" + alias + " - " + nle, alias,  true);
			//log.info("Something went wrong when attempting to enter text: ", nle);
		} catch (Exception e) {
			ReportLog.reportAStep(driver, "Fail", "Getting Exception for object" + alias + " - " + e, alias,  true);
			//log.info("Something went wrong when attempting to enter text: ", e);
		}
	}
	
	public static void waitForElementToBeVisible(String alias) {
		try {
			By by = getByObject(alias);
			wdWait.until(ExpectedConditions.presenceOfElementLocated(by));
		} catch (Exception e) {
			ReportLog.reportAStep(driver, "Fail", "Getting Exception for object" + alias + " - " + e, alias,  true);
			//log.info("Something went wrong when attempting to enter text: ", e);
		}
	}
	
	public static void waitForElementToDisappear(String alias) {
		//log.info("Waiting for element to disappear:" + alias);
		try {
			By by = getByObject(alias);
			wdWait.until(ExpectedConditions.invisibilityOfElementLocated(by));
		} catch (Exception e) {
			ReportLog.reportAStep(driver, "Fail", "Getting Exception for object" + alias + " - " + e, alias,  true);
			//log.info("Something went wrong when attempting to enter text: ", e);
			throw new RuntimeException(String.format("Element did not disapppear: [%s]", alias));
		}
	}
	
	public static void selectOption(String alias, Object value) {
		try {
			WebElement element = getElement(alias);
			List<WebElement> options = element.findElements(getTagName("option"));
			select = new Select(element);
			for (WebElement option : options) {
				if (option.isDisplayed()) {
					if (value instanceof String) {
						select.selectByValue((String) value);
						break;
					} else if (value instanceof Integer) {
						select.selectByIndex((Integer) value);
						break;
					}
				}
			}
		} catch (NullPointerException nle) {
			ReportLog.reportAStep(driver, "Fail", "Getting Null Pointer Exception for object" + alias + " - " + nle, alias,  true);
			//log.info("Something went wrong when attempting to enter text: ", nle);
		} catch (Exception e) {
			ReportLog.reportAStep(driver, "Fail", "Getting Exception for object" + alias + " - " + e, alias,  true);
			//log.info("Something went wrong when attempting to enter text: ", e);
		}
	}
	
	public static By getTagName (String Selector) {
		return By.tagName(Selector);
	}
	
	public static String handleAlerts(String alerttype) {
		String txt = null;
		int timer = 0;
		while (!isAlertPresent()) {
			delay(1);
			timer = timer + 1;
			if (timer > 8) {
				break;
			}
		}
		if (isAlertPresent()) {
			switch (alerttype.toLowerCase()) {
			case "accept":
				driver.switchTo().alert().accept();
				break;
			case "dismiss":
				driver.switchTo().alert().dismiss();
				break;
			case "gettext":
				driver.switchTo().alert().getText();
				break;
			default:
				break;
			}
		}
		return txt;
	}
	
	public static boolean isAlertPresent() {
		try {
			driver.switchTo().alert();
			return true;
		} catch (NoAlertPresentException e) {
			return false;
		}
	}
	
	public static void clearTextBoxValue(String alias) {
		WebElement element;
		try {
			element = getElement(alias);
			if (!element.isEnabled()) {
				ReportLog.reportAStep(driver, "Fail", "Getting Null Pointer Exception for object", "Getting Null Pointer Exception for object",  true);
				//log.info("Something went wrong when attempting to enter text: ", nle);
			} else {
				element.clear();
				delay(0.5);
				ReportLog.reportAStep(driver, "Fail", "Getting Null Pointer Exception for object" , "Getting Null Pointer Exception for object",  true);
				//log.info("Something went wrong when attempting to enter text: ", nle);
			}
		} catch (NullPointerException nle) {
			ReportLog.reportAStep(driver, "Fail", "Getting Null Pointer Exception for object" + alias + " - " + nle, alias,  true);
			//log.info("Something went wrong when attempting to enter text: ", nle);
		} catch (Exception e) {
			ReportLog.reportAStep(driver, "Fail", "Getting Exception for object" + alias + " - " + e, alias,  true);
			//log.info("Something went wrong when attempting to enter text: ", e);
		}
	}
	
	
	public static String getPageSource() {
		return driver.getPageSource();
	}
	
	
	public static void clickLinkwithText(String sText) {
		WebElement element;
		try {
			element = driver.findElement(By.linkText(sText));
			if (!element.isEnabled()) {
				//ReportLog.reportAStep(driver, "Fail", "Getting Null Pointer Exception for object" + alias + " - " + nle, alias,  true);
				//log.info("Something went wrong when attempting to enter text: ", nle);
			} else {
				element.click();
			}
		} catch (Exception e) {
			ReportLog.reportAStep(driver, "Fail", "Getting Exception for object" , "Getting Exception for object",  true);
			//log.info("Something went wrong when attempting to enter text: ", e);
		}
	}
	
	public static void mouseHover(String alias) {
		WebElement element;
		Actions action = new Actions(driver);
		try {
			element = getElement(alias);
			if (!element.isDisplayed()) {
				ReportLog.reportAStep(driver, "Fail", "Getting Exception for object" + alias , alias,  true);
				//log.info("Something went wrong when attempting to enter text: ", e);
			} else {
				action.moveToElement(element).perform();
				//ReportLog.reportAStep(driver, "Fail", "Getting Exception for object" + alias + " - " + e, alias,  true);
			} 
				
		} catch (Exception e) {
			ReportLog.reportAStep(driver, "Fail", "Getting Exception for object" + alias + " - " + e, alias,  true);
			//log.info("Something went wrong when attempting to enter text: ", e);
		}
	}
	
	
	public static void switchToFrame(String alias) {
		WebElement element;
		element = getElement(alias);
		driver.switchTo().frame(element);
		//ReportLog.reportAStep(driver, "Fail", "Getting Exception for object" + alias + " - " + e, alias,  true);
	}
	
	
	public static void PageRefresh() {
		String sParentWindowHandle = UIControls.getCurrentWindowHandle();
		UIControls.driver.navigate().refresh();
		
		UIControls.delay(2);
		
		UIControls.driver.switchTo().window(sParentWindowHandle);
	}
	
	public static void simulateKeyEvents(String sKeyEvent) throws AWTException {
		Robot rb = new Robot();
		switch (sKeyEvent.toLowerCase()) {
		case "enter":
		case "\n":
			rb.keyPress(KeyEvent.VK_ENTER);
			rb.keyRelease(KeyEvent.VK_ENTER);
			break;
			
		case "escape":
		case "esc":
			rb.keyPress(KeyEvent.VK_ESCAPE);
			rb.keyRelease(KeyEvent.VK_ESCAPE);
			break;
			
		case "tab":
			rb.keyPress(KeyEvent.VK_TAB);
			rb.keyRelease(KeyEvent.VK_TAB);
			break;
		default:
			//ReportLog.reportAStep(driver, "Fail", "Getting Exception for object" + alias + " - " + sKeyEvent, sKeyEvent,  true);
		}
		//ReportLog.reportAStep(driver, "Fail", "Getting Exception for object" + alias + " - " + sKeyEvent);
	}
	
	
	public static String getData(String mehtod, String value) throws IOException{
		return ExcelUtils.getCellValWithRowColName(ExcelUtils.sTestRegMax, "Data", mehtod, value);
	}
	
	public static String getCredentials(String mehtod, String value) throws IOException{
		return ExcelUtils.getCellValWithRowColName(ExcelUtils.sTestRegMax, "Credentials", mehtod, value);
	}
	
	public static void switchFrameByProfile() {
		if (UIControls.driver.getPageSource().contains("iframe")) {
			UIControls.driver.switchTo().defaultContent();
			UIControls.driver.switchTo().frame("");
			log.info("swithed to frame");
		}
	}
	
	
	
}
