package edu.lu.uni.data.processor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import edu.lu.uni.util.FileHelper;

public class FeaturesVectorizer {
	
	private String inputFilePath1;  // encodedFilePath
	private String inputFilePath2;  // rawTokenFilePath
	private String outputFilePath;  // encodedFilePath
	private int sizeOfZeroVector;
	private String inputFileExtension;
	private String outputFileExtension;
	
	public void setInputFilePath1(String inputFilePath1) {
		this.inputFilePath1 = inputFilePath1;
	}

	public void setInputFilePath2(String inputFilePath2) {
		this.inputFilePath2 = inputFilePath2;
	}

	public void setOutputFilePath(String outputFilePath) {
		this.outputFilePath = outputFilePath;
	}

	public void setSizeOfZeroVector(int sizeOfZeroVector) {
		this.sizeOfZeroVector = sizeOfZeroVector;
	}

	public void setInputFileExtension(String inputFileExtension) {
		this.inputFileExtension = inputFileExtension;
	}

	public void setOutputFileExtension(String outputFileExtension) {
		this.outputFileExtension = outputFileExtension;
	}

	public void vectorizeFeatures() throws IOException {
		List<File> encodedFiles = FileHelper.getAllFiles(inputFilePath1, inputFileExtension);
		List<File> rawTokenFiles = FileHelper.getAllFiles(inputFilePath2, inputFileExtension);
		
		for (File encodedFile : encodedFiles) {
			for (File rawTokenFile : rawTokenFiles) {
				if (isMatched(encodedFile, rawTokenFile)) {
					vectorizeFeatures(encodedFile, rawTokenFile, outputFilePath, sizeOfZeroVector);
					break;
				}
			}
		}
	}
	
	/**
	 * 
	 * @param encodedFilePath
	 * @param rawTokenFilePath
	 * @param outputFilePath
	 * @param tokensPath1, tokens with high frequency.
	 * @param tokensPath2, tokens of method names.
	 * @throws IOException
	 */
	public void vectorizeFeatures(int sizeOfZeroVector, String encodedFilePath, String rawTokenFilePath, String outputFilePath, String tokensPath1, String tokensPath2) throws IOException {
		List<File> encodedFiles = FileHelper.getAllFiles(encodedFilePath, inputFileExtension);
		List<File> rawTokenFiles = FileHelper.getAllFiles(rawTokenFilePath, inputFileExtension);
		List<File> highFrequentTokenFiles = FileHelper.getAllFiles(tokensPath1, inputFileExtension);
		List<File> tokenFiles = FileHelper.getAllFiles(tokensPath2, inputFileExtension); // the files of tokens of method names.
		
		for (File encodedFile : encodedFiles) {
			for (File rawTokenFile : rawTokenFiles) {
				if (isMatched(encodedFile, rawTokenFile)) {
					File highFrequentTokenFile = getMatchedFile(encodedFile, highFrequentTokenFiles);
					File tokenFile = getMatchedFile(encodedFile, tokenFiles);
					vectorizeFeatures(encodedFile, rawTokenFile, encodedFilePath, outputFilePath, sizeOfZeroVector, highFrequentTokenFile, tokenFile);
					break;
				}
			}
		}
	}

	private File getMatchedFile(File encodedFile, List<File> tokenFiles) {
		
		for (File tokenFile : tokenFiles) {
			String fileName = tokenFile.getName().replace(inputFileExtension, "");
			if (encodedFile.getName().contains(fileName)) {
				return tokenFile;
			}
		}
		
		return null;
	}

