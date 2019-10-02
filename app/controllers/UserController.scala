package controllers

import api.DataBase
import api.JsonHelper._
import javax.inject._
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class UserController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  var db = new DataBase
  val logger: Logger = Logger(this.getClass())

  def index() = Action { request: Request[AnyContent] =>

    val user = request.session.get("connected")
    val usertype = request.session.get("usertype")

    (user, usertype) match {
      case (Some(u), Some(t)) => {
        logger.info(s"Logged in user: '$u'")
        Ok(Json.obj("user" -> Json.toJson(db.findUserByName(u))))
      }
      case (_, _) => {
        logger.warn("Not connected user trying to open index page.")
        Unauthorized(jsonErrResponse("Oops, you are not connected"))
      }
    }
  }

  def login() = Action { request =>
    request.session
      .get("connected")
      .map { name =>
        logger.info(s"Already logged in user: '$name'")
        Ok(Json.arr(jsonErrResponse("Already logged in"), "user" -> Json.toJson(db.findUserByName(name))))
      }
      .getOrElse {
        val json = request.body.asJson
        if (json.isEmpty) {
          logger.error("Empty Json data.")
          BadRequest(jsonErrResponse("Expecting Json data"))
        }

        val name = (json.get \ "name").asOpt[String]
        val password = (json.get \ "password").asOpt[String]

        (name, password) match {
          case (None, None) => {
            logger.warn("Missing parameter [name] and [password]")
            BadRequest(jsonErrResponse("Missing parameter [name] and [password]"))
          }
          case (None, Some(p)) => {
            logger.warn("Missing parameter [name]")
            BadRequest(jsonErrResponse("Missing parameter [name]"))
          }
          case (Some(n), None) => {
            logger.warn("Missing parameter [password]")
            BadRequest(jsonErrResponse("Missing parameter [password]"))
          }
          case (Some(n), Some(p)) => {
            db.findUserByName(n) match {
              case None => {
                logger.info("No user found for name $n")
                BadRequest(jsonErrResponse(s"No user found for name $n"))
              }
              case Some(u) => if (u.password != p) {
                logger.warn("Bad password");
                BadRequest(jsonErrResponse("Bad password"))
              } else {
                logger.info("Success login.")
                Ok(Json.arr(jsonSuccessResponse("login"), Json.obj("user" -> Json.toJson(u)))).withSession(request.session + ("connected" -> s"$n") + ("usertype" -> u.typeOfUser.toString))
              }
            }
          }
        }
      }
  }

  def logout() = Action { request =>
    request.session
      .get("connected")
      .map { name =>
        logger.info("Log out")
        Ok(Json.arr(jsonSuccessResponse("logout"), Json.obj("user" -> Json.toJson(db.findUserByName(name))))).withSession(request.session - "connected" - "usertype")
      }
      .getOrElse {
        logger.info("You are not signed in!")
        BadRequest(jsonErrResponse("You are not signed in!"))
      }
  }

}