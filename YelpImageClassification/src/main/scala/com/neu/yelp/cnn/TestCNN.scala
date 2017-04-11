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
object TestCNN {

  def makePredictionOnTestData(transformData:TransformData,modelNumber: Int): Unit ={

     val nddarr = makeTestINDArray(transformData)

     val modelTest = loadCNN("results\\models_%1$s.json".format(modelNumber),"results\\models_%1$s.bin".format(modelNumber))

     val predictionTest = Scoring.scoreModel(modelTest,nddarr)

     println(predictionTest)

  }


  def makePredictionOnTestDataInBatches(transformData:TransformData,modelNumber: Int): Unit ={

    val ds : DataSet = makeTestDataset(transformData)
    val dsiterTest = new ListDataSetIterator( ds.asList() , 128)
    val epochitTest : MultipleEpochsIterator = new MultipleEpochsIterator(5, dsiterTest)
    val modelTest = loadCNN("results\\models_%1$s.json".format(modelNumber),"results\\models_%1$s.bin".format(modelNumber))

    while(epochitTest.hasNext) {
      val testDS = epochitTest.next(128)
      val predictionTest: INDArray = modelTest.output(testDS.getFeatureMatrix)
      println(predictionTest)
    }


   /* val predictionTest = Scoring.scoreModel(modelTest, dsiterTest)

    println(predictionTest)*/

  }

  def makeTestINDArray(transformData: TransformData): INDArray ={
     //new DataSet(transformData.getImgVectors.toNDArray, null)
    transformData.getImgVectors.toNDArray
  }

  def makeTestDataset(transformData: TransformData): DataSet ={
    val ds: DataSet = new DataSet(transformData.getImgVectors.toNDArray, null)
   // ds.addFeatureVector(transformData.getImgVectors.toNDArray)
    ds
    //transformData.getImgVectors.toNDArray
  }







}
