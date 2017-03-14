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

	private static final String INPUT_FILE_PATH = "inputData/selected_data/";
	private static final String OUTPUT_FILE_PATH = "outputData/inputDataOfCode2Vec/";
	
	public static void main(String[] args) throws IOException {
		List<File> files = FileHelper.getAllFiles(INPUT_FILE_PATH, ".list");
		for (File file : files) {
			Code2VecDataProcessor processor = new Code2VecDataProcessor(file, INPUT_FILE_PATH, OUTPUT_FILE_PATH);
			processor.processData();
		}
	}

	private File file;
	private String inputFilePath;
	private String outputFilePath;

	public Code2VecDataProcessor(File file, String inputFilePath, String outputFilePath) {
		super();
		this.file = file;
		this.inputFilePath = inputFilePath;
		this.outputFilePath = outputFilePath;
	}

	public void processData() throws IOException {
		String fileName = file.getPath().replace(inputFilePath, outputFilePath);
		String splitString = ", ";
		int a = 2;
		int b = 1;
//		if (file.getParent().contains("/method_name/")) {
//			splitString = ",";
//			a = 1;
//		} else {
//			splitString = ", ";
//			a = 2;
//			b = 1;
//		}
		String content = FileHelper.readFile(file);
		BufferedReader br = new BufferedReader(new StringReader(content));
		String line = null;
		StringBuilder outputData = new StringBuilder();
		int counter = 0;
		while ((line = br.readLine()) != null) {
			int indexOfHarshKey = line.indexOf("#");
			
			if (indexOfHarshKey < 0) {
				logger.error("The below raw feature is invalid!\n" + line);
				continue;
			}
			
			List<String> featureVector = Arrays.asList(line.substring(indexOfHarshKey + a, line.length() - b).split(splitString));
			int size = featureVector.size();
			for (int i = 0; i < size - 1; i ++) {
				outputData.append(featureVector.get(i).replaceAll(" ", "") + " ");
			}
			outputData.append(featureVector.get(size - 1).replaceAll(" ", "") + "\n");
			
			counter ++;
			if (counter % 1000 == 0) {
				FileHelper.outputToFile(fileName, outputData);
				outputData.setLength(0);
			}
		}
		
//		if (counter % 1000 != 0) {
//			FileHelper.outputToFile(fileName, outputData);
//		}
	}
}
