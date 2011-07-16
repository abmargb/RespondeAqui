package com.merespondeaqui;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Configuration {

	private static final String PROPERTIES_FILE = "conf.properties";
	private static Configuration instance;
	
	private Properties properties;
	
	private Configuration() {
		this.properties = new Properties();
		try {
			properties.load(new FileInputStream(PROPERTIES_FILE));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	public static Configuration getInstance() {
		if (instance == null) {
			instance = new Configuration();
		}
		return instance;
	}
	
	public String getProperty(String key) {
		return properties.getProperty(key);
	}
	
}
