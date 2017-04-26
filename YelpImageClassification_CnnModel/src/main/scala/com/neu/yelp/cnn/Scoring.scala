package com.neu.yelp.cnn

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator

/**
  * Created by ManasiLaddha on 4/11/2017
  *
  */
object Scoring {

  def scoreModel(model: MultiLayerNetwork, ds: INDArray) : INDArray = {
    model.output(ds)
  }

  def scoreModel(model: MultiLayerNetwork, ds: DataSetIterator) : INDArray = {
    model.output(ds)
  }

  /**
    * Aggregate the prediction scores to find the average
    * @param scores
    * @return
    */
  def aggregateScores(scores: INDArray) : Double = {

    var sum=0.0
    for(row <- 0 until  scores.rows()){
      sum+=scores.getRow(row).getColumn(0).toString.toDouble
    }
    println("aggregateScores : Sum : "+sum)
    println("aggregateScores : Average : "+sum/scores.rows())
    sum/scores.rows()
  }
  
}