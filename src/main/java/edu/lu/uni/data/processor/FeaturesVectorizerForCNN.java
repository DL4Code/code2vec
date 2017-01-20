package edu.lu.uni.data.processor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.lu.uni.util.FileHelper;

public class FeaturesVectorizerForCNN {
	
	private static Logger logger = LoggerFactory.getLogger(FeaturesVectorizerForCNN.class);

	private static final String ENCODED_FILE_PATH = "outputData/outputDataOfCode2Vec/";
	private static final String FEATURE_FILE_PATH = "outputData/filter/";
	private static final String OUTPUT_FILE_PATH = "outputData/encoder/";
		
	public static void main(String[] args) throws IOException {
		FileHelper.deleteDirectory(OUTPUT_FILE_PATH);
		
		FeaturesVectorizerForCNN fv = new FeaturesVectorizerForCNN();
		fv.vectorizeFeatures(ENCODED_FILE_PATH,FEATURE_FILE_PATH, OUTPUT_FILE_PATH);
	}

	public void vectorizeFeatures(String encodedFilePath, String featureFilePath, String outputFilePath) throws IOException {
		List<File> encodedFiles = FileHelper.getAllFiles(encodedFilePath, ".txt");
		List<File> featureFiles = FileHelper.getAllFiles(featureFilePath, ".list");
		
		for (File encodedFile : encodedFiles) {
			for (File featureFile : featureFiles) {
				if (isMatched(encodedFile, featureFile)) {
					vectorizeFeatures(encodedFile, featureFile, outputFilePath);
					break;
				}
			}
		}
	}

	public void vectorizeFeatures(File encodedFile, File featureFile, String outputFilePath) throws IOException {
		Map<String, List<String>> featuresVectors = getFeaturesVectors(encodedFile);
		String outputFileName = encodedFile.getPath();
		String splitStr = "";
		int a = 0;
		int b = 0;
		int sizeOfZeroVector = 0;
		if (outputFileName.contains("/features/")) {
			splitStr = ", ";
			a = 2;
			b = 1;
			sizeOfZeroVector = 80;
		} else if (outputFileName.contains("/labels/")) {
			splitStr = ",";
			a = 1;
			sizeOfZeroVector = 10;
		}
		outputFileName = outputFileName.replace(ENCODED_FILE_PATH, outputFilePath);
		String outputCSVFileName = outputFileName.replace(".txt", ".csv");
		
		int maxSize = Integer.parseInt(outputFileName.substring(outputFileName.toUpperCase().lastIndexOf("SIZE=") + "SIZE=".length(), outputFileName.lastIndexOf(".txt")));
		List<String> zeroList = new ArrayList<String>();
		for (int i = 0; i < sizeOfZeroVector; i ++) {
			zeroList.add("0");
		}
		
		String rawFeatures = FileHelper.readFile(featureFile);
		BufferedReader br = new BufferedReader(new StringReader(rawFeatures));
		String rawFeature = null;
		StringBuilder outputData = new StringBuilder();
		StringBuilder outputCSVData = new StringBuilder();
		int index = 0;
		
		while ((rawFeature = br.readLine()) != null) {
			int indexOfHarshKey = rawFeature.indexOf("#");
			
			if (indexOfHarshKey < 0) {
				logger.error("The below raw feature is invalid!\n" + rawFeature);
				continue;
			}
			
			String dataKey = rawFeature.substring(0, indexOfHarshKey + 1);
			String dataVector = rawFeature.substring(indexOfHarshKey + a, rawFeature.length() - b);
			List<String> rawFeatureVector = Arrays.asList(dataVector.split(splitStr));
			int size = rawFeatureVector.size();
			List<String> outputDataVector = new ArrayList<String>();
			
			outputDataVector.add(dataKey);
			for (int i = 0; i < size; i ++) {
				String featureElement = rawFeatureVector.get(i).replace(" ", "");
				if (featuresVectors.containsKey(featureElement)) {
					List<String> featureVector = featuresVectors.get(featureElement);
					outputDataVector.addAll(featureVector);
				} else {
					logger.error("The feature cannot be found: " + featureElement);
				}
			}
			
			for (int i = size; i < maxSize; i ++) {
				outputDataVector.addAll(zeroList);
			}
			outputData.append(outputDataVector.toString().replace("[", "").replace("]", "") + "\n");
			outputDataVector.remove(0);
			outputCSVData.append(outputDataVector.toString().replace("[", "").replace("]", "") + "\n");
			
			index ++;
			if (index % 1000 == 0) {
				FileHelper.outputToFile(outputFileName, outputData);
				FileHelper.outputToFile(outputCSVFileName, outputCSVData);
				outputData.setLength(0);
				outputCSVData.setLength(0);
			}
		}
		
		if (outputData.length() > 0) {
			FileHelper.outputToFile(outputFileName, outputData);
			FileHelper.outputToFile(outputCSVFileName, outputCSVData);
		}
	}

	private Map<String, List<String>> getFeaturesVectors(File encodedFile) throws IOException {
		Map<String, List<String>> featuresVectors = new HashMap<>();
		
		String content = FileHelper.readFile(encodedFile);
		BufferedReader br = new BufferedReader(new StringReader(content));
		String line = null;
		
		while ((line = br.readLine()) != null) {
			String[] vectorLine = line.split(" ");
			String key = "";
			List<String> value = new ArrayList<>();
			
			for (int i = 0, length = vectorLine.length; i < length; i ++) {
				if (i == 0) {
					key = vectorLine[i];
				} else {
					value.add(vectorLine[i]);
				}
			}
			
			featuresVectors.put(key, value);
		}
		
		return featuresVectors;
	}

	private boolean isMatched(File encodedFile, File featureFile) {
		String encodedFileName = encodedFile.getName();
		String featureFileName = featureFile.getName();
		featureFileName = featureFileName.substring(0, featureFileName.lastIndexOf(".list"));
		
		String encodedFilePath = encodedFile.getPath();
		String featureFilePath = featureFile.getPath();
		
		if (encodedFileName.contains(featureFileName)) {
			if ((encodedFilePath.contains("/features/") && featureFilePath.contains("/features/"))) {
				return true;
			}
			if ((encodedFilePath.contains("/RAW_CAMEL_TOKENIATION/") && featureFilePath.contains("/RAW_CAMEL_TOKENIATION/"))) {
				return true;
			}
			if ((encodedFilePath.contains("/SIMPLIFIED_NLP/") && featureFilePath.contains("/SIMPLIFIED_NLP/"))) {
				return true;
			}
			if ((encodedFilePath.contains("/TOKENAZATION_WITH_NLP/") && featureFilePath.contains("/TOKENAZATION_WITH_NLP/"))) {
				return true;
			}
		}
		
		return false;
	}
}
