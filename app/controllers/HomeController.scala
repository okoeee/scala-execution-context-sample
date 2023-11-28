package controllers

import executor.ExecutorSample

import javax.inject._
import play.api.mvc._

@Singleton
class HomeController @Inject() (cc: ControllerComponents)
  extends AbstractController(cc) {

  def index(): Action[AnyContent] = Action { req =>
    ExecutorSample.newLine
    ExecutorSample.sample3
    ExecutorSample.newLine
    Ok(views.html.index())
  }

}
