package controllers

import model.{Business, BusinessForm}
import play.api.mvc._
import scala.concurrent.Future
import service.BusinessService
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.Logger._


class ApplicationController extends Controller {

  def index = Action.async { implicit request =>
    BusinessService.listAllBusinessess map { businessess =>
      Ok(views.html.index(BusinessForm.form, businessess))
    }
  }

  def getBusiness() = Action.async { implicit request =>
    BusinessForm.form.bindFromRequest.fold(
      // if any error in submitted data
      errorForm => Future.successful(Ok(views.html.index(errorForm, Seq.empty[Business]))),
      data => {
        logger.info("getBusiness for : " + data.biz_name)
        BusinessService.getAllBusinesses(data.biz_name).map(res =>
          Ok(views.html.index(BusinessForm.form, res))
        )
      })
  }




}

