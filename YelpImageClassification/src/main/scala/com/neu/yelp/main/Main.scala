package com.neu.yelp.main

import java.io.File
import javax.imageio.ImageIO
import com.neu.yelp.processing.imageUtils

/**
  * Created by Pranay on 3/23/2017.
  */
object Main{

  def main(args: Array[String]): Unit = {

    val imgArray = new File("C:\\Users\\Pranay\\Pictures\\Scala").listFiles.filter(_.getName.endsWith(".jpeg"))

    for{
      ia <- imgArray
    }yield{
      val img = ImageIO.read(ia)
      val imgSquare = imageUtils.makeSquare(img)
      val imgResize = imageUtils.resizeImg(imgSquare,128,128)
      val imgVector = imageUtils.image2Vector(imgResize)
      imgVector.map(println)
      println("------------------------------")
    }

  }
}
