package com.neu.yelp.processing

import org.nd4j.linalg.dataset.DataSet
import org.nd4s.Implicits.intMtrix2INDArray

/**
  * Created by Pranay on 3/27/2017.
  */
class MakeND4JDataSet {

  def makeDataSet(transformedData: TransformData, bizClass: Int): DataSet = {
    val alignedXData = transformedData.getImgVectors.toNDArray
    val alignedLabs = transformedData.getBizLabels.map(x => if (x.contains(bizClass)) Vector(1, 0) else Vector(0, 1)).toNDArray
    new DataSet(alignedXData, alignedLabs)
  }

}
