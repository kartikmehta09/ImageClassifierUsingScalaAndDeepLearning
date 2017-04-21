package com.neu.yelp.cnn


import java.io.{File, PrintWriter}

import com.neu.yelp.postprocessing.{MakeND4JDataSet, TransformData}
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4s.Implicits._
import com.neu.yelp.cnn.LoadSaveCNN.loadCNN
import org.deeplearning4j.datasets.iterator.MultipleEpochsIterator
import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.nd4j.linalg.dataset.DataSet
import com.neu.yelp.cnn.Scoring.{aggImgScores2Biz, scoreModel}

import scala.concurrent.Future

/**
  * Created by ManasiLaddha on 4/6/2017
  * Modified by Kunal on 4/11/2017
  */
object PredictCNN {
  /**
    *
    * @param transformData
    * @param modelNumber
    */
  def doPredictionForLabel(transformData:TransformData, unpredictedBizIds: List[String], modelNumber: Int, model: MultiLayerNetwork ,writer:PrintWriter): List[(String, Int)]={

    //generate the modelPath
    val modelPath = "..\\Output_Models\\models_%1$s".format(modelNumber)
    // load model only if the model is not in the memory
    val cnnModel =
      if(model == null) {
        println("CNN Model need to be loaded..")
        loadCNN(modelPath + ".json", modelPath + ".bin")
      }
      else model

    var predictionResults: List[(String, Int)] =  List[(String, Int)]()


    for( bid <- unpredictedBizIds){
      println("----Prediction for business----  " + bid)
      val filteredTransformData = transformData.data.filter(_._2.contains(bid))

      val ndds : DataSet = makeDataSet(filteredTransformData, bid)

      val listNDDS = ndds.asList()
      println("Dataset size : " + listNDDS.size())
      val nddsIterator = new ListDataSetIterator( listNDDS , 128)

      val predictedScores = scoreModel(cnnModel, nddsIterator)
      println("predicted data: " + predictedScores)

      val labelProbabilty = aggImgScores2Biz(predictedScores)

      writer.write(bid+","+modelNumber+","+labelProbabilty+","+(if(labelProbabilty >= 0.94)1 else 0)+"\n")
      // If 50% of the results predict true for the label, then the business will have that label
      if(labelProbabilty >= 0.94)
        predictionResults = predictionResults.::((bid, modelNumber))
        println(predictionResults)
    }

    predictionResults
  }

  /*def doPredictionForLabel(transformData:TransformData, unpredictedBizIds: List[String], modelNumber: Int, model: MultiLayerNetwork ): List[(String, Int)] ={

    //generate the modelPath
    val modelPath = "..\\Output_Models\\models_%1$s".format(modelNumber)
    // load model only if the model is not in the memory
    val cnnModel =
      if(model == null) {
        println("CNN Model need to be loaded..")
        loadCNN(modelPath + ".json", modelPath + ".bin")
      }
      else model

    var predictionResults: List[(String, Int)] =  List[(String, Int)]()

    for( bid <- unpredictedBizIds) {
      println("----Prediction for business----  " + bid)
      val ndds: INDArray = makeDataSet(transformData, bid)
      // we want to load the unpredicted data in batches so as to avoid memory overflow exception
      // 128 is the batch size
      val predictionTest: INDArray = scoreModel(cnnModel,ndds)
      println("predictionTest : " + predictionTest)
      val sum = predictionTest.sumNumber().intValue() - (predictionTest.rows() * modelNumber)
      println("The running average : " + sum)

      // If 50% of the results predict true for the label, then the business will have that label
      val avg_true = sum / ndds.rows();
      if(avg_true >= 0.5)
        predictionResults = predictionResults.::((bid, modelNumber))

      println(predictionResults)

    }
       predictionResults
  }*/
  /**
    * This function will generate ND4J Dataset from the transform data
    * The Image Vectors are the features
    * The labels are boolean false by default
    * @param filteredTransformData
    * @return
    */
  def makeDataSet(filteredTransformData: List[(Int,String,Vector[Int],List[Int])], bizId: String): DataSet = {
    val alignedXData = filteredTransformData.map(_._3).toNDArray
    val alignedLabs = filteredTransformData.map(x => Vector(0, 1)).toNDArray
    new DataSet(alignedXData, alignedLabs)
  }

  /*def makeDataSet(td:TransformData, bizId:String):INDArray={
    val filteredTransformData = td.data.filter(_._2.equals(bizId))
    filteredTransformData.map(_._3).toNDArray
  }*/





}
