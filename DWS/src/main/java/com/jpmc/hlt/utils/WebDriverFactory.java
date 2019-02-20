package com.jpmc.hlt.utils;

import java.io.File;
import java.lang.ref.PhantomReference;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.GeckoDriverService;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerDriverService;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.os.WindowsUtils;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

public class WebDriverFactory {
	static Logger log = Logger.getLogger(WebDriverFactory.class);
	private static final String DRIVER_EXE_PATH = "./lib/";
	private static final String IE_DRIVER_EXE_NAME = "IEDriverSever-2.45.0.exe";
	private static final String GECKO_DRIVER_EXE_NAME = "geckodriver_0.18.exe";
	private static final String CHROME_DRIVER_EXE_NAME = "chromedriver-2.40.exe";
	private static final String PHANTOMJS_DRIVER_EXE_NAME = WindowsUtils.thisIsWindows()?"phantomjs_2.1.1.exe"
			:"phantomjs-linux_2.1.1";
	private static final String SELENIUM_GRID_HUB_URL = System.getProperty("hubURL");
	private static final String WINDOWS_KILL_PROCESS = "taskkill /F /IM";
	private static BrowserType currentBrowser;
	
	
	private static final Thread QUIT_DRIVER_ON_EXIT = new Thread() {
		
		@Override
		public void run() {
			
			if(WindowsUtils.thisIsWindows() && !"remote".equalsIgnoreCase(System.getProperty("browser"))) {
				try {
					log.info("Killing Local Driver...");
					//SSLoanCondition.driver.close();
					switch (currentBrowser) {
					case INTERNET_EXPLORER:
						Runtime.getRuntime().exec(WINDOWS_KILL_PROCESS + IE_DRIVER_EXE_NAME);
						break;
					case FIREFOX:
						Runtime.getRuntime().exec(WINDOWS_KILL_PROCESS + GECKO_DRIVER_EXE_NAME);
						break;
					case CHROME:
						Runtime.getRuntime().exec(WINDOWS_KILL_PROCESS + CHROME_DRIVER_EXE_NAME);
						break;
					case PHANTOMJS:
						Runtime.getRuntime().exec(WINDOWS_KILL_PROCESS + PHANTOMJS_DRIVER_EXE_NAME);
						break;
					default:
						log.error("Invalid Browser, '" + currentBrowser.name() + "'");
						break;
						
					}
					
					
				} catch (Exception e) {
					UIControls.driver.close();
				}
				
			} else {
				log.info("Quitting remote driver...");
				UIControls.driver.quit();
			}
			
			
		}
		
		
		
	};
	
	static {
		log.info("Triggered: 'Runtime.getRuntime().addShutdownHook(QUIT_DRIVER_ON_EXIT)'");
		Runtime.getRuntime().addShutdownHook(QUIT_DRIVER_ON_EXIT);
	}
	
	public static WebDriver getDriver(String browserType) throws IllegalAccessException {
		return getDriver(BrowserType.get(browserType));
	}
	
	public static WebDriver getDriver(BrowserType browserType) throws IllegalAccessException {
		currentBrowser = browserType;
		Capabilities browserCapabilities = getWebDriverCapabilities(browserType);
		
		switch (browserType) {
		case INTERNET_EXPLORER:
			return new InternetExplorerDriver(new InternetExplorerOptions(browserCapabilities));
		case FIREFOX:
			return new FirefoxDriver(new FirefoxOptions(browserCapabilities));
		case CHROME:
			return new ChromeDriver(new ChromeOptions().merge(browserCapabilities));
		case PHANTOMJS:
			//return new PhantomJSDriver(browserCapabilities);
		case REMOTE:
			return getRemoteDriver();
		default:
			throw new IllegalAccessException("Bad browserType specified: " + browserType);
		}
	}
	
	public static WebDriver getRemoteDriver() {
		BrowserType browserType = BrowserType.get(System.getProperty("remoteBrowserType"));
		
		if (browserType == BrowserType.PHANTOMJS) {
			//return new phantomJSDriver(getWebDriverCapabilities(browserType));    this is the correct line
			return null;
		} else {
			try {
				return new RemoteWebDriver(new URL(SELENIUM_GRID_HUB_URL), getWebDriverCapabilities(browserType));
			} catch (MalformedURLException murle) {
				throw new RuntimeException(
						"Unable to create a valid URL with given URL \"" + SELENIUM_GRID_HUB_URL + "\"", murle);
			}
		}
		
	}
	
	private static Capabilities getWebDriverCapabilities(BrowserType browserType) {
		switch (browserType) {
		case INTERNET_EXPLORER:
			System.setProperty(InternetExplorerDriverService.IE_DRIVER_EXE_PROPERTY, DRIVER_EXE_PATH + IE_DRIVER_EXE_NAME);
			InternetExplorerOptions ieOptions = new InternetExplorerOptions().ignoreZoomSettings()
					.introduceFlakinessByIgnoringSecurityDomains().destructivelyEnsureCleanSession()
					.withInitialBrowserUrl(""); //optional
			
			ieOptions.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
			ieOptions.setCapability(CapabilityType.HAS_NATIVE_EVENTS, true);
			ieOptions.setCapability(CapabilityType.PAGE_LOAD_STRATEGY, "none");
			ieOptions.setCapability(CapabilityType.SUPPORTS_FINDING_BY_CSS, true);
			ieOptions.setCapability(InternetExplorerDriver.IE_ENSURE_CLEAN_SESSION, true);
			return ieOptions;
			
		case FIREFOX:
			System.setProperty(GeckoDriverService.GECKO_DRIVER_EXE_PROPERTY, DRIVER_EXE_PATH + GECKO_DRIVER_EXE_NAME);
			FirefoxOptions firefoxOptions = new FirefoxOptions().addPreference("extensions.firebug.showFirstRunPage", false);
			return firefoxOptions;
			
			
		case CHROME:
			System.setProperty(GeckoDriverService.GECKO_DRIVER_EXE_PROPERTY, DRIVER_EXE_PATH + CHROME_DRIVER_EXE_NAME);
			ChromeOptions chromeOptions = new ChromeOptions().setExperimentalOption("useAutomationExtension", false);
			return chromeOptions;
		case PHANTOMJS:
			DesiredCapabilities phantomJsCapabilities = new DesiredCapabilities();
			File phantomJsDriverFile = new File(DRIVER_EXE_PATH + PHANTOMJS_DRIVER_EXE_NAME);
			if (!WindowsUtils.thisIsWindows()) {
				phantomJsDriverFile.setExecutable(true);
			}
			
//			phantomJsCapabilities.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, 
//					phantomJsDriverFile.getAbsolutePath());
//			phantomJsCapabilities.setJavascriptEnabled(true);
//			phantomJsCapabilities.setCapability("takeScreenshot", true);
//			phantomJsCapabilities.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, 
//					new String[] {"--ssl-protocol=any"});
			return phantomJsCapabilities;
			
		case REMOTE:
			//return getRemoteDriver();
		default:
			throw new IllegalArgumentException("Bad browser type specified: " + browserType);
		}
	}
	
}
