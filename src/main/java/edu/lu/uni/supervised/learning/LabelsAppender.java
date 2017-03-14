package edu.lu.uni.supervised.learning;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import edu.lu.uni.util.FileHelper;

public class LabelsAppender {
	
	private static final String DATA_FILE_PATH = "outputData/encoder/outputDataOfCode2Vec_2/method_body/";
	private static final String TRAINING_DATA_PATH = "outputData/supervised-learning/training/";
	private static final String TESTING_DATA_PATH = "outputData/supervised-learning/testing/";
	
	public static void main(String[] args) throws IOException {
		
		LabelsAppender appender = new LabelsAppender();
		List<File> files = FileHelper.getAllFiles(DATA_FILE_PATH, ".csv");
		
		for (File file : files) {
			String featureFile = file.getName();
			String labelsFile = "inputData/selected_data/method_name/SIMPLIFIED_NLP(2)/" + featureFile.substring(0, featureFile.lastIndexOf("SIZE")) + ".list";
			File trainingFile = new File(TRAINING_DATA_PATH + featureFile);
			File testingFile = new File(TESTING_DATA_PATH + featureFile);
			if (trainingFile.exists()) trainingFile.delete();
			if (testingFile.exists()) testingFile.delete();
			
			appender.generateData(file, labelsFile, trainingFile, testingFile);
		}
	}
	
	public void generateData(File featuresFile, String labelsFile, File trainingFile, File testingFile) throws IOException {
		List<Integer> labels = getLabels(labelsFile, 1);
//		String fileName = featuresFile.getName();
//		int sizeOfVector = Integer.parseInt(fileName.substring(fileName.lastIndexOf("SIZE=") + "SIZE=".length(), fileName.lastIndexOf(".csv")));
		
		int index = 0;
		int training = 0;
		int testing = 0;
		StringBuilder trainingData = new StringBuilder();
		StringBuilder testingData = new StringBuilder();
		Random randomGenerator = new Random();
		FileInputStream fis = new FileInputStream(featuresFile);
		Scanner scanner = new Scanner(fis);
		scanner.nextLine();
		
		while (scanner.hasNextLine()) {
			String features = scanner.nextLine();
			int random = randomGenerator.nextInt(10000);
			if (labels.get(index) == -1) {
				index ++;
				continue;
			}
			if (random < 9000) {
				trainingData.append(features.replaceAll(" ", "") + "," + labels.get(index) + "\n");
				training ++;
			} else {
				testingData.append(features.replaceAll(" ", "") + "," + labels.get(index) + "\n");
				testing ++;
			}
			
			index ++;
			
			if (training % 100 == 0) {
				FileHelper.outputToFile(trainingFile.getPath(), trainingData);
				trainingData.setLength(0);
			}
			if (testing % 50 == 0) {
				FileHelper.outputToFile(testingFile.getPath(), testingData);
				testingData.setLength(0);
			}
			
//			String[] ll = features.split(", ");
//			if (ll.length > sizeOfVector) {
//				System.out.println(index + "::" + ll.length);
//			}
		}
		System.out.println("Training: " + training + ", Testing: " + testing);
//		FileHelper.outputToFile(trainingFile, trainingData);
//		FileHelper.outputToFile(testingFile, testingData);
		
		scanner.close();
		fis.close();
	}

	private List<Integer> getLabels(String fileName, int location) throws IOException {
		String contents = FileHelper.readFile(new File(fileName));
		BufferedReader br = new BufferedReader(new StringReader(contents));
		String label = null;

		int value = 0;
		Map<String, Integer> map = new HashMap<>();
		Map<String, Integer> labelsNum = new HashMap<>();
		List<Integer> labels = new ArrayList<>();
		while ((label = br.readLine()) != null) {
			label = label.substring(label.lastIndexOf("#") + 1);
			String[] labelArray = label.split(",");
			String key = "";
			if (labelArray.length >= location) {
				key = labelArray[location - 1];
				
//				if (!"VB".equals(key) // 52356
//						&& !"NN".equals(key) && !"VBZ".equals(key) // 4549, 4537
//						&& !"JJ".equals(key) && !"NNP".equals(key) && !"TO".equals(key)// 2000+
//						&& !"NNS".equals(key) //&& !"IN".equals(key)
//						) {
//					key = "OTHERS";
//				}
//				if ("NNP".equals(key) ||"NNS".equals(key)) {
//					key = "NNP";
//				}
//				if ("JJ".equals(key) ||"TO".equals(key)) {
//					key = "JJ";
//				}
			} else {
				key = "NULL";
			}
			if (!map.containsKey(key)) {
//				if ("VB".equals(key) || "OTHERS".equals(key)) {
//					map.put(key, -1);
//				} else {
//					map.put(key, value ++);
//				}
				map.put(key, value ++);
			}
			labels.add(map.get(key));
			
			if (!labelsNum.containsKey(key)) {
				labelsNum.put(key, 1);
			} else {
				labelsNum.put(key, labelsNum.get(key) + 1);
			}
		}
		System.out.println(labels.size());
		System.out.println(map.size() + ": " + map);
		System.out.println(labelsNum.size() + ": " + labelsNum);
		
		return labels;
	}
}
