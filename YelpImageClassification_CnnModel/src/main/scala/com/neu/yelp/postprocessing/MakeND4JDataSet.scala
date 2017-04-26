package com.neu.yelp.postprocessing

import org.nd4j.linalg.dataset.DataSet
import org.nd4s.Implicits._

/**
  * Created by Pranay on 3/27/2017
  */
object MakeND4JDataSet {
  /***
    * This will transform the feture matrix to ND4J Datasst
    * @param transformedData
    * @param bizLabel
    * @return
    */
  def makeND4JDataSet(transformedData: TransformData, bizLabel: Int): DataSet = {
    println("Making ND4J Dataset....")
    val alignedXData = transformedData.getImgVectors.toNDArray
    //the labels (these should be binarized label matrices such that the specified label has a value of 1 in the desired column with the label)
    val alignedLabs = transformedData.getBizLabels.map(x => if (x.contains(bizLabel)) Vector(1, 0) else Vector(0, 1)).toNDArray
    new DataSet(alignedXData, alignedLabs)
  }

}
