package com.soundcenter.soundcenter.client.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigProperties extends Properties{

	private File file;

	public ConfigProperties(File file) {
		this.file = file;
	}

	public void load() throws FileNotFoundException, IOException {
		if (file.exists()) {
			load(new FileInputStream(file));
		}
	}

	public void save(String start) throws FileNotFoundException, IOException {
		if (!file.exists()) {
			file.getParentFile().mkdirs();
			file.createNewFile();
		}
		store(new FileOutputStream(file), start);
	}

	public void addInteger(String key, int value) {
		addString(key, String.valueOf(value));
	}
	
	public int getInteger(String key, int value) {
		if (containsKey(key)) {
			try {
				return Integer.parseInt(String.valueOf(getProperty(key)));
			} catch (NumberFormatException e) {}
		}
		return value;
	}
	
	public void addString(String key, String value) {
		put(key, String.valueOf(value));
	}
	
	public String getString(String key, String value) {
		if (containsKey(key)) {
			return String.valueOf(getProperty(key));
		}
		return value;
	}

	public String getStringAndReplaceKey(String oldKey, String newKey, String value) {
		if (containsKey(newKey)) {
			return String.valueOf(getProperty(newKey));
		} else if(containsKey(oldKey)) {
			value = String.valueOf(getProperty(oldKey));
			remove(oldKey);
			put(newKey, String.valueOf(value));
		}
		return value;
	}
	
	public void addBoolean(String key, boolean value) {
		put(key, String.valueOf(value));
	}
	
	public Boolean getBoolean(String key, boolean value) {
		if (containsKey(key)) {
			return Boolean.valueOf(getProperty(key));
		}
		return value;
	}
	
}
