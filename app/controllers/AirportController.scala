package controllers

import helpers.AuthorizationAction
import helpers.JsonHelper.jsonErrResponse
import javax.inject.{Inject, Singleton}
import models.{Regular, User}
import play.api.Logger
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import services.AirportService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class AirportController @Inject()(authorizationAction: AuthorizationAction, cc: ControllerComponents, service: AirportService) extends AbstractController(cc) {

  val logger: Logger = Logger(this.getClass)

  def importAirport() = authorizationAction.async({ request: Request[AnyContent] =>
    val jsValueUser: JsValue = Json.parse(request.session.get("user").get)
    val userObj = Json.fromJson[User](jsValueUser).getOrElse(User(0, "", "", ",", "", "", Regular))
    getServiceResponse(service.importAirport(userObj))
  })

  def getFlights() = authorizationAction.async({ request: Request[AnyContent] =>
    val from = request.body.asJson.get("from").asOpt[String]
    val to = request.body.asJson.get("to").asOpt[String]

    (from, to) match {
      case (Some(sourceCityName), Some(destinationCityName)) => getServiceResponse(service.getFlights(sourceCityName, destinationCityName))
      case (_, _) => Future(BadRequest(jsonErrResponse("Bad input")))
    }
  })

  private def getServiceResponse(serviceResponse : Future[JsObject]) = {
    serviceResponse.map(jsResponse => jsResponse.keys.contains("success") match {
      case true => Ok(jsResponse)
      case false => BadRequest(jsResponse)
    })
  }
}
