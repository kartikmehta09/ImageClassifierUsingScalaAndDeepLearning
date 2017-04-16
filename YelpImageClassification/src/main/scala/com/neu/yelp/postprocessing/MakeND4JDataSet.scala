package com.neu.yelp.postprocessing

import org.nd4j.linalg.dataset.DataSet
import org.nd4s.Implicits._

/**
  * Created by Pranay on 3/27/2017
  */
object MakeND4JDataSet {

  def makeDataSet(transformedData: TransformData, bizLabel: Int): DataSet = {
    println("Making ND4J Dataset....")
    val alignedXData = transformedData.getImgVectors.toNDArray
    val alignedLabs = transformedData.getBizLabels.map(x => if (x.contains(bizLabel)) Vector(1, 0) else Vector(0, 1)).toNDArray
    new DataSet(alignedXData, alignedLabs)
  }

}
