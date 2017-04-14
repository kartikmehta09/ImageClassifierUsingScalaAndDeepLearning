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

    val cnnModel = loadCNN("..\\..\\Output_Models\\models_%1$s.json".format(modelNumber),"..\\..\\Output_Models\\models_%1$s.bin".format(modelNumber))
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

      // avg ~ 50%
      val avg_true = runningAverage / listNDDS.size();
      if(avg_true >= 0.5)
        predictionResults.::((bid, modelNumber))
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
