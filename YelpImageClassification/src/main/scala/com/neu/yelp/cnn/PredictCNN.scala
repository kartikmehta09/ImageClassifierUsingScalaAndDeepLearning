package com.neu.yelp.cnn


import com.neu.yelp.postprocessing.{MakeND4JDataSet, TransformData}
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4s.Implicits._
import com.neu.yelp.cnn.LoadSaveCNN.loadCNN
import org.deeplearning4j.datasets.iterator.MultipleEpochsIterator
import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.nd4j.linalg.dataset.DataSet

/**
  * Created by ManasiLaddha on 4/6/2017.
  * Modified by Kunal on 4/11/2017
  */
object PredictCNN {
  /**
    *
     * @param transformData
    * @param modelNumber
    */
  def doPredictionForLabel(transformData:TransformData, unpredictedBizIds: List[String], modelNumber: Int, model: MultiLayerNetwork ): List[(String, Int)] ={

    //generate the modelPath
    val modelPath = "..\\..\\Output_Models\\models_%1$s".format(modelNumber)
    // load model only if the model is not in the memory
    val cnnModel =
      if(model == null)
        loadCNN(modelPath+".json", modelPath+".bin")
      else model

    var predictionResults: List[(String, Int)] =  List[(String, Int)]()

    for( bid <- unpredictedBizIds){
      println("----prediction for business----  " + bid)
      val ndds : DataSet = makeDataSet(transformData, bid, modelNumber)
      // we want to load the unpredicted data in batches so as to avoid memory overflow exception
      // 128 is the batch size
      val listNDDS = ndds.asList()
      val nddsIterator = new ListDataSetIterator( listNDDS , 128)
      var runningAverage = 0
      while(nddsIterator.hasNext) {
        val unpredicted_ndds = nddsIterator.next()
        val predictionTest: INDArray = cnnModel.output(unpredicted_ndds.getFeatureMatrix)
        println("The predicted sumber value--" +predictionTest.sumNumber().intValue())
        runningAverage += predictionTest.sumNumber().intValue() - (predictionTest.rows() * modelNumber)
        println("The running average is ==" +runningAverage)
      }

      // If 50% of the results predict true for the label, then the business will have that label
      val avg_true = runningAverage / listNDDS.size();
      if(avg_true >= 0.5)
        predictionResults = predictionResults.::((bid, modelNumber))
        println(predictionResults)
    }

    predictionResults
  }

  /**
    * This function will generate ND4J Dataset from the transform data
    * The Image Vectors are the features
    * The labels are boolean false by default
    * @param td
    * @param bizLabel
    * @return
    */
  def makeDataSet(td: TransformData, bizId: String, bizLabel: Int): DataSet = {
    val filteredTransformData = td.data.filter(_._2.equals(bizId))
    val alignedXData = filteredTransformData.map(_._3).toNDArray
    val alignedLabs = filteredTransformData.map(x => Vector(0, 1)).toNDArray
    new DataSet(alignedXData, alignedLabs)
  }







}
