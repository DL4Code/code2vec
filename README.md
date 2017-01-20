# code2vec

## Original input data
The output of model valid-method-ids(i.e., raw tokens vectors of method body)  and the output of model vmids(i.e., three kinds of labels of method name).

## The first step: filter out outliers which the sizes of method bodies vectors are too large.
edu.lu.uni.data.filter.FeaturesFilter.java
edu.lu.uni.data.filter.LabelsFilter.java

## The second step: preprocess data for converting code to vector (i.e., code2vec).
edu.lu.uni.data.processor.Code2VecDataProcessor.java

## The third step: token embeding.
edu.lu.uni.Code2Vector.java

## The forth step: prepare data for deeplearning.
edu.lu.uni.data.processor.FeaturesVectorizeForCNN.java

## The fifth step: extract features with CNN.
edu.lu.uni.cnn.*.java
