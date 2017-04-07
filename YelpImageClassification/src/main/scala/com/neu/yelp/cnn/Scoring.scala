package com.neu.yelp.cnn

import com.neu.yelp.postprocessing.TransformData
import org.bytedeco.javacpp.hdf5.DataSet
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator

object Scoring {
  
  def scoreModel(model: MultiLayerNetwork, ds: INDArray) : INDArray = {
    model.output(ds)
  }

  def scoreModel(model: MultiLayerNetwork, ds: DataSetIterator) : INDArray = {
    model.output(ds)
  }
  
  /** Take model predictions from scoreModel and merge with transformedData*/
  
  def aggImgScores2Biz(scores: INDArray, transformData: TransformData ) : List[(String, Double)] = {
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