package edu.lu.uni.data.filter;

import java.io.File;
import java.io.IOException;
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
public class FeaturesFilter {
	
	private static Logger logger = LoggerFactory.getLogger(FeaturesFilter.class);

	private static final String INPUT_FILE_PATH = "inputData/original-features/";
	private static final String OUTPUT_FILE_PATH = "outputData/filter/features/";
	
	public static void main(String[] args) throws IOException {
		/**
		 * Data selection:
		 * apache$commons-math$feature-ast-node-name-with-node-label
		 * upper whisker: 35 + 1.5 * (35 - 4) = 81.5 (82)
 		 *
		 * apache$commons-math$feature-only-ast-node-name
		 * upper whisker: 35 + 1.5 * (35 - 4) = 81.5 (82)
 		 *
		 * apache$commons-math$feature-raw-tokens-with-operators
		 * upper whisker: 36 + 1.5 * (36 - 4) = 84
 		 *
		 * apache$commons-math$feature-raw-tokens-without-operators        
		 * upper whisker: 31 + 1.5 * (31 - 4) = 71.5 (72)
 		 *
		 * apache$commons-math$feature-statement-node-name-with-all-node-label
		 * upper whisker: 35 + 1.5 * (35 - 4) = 81.5 (82)
 		 * 
		 */
		FeaturesFilter filter = new FeaturesFilter();
		filter.fliterOutliersInAllFiles(INPUT_FILE_PATH, ".list");
//		encoder.encodeRawFeaturesInOneFile(new File(INPUT_FILE_PATH + "apache$commons-math$feature-ast-node-name-with-node-label.list"));
		
	}
	
	public void fliterOutliersInAllFiles(String filePath, String fileType) throws IOException {
		RawDataReader rdReader = new RawDataReader();
		
		Map<String, List<String>> rawFeatures = rdReader.readRawFeaturesFromFiles(filePath, fileType);
		
		for (Map.Entry<String, List<String>> entry : rawFeatures.entrySet()) {
			filterOutliers(entry.getKey(), entry.getValue());
		}
	}
	
	public void filterOutliersInOneFile(File file) throws IOException {
		RawDataReader rdReader = new RawDataReader();
		
		List<String> rawFeatures = rdReader.readRawFeaturesFromFile(file);
		
		filterOutliers(file.getName(), rawFeatures);
	}

	private void filterOutliers(String fileName, List<String> rawFeatures) {
		logger.info("Start to filter outliers...");
		int maxSize = 0;
		if (fileName.contains("apache$commons-math$feature-raw-tokens-with-operators")) {
			maxSize = 84;
		} else if (fileName.contains("apache$commons-math$feature-raw-tokens-without-operators")) {
			maxSize = 72;
		} else if (fileName.contains("apache$commons-math$feature-only-ast-node-name")
				|| fileName.contains("apache$commons-math$feature-ast-node-name-with-node-label")
				|| fileName.contains("apache$commons-math$feature-statement-node-name-with-all-node-label")) {
			maxSize = 82;
		}
		fileName = fileName.replace(".list", "SIZE=" + maxSize + ".list");
		
		StringBuilder outputData = new StringBuilder();
		int numOfLines = 0;
		for (String rawFeature : rawFeatures) {
			
			int indexOfHarshKey = rawFeature.indexOf("#");
			
			if (indexOfHarshKey < 0) {
				logger.error("The below raw feature is invalid!\n" + rawFeature);
				continue;
			}
			
			String[] featureVetor = rawFeature.substring(indexOfHarshKey + 2, rawFeature.length() - 1).split(", ");
						
			if (featureVetor.length <= maxSize) {
				outputData.append(rawFeature + "\n");
				numOfLines ++;
				if (numOfLines % 1000 == 0) {
					FileHelper.outputToFile(OUTPUT_FILE_PATH + fileName, outputData);
					outputData = new StringBuilder();
				}
			}
		}
		if (outputData.length() > 0) {
			FileHelper.outputToFile(OUTPUT_FILE_PATH + fileName, outputData);
		}
		
		logger.info("Finish off filtering outliers...");
	}
}
