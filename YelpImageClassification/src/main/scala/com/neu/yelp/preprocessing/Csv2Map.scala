package com.neu.yelp.preprocessing

import scala.io.Source

/**
  * Created by Pranay on 3/24/2017.
  */
object Csv2Map {

  /** Generic function to load in CSV */

  def readcsv(csv: String): List[List[String]] = {
    val src = Source.fromFile(csv)
    src.getLines.map(x => x.split(",").toList).toList
  }

  def photoToBizId2Map(csvLocation: String , bizIds: List[String]) : Map[Int, String] = {
    val csv = readcsv(csvLocation)
    csv.drop(1)
      .filter(x=> bizIds.contains(x(1).split(" ").head))
      .map(x => (x(0).toInt, x(1).split(" ").head))
      .toMap
  }

  def bizToLabel2Map(csvLocation: String): Map[String, List[Int]] = {
    val csv = readcsv(csvLocation)
    csv.drop(1).map(x => (x(0), (x(1).split(" ").map(_.toInt)).toList)).toMap
  }

  def getUniqueBizIDForTest(csvLocation: String): List[String] = {
    val csv = readcsv(csvLocation)
    csv.drop(1).map(x => (x(0))).toList
  }
}
