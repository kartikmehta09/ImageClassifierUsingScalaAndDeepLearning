package com.neu.yelp.processing

import java.awt.image.BufferedImage

import org.imgscalr.Scalr

/**
  * Created by Pranay on 3/23/2017.
  */
object imageUtils {

  // make image square
  def makeSquare(img:BufferedImage) = {
    val w = img.getWidth
    val h = img.getHeight
    val dim = List(w, h).min

    img match {
      case x if w == h => img
      case x if w > h => Scalr.crop(img, (w-h)/2, 0, dim, dim)
      case x if w < h => Scalr.crop(img, 0, (h-w)/2, dim, dim)
    }
  }

  // resize pixels
  def resizeImg(img:BufferedImage,width: Int, height: Int) = {
    Scalr.resize(img, Scalr.Method.BALANCED, width, height)
  }

  def pixels2gray(red: Int, green:Int, blue: Int): Int = (red + green + blue) / 3

  def image2Vector(img:BufferedImage): Vector[Int] ={
    val w = img.getWidth
    val h = img.getHeight
    for{
      w1 <- (0 until w).toVector
      h1 <- (0 until h).toVector
    }yield {
      //println(w1+" , "+h1)
      val col = img.getRGB(w1, h1)
      val red =  (col & 0xff0000) / 65536//2^16
      val green = (col & 0xff00) / 256//2^8
      val blue = (col & 0xff)
      pixels2gray(red,green,blue)
    }
  }

}
