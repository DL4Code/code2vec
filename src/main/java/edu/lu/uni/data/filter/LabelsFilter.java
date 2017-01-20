package edu.lu.uni.data.filter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.lu.uni.util.FileHelper;
import edu.lu.uni.util.RawDataReader;

/**
 * 
 * @author kui.liu
 *
 */
public class LabelsFilter {

	private static Logger logger = LoggerFactory.getLogger(LabelsFilter.class);
	
	private static final String LABEL_FILE_PATH = "inputData/labels/";
	private static final String FEATURE_INTEGER_VECTOR_FILE_PATH = "outputData/filter/original-features/";
	private static final String OUTPUT_FILE_PATH = "outputData/filter/labels/";
	
	public static void main(String[] args) throws IOException {
		LabelsFilter encoder = new LabelsFilter();
		
		/**
		 * 1. select labels by encoded feature file "apache$commons-math$feature-raw-tokens-with-operatorsSIZE=84.list";
		 * 2. select labels by encoded feature file "apache$commons-math$feature-raw-tokens-without-operatorsSIZE=72.list";
		 * 3. select labels by encoded feature file "apache$commons-math$feature-only-ast-node-nameSIZE=82.list",
		 * 					or encoded feature file "apache$commons-math$feature-ast-node-name-with-node-labelSIZE=82.list",
		 * 					or encoded feature file "apache$commons-math$feature-statement-node-name-with-all-node-labelSIZE=82.list".
		 * 
		 */
		Map<String, List<String>> allFeatures = new HashMap<>();
		List<File> files = FileHelper.getAllFilesInCurrentFolder(FEATURE_INTEGER_VECTOR_FILE_PATH, ".list");
		boolean flag = false;
		for (File file : files) {
			if (!flag || !file.getName().endsWith("SIZE=82.list")) {
				allFeatures.put(file.getName(), encoder.getFeatures(file));
				if (file.getName().endsWith("SIZE=82.list")) flag = true;
			}
		}
//		List<String> features = getFeatures("filePath");
		logger.debug("Start to read labels...");
//		encoder.encodeLabelsInOneFile(new File(LABEL_FILE_PATH + "apache$commons-math$feature-raw-tokens-with-operators.list(SIMPLIFIED_NLP).list"), allFeatures);
		encoder.encodeLabelsInAllFiles(LABEL_FILE_PATH, allFeatures);
		logger.debug("Finish off reading labels...");
	}
	
	private List<String> getFeatures(File file) throws IOException {
		List<String> features = new ArrayList<>();
		
		String fileContent = FileHelper.readFile(file);
		BufferedReader br = new BufferedReader(new StringReader(fileContent));
		
		String feature = null;
		
		while ((feature = br.readLine()) != null) {
			features.add(feature.substring(0, feature.indexOf("#") + 1));
		}
		
		return features;
	}

	public void encodeLabelsInAllFiles(String filePath, Map<String, List<String>> allFeatures) throws IOException {
		List<File> files = FileHelper.getAllFiles(filePath, ".list");
		for (File file : files) {
			encodeLabelsInOneFile(file, allFeatures);
		}
	}
	
	public void encodeLabelsInOneFile(File file, Map<String, List<String>> allFeatures) throws IOException {
		RawDataReader rdReader = new RawDataReader();
		
		List<String> rawFeatures = rdReader.readRawFeaturesFromFile(file);
		
		encodeLabels(file.getName(), rawFeatures, allFeatures);
	}

	private void encodeLabels(String fileName, List<String> labels, Map<String, List<String>> allFeatures) {
		for (Map.Entry<String, List<String>> entry : allFeatures.entrySet()) {
			encodelLabels(fileName, labels, entry.getKey(), entry.getValue());
		}
	}

	private void encodelLabels(String fileName, List<String> labels, String fileNameOfFeatures, List<String> features) {
		logger.info("Start to map tokens into integers...");
		fileName = fileName.substring(fileName.lastIndexOf("(") + 1, fileName.lastIndexOf(")")) + "/" + fileNameOfFeatures;
		StringBuilder outputData = new StringBuilder();
		int maxSize = 0;

		for (String label : labels) {
			
			int indexOfHarshKey = label.indexOf("#");
			
			if (indexOfHarshKey < 0) {
				logger.error("The below raw label is invalid!\n" + label);
				continue;
			}
			
			String dataKey = label.substring(0, indexOfHarshKey + 1);
			
			if (!features.contains(dataKey)) {
				continue;
			}
			
			String[] dataVector = label.substring(indexOfHarshKey + 1, label.length()).split(",");
			if (dataVector.length > maxSize) {
				maxSize = dataVector.length;
			}
			
			outputData.append(label + "\n");
		}
		fileName = fileName.replace(".list", "MAXSize=" + maxSize + ".list");
		FileHelper.outputToFile(OUTPUT_FILE_PATH + fileName, outputData);
		
		logger.info("Finish off mapping tokens into integers...");
	}
}
