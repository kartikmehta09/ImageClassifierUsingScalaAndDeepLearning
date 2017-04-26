package com.neu.yelp.cnn


import java.io.{File, PrintWriter}

import org.nd4s.Implicits._
import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.nd4j.linalg.dataset.DataSet
import com.neu.yelp.cnn.Scoring.{aggregateScores, scoreModel}
import com.neu.yelp.postprocessing.TransformData
import com.neu.yelp.cnn.LoadSaveCNN.loadCNN

/**
  * Created by ManasiLaddha on 4/6/2017
  * Modified by Kunal on 4/11/2017
  */
object PredictCNN {
  /**
    * This function is used to do predictions for the biz label on the feature matrix generated
    * Predictions are done in batches to ensure scalability
    * @param transformData
    * @param modelNumber
    */
  def doPredictionForLabel(transformData:TransformData, unpredictedBizIds: List[String], modelNumber: Int, model: MultiLayerNetwork ): List[(String, Int)]={

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

    val writer = new PrintWriter(new File("results\\final_predictions_%1$s.csv".format(modelNumber) ))
    writer.write("businessId,label,averageScore,prediction\n")

    for( bid <- unpredictedBizIds){
      println("----Prediction for business----  " + bid)
      val filteredTransformData = transformData.data.filter(_._2.contains(bid))

      val ndds : DataSet = makeDataSet(filteredTransformData, bid)

      val listNDDS = ndds.asList()
      println("Dataset size : " + listNDDS.size())
      // 128 is the batch size
      val nddsIterator = new ListDataSetIterator( listNDDS , 128)

      val predictedScores = scoreModel(cnnModel, nddsIterator)
      println("predicted data: " + predictedScores)

      val labelProbabilty = aggregateScores(predictedScores)

      writer.write(bid+","+modelNumber+","+labelProbabilty+","+(if(labelProbabilty >= 0.94)1 else 0)+"\n")
      // If 94% of the results predict true for the label, then the business will have that label
      if(labelProbabilty >= 0.94)
        predictionResults = predictionResults.::((bid, modelNumber))
        println(predictionResults)
    }

    writer.close()

    predictionResults
  }


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

}
