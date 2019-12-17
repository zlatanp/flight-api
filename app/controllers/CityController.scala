package controllers

import helpers.AuthorizationAction
import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import services.CityService

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class CityController @Inject()(authorizationAction: AuthorizationAction, cc: ControllerComponents, service: CityService) extends AbstractController(cc) {

  val logger: Logger = Logger(this.getClass())

  def all() = authorizationAction.async({ request: Request[AnyContent] =>
    service.all(request.body.asJson).map(jsResponse => jsResponse.keys.contains("success") match {
      case true => Ok(jsResponse)
      case false => BadRequest(jsResponse)
    })
  })

  def add() = authorizationAction.async({ request: Request[AnyContent] =>
    service.add(request.session.get("usertype").get, request.body.asJson).map(jsResponse => jsResponse.keys.contains("success") match {
      case true => Ok(jsResponse)
      case false => BadRequest(jsResponse)
    })
  })

  def comment() = authorizationAction.async({ request: Request[AnyContent] =>
    service.comment(request.session.get("name").get, request.body.asJson).map(jsResponse => jsResponse.keys.contains("success") match {
      case true => Ok(jsResponse)
      case false => BadRequest(jsResponse)
    })
  })

  def delete() = authorizationAction.async({ request: Request[AnyContent] =>
    service.delete(request.session.get("name").get, request.body.asJson).map(jsResponse => jsResponse.keys.contains("success") match {
      case true => Ok(jsResponse)
      case false => BadRequest(jsResponse)
    })
  })

  def getCity() = authorizationAction.async({ request: Request[AnyContent] =>
    service.getCity(request.session.get("name").get, request.body.asJson).map(jsResponse => jsResponse.keys.contains("success") match {
      case true => Ok(jsResponse)
      case false => BadRequest(jsResponse)
    })
  })
}