	/**
	 * 
	 * @param encodedFile
	 * @param rawTokenFile
	 * @param encodedFilePath
	 * @param outputFilePath
	 * @param sizeOfZeroVector
	 * @param tokenFile1, the file of tokens with high frequency.
	 * @param tokenFile2, the file of tokens of method names.
	 * @throws IOException
	 */
	public void vectorizeFeatures(File encodedFile, File rawTokenFile, String encodedFilePath, String outputFilePath, int sizeOfZeroVector, File tokenFile1, File tokenFile2) throws IOException {
		List<String> highFrequentTokens = readTokens(tokenFile1);
		List<String> tokensOfMethodNames = readTokens(tokenFile2);
		Map<String, List<String>> featuresVectors = getFeaturesVectors(encodedFile);
		String outputFileName = encodedFile.getPath();
		String splitStr = " ";
		outputFileName = outputFileName.replace(encodedFilePath, outputFilePath).replace(inputFileExtension, outputFileExtension);
		
		int maxSize = getMaxSize(rawTokenFile);
		List<String> zeroList = new ArrayList<String>();
		for (int i = 0; i < sizeOfZeroVector; i ++) {
			zeroList.add("0");
		}
		
		FileInputStream fis = new FileInputStream(rawTokenFile);
		Scanner scanner = new Scanner(fis);
		String rawFeature = null;
		StringBuilder outputData = new StringBuilder();
		
		int index = 0;
		if (outputFileName.endsWith("SIZE=142.csv")) {
			while (scanner.hasNextLine()) {
				rawFeature = scanner.nextLine();
				List<String> rawFeatureVector = Arrays.asList(rawFeature.split(splitStr));
				int size = rawFeatureVector.size();
				List<String> outputDataVector = new ArrayList<String>();
				
				for (int i = 0; i < size; i ++) {
					String featureElement1 = rawFeatureVector.get(i);
					i ++;
					String featureElement2 = rawFeatureVector.get(i);
					if (highFrequentTokens.contains(featureElement2) || tokensOfMethodNames.contains(featureElement2.toLowerCase())) {
						outputDataVector.addAll(featuresVectors.get(featureElement1));
						outputDataVector.addAll(featuresVectors.get(featureElement2));
					} else {
						outputDataVector.addAll(zeroList);
						outputDataVector.addAll(zeroList);
					}
				}
				
				for (int i = size; i < maxSize; i ++) {
					outputDataVector.addAll(zeroList);
				}
				
				outputData.append(outputDataVector.toString().replace("[", "").replace("]", "") + "\n");
				
				index ++;
				if (index % 1000 == 0) {
					FileHelper.outputToFile(outputFileName, outputData);
					outputData.setLength(0);
				}
			}
		} else {
			while (scanner.hasNextLine()) {
				rawFeature = scanner.nextLine();
				List<String> rawFeatureVector = Arrays.asList(rawFeature.split(splitStr));
				int size = rawFeatureVector.size();
				List<String> outputDataVector = new ArrayList<String>();
				
				for (int i = 0; i < size; i ++) {
					String featureElement = rawFeatureVector.get(i);
					if (highFrequentTokens.contains(featureElement) || tokensOfMethodNames.contains(featureElement.toLowerCase())) {
						List<String> featureVector = featuresVectors.get(featureElement);
						outputDataVector.addAll(featureVector);
					} else {
						outputDataVector.addAll(zeroList);
					}
				}
				
				for (int i = size; i < maxSize; i ++) {
					outputDataVector.addAll(zeroList);
				}
				
				outputData.append(outputDataVector.toString().replace("[", "").replace("]", "") + "\n");
				
				index ++;
				if (index % 1000 == 0) {
					FileHelper.outputToFile(outputFileName, outputData);
					outputData.setLength(0);
				}
			}
		}
		
		scanner.close();
		fis.close();
		
		if (outputData.length() > 0) {
			FileHelper.outputToFile(outputFileName, outputData);
		}
	}
	
	
	private List<String> readTokens(File tokenFile) throws IOException {
		List<String> tokens = new ArrayList<>();
		
		String content = FileHelper.readFile(tokenFile);
		BufferedReader br = new BufferedReader(new StringReader(content));
		String line = null;
		
		while ((line = br.readLine()) != null) {
			tokens.add(line);
		}
		
		return tokens;
	}

