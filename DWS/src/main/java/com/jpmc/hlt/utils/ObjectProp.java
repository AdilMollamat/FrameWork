package com.jpmc.hlt.utils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import org.apache.log4j.*;

public class ObjectProp {

	static Logger log = Logger.getLogger(ObjectProp.class);
	private static final Properties RUNTIME_PROPERTIES = new Properties();
	
	public static void loadProperties(Path path) {
		try {
			RUNTIME_PROPERTIES.load(Files.newBufferedReader(path));
		} catch (IOException ioe) {
			log.info("IOException:" + ioe);
		}
		log.info("Properties from" + path.getFileName() + " loaded successfully....");
	}
	
	public static void loadProperties(String path) {
		loadProperties(Paths.get(path));
	}
	
	public static String getObjectProp(String key) {
		return RUNTIME_PROPERTIES.getProperty(key);
	}
	
}
