package com.neu.yelp.cnn

import java.io.{DataOutputStream, File}
import java.nio.file.{Files, Paths}
import java.util.Random

import com.neu.yelp.postprocessing.MakeND4JDataSet.makeDataSet
import com.neu.yelp.postprocessing.TransformData
import org.apache.commons.io.FileUtils
import org.deeplearning4j.api.storage.StatsStorage
import org.deeplearning4j.datasets.iterator.MultipleEpochsIterator
import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator
import org.deeplearning4j.eval.Evaluation
import org.deeplearning4j.nn.api.OptimizationAlgorithm
import org.deeplearning4j.nn.conf.inputs.InputType
import org.deeplearning4j.nn.conf.layers.setup.ConvolutionLayerSetup
import org.deeplearning4j.nn.conf.layers.{ConvolutionLayer, DenseLayer, OutputLayer, SubsamplingLayer}
import org.deeplearning4j.nn.conf.{MultiLayerConfiguration, NeuralNetConfiguration, Updater}
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
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
/**
  * Created by Pranay on 3/29/2017.
  */
object TrainCNN {

  def trainModel(transformedData: TransformData, bizLabel:Int = 1, saveNN:String= ""): Unit ={
    // convert dataset to ND4J dataset
    val ndds = makeDataSet(transformedData, bizLabel)

    //start the UI
    val uIServer = UIServer.getInstance()
    val statsStorage:StatsStorage = new InMemoryStatsStorage()
    uIServer.attach(statsStorage)

    println("start training!!")
    println("class for training: " + bizLabel)

    val begintime = System.currentTimeMillis()

    //lazy val log = LoggerFactory.getLogger(TrainCNN.getClass)
    //log.info("Begin time: " + java.util.Calendar.getInstance().getTime())
    println("Begin time: " + java.util.Calendar.getInstance().getTime())

    val nfeatures = ndds.getFeatures.getRow(0).length // hyper, hyper parameter
    val numRows =  Math.sqrt(nfeatures).toInt // numRows * numColumns must equal columns in initial data * channels
    val numColumns = Math.sqrt(nfeatures).toInt // numRows * numColumns must equal columns in initial data * channels
    val nChannels = 1 // would be 3 if color image w R,G,B
    val outputNum = 2 // # of classes (# of columns in output)
    val iterations = 1
    val splitTrainNum = math.ceil(ndds.numExamples*0.8).toInt // 80/20 training/test split
    val seed = 123
    val listenerFreq = 1
    val nepochs = 5
    val nbatch = 128 // recommended between 16 and 128

    //val nOutPar = 500 // default was 1000.  # of output nodes in first layer

    println("rows: " + ndds.getFeatures.size(0))
    println("columns: " + ndds.getFeatures.size(1))

    /**
      *Set a neural network configuration with multiple layers
      */
    //log.info("Load data....")
    println("Load data....")

    ndds.normalize()
    //ndds.normalizeZeroMeanZeroUnitVariance() // this changes ds
    println("Loaded " + ndds.labelCounts)
    Nd4j.shuffle(ndds.getFeatureMatrix, new Random(seed), 1) // this changes ds.  Shuffles rows
    Nd4j.shuffle(ndds.getLabels, new Random(seed), 1) // this changes ds.  Shuffles labels accordingly
    val trainTest: SplitTestAndTrain = ndds.splitTestAndTrain(splitTrainNum, new Random(seed)) // Random Seed not needed here


    // creating epoch dataset iterator
    val dsiterTr = new ListDataSetIterator(trainTest.getTrain.asList(), nbatch)
    val dsiterTe = new ListDataSetIterator(trainTest.getTest.asList(), nbatch)
    val epochitTr: MultipleEpochsIterator = new MultipleEpochsIterator(nepochs, dsiterTr)
    val epochitTe: MultipleEpochsIterator = new MultipleEpochsIterator(nepochs, dsiterTe)

    val builder: MultiLayerConfiguration.Builder = new NeuralNetConfiguration.Builder()
      .seed(seed)
      .iterations(iterations) // Training iterations as above
      .regularization(true)
      //.miniBatch(true)
      .l2(0.0005)
      .learningRate(0.01)
      .weightInit(WeightInit.XAVIER)
      .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
      .updater(Updater.NESTEROVS)
      .momentum(0.9)
      .list()
      .layer(0, new ConvolutionLayer.Builder(5, 5)
        //nIn and nOut specify depth. nIn here is the nChannels and nOut is the number of filters to be applied
        .nIn(nChannels)
        .stride(1, 1)
        .nOut(20)
        .activation(Activation.IDENTITY)
        .build())
      .layer(1, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
        .kernelSize(2,2)
        .stride(2,2)
        .build())
      .layer(2, new ConvolutionLayer.Builder(5, 5)
        //Note that nIn need not be specified in later layers
        .stride(1, 1)
        .nOut(50)
        .activation(Activation.IDENTITY)
        .build())
      .layer(3, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
        .kernelSize(2,2)
        .stride(2,2)
        .build())
      .layer(4, new DenseLayer.Builder().activation(Activation.RELU)
        .nOut(500).build())
      .layer(5, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
        .nOut(outputNum).build())
      .backprop(true).pretrain(false)

    new ConvolutionLayerSetup(builder, numRows, numColumns, nChannels)

    //builder.setInputType(InputType.convolutional(numRows, numColumns, nChannels))

    val conf: MultiLayerConfiguration = builder.build()

    //log.info("Build model....")
    println("Build model....")
    val model: MultiLayerNetwork = new MultiLayerNetwork(conf)
    model.init()

    val scoreListerner: IterationListener = new ScoreIterationListener(listenerFreq)
    val statsListerner: IterationListener = new StatsListener(statsStorage)

    model.setListeners(Seq[IterationListener](scoreListerner, statsListerner).asJava)

    //log.info("Train model....")
    println(("Train model...."))
    println("Training on " + dsiterTr.getLabels) // this might return null
    model.fit(epochitTr)

    // I think this could be done without an iterator and batches.
    //log.info("Evaluate model....")
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
    //log.info("End time: " + java.util.Calendar.getInstance().getTime())
    println("End time: " + java.util.Calendar.getInstance().getTime())
    //log.info("computation time: " + (endtime-begintime)/1000.0 + " seconds")
    println("computation time: " + (endtime-begintime)/1000.0 + " seconds")
    //log.info("Write results....")
    println("Write results....")

    if(!saveNN.isEmpty) {
      // model config
      FileUtils.write(new File(saveNN + ".json"), model.getLayerWiseConfigurations().toJson())

      // model parameters
      val dos: DataOutputStream = new DataOutputStream(Files.newOutputStream(Paths.get(saveNN + ".bin")))
      Nd4j.write(model.params(), dos)
    }

    //log.info("****************Example finished********************")
    println("****************Example finished********************")

  }
}
