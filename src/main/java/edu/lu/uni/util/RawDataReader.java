package edu.lu.uni.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RawDataReader {

	/**
	 * Read the content of files of original features.
	 * 
	 * @param filePath, the file path of original features.
	 * @return String: file name, List<String>: file content.
	 * @throws IOException
	 */
	public Map<String, List<String>> readRawFeaturesFromFiles(String filePath, String fileType) throws IOException {
		List<File> files = FileHelper.getAllFiles(filePath, fileType);
		Map<String, List<String>> features = new HashMap<>();
		
		for (File file : files) {
			features.put(file.getName(), readRawFeaturesFromFile(file));
		}
		
		return features;
	}
	
	public List<String> readRawFeaturesFromFile(File file) throws IOException {
		List<String> features = new ArrayList<>();
		String fileContent = FileHelper.readFile(file);
		BufferedReader br = new BufferedReader(new StringReader(fileContent));
		
		String feature = null;
		
		while ((feature = br.readLine()) != null) {
			features.add(feature);
		}
		
		return features;
	}
}
