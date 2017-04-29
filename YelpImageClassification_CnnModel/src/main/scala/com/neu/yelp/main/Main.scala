package com.neu.yelp.main

import com.neu.yelp.preprocessing.ImageUtils
import com.neu.yelp.cnn.TrainCNN.trainModel
import com.neu.yelp.postprocessing.TransformData
import com.neu.yelp.cnn.PredictCNN.doPredictionForLabel
import com.neu.yelp.preprocessing.Csv2Map.{bizToLabel2Map, getUniqueBizIDForTest, photoToBizId2Map}
import org.deeplearning4j.ui.api.UIServer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by Pranay on 3/23/2017
  * Modified by Kunal on 4/11/2017
  * Modified by Manasi on 4/15/2017
  */
object Main{

  def main(args: Array[String]) = {

    /*** TRAINING & TESTING ***/
    println(" 1) Starting the training and testing phase....")

    // read the business ids and their lables and generate the a map of business ids and list of labels
    val biz2LabelMap = bizToLabel2Map("..\\Input_Datasets\\filtered_train_biz_ids.csv")
    println("biz2LabelMap : " + biz2LabelMap.size)

    // read the image ids and their business ids and generate the a map of image ids and business ids
    val image2BizMap = photoToBizId2Map("..\\Input_Datasets\\train_photo_to_biz_ids.csv", biz2LabelMap.keySet.toList)
    println("image2BizMap : " + image2BizMap.size)

    // read the image vector data and generate the a map of image ids and image vector data
    // This step involves prepreprocessing of images - 1) Resizing 2) Grayling 3) Pixelating 128 x 128
    val img2DataMap = ImageUtils.img2Map("..\\..\\Input_Datasets\\train_photos",image2BizMap)
    println("img2DataMap : " + img2DataMap.size)

    // transform data is our input dataset, i.e. [image_id, business_id, image_vector_data, business_label]
    val transformedData = new TransformData(img2DataMap,image2BizMap,biz2LabelMap,"train_test")
    println("transformedData generated lazyly...")

    // start the UI Server to monitor the model training
    // localhost:9090/train
    // same object of ui server is passed, so that all the models can be seen on same session of the ui
    println("Starting the training UI on localhost:9090/train ")
    val uIServer = UIServer.getInstance()

    // train the model for each business label on the transformed data and save the model under results folder
    val cnnModel0= Future{
      trainModel(transformedData, bizLabel = 0, saveCNN= true, uIServer)
    }

    val cnnModel1=  Future{
      trainModel(transformedData, bizLabel = 1,saveCNN= true, uIServer)
    }

    val cnnModel2= Future{
      trainModel(transformedData, bizLabel = 2, saveCNN= true, uIServer)
    }

    val cnnModel3= Future {
      trainModel(transformedData, bizLabel = 3, saveCNN= true, uIServer)
    }

    val cnnModel4= Future{
      trainModel(transformedData, bizLabel = 4, saveCNN= true, uIServer)
    }

    /*** PREDICTION ***/
    println(" 2) Starting the prediction phase....")
    // fetch the list of business id for which prediction will be performed
    val unpredictedBizIds = getUniqueBizIDForTest("..\\Input_Datasets\\unpredicted_biz_ids.csv")
    println("unpredictedBizIds : " + unpredictedBizIds.size)

    // read the image ids for the unpredicted business ids and generate the a map of image ids and business ids
    val unpredictedImg2BizIdsMap = photoToBizId2Map("..\\Input_Datasets\\unpredicted_photo_to_biz_ids.csv", unpredictedBizIds)
    println("unpredictedImg2BizIdsMap : " + unpredictedImg2BizIdsMap.size)

    // read the image vector data and generate the a map of image ids and image vector data
    // This step involves prepreprocessing of images - 1) Resizing 2) Grayling 3) Pixelating 128 x 128
    val unpredictedImg2DataMap = ImageUtils.img2Map("..\\..\\Input_Datasets\\test_photos",unpredictedImg2BizIdsMap)
    println("unpredictedImg2DataMap : " + unpredictedImg2DataMap.size)

    // transform data is our input dataset, i.e. [image_id, business_id, image_vector_data, business_label]
    // for unpredicted business ids , the labels are initally empty so we will pass null
    println("transformedDataTest generating lazyly....")
    val transformedDataTest = new TransformData(unpredictedImg2DataMap,unpredictedImg2BizIdsMap, null, "predict")


    // Make predictions for this transformed test for each business label
    // Run each label model to predict if the label is valid for the business id
    println("Starting the prediction using each label's model....")

    val predictions_0: Future[List[(String, Int)]] = cnnModel0.map{
      case model0=> doPredictionForLabel(transformedDataTest, unpredictedBizIds, 0, model0)
    }

    val predictions_1: Future[List[(String, Int)]] = cnnModel1.map{
      case model1=> doPredictionForLabel(transformedDataTest, unpredictedBizIds, 1, model1)
    }

    val predictions_2: Future[List[(String, Int)]] = cnnModel2.map{
      case model2=> doPredictionForLabel(transformedDataTest, unpredictedBizIds, 2, model2)
    }

    val predictions_3: Future[List[(String, Int)]] = cnnModel3.map{
      case model3=> doPredictionForLabel(transformedDataTest, unpredictedBizIds, 3, model3)
    }

    val predictions_4: Future[List[(String, Int)]] = cnnModel4.map{
      case model4=> doPredictionForLabel(transformedDataTest, unpredictedBizIds, 4, model4)
    }

    predictions_0.onSuccess{
      case p0 : List[(String, Int)] => predictions_1.onSuccess{
        case p1 : List[(String, Int)]=> predictions_2.onSuccess {
          case p2: List[(String, Int)] => predictions_3.onSuccess {
            case p3: List[(String, Int)] => predictions_4.onSuccess {
              case p4: List[(String, Int)] =>
                /*** Analyzing the Predictions ***/
                println(" 3) Analyzing the predictions....")

                val all_predictions: List[(String, Int)] = p0 //::: p1 ::: p2 ::: p3 ::: p4
                val predictedMap: Map[String, List[Int]] = all_predictions.map(s => (s._1, s._2))
                  .groupBy(_._1)
                  .mapValues(_.map(_._2))

                  println("Final Predictions :")
                  predictedMap.foreach(println)

                  println("Business Labels predicted using Image Classification Done !!")


                  // stop the ui server after the model has been trained
                  uIServer.stop();

            }
          }
        }
      }
    }

  }

}
