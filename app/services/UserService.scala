package services

import database.DataBase
import helpers.JsonHelper.{jsonErrResponse, _}
import javax.inject.Inject
import models.{Admin, Regular, User}
import play.api.Logger
import play.api.libs.json.{JsObject, Json}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserService @Inject()(db: DataBase) {

  val logger: Logger = Logger(this.getClass())

  def login(user: User): Future[JsObject] = {
    val userName = user.name
    val userPassword = user.password
    db.logIn(userName, userPassword).map({
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
            logger.warn("Login failed!")
            jsonErrResponse(s"No user found for name $userName or bad password")
          }
        }
      }
      case None => {
        logger.info(s"No user found for name $userName or bad password")
        jsonErrResponse(s"No user found for name $userName or bad password")
      }
    })
  }


}
