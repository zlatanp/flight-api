package controllers

import api.JsonHelper.jsonErrResponse
import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import services.CityService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class CityController @Inject()(cc: ControllerComponents, service: CityService) extends AbstractController(cc) {

  val logger: Logger = Logger(this.getClass())

  def all() = Action.async({ request: Request[AnyContent] =>
    request.session
      .get("connected")
      .map { name =>
        service.all(request.body.asJson).map(jsResponse => jsResponse.keys.contains("success") match {
          case true => Ok(jsResponse)
          case false => BadRequest(jsResponse)
        })
      }
      .getOrElse {
        logger.warn("You are not signed in!")
        Future(BadRequest(jsonErrResponse("You are not signed in!")))
      }
  })

  def add() = Action.async({
    request: Request[AnyContent] =>
      request.session
        .get("usertype")
        .map {
          usertype =>
            service.add(usertype, request.body.asJson).map(jsResponse => jsResponse.keys.contains("success") match {
              case true => Ok(jsResponse)
              case false => BadRequest(jsResponse)
            })
        }
        .getOrElse {
          logger.info("You are not signed in!")
          Future(BadRequest(jsonErrResponse("You are not signed in!")))
        }
  })

  def comment() = Action.async({
    request: Request[AnyContent] =>
      request.session
        .get("connected")
        .map { userName =>
          service.comment(userName, request.body.asJson).map(jsResponse => jsResponse.keys.contains("success") match {
            case true => Ok(jsResponse)
            case false => BadRequest(jsResponse)
          })
        }
        .getOrElse {
          logger.warn("You are not signed in!")
          Future(BadRequest(jsonErrResponse("You are not signed in!")))
        }
  })

  def delete() = Action.async({
    request: Request[AnyContent] =>
      request.session
        .get("connected")
        .map { userName =>
          service.delete(userName, request.body.asJson).map(jsResponse => jsResponse.keys.contains("success") match {
            case true => Ok(jsResponse)
            case false => BadRequest(jsResponse)
          })
        }
        .getOrElse {
          logger.warn("You are not signed in!")
          Future(BadRequest(jsonErrResponse("You are not signed in!")))
        }
  })

  def getCity() = Action.async({
    request: Request[AnyContent] =>
      request.session
        .get("connected")
        .map { userName =>
          request.body.asJson
          service.getCity(userName, request.body.asJson).map(jsResponse => jsResponse.keys.contains("success") match {
            case true => Ok(jsResponse)
            case false => BadRequest(jsResponse)
          })

        }
        .getOrElse {
          logger.warn("You are not signed in!")
          Future(BadRequest(jsonErrResponse("You are not signed in!")))
        }
  })
}
