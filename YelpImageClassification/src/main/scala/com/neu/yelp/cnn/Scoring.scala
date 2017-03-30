package com.neu.yelp.cnn

import com.neu.yelp.postprocessing.TransformData
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.nd4j.linalg.api.ndarray.INDArray

object Scoring {
  
  def scoreModel(model: MultiLayerNetwork, ds: INDArray) = {
    model.output(ds)
  }
  
  /** Take model predictions from scoreModel and merge with transformedData*/
  
  def aggImgScores2Biz(scores: INDArray, transformData: TransformData ) = {
    assert(scores.size(0) == transformData.data.length, "transformedData and scores length are different.  They must be equal")
    def getRowIndices4Biz(mylist: List[String], mybiz: String): List[Int] = mylist.zipWithIndex.filter(x => x._1 == mybiz).map(_._2)
    def mean(xs: List[Double]) = xs.sum / xs.size

    transformData.getBizIds.distinct.map(x => (x, {
      val irows = getRowIndices4Biz(transformData.getBizIds, x)
      val ret = for(row <- irows) yield scores.getRow(row).getColumn(1).toString.toDouble
      mean(ret)
    }))
    
  }
  
}