package controllers

import executor.ExecutorSample

import javax.inject._
import play.api.mvc._

class HomeController @Inject() (cc: ControllerComponents)
  extends AbstractController(cc) {

  def index(): Action[AnyContent] = Action { implicit req =>
    ExecutorSample.newLine
    ExecutorSample.futureSample1()
    // ExecutorSample.newLine
    Ok(views.html.index())
  }

}
