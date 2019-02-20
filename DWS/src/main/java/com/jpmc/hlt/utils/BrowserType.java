package com.jpmc.hlt.utils;

import java.util.List;
import java.util.Locale;

import edu.emory.mathcs.backport.java.util.Arrays;

public enum BrowserType {
	FIREFOX("firefox", "ff"),
	CHROME("chrome"),
	INTERNET_EXPLORER("internet explorer", "internet_explorer", "internetexplorer", "ie"),
	PHANTOMJS("phantomjs", "phantom_js", "phantomjsdriver"),
	REMOTE("remote", "remotewebdriver");
	
	private static final BrowserType[] BROWSER_TYPES = values();
	
	private final List<String> browserAliases;
	BrowserType(String...browserAliases) {
		this.browserAliases = Arrays.asList(browserAliases);
	}
	
	public static BrowserType get(String name) {
		for (BrowserType browserType : BROWSER_TYPES) {
			if (browserType.browserAliases.contains(name.toLowerCase(Locale.ROOT))) {
				return browserType;
			}
		}
		throw new IllegalArgumentException("Unrecognized Browser Type \"" + name + "\"");
	}
	
}
