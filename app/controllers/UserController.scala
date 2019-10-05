package controllers

import api.DataBase
import api.JsonHelper._
import javax.inject._
import models.User
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class UserController @Inject()(cc: ControllerComponents, db: DataBase) extends AbstractController(cc) {

  val logger: Logger = Logger(this.getClass())

  def index() = Action.async({ request: Request[AnyContent] =>

    val user = request.session.get("connected")
    val usertype = request.session.get("usertype")

    (user, usertype) match {
      case (Some(u), Some(t)) => {
        db.findUserByName(u).map({
          case Some(userExist) => {
            userExist match {
              case (id, firstName, lastName, name, password, salt, typeOfUser) => {
                val loggedInUser = User(id, firstName, lastName, name, password, salt, typeOfUser)
                logger.info(s"Logged in user: '$u'")
                Ok(Json.obj("user" -> Json.toJson(loggedInUser)))
              }
              case _ => {
                logger.warn("Not connected user trying to open index page.")
                Unauthorized(jsonErrResponse("Oops, you are not connected"))
              }
            }
          }
          case None => {
            logger.warn("Not connected user trying to open index page.")
            Unauthorized(jsonErrResponse("Oops, you are not connected"))
          }
        })
      }
      case (_, _) => {
        logger.warn("Not connected user trying to open index page.")
        Future(Unauthorized(jsonErrResponse("Oops, you are not connected")))
      }
    }
  })


  def login() = Action.async { request =>
    request.session
      .get("connected")
      .map { name =>
        logger.info(s"Already logged in user: '$name'")
        Future(Ok(Json.arr(jsonErrResponse("Already logged in"), Json.obj("user" -> Json.toJson(name)))))
      }
      .getOrElse {
        val json = request.body.asJson
        if (json.isEmpty) {
          logger.error("Empty Json data.")
          Future(BadRequest(jsonErrResponse("Expecting Json data")))
        } else {
          val name = (json.get \ "name").asOpt[String]
          val password = (json.get \ "password").asOpt[String]

          (name, password) match {
            case (None, None) => {
              logger.warn("Missing parameter [name] and [password]")
              Future(BadRequest(jsonErrResponse("Missing parameter [name] and [password]")))
            }
            case (None, Some(p)) => {
              logger.warn("Missing parameter [name]")
              Future(BadRequest(jsonErrResponse("Missing parameter [name]")))
            }
            case (Some(n), None) => {
              logger.warn("Missing parameter [password]")
              Future(BadRequest(jsonErrResponse("Missing parameter [password]")))
            }
            case (Some(n), Some(p)) => {
              db.logIn(n, p).map({
                case Some(userExist) => {
                  userExist match {
                    case (id, firstName, lastName, name, password, salt, typeOfUser) => {
                      val loggedInUser = User(id, firstName, lastName, name, password, salt, typeOfUser)
                      logger.info("Success login.")
                      Ok(Json.arr(jsonSuccessResponse("login"), Json.obj("user" -> Json.toJson(loggedInUser)))).withSession(request.session + ("connected" -> s"$n") + ("usertype" -> loggedInUser.typeOfUser.toString))
                    }
                    case _ => {
                      logger.warn("Not connected user trying to open index page.")
                      Unauthorized(jsonErrResponse("Oops, you are not connected"))
                    }
                  }
                }
                case None => {
                  logger.info(s"No user found for name $n or bad password")
                  BadRequest(jsonErrResponse(s"No user found for name $n or bad password"))
                }
              })
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
        Ok(Json.arr(jsonSuccessResponse("logout"), Json.obj("user" -> Json.toJson(name)))).withSession(request.session - "connected" - "usertype")
      }
      .getOrElse {
        logger.info("You are not signed in!")
        BadRequest(jsonErrResponse("You are not signed in!"))
      }
  }

}