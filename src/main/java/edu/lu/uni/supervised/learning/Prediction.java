package edu.lu.uni.supervised.learning;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
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

public class Prediction {
	
	private static Logger log = LoggerFactory.getLogger(Prediction.class);
	private static final String TRAINING_DATA = "outputData/supervised-learning/training/";
	private static final String TESTING_DATA = "outputData/supervised-learning/testing/";
	
	public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException {
		String fileName = "projects$feature-statement-node-name-with-all-node-labelSIZE=74.csv";
		File trainingFile = new File(TRAINING_DATA + fileName);
		int sizeOfVector = Integer.parseInt(fileName.substring(fileName.lastIndexOf("SIZE=") + "SIZE=".length(), fileName.lastIndexOf(".csv")));
		predictWithCNN(trainingFile, sizeOfVector, sizeOfVector, 30);
	}
	
	private static void predictWithCNN(File file, int sizeOfVector, int labelIndex, int numClasses) throws FileNotFoundException, IOException, InterruptedException {
		int batchSize = 1000;
		int nChannels = 1;   // Number of input channels
        int outputNum = numClasses; // The number of possible outcomes
        
        int nEpochs = 1;     // Number of training epochs
        int iterations = 1;  // Number of training iterations
        int seed = 123;      //

        log.info("Load data....");
        RecordReader trainingDataReader = new CSVRecordReader();
        trainingDataReader.initialize(new FileSplit(file));
        DataSetIterator trainingData = new RecordReaderDataSetIterator(trainingDataReader,batchSize,labelIndex * 100,numClasses);
        RecordReader testingDataReader = new CSVRecordReader();
        int testBatchSize = 50; 
        testingDataReader.initialize(new FileSplit(new File(TESTING_DATA + file.getName())));
        DataSetIterator testData = new RecordReaderDataSetIterator(testingDataReader,testBatchSize,labelIndex * 100,numClasses);
        
        /*
         *  Construct the neural network
         */
        log.info("Build model....");
        MultiLayerConfiguration.Builder builder = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .iterations(iterations) // Training iterations as above
                .regularization(true).l2(0.0005)
                .learningRate(.01)//.biasLearningRate(0.02)
                //.learningRateDecayPolicy(LearningRatePolicy.Inverse).lrPolicyDecayRate(0.001).lrPolicyPower(0.75)
                .weightInit(WeightInit.XAVIER)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(Updater.NESTEROVS).momentum(0.9)
                .list()
                .layer(0, new ConvolutionLayer.Builder(1, 100)
                        //nIn and nOut specify depth. nIn here is the nChannels and nOut is the number of filters to be applied
                        .nIn(nChannels)
                        .stride(1, 1)
                        .nOut(20)
                        .activation("identity")
                        .build())
                .layer(1, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(2, 1)
                        .stride(2, 1)
                        .build())
                .layer(2, new ConvolutionLayer.Builder(3, 1)
                        .stride(1, 1)
                        .nOut(50)
                        .activation("identity")
                        .build())
                .layer(3, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(2, 1)
                        .stride(2, 1)
                        .build())
                .layer(4, new DenseLayer.Builder().activation("relu")
                        .nOut(500).build())
                .layer(5, new OutputLayer.Builder(LossFunctions.LossFunction.MEAN_ABSOLUTE_ERROR)
                        .nOut(outputNum)
                        .activation("softmax")
                        .build())
                .setInputType(InputType.convolutionalFlat(labelIndex, 100,1))
                .backprop(true).pretrain(false);

        MultiLayerConfiguration conf = builder.build();
        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();


        log.info("Train model....");
        model.setListeners(new ScoreIterationListener(1));
        for( int i=0; i<nEpochs; i++ ) {
            model.fit(trainingData);
            log.info("*** Completed epoch {} ***", i);

            log.info("Evaluate model....");
            Evaluation eval = new Evaluation(outputNum);
            while(testData.hasNext()){
                DataSet ds = testData.next();
                INDArray output = model.output(ds.getFeatureMatrix(), false);
                eval.eval(ds.getLabels(), output);

            }
            log.info(eval.stats());
            testData.reset();
        }
        log.info("****************Example finished********************");
	}
        
//        int i = 0;
//        String fileName = file.getPath().replace("outputData/", "outputData/CNN/");
//        StringBuilder features = new StringBuilder();
//        for(org.deeplearning4j.nn.api.Layer layer : model.getLayers()) {
//            if (i == 5) {
//                INDArray input = layer.input();
//            	features.append(input);
//            	FileHelper.createFile(new File(fileName), 
//            			features.toString().replace("[[", "").replaceAll("\\],", "")
//            			.replaceAll(" \\[", "").replace("]]", ""));
//            }
//            i ++;
//        }
//        
//        addMethodNameToFeatures(fileName);
//	}

//	private static void addMethodNameToFeatures(String file) throws IOException {
//		List<File> integerFeatureFiles = FileHelper.getAllFiles(INTEGER_FEATURE_FILE_PATH, ".list");
//		
//		for (File integerFeatureFile : integerFeatureFiles) {
//			String fileName = integerFeatureFile.getName();
//			if (file.contains(fileName.substring(0, fileName.lastIndexOf(".list")))) {
//				addMethodName(file, integerFeatureFile);
//				break;
//			}
//		}
//	}
//
//	private static void addMethodName(String file, File integerFeatureFile) throws IOException {
//		String features = FileHelper.readFile(new File(file));
//		String methodNames = FileHelper.readFile(integerFeatureFile);
//		BufferedReader br1 = new BufferedReader(new StringReader(features));
//		BufferedReader br2 = new BufferedReader(new StringReader(methodNames));
//		String featureLine = null;
//		String methodNameLine = null;
//		StringBuilder content = new StringBuilder();
//		
//		while ((featureLine = br1.readLine()) != null && (methodNameLine = br2.readLine()) != null) {
//			int indexOfHarshKey = methodNameLine.indexOf("#");
//			
//			if (indexOfHarshKey < 0) {
//				log.error("The below raw feature is invalid!\n" + methodNameLine);
//				continue;
//			}
//			
//			String methodName = methodNameLine.substring(0, indexOfHarshKey + 1);
//			content.append(methodName + "[" + featureLine + "]\n");
//		}
//		
//		FileHelper.createFile(new File(file.replace(".csv", ".list")), content.toString());
//	}
	
}
