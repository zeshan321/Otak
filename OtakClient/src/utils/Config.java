package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

public class Config {

	private File file;
	private HashMap<String, Object> map = new HashMap<String, Object>();

	public Config() {
		try {
			String fileName = "otakclient.properties";

			file = new File(fileName);
			
			// Create file if it doesn't exist
			file.createNewFile();

			// Load values into map
			try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			    for (String line; (line = br.readLine()) != null;) {
			        String[] seperate = line.split(" = ", 2);
			        map.put(seperate[0], seperate[1]);
			    }
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void set(String key, Object value) {
		map.put(key, value);
	}
	
	public void remove(String key) {
		map.remove(key);
	}
	
	public boolean contains(String key) {
		return map.containsKey(key);
	}

	public String getString(String key) {
		return String.valueOf(map.get(key));
	}

	public boolean getBoolean(String key) {
		return Boolean.valueOf(getString(key));
	}

	public int getInt(String key) {
		return Integer.valueOf(getString(key));
	}
	
	public void save() {
		try {
			PrintWriter printWriter = new PrintWriter(file);
			
			for (String key: map.keySet()) {
				printWriter.println(key + " = " + map.get(key));
			}
			
			printWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
