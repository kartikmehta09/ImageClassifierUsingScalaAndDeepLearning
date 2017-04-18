package service

import model.{Business, Businesses}
import scala.concurrent.Future

object BusinessService {

  def addBusiness(business: Business): Future[String] = {
    Businesses.add(business)
  }

  def deleteBusiness(id: Long): Future[Int] = {
    Businesses.delete(id)
  }

  def getBusiness(id: Long): Future[Option[Business]] = {
    Businesses.get(id)
  }

  def getAllBusinesses(label: String):  Future[Seq[Business]] = {
    Businesses.getAllBusiness(label)
  }

  def listAllBusinessess: Future[Seq[Business]] = {
    Businesses.listAll
  }
}
