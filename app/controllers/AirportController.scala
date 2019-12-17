package controllers

import helpers.AuthorizationAction
import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import services.AirportService

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class AirportController @Inject()(authorizationAction: AuthorizationAction, cc: ControllerComponents, service: AirportService) extends AbstractController(cc) {

  val logger: Logger = Logger(this.getClass())

  def importAirport() = authorizationAction.async({ request: Request[AnyContent] =>
    service.importAirport(request.session.get("usertype").get).map(jsResponse => jsResponse.keys.contains("success") match {
      case true => Ok(jsResponse)
      case false => BadRequest(jsResponse)
    })
  })

  def getFlights() = authorizationAction.async({ request: Request[AnyContent] =>
    service.getFlights(request.body.asJson).map(jsResponse => jsResponse.keys.contains("success") match {
      case true => Ok(jsResponse)
      case false => BadRequest(jsResponse)
    })
  })
}
