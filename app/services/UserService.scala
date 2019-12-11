package services

import database.DataBase
import helpers.JsonHelper.{jsonErrResponse, _}
import javax.inject.Inject
import models.User
import models.UserType.{Admin, Regular}
import play.api.Logger
import play.api.libs.json.{JsObject, JsValue, Json}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserService @Inject()(db: DataBase) {

  val logger: Logger = Logger(this.getClass())

  def index(user: Option[String], usertype: Option[String]): Future[JsObject] = {
    (user, usertype) match {
      case (Some(u), Some(t)) => {
        db.findUserByName(u).map({
          case Some(userExist) => {
            userExist match {
              case (id, firstName, lastName, name, password, salt, typeOfUser) => {
                val loggedInUser = typeOfUser match {
                  case Admin => User(id, firstName, lastName, name, password, salt, Admin)
                  case Regular => User(id, firstName, lastName, name, password, salt, Regular)
                }
                logger.info(s"Logged in user: '$u'")
                Json.obj("user" -> Json.toJson(loggedInUser))
              }
              case _ => {
                logger.warn("Not connected user trying to open index page.")
                jsonErrResponse("Oops, you are not connected")
              }
            }
          }
          case None => {
            logger.warn("Not connected user trying to open index page.")
            jsonErrResponse("Oops, you are not connected")
          }
        })
      }
      case (_, _) => {
        logger.warn("Not connected user trying to open index page.")
        Future(jsonErrResponse("Oops, you are not connected"))
      }
    }
  }

  def login(json: Option[JsValue]): Future[JsObject] = {
    if (json.isEmpty) {
      logger.error("Empty Json data.")
      Future(jsonErrResponse("Expecting Json data"))
    } else {
      val name = (json.get \ "name").asOpt[String]
      val password = (json.get \ "password").asOpt[String]

      (name, password) match {
        case (None, None) => {
          logger.warn("Missing parameter [name] and [password]")
          Future(jsonErrResponse("Missing parameter [name] and [password]"))
        }
        case (None, Some(p)) => {
          logger.warn("Missing parameter [name]")
          Future(jsonErrResponse("Missing parameter [name]"))
        }
        case (Some(n), None) => {
          logger.warn("Missing parameter [password]")
          Future(jsonErrResponse("Missing parameter [password]"))
        }
        case (Some(n), Some(p)) => {
          db.logIn(n, p).map({
            case Some(userExist) => {
              userExist match {
                case (id, firstName, lastName, name, password, salt, typeOfUser) => {
                  val loggedInUser = typeOfUser match {
                    case Admin => User(id, firstName, lastName, name, password, salt, Admin)
                    case Regular => User(id, firstName, lastName, name, password, salt, Regular)
                  }
                  logger.info("Success login.")
                  jsonSuccessResponse("login") ++ Json.obj("user" -> Json.toJson(loggedInUser))
                }
                case _ => {
                  logger.warn("Not connected user trying to open index page.")
                  jsonErrResponse("Oops, you are not connected")
                }
              }
            }
            case None => {
              logger.info(s"No user found for name $n or bad password")
              jsonErrResponse(s"No user found for name $n or bad password")
            }
          })
        }
      }
    }
  }

}
