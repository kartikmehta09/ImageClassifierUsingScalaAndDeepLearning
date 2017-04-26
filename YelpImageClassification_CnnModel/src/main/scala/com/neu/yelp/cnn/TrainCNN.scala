package com.neu.yelp.cnn

import java.util.Random
import com.neu.yelp.postprocessing.MakeND4JDataSet.makeND4JDataSet
import com.neu.yelp.postprocessing.TransformData
import org.deeplearning4j.api.storage.StatsStorage
import org.deeplearning4j.datasets.iterator.MultipleEpochsIterator
import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator
import org.deeplearning4j.eval.Evaluation
import org.deeplearning4j.nn.api.OptimizationAlgorithm
import org.deeplearning4j.nn.conf.layers.setup.ConvolutionLayerSetup
import org.deeplearning4j.nn.conf.layers.{ConvolutionLayer, DenseLayer, OutputLayer, SubsamplingLayer}
import org.deeplearning4j.nn.conf.{MultiLayerConfiguration, NeuralNetConfiguration}
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.nn.weights.WeightInit
import org.deeplearning4j.optimize.api.IterationListener
import org.deeplearning4j.optimize.listeners.ScoreIterationListener
import org.deeplearning4j.ui.api.UIServer
import org.deeplearning4j.ui.stats.StatsListener
import org.deeplearning4j.ui.storage.InMemoryStatsStorage
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.dataset.SplitTestAndTrain
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.lossfunctions.LossFunctions
import scala.collection.JavaConverters._

/**
  * Created by Pranay on 3/29/2017
  * Modified by Kunal on 4/15/2017
  */
object TrainCNN {

  /***
    *
    * @param transformedData
    * @param bizLabel
    * @param saveCNN
    * @param uIServer
    * @return
    */
  def trainModel(transformedData: TransformData, bizLabel:Int = 1, saveCNN: Boolean ,uIServer: UIServer): MultiLayerNetwork ={

    // Convert Dataset to ND4J Dataset
    // Here the tranformation will be performed which was lazy initially
    val ndds = makeND4JDataSet(transformedData, bizLabel)

    // This will load Model Training Stats on The UI
    val statsStorage:StatsStorage = new InMemoryStatsStorage()
    uIServer.attach(statsStorage)

    println("Start training....")
    println("Biz Label to train for : " + bizLabel)

    val begintime = System.currentTimeMillis()
    println("Begin time: " + begintime)

    val nfeatures = ndds.getFeatures.getRow(0).length // hyper, hyper parameter
    val numRows =  Math.sqrt(nfeatures).toInt // numRows * numColumns must equal columns in initial data * channels
    val numColumns = Math.sqrt(nfeatures).toInt // numRows * numColumns must equal columns in initial data * channels
    val nChannels = 1 // would be 3 if color image w R,G,B
    val outputNum = 2 // # of classes (# of columns in output)
    val iterations = 1 // no of iterations for training
    val splitTrainNum = math.ceil(ndds.numExamples*0.8).toInt // 80/20 training/test split
    val seed = 123 // initial seed value
    val listenerFreq = 1
    val nepochs = 20  // no of epochs to be done on training dataset
    val nbatch = 128 // recommended between 16 and 128, this will be size of mini batch


    println("Dataset Rows: " + ndds.getFeatures.size(0))
    println("Feature Columns: " + ndds.getFeatures.size(1))


    println("Normalize data....")
    ndds.normalize()
    ndds.normalizeZeroMeanZeroUnitVariance() // this changes ds
    println("Loaded " + ndds.labelCounts)
    Nd4j.shuffle(ndds.getFeatureMatrix, new Random(seed), 1) // this changes ds.  Shuffles rows
    Nd4j.shuffle(ndds.getLabels, new Random(seed), 1) // this changes ds.  Shuffles labels accordingly
    val trainTest: SplitTestAndTrain = ndds.splitTestAndTrain(splitTrainNum, new Random(seed)) // Random Seed not needed here


    // creating epoch dataset iterator
    val dsiterTr = new ListDataSetIterator(trainTest.getTrain.asList(), nbatch)
    val dsiterTe = new ListDataSetIterator(trainTest.getTest.asList(), nbatch)
    val epochitTr: MultipleEpochsIterator = new MultipleEpochsIterator(nepochs, dsiterTr)
    val epochitTe: MultipleEpochsIterator = new MultipleEpochsIterator(nepochs, dsiterTe)

    //Set a neural network configuration with multiple layers
    val builder: MultiLayerConfiguration.Builder = new NeuralNetConfiguration.Builder()
      .seed(seed)
      .iterations(iterations) // Training iterations as above
      //.regularization(true)
      .miniBatch(true)
      //.l2(0.0005)
      .learningRate(0.01)
      //.weightInit(WeightInit.XAVIER)
      .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
      //.updater(Updater.NESTEROVS)
      .momentum(0.9)
      .list()
      .layer(0, new ConvolutionLayer.Builder(6, 6)
        //nIn and nOut specify depth. nIn here is the nChannels and nOut is the number of filters to be applied
        .nIn(nChannels)
        .stride(2, 2)
        .nOut(20)
        .dropOut(0.5) // added for reducing over fitting of data
        .activation(Activation.RELU)
        .weightInit(WeightInit.RELU)
        .build())
      .layer(1, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
        .build())
      .layer(2, new DenseLayer.Builder()
        .nOut(40)
        .activation(Activation.RELU)
        .build())
      .layer(3, new OutputLayer.Builder(LossFunctions.LossFunction.MCXENT)
        .nOut(outputNum)
        .activation(Activation.SOFTMAX)
        .weightInit(WeightInit.XAVIER)
        .build())
      .backprop(true).pretrain(false)

    new ConvolutionLayerSetup(builder, numRows, numColumns, nChannels)
    val conf: MultiLayerConfiguration = builder.build()

    println("Build model....")
    val model: MultiLayerNetwork = new MultiLayerNetwork(conf)
    model.init()

    val scoreListerner: IterationListener = new ScoreIterationListener(listenerFreq)
    val statsListerner: IterationListener = new StatsListener(statsStorage)

    // attach the stats and score listerner to the model to show the updates on console and ui server
    model.setListeners(Seq[IterationListener](scoreListerner, statsListerner).asJava)

    println(("Fitting data on model...."))
    println("Training on " + dsiterTr.getLabels) // this might return null, acceptable
    model.fit(epochitTr)

    println("Evaluate model....")
    println("Testing on ...")
    val eval = new Evaluation(outputNum)
    while(epochitTe.hasNext) {
      val testDS = epochitTe.next(nbatch)
      val output: INDArray = model.output(testDS.getFeatureMatrix)
      eval.eval(testDS.getLabels(), output)
    }
    println(eval.stats())


    val endtime = System.currentTimeMillis()
    println("End Time: " + endtime)
    println("Computation Time: " + (endtime-begintime)/1000.0 + " seconds")

    // save the model on disk
    if(saveCNN) {
       println("Saving CNN on local disk....")
       val modelPath = "..\\Output_Models\\models_%1$s".format(bizLabel)
       LoadSaveCNN.saveCNN(model, modelPath+".json", modelPath+".bin")
    }

    // returning the model
    model


  }
}
