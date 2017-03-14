package edu.lu.uni;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.lu.uni.data.processor.Code2VecDataProcessor;
import edu.lu.uni.data.processor.FeaturesVectorizer;
import edu.lu.uni.data.processor.MyTokenPreprocessor;
import edu.lu.uni.util.FileHelper;

/**
 * 
 * @author kui.liu
 *
 */
public class Code2Vector {

	private static Logger log = LoggerFactory.getLogger(Code2Vector.class);
	private static final String SELECTED_DATA = "inputData/selected_data/method_body/";
	private static final String TOKENS_OF_METHOD_NAMES = "inputData/selected_data/tokens-of-method-names/";
	private static final String INPUT_FILE_PATH = "outputData/inputDataOfCode2Vec/method_body/";
	private static final String ENCODED_FILE_PATH = "outputData/outputDataOfCode2Vec_";
	private static final String HIGH_FREQUENT_TOKENS_FILE_PATH = "outputData/highFrequentTokens/";
	private static final String OUTPUT_FILE_PATH = "outputData/encoder/";

	public static void main(String[] args) throws Exception {
		
		FileHelper.deleteDirectory(INPUT_FILE_PATH);
		FileHelper.deleteDirectory(ENCODED_FILE_PATH + "1/");
		FileHelper.deleteDirectory(HIGH_FREQUENT_TOKENS_FILE_PATH);
		FileHelper.deleteDirectory(OUTPUT_FILE_PATH);
		/*
		 * The first step: preprocess data for converting code to vector (i.e., code2vec).
		 */
		List<File> files = FileHelper.getAllFiles(SELECTED_DATA, ".list");
		for (File file : files) {
			Code2VecDataProcessor processor = new Code2VecDataProcessor(file, SELECTED_DATA, INPUT_FILE_PATH);
			processor.processData();
		}
		
		/*
		 * The second step: token embeding with Word2Vec.
		 */
    	Code2Vector cv = new Code2Vector();
    	int sizeOfVector = 300; // Size of vector of each token.
    	int minWordFrequency = 1;
    	cv.convertCodeToVector(INPUT_FILE_PATH, minWordFrequency, sizeOfVector);
    	/*
    	 * 
    	 */
    	minWordFrequency = 1;
    	cv.collectHighFrequencyWord(INPUT_FILE_PATH, minWordFrequency, sizeOfVector, ENCODED_FILE_PATH, HIGH_FREQUENT_TOKENS_FILE_PATH, ".list");
    	
    	/*
    	 * The third step: vectorization(prepare data) for deep learning.
    	 * Token selection: 1. tokens with high frequency selected by previous step.
    	 * 					2. tokens contained in the tokens of method names.
    	 */
    	minWordFrequency = 1;
		FeaturesVectorizer fv = new FeaturesVectorizer();
		fv.vectorizeFeatures(sizeOfVector, ENCODED_FILE_PATH + minWordFrequency + "/", INPUT_FILE_PATH, 
				ENCODED_FILE_PATH.replace("outputData/", OUTPUT_FILE_PATH) + minWordFrequency + "/",
				HIGH_FREQUENT_TOKENS_FILE_PATH, TOKENS_OF_METHOD_NAMES);
    }

	public void convertCodeToVector(String inputFilePath, int minWordFrequency, int layerSize) throws IOException {
		List<File> files = FileHelper.getAllFiles(inputFilePath, ".list");
		
		for (File file : files) {
			embedTokens(file,minWordFrequency, layerSize, INPUT_FILE_PATH, ENCODED_FILE_PATH);
		}
	}

	public void collectHighFrequencyWord(String inputFilePath, int minWordFrequency, int sizeOfVector,
			String encodedFilePath, String highFrequentTokensFilePath, String fileExtension) throws IOException {
		
		List<File> filesList = FileHelper.getAllFiles(inputFilePath, fileExtension);
		
		for (File file : filesList) {
			collectHighFrequencyWord(file, minWordFrequency, sizeOfVector, inputFilePath, encodedFilePath, highFrequentTokensFilePath);
		}
	}

	public void collectHighFrequencyWord(File file, int minWordFrequency, int sizeOfVector, String inputFilePath, String encodedFilePath, 
			 String highFrequentTokensFilePath) throws IOException {
		
		String fileName = file.getPath().replace(inputFilePath, highFrequentTokensFilePath);
		
		Map<String, Integer> tokensMap = new HashMap<>();
		FileInputStream fis = new FileInputStream(file);
		Scanner scanner = new Scanner(fis);
		
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			String[] tokensArray = line.split(" ");
			
			for (int i = 0, length = tokensArray.length; i < length; i ++) {
				if (tokensMap.containsKey(tokensArray[i])) {
					tokensMap.put(tokensArray[i], tokensMap.get(tokensArray[i]) + 1);
				} else {
					tokensMap.put(tokensArray[i], 1);
				}
			}
		}
		
		scanner.close();
		fis.close();
		
		StringBuilder tokens = new StringBuilder();
		int counter = 0;
		for (Map.Entry<String , Integer> entry : tokensMap.entrySet()) {
			if (entry.getValue() >= minWordFrequency) {
				tokens.append(entry.getKey() + "\n");
				counter ++;
				if (counter % 10000 == 0) {
					FileHelper.outputToFile(fileName, tokens);
					tokens.setLength(0);
				}
			}
		}
		
		if (counter % 10000 != 0) {
			FileHelper.outputToFile(fileName, tokens);
		}
	}
	

	@SuppressWarnings("deprecation")
	public void embedTokens(File file, int minWordFrequency, int layerSize, String inputFilePath, String encodedFilePath) throws IOException {
		String fileName = file.getPath();

        log.info("Load & Vectorize Sentences....");
        // Strip white space before and after for each line
        SentenceIterator iter = new BasicLineIterator(file);
        // Split on white spaces in the line to get words
        TokenizerFactory t = new DefaultTokenizerFactory();

        /*
            CommonPreprocessor will apply the following regex to each token: [\d\.:,"'\(\)\[\]|/?!;]+
            So, effectively all numbers, punctuation symbols and some special symbols are stripped off.
            Additionally it forces lower case for all tokens.
         */
        t.setTokenPreProcessor(new MyTokenPreprocessor());

        log.info("Building model....");
        Word2Vec vec = new Word2Vec.Builder()
        		.epochs(1)
//        		.batchSize(100)
//        		.useAdaGrad(reallyUse)
                .iterations(1)
                .learningRate(.01)
                .seed(42)
                .windowSize(5)
                .minWordFrequency(minWordFrequency)
                .layerSize(layerSize)
                .iterate(iter)
                .tokenizerFactory(t)
                .build();

        log.info("Fitting Word2Vec model....");
        vec.fit();

        log.info("Writing word vectors to text file....");
        // Write word vectors to file
        fileName = fileName.replace(inputFilePath, encodedFilePath + minWordFrequency + "/");
        FileHelper.makeDirectory(fileName);
        WordVectorSerializer.writeWordVectors(vec, fileName);

        // Evaluation
//        try {
//			log.info("Evaluation: Closest Words");
//			Collection<String> lst = vec.wordsNearest("true", 10);
//			System.out.println("10 Words closest to 'true': " + lst);
//			
//			double cosSim = vec.similarity("true", "false");
//			System.out.println("Cosin similarity: " + cosSim);
//		} catch (java.lang.NullPointerException e) {
//			e.printStackTrace();
//		}
	}
}
