package com.neu.yelp.main

import java.io.File
import javax.imageio.ImageIO

import com.neu.yelp.preprocessing.{Csv2Map, ImageUtils}
import com.neu.yelp.cnn.TrainCNN.trainModel
import com.neu.yelp.postprocessing.TransformData
/**
  * Created by Pranay on 3/23/2017.
  */
object Main{

  def main(args: Array[String]): Unit = {

    val biz2LabelMap = Csv2Map.bizToLabel2Map("data\\filtered_train.csv")
    val image2BizMap = Csv2Map.photoToBizId2Map("data\\train_photo_to_biz_ids.csv", biz2LabelMap.keySet.toList)

    // for biz_id 1000, 54 image ids
    println(image2BizMap.size)

    val img2DataMap = ImageUtils.img2Map("C:\\MyFiles\\NEU\\Scala\\Project\\Data\\train_photos",image2BizMap);

    // for biz_id 1000, 54 image ids
    println(img2DataMap.size)


    val transformedData = new TransformData(img2DataMap,image2BizMap,biz2LabelMap)

    val cnn0 = trainModel(transformedData, bizLabel = 1, saveNN = "results\\models_1")

  }
}
