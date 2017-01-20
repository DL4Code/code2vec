package edu.lu.uni.data.processor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.lu.uni.util.FileHelper;

public class Code2VecDataProcessor {
	
	private static Logger logger = LoggerFactory.getLogger(Code2VecDataProcessor.class);

	private static final String INPUT_FILE_PATH = "outputData/filter/";
	private static final String OUTPUT_FILE_PATH = "outputData/inputDataOfCode2Vec/";
	
	public static void main(String[] args) throws IOException {
		Code2VecDataProcessor processor = new Code2VecDataProcessor();
		
		processor.processData(INPUT_FILE_PATH );
	}

	private void processData(String inputFilePath) throws IOException {
		List<File> files = FileHelper.getAllFiles(inputFilePath, ".list");
		for (File file : files) {
			processData(file);
		}
	}

	private void processData(File file) throws IOException {
		String fileName = file.getName().replace(".list", ".txt");
		String splitString = "";
		int a = 0;
		int b = 0;
		if (file.getParent().contains("filter/labels/")) {
			splitString = ",";
			a = 1;
		} else {
			splitString = ", ";
			a = 2;
			b = 1;
		}
		String content = FileHelper.readFile(file);
		BufferedReader br = new BufferedReader(new StringReader(content));
		String line = null;
		StringBuilder outputData = new StringBuilder();
		
		while ((line = br.readLine()) != null) {
			int indexOfHarshKey = line.indexOf("#");
			
			if (indexOfHarshKey < 0) {
				logger.error("The below raw feature is invalid!\n" + line);
				continue;
			}
			
			List<String> featureVector = Arrays.asList(line.substring(indexOfHarshKey + a, line.length() - b).split(splitString));
			int size = featureVector.size();
			for (int i = 0; i < size - 1; i ++) {
				outputData.append(featureVector.get(i).replace(" ", "") + " ");
			}
			outputData.append(featureVector.get(size - 1).replace(" ", "") + "\n");
		}
		
		FileHelper.outputToFileCover(file.getParent().replace(INPUT_FILE_PATH, OUTPUT_FILE_PATH) + "/" + fileName, outputData);
	}
}
