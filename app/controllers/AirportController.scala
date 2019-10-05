package controllers

import api.DataBase
import api.JsonHelper.{jsonErrResponse, jsonSuccessResponse}
import com.typesafe.config.ConfigFactory
import javax.inject.{Inject, Singleton}
import models.{Airport, Route}
import play.api.Logger
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import services.AirportService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.Source

@Singleton
class AirportController @Inject()(cc: ControllerComponents, service: AirportService) extends AbstractController(cc) {

  val logger: Logger = Logger(this.getClass())

  def importAirport() = Action.async({ request: Request[AnyContent] =>
    request.session
      .get("usertype")
      .map { usertype =>
        service.importAirport(usertype).map(jsResponse => jsResponse.keys.contains("success") match {
          case true => Ok(jsResponse)
          case false => BadRequest(jsResponse)
        })
      }
      .getOrElse {
        logger.info("You are not signed in!")
        Future(BadRequest(jsonErrResponse("You are not signed in!")))
      }
  })
}
