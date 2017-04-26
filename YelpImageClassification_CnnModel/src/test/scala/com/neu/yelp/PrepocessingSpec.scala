package com.neu.yelp

import java.io.File
import javax.imageio.ImageIO

import com.neu.yelp.preprocessing.Csv2Map.{bizToLabel2Map, getUniqueBizIDForTest}
import com.neu.yelp.preprocessing.ImageUtils
import org.scalatest.time.{Seconds, Span}
import org.scalatest.{FlatSpec, Matchers}


/**
  * Created by Pranay on 4/26/2017.
  */
class PrepocessingSpec extends FlatSpec with Matchers  {

  behavior of "ImageUtils"

  it should "work for image2Vector" in{
    val image = new File("..\\..\\Spec\\1.jpeg");

    val imageBuf = ImageIO.read(image)
    val imageVector = ImageUtils.image2Vector(imageBuf)

    imageVector.length shouldBe (imageBuf.getHeight*imageBuf.getWidth)

  }

  it should "work for img2Map" in {

    val map = Map(1->"123",2->"123");
    val imageMap = ImageUtils.img2Map("..\\..\\Spec",map)

    imageMap.size shouldBe 1

    for(
      (k,v)<-imageMap
    )yield {
      v.length shouldBe 16384
    }
  }

  it should "work for makeSquare" in {

    val image = new File("..\\..\\Spec\\1.jpeg");
    val imageBuf = ImageIO.read(image)
    val imageCrop = ImageUtils.makeSquare(imageBuf)

    imageCrop.getHeight shouldBe imageCrop.getWidth

  }

  behavior of "Csv2Map"

  it should "work for getUniqueBizIDForTest" in {
    val unpredictedBizIds = getUniqueBizIDForTest("..\\Input_Datasets\\unpredicted_biz_ids.csv")

    unpredictedBizIds.length shouldBe 5

  }

  it should "work for bizToLabel2Map" in {
    val biz2LabelMap = bizToLabel2Map("..\\Input_Datasets\\filtered_train_biz_ids.csv")

    for(
      (k,v) <- biz2LabelMap
    )yield {
      v.length should be >= 1
    }

  }




}
