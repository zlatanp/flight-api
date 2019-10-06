package controllers

import api.JsonHelper.{jsonErrResponse, jsonSuccessResponse}
import javax.inject._
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc._
import services.UserService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class UserController @Inject()(cc: ControllerComponents, service: UserService) extends AbstractController(cc) {

  val logger: Logger = Logger(this.getClass())

  def index() = Action.async({ request: Request[AnyContent] =>
    service.index(request.session.get("connected"), request.session.get("usertype")).map(jsResponse => jsResponse.keys.contains("success") match {
      case true => Ok(jsResponse)
      case false => BadRequest(jsResponse)
    })
  })


  def login() = Action.async { request =>
    request.session
      .get("connected")
      .map { name =>
        logger.info(s"Already logged in user: '$name'")
        Future(Ok(jsonErrResponse("Already logged in") ++ Json.obj("user" -> Json.toJson(name))))
      }
      .getOrElse {
        service.login(request.body.asJson).map(jsResponse => jsResponse.keys.contains("success") match {
          case true => {
            val connected = (jsResponse \ "user" \ "name").get.toString.replaceAll("\"", "")
            val userType = (jsResponse \ "user" \ "typeOfUser").get.toString.replaceAll("\"", "")
            Ok(jsResponse).withSession(request.session + ("connected" -> connected) + ("usertype" -> userType))
          }
          case false => BadRequest(jsResponse)
        })
      }
  }

  def logout() = Action { request =>
    request.session
      .get("connected")
      .map { name =>
        logger.info("Log out")
        Ok(jsonSuccessResponse("logout") ++ Json.obj("user" -> Json.toJson(name))).withSession(request.session - "connected" - "usertype")
      }
      .getOrElse {
        logger.info("You are not signed in!")
        BadRequest(jsonErrResponse("You are not signed in!"))
      }
  }

}