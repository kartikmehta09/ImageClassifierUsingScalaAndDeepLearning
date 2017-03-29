package com.neu.yelp.main

import java.io.File
import javax.imageio.ImageIO

import com.neu.yelp.processing.{Csv2Map, ImageUtils, TransformData}
import com.neu.yelp.cnn.TrainCNN.trainModel
/**
  * Created by Pranay on 3/23/2017.
  */
object Main{

  def main(args: Array[String]): Unit = {

    val biz2LabelMap = Csv2Map.bizToLabel2Map("C:\\MyFiles\\NEU\\Scala\\Project\\Data\\train.csv")
    val image2BizMap = Csv2Map.photoToBizId2Map("C:\\MyFiles\\NEU\\Scala\\Project\\Data\\train_photo_to_biz_ids.csv")
    val img2DataMap = ImageUtils.img2Map("C:\\MyFiles\\NEU\\Scala\\Project\\Data\\train_photos",image2BizMap);

    val transformedData = new TransformData(img2DataMap,image2BizMap,biz2LabelMap)

    val cnn0 = trainModel(transformedData, bizLabel = 0, saveNN = "C:\\MyFiles\\NEU\\Scala\\YelpImageClassification\\results\\modelsV0\\model0_img16k_epoch15_batch128_pixels64_nout100_200")
    
  }
}