	public void vectorizeFeatures(File encodedFile, File rawTokenFile, String outputFilePath, int sizeOfZeroVector) throws IOException {
		Map<String, List<String>> featuresVectors = getFeaturesVectors(encodedFile);
		String outputFileName = encodedFile.getName();
		String splitStr = " ";
		outputFileName = outputFilePath + outputFileName.replace(inputFileExtension, outputFileExtension);
		
		int maxSize = getMaxSize(rawTokenFile);
		List<String> zeroList = new ArrayList<String>();
		for (int i = 0; i < sizeOfZeroVector; i ++) {
			zeroList.add("0");
		}
		
		FileInputStream fis = new FileInputStream(rawTokenFile);
		Scanner scanner = new Scanner(fis);
		String rawFeature = null;
		StringBuilder outputData = new StringBuilder();
		
		int index = 0;
		while (scanner.hasNextLine()) {
			rawFeature = scanner.nextLine();
			List<String> rawFeatureVector = Arrays.asList(rawFeature.split(splitStr));
			int size = rawFeatureVector.size();
			List<String> outputDataVector = new ArrayList<String>();
			
			for (int i = 0; i < size; i ++) {
				String featureElement = rawFeatureVector.get(i);
				if (featuresVectors.containsKey(featureElement)) {
					List<String> featureVector = featuresVectors.get(featureElement);
					outputDataVector.addAll(featureVector);
				} else {
					outputDataVector.addAll(zeroList);
				}
			}
			
			for (int i = size; i < maxSize; i ++) {
				outputDataVector.addAll(zeroList);
			}
			
			outputData.append(outputDataVector.toString().replace("[", "").replace("]", "") + "\n");
			
			index ++;
			if (index % 1000 == 0) {
				FileHelper.outputToFile(outputFileName, outputData);
				outputData.setLength(0);
			}
		}
		
		scanner.close();
		fis.close();
		
		if (outputData.length() > 0) {
			FileHelper.outputToFile(outputFileName, outputData);
		}
	}

	private int getMaxSize(File file) throws IOException {
		int maxSize = 0;
		String fileName = file.getName();
		if (fileName.contains("SIZE")) { // method_body
			maxSize = Integer.parseInt(fileName.substring(fileName.toUpperCase().lastIndexOf("SIZE=") + "SIZE=".length(), fileName.lastIndexOf(inputFileExtension)));
		} else { // method_name
			FileInputStream fis = new FileInputStream(file);
			Scanner scanner = new Scanner(fis);
			
			while (scanner.hasNextLine()) {
				String[] tokens = scanner.nextLine().split(" ");
				int length = tokens.length;
				maxSize = length > maxSize ? length : maxSize;
			}
			
			scanner.close();
			fis.close();
		}
		
		return maxSize;
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

	private boolean isMatched(File encodedFile, File rawTokenFile) {
		String encodedFileName = encodedFile.getName();
		String rawTokenFileName = rawTokenFile.getName();
//		rawTokenFileName = rawTokenFileName.substring(0, rawTokenFileName.lastIndexOf(inputFileExtension));
		
//		String encodedFilePath = encodedFile.getPath();
//		String rawTokenFilePath = rawTokenFile.getPath();
		
		if (encodedFileName.equals(rawTokenFileName)) {
//			if ((encodedFilePath.contains("/method_body/") && rawTokenFilePath.contains("/method_body/"))) {
//				return true;
//			}
//			if ((encodedFilePath.contains("/RAW_CAMEL_TOKENIATION/") && rawTokenFilePath.contains("/RAW_CAMEL_TOKENIATION/"))) {
//				return true;
//			}
//			if ((encodedFilePath.contains("/SIMPLIFIED_NLP/") && rawTokenFilePath.contains("/SIMPLIFIED_NLP/"))) {
//				return true;
//			}
//			if ((encodedFilePath.contains("/SIMPLIFIED_NLP(2)/") && rawTokenFilePath.contains("/SIMPLIFIED_NLP(2)/"))) {
//				return true;
//			}
//			if ((encodedFilePath.contains("/TOKENAZATION_WITH_NLP/") && rawTokenFilePath.contains("/TOKENAZATION_WITH_NLP/"))) {
//				return true;
//			}
//			if ((encodedFilePath.contains("/TOKENAZATION_WITH_NLP(2)/") && rawTokenFilePath.contains("/TOKENAZATION_WITH_NLP(2)/"))) {
//				return true;
//			}
			return true;
		}
		
		return false;
	}
}
