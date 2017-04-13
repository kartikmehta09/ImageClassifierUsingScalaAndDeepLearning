package com.neu.yelp.cnn


import com.neu.yelp.postprocessing.{MakeND4JDataSet, TransformData}
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4s.Implicits._
import com.neu.yelp.cnn.LoadSaveCNN.loadCNN
import org.deeplearning4j.datasets.iterator.MultipleEpochsIterator
import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator
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
  def doPredictionForLabel(transformData:TransformData, unpredictedBizIds: List[String], modelNumber: Int): List[(String, Int)] ={

    val cnnModel = loadCNN("results\\models_%1$s.json".format(modelNumber),"results\\models_%1$s.bin".format(modelNumber))
    var predictionResults: List[(String, Int)] = null

    for( bid <- unpredictedBizIds){
      val ndds : DataSet = makeDataSet(transformData, bid, modelNumber)
      // we want to load the unpredicted data in batches so as to avoid memory overflow exception
      // 128 is the batch size
      val listNDDS = ndds.asList()
      val nddsIterator = new ListDataSetIterator( listNDDS , 128)
      var runningAverage = 0
      while(nddsIterator.hasNext) {
        val unpredicted_ndds = nddsIterator.next()
        val predictionTest: INDArray = cnnModel.output(unpredicted_ndds.getFeatureMatrix)
        runningAverage += predictionTest.sumNumber().intValue() - (predictionTest.length * modelNumber)
      }

      // avg ~ 50%
      val avg_true = runningAverage / listNDDS.size();
      if(avg_true >= 50)
        predictionResults.::((bid, modelNumber))
    }

    predictionResults
  }

  /**
    * This function will generate ND4J Dataset from the transform data
    * The Image Vectors are the features
    * The labels are boolean false by default
    * @param transformedData
    * @param bizLabel
    * @return
    */
  def makeDataSet(transformedData: TransformData, bizId: String, bizLabel: Int): DataSet = {
    val filteredTransformData = transformedData.data.filter(_._2.equals(bizId))
    val alignedXData = filteredTransformData.map(_._3).toNDArray
    val alignedLabs = filteredTransformData.map(x => Vector(0, bizLabel)).toNDArray
    new DataSet(alignedXData, alignedLabs)
  }







}
