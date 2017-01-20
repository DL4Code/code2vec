package edu.lu.uni;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.lu.uni.data.processor.MyTokenPreprocessor;
import edu.lu.uni.util.FileHelper;

/**
 * 
 * @author kui.liu
 *
 */
public class Code2Vector {

	private static Logger log = LoggerFactory.getLogger(Code2Vector.class);
	private static final String INPUT_FILE_PATH = "outputData/inputDataOfCode2Vec/features/";
	private static final String OUTPUT_FILE_PATH = "outputData/outputDataOfCode2Vec/";

	public static void main(String[] args) throws Exception {
		FileHelper.deleteFiles(OUTPUT_FILE_PATH);
    	Code2Vector cv = new Code2Vector();
    	cv.convertCodeToVector(INPUT_FILE_PATH);
    }

	public void convertCodeToVector(String inputFilePath) throws IOException {
		List<File> files = FileHelper.getAllFiles(inputFilePath, ".txt");
		
		for (File file : files) {
			convertCodeToVector(file);
//			break;
		}
	}

	@SuppressWarnings("deprecation")
	public void convertCodeToVector(File file) throws IOException {
		String fileName = file.getPath();
//		String indexStr = "";
		int layerSize = 0;
		if (fileName.contains("/features/")) {
//			indexStr = "SIZE=";
			layerSize = 80;
		} else {
//			indexStr = "MAXSize=";
			layerSize = 10;
			
		}
		layerSize = 300;
//		int layerSize = 100;//Integer.parseInt(fileName.substring(fileName.lastIndexOf(indexStr) + indexStr.length(), fileName.lastIndexOf(".txt")));

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
                .minWordFrequency(1)
                .layerSize(layerSize)
                .iterate(iter)
                .tokenizerFactory(t)
                .build();

        log.info("Fitting Word2Vec model....");
        vec.fit();

        log.info("Writing word vectors to text file....");

        // Write word vectors to file
        WordVectorSerializer.writeWordVectors(vec, 
        		fileName.replace(INPUT_FILE_PATH, OUTPUT_FILE_PATH));

        // Evaluation
        try {
			log.info("Evaluation: Closest Words");
			Collection<String> lst = vec.wordsNearest("true", 10);
			System.out.println("10 Words closest to 'true': " + lst);
			
			double cosSim = vec.similarity("true", "false");
			System.out.println("Cosin similarity: " + cosSim);
		} catch (java.lang.NullPointerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
