package edu.lu.uni.cnn;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.lu.uni.util.FileHelper;

public class FeatureExtractorOfMethodBody {
	
	private static Logger log = LoggerFactory.getLogger(FeatureExtractorOfMethodBody.class);
	private static final String INPUT_FILE_PATH = "outputData/encoder/outputDataOfCode2Vec_1/method_body/";
	private static final String OUTPUT_FILE_PATH = "outputData/cnn/";
	
	public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException {
		FileHelper.deleteDirectory(OUTPUT_FILE_PATH);
		
		List<File> files = FileHelper.getAllFiles(INPUT_FILE_PATH, ".csv");
		for (File file : files) {
			String fileName = file.getName();
			int sizeOfVector = Integer.parseInt(fileName.substring(fileName.lastIndexOf("=") + 1, fileName.lastIndexOf(".csv")));
			int batchSize = 1000;
			
			int sizeOfCodeVec = 100;
			
			extracteFeaturesWithCNN(file, sizeOfVector, batchSize, sizeOfCodeVec); 
		}
		
		
	}
	
	private static void extracteFeaturesWithCNN(File file, int sizeOfVector, int batchSize, int sizeOfCodeVec) throws FileNotFoundException, IOException, InterruptedException {
		
		int nChannels = 1;   // Number of input channels
        int outputNum = sizeOfVector * sizeOfCodeVec; // The number of possible outcomes
        
        int nEpochs = 1;     // Number of training epochs
        int iterations = 1;  // Number of training iterations
        int seed = 123;      //
        int sizeOfFeatureVector = 300;

        log.info("Load data....");
        RecordReader trainingDataReader = new CSVRecordReader();
        trainingDataReader.initialize(new FileSplit(file));
        DataSetIterator trainingDataIter = new RecordReaderDataSetIterator(trainingDataReader,batchSize);
//        DataSet trainingData = trainingDataSet.next();
        
        /*
         *  Construct the neural network
         */
        log.info("Build model....");
        MultiLayerConfiguration.Builder builder = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .iterations(iterations) // Training iterations as above
                .regularization(true).l1(1e-3).l2(0.0005).dropOut(0.5)//.l2(0.0005)
                .learningRate(.01)//.biasLearningRate(0.02)
                //.learningRateDecayPolicy(LearningRatePolicy.Inverse).lrPolicyDecayRate(0.001).lrPolicyPower(0.75)
                .weightInit(WeightInit.XAVIER)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(Updater.NESTEROVS).momentum(0.9)
                .list()
                .layer(0, new ConvolutionLayer.Builder(1, sizeOfCodeVec)
                        //nIn and nOut specify depth. nIn here is the nChannels and nOut is the number of filters to be applied
                        .nIn(nChannels)
                        .stride(1, 1)
                        .nOut(20)
                        .activation("identity")
                        .build())
                .layer(1, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(2,1)
                        .stride(2,1)
                        .build())
                .layer(2, new ConvolutionLayer.Builder(3, 1)
                        .stride(1, 1)
                        .nOut(50)
                        .activation("identity")
                        .build())
                .layer(3, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(2,1)
                        .stride(2,1)
                        .build())
                .layer(4, new DenseLayer.Builder().activation("relu")
                        .nOut(sizeOfFeatureVector).build())
                .layer(5, new OutputLayer.Builder(LossFunctions.LossFunction.MEAN_ABSOLUTE_ERROR)
                        .nOut(outputNum)
                        .activation("softmax")
                        .build())
                .setInputType(InputType.convolutionalFlat(sizeOfVector, sizeOfCodeVec,1))
                .backprop(true).pretrain(false);

        MultiLayerConfiguration conf = builder.build();
        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();

        StringBuilder features = new StringBuilder();
		
        log.info("Train model....");
        model.setListeners(new ScoreIterationListener(1));
        for( int i=0; i<nEpochs; i++ ) {
        	while (trainingDataIter.hasNext()) {
        		DataSet next = trainingDataIter.next();
                model.fit(new DataSet(next.getFeatureMatrix(),next.getFeatureMatrix()));
                INDArray input = model.getOutputLayer().input();
            	features.append(input + "\n");
        	}
//            model.fit(trainingData);
            log.info("*** Completed epoch {} ***", i);
        }
        log.info("****************Example finished********************");
        
        
        /*
         * Output extracted features to a file.	
         */
        String fileName = file.getPath().replace("outputData/", OUTPUT_FILE_PATH);
        
    	FileHelper.createFile(new File(fileName), 
    			features.toString().replace("[[", "").replaceAll("\\],", "")
    			.replaceAll(" \\[", "").replace("]]", ""));
        features.setLength(0);
        
//        FileHelper.addMethodNameToFeatures(fileName, INPUT_FILE_PATH, ".txt", ".csv", ".txt");
	}

}
