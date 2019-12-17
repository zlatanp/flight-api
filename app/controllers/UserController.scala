package controllers

import helpers.AuthorizationAction
import helpers.JsonHelper.{jsonErrResponse, jsonSuccessResponse, getUserJsonFromRequest}
import javax.inject._
import models.{Regular, User}
import play.api.Logger
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc._
import services.UserService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class UserController @Inject()(authorizationAction: AuthorizationAction, cc: ControllerComponents, service: UserService) extends AbstractController(cc) {

  val logger: Logger = Logger(this.getClass())

  def index() = authorizationAction { request: Request[AnyContent] =>
    val jsValueUser: JsValue = Json.parse(request.session.get("user").get)
    logger.info("Index page")
    Ok(jsValueUser)
  }

  def login() = Action(parse.json).async { request: Request[JsValue] =>
    request.session
      .get("user")
      .map { user =>
        logger.info(s"Already logged in user: '$user'")
        Future(Ok(jsonErrResponse("Already logged in") ++ Json.obj("user" -> Json.parse(user))))
      }
      .getOrElse {
        val userJson: JsObject = getUserJsonFromRequest(request.body.as[JsObject])
        val userObj = Json.fromJson[User](userJson).getOrElse(User(0, "", "", ",", "", "", Regular))
        service.login(userObj).map(jsResponse => jsResponse.keys.contains("success") match {
          case true => {
            val loggedUser = (jsResponse \ "user").get
            val name = (loggedUser \ "name").get.toString.replaceAll("\"", "")
            val userType = (loggedUser \ "typeOfUser").get.toString.replaceAll("\"", "")
            Ok(jsResponse).withSession(request.session + ("name" -> name) + ("usertype" -> userType) + ("user" -> Json.prettyPrint(loggedUser)))
          }
          case false => BadRequest(jsResponse)
        })
      }
  }

  def logout() = authorizationAction { request =>
    logger.info("Log out")
    Ok(jsonSuccessResponse("logout") ++ Json.obj("user" -> Json.toJson(request.session.get("name").get))).withSession(request.session - "name" - "usertype" - "user")
  }
}