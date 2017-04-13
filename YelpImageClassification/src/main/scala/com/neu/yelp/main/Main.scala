package com.neu.yelp.main

import java.io.File
import javax.imageio.ImageIO

import com.neu.yelp.preprocessing.{Csv2Map, ImageUtils}
import com.neu.yelp.cnn.TrainCNN.trainModel
import com.neu.yelp.postprocessing.TransformData
import com.neu.yelp.cnn.TestCNN.{makePredictionOnTestData,makePredictionOnTestDataInBatches}

/**
  * Created by Pranay on 3/23/2017.
  */
object Main{

  def main(args: Array[String]): Unit = {

    /*val biz2LabelMap = Csv2Map.bizToLabel2Map("data\\filtered_train.csv")
    val image2BizMap = Csv2Map.photoToBizId2Map("data\\train_photo_to_biz_ids.csv", biz2LabelMap.keySet.toList)

    // for biz_id 1000, 54 image ids
    println(image2BizMap.size)

    val img2DataMap = ImageUtils.img2Map("photos\\train_photos",image2BizMap);

    // for biz_id 1000, 54 image ids
    println(img2DataMap.size)

    val transformedData = new TransformData(img2DataMap,image2BizMap,biz2LabelMap,"train")

    trainModel(transformedData, bizLabel = 1, saveNN = "results\\models_1")*/


    // Pre-processing for test data
    val uniqueBizIdTest = Csv2Map.getUniqueBizIDForTest("data\\uniquetest.csv")
    val image2BizMapTest = Csv2Map.photoToBizId2Map("data\\filtered_test_photo_to_biz_ids.csv", uniqueBizIdTest)
    val img2DataMapTest = ImageUtils.img2Map("D:\\ScalaClass\\Full_Yelp_Dataset\\test_photos",image2BizMapTest)
    val transformedDataTest = new TransformData(img2DataMapTest,image2BizMapTest, null, "test")

    makePredictionOnTestDataInBatches(transformedDataTest,1)





    /*val cnn1 = trainModel(transformedData, bizLabel = 1, saveNN = "results\\models_1")
    val cnn2 = trainModel(transformedData, bizLabel = 2, saveNN = "results\\models_2")
    val cnn3 = trainModel(transformedData, bizLabel = 3, saveNN = "results\\models_3")
    val cnn4 = trainModel(transformedData, bizLabel = 4, saveNN = "results\\models_4")*/


    // Run each cnn model on the test data to see whether that business matches the label or not.




  }
}
