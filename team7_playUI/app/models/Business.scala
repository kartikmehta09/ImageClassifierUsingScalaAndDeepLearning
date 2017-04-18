package model

import play.api.Play
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.slick.DatabaseConfigProvider
import scala.concurrent.Future
import slick.driver.JdbcProfile
import slick.driver.MySQLDriver.api._
import scala.concurrent.ExecutionContext.Implicits.global


case class Business(biz_id: Long, biz_name: String, good_for_lunch: String, good_for_dinner: String,
                    take_reservations: String, outdoor_sitting: String, restaurant_is_expensive: String)

case class BusinessFormData(biz_name: String)

object BusinessForm {

  val form = Form(
    mapping(
      "biz_name" -> nonEmptyText
    )(BusinessFormData.apply)(BusinessFormData.unapply)
  )
}

class BusinessTableDef(tag: Tag) extends Table[Business](tag, "business") {

  def biz_id = column[Long]("biz_id", O.PrimaryKey,O.AutoInc)
  def biz_name = column[String]("biz_name")
  def good_for_lunch = column[String]("good_for_lunch")
  def good_for_dinner = column[String]("good_for_dinner")
  def take_reservations = column[String]("take_reservations")
  def outdoor_sitting = column[String]("outdoor_sitting")
  def restaurant_is_expensive = column[String]("restaurant_is_expensive")

  override def * =
    (biz_id, biz_name, good_for_lunch, good_for_dinner, take_reservations, outdoor_sitting, restaurant_is_expensive) <> (Business.tupled, Business.unapply)
}

object Businesses {

  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)

  val businessess = TableQuery[BusinessTableDef]

  def add(b: Business): Future[String] = {
    dbConfig.db.run(businessess += b).map(res => "Business successfully added").recover {
      case ex: Exception => ex.getCause.getMessage
    }
  }

  def delete(id: Long): Future[Int] = {
    dbConfig.db.run(businessess.filter(_.biz_id === id).delete)
  }

  def get(id: Long): Future[Option[Business]] = {
    dbConfig.db.run(businessess.filter(_.biz_id === id).result.headOption)
  }

  def getAllBusiness(label: String): Future[Seq[Business]] = {

    if(label == "good_for_dinner")
      dbConfig.db.run(businessess.filter(_.good_for_dinner === "Y").result)
    else if (label == "good_for_lunch")
      dbConfig.db.run(businessess.filter(_.good_for_dinner  === "Y").result)
    else if (label == "outdoor_sitting")
      dbConfig.db.run(businessess.filter(_.outdoor_sitting  === "Y").result)
    else if (label == "restaurant_is_expensive")
      dbConfig.db.run(businessess.filter(_.restaurant_is_expensive  === "Y").result)
    else if (label == "take_reservations")
      dbConfig.db.run(businessess.filter(_.take_reservations  === "Y").result)
    else
      dbConfig.db.run(businessess.result)
  }

  def listAll: Future[Seq[Business]] = {
    dbConfig.db.run(businessess.result)
  }

}
