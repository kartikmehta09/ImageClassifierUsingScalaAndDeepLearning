package com.neu.yelp.postprocessing

/**
  * Created by Pranay on 3/27/2017.
  */
class TransformData(img2DataMap:Map[Int,Vector[Int]],image2BizMap:Map[Int,String],biz2LabelMap:Map[String,List[Int]]) {

  def transformBizImageIds(img2DataMap:Map[Int,Vector[Int]],image2BizMap:Map[Int,String]): List[(Int,String,Vector[Int])] ={
    val rowIndices = img2DataMap.keySet.toList
    for{
      pid <- rowIndices
      val bizId = image2BizMap.get(pid);
      val bid = if(bizId != None) {bizId.get} else "-1"
      if(img2DataMap.keys.toSet.contains(pid) && bizId!=None)
    }yield {
      (pid,bid,img2DataMap.get(pid).get)
    }
  }


  def transform(img2DataMap:Map[Int,Vector[Int]],image2BizMap:Map[Int,String],biz2LabelMap:Map[String,List[Int]]): List[(Int,String,Vector[Int],List[Int])] ={
    val bizImageIds = transformBizImageIds(img2DataMap,image2BizMap)
    for{
      (pid,bid,data) <- bizImageIds
    }yield {
      val labs = if(biz2LabelMap.keySet.contains(bid)) biz2LabelMap.get(bid).get else List[Int]()
      (pid,bid,data,labs)
    }
  }

  lazy val data = transform(img2DataMap,image2BizMap,biz2LabelMap);

  def getImgIds = data.map(_._1)
  def getBizIds = data.map(_._2)
  def getImgVectors = data.map(_._3)
  def getBizLabels = data.map(_._4)

}
