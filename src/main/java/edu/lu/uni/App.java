package edu.lu.uni;

import java.io.File;
import java.io.IOException;
import java.util.List;

import edu.lu.uni.data.processor.Code2VecDataProcessor;
import edu.lu.uni.data.processor.FeaturesVectorizer;
import edu.lu.uni.util.FileHelper;

public class App {

	public static void main(String[] args) {
		App example = new App();
		try {
			/*
			 * The first step: preprocess data for converting code to vector (i.e., code2vec).
			 */
			example.dataPreprocessForCode2Vec();
			/*
			 * The second step: token embeding with Word2Vec.
			 */
			example.embedTokenWithWord2Vec();
			/*
	    	 * The third step: vectorization(prepare data) for deep learning.
	    	 * Token selection: 1. tokens with high frequency selected by previous step.
	    	 * 					2. tokens contained in the tokens of method names.
	    	 */
			example.vectorizeEmbeddedTokens();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void dataPreprocessForCode2Vec() throws IOException {
		String inputFilePath = Configuration.SELECTED_METHOD_BODY_PATH;
		String outputFilePath = Configuration.EMBEDDING_TOKENS_FILE_PATH;
		FileHelper.deleteDirectory(outputFilePath);
		
		List<File> files = FileHelper.getAllFiles(inputFilePath, ".list");
		for (File file : files) {
			Code2VecDataProcessor processor = new Code2VecDataProcessor(file, inputFilePath, outputFilePath);
			processor.processData();
		}
	}

	private void embedTokenWithWord2Vec() throws IOException {
		String inputFilePath = Configuration.EMBEDDING_TOKENS_FILE_PATH;
    	int sizeOfVector = Configuration.SIZE_OF_VECTOR;   // Size of vector of each token.
    	int minWordFrequency = Configuration.MIN_WORD_FREQUENCY;
    	String outputFilePath = Configuration.EMBEDDED_TOKENS_FILE_PATH;
    	
    	FileHelper.deleteDirectory(outputFilePath);
    	
    	List<File> files = FileHelper.getAllFiles(inputFilePath, ".list");
		Code2Vector cv = new Code2Vector();
		for (File file : files) {
			cv.embedTokens(file, minWordFrequency, sizeOfVector, inputFilePath, outputFilePath);
		}
	}
	
	public void vectorizeEmbeddedTokens() throws IOException {
		String outputFilePath = Configuration.EMBEDDED_TOKENS_VECTOR_FILE_PATH;
		String encodedTokenFilePath = Configuration.EMBEDDED_TOKENS_FILE_PATH;
		String rawTokenFilePath = Configuration.SELECTED_METHOD_BODY_PATH;
		FileHelper.deleteDirectory(outputFilePath);
		
		FeaturesVectorizer fv = new FeaturesVectorizer();
		int sizeOfZeroVector = 100;
		int minWordFrequency = Configuration.MIN_WORD_FREQUENCY;
		fv.vectorizeFeatures(sizeOfZeroVector, encodedTokenFilePath + minWordFrequency + "/", rawTokenFilePath, 
				encodedTokenFilePath.replace("outputData/", outputFilePath) + minWordFrequency + "/");
	}
}
