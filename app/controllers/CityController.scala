package controllers

import helpers.AuthorizationAction
import helpers.JsonHelper.jsonErrResponse
import javax.inject.{Inject, Singleton}
import models._
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import services.CityService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class CityController @Inject()(authorizationAction: AuthorizationAction, cc: ControllerComponents, service: CityService) extends AbstractController(cc) {

  val logger: Logger = Logger(this.getClass)

  def all() = authorizationAction.async({ request: Request[AnyContent] =>
    if (request.body.asJson.isEmpty) {
      logger.error("Empty Json data.")
      Future(Ok(jsonErrResponse("Expecting Json data")))
    } else {
      request.body.asJson match {
        case Some(j) => {
          val numberOfComments = (j \ "comments").asOpt[Int]
          getServiceResponse(service.all(numberOfComments))
        }
        case None => {
          logger.debug("Bad input")
          Future(BadRequest(jsonErrResponse("Bad input")))
        }
      }
    }
  })

  def getCity() = authorizationAction.async({ request: Request[AnyContent] =>
    if (request.body.asJson.isEmpty) {
      logger.error("Empty Json data.")
      Future(BadRequest(jsonErrResponse("Expecting Json data")))
    } else {
      val cityName = (request.body.asJson.get \ "cityName").asOpt[String]
      val numberOfComments = (request.body.asJson.get \ "comments").asOpt[Int]

      cityName match {
        case Some(city) => {
          getServiceResponse(service.getCity(city, numberOfComments))
        }
        case _ => {
          logger.debug("Bad input")
          Future(BadRequest(jsonErrResponse("Bad input")))
        }
      }
    }
  })

  def add() = authorizationAction.async({ request: Request[AnyContent] =>
    val jsValueUser: JsValue = Json.parse(request.session.get("user").get)
    val userObj = Json.fromJson[User](jsValueUser).getOrElse(User(0, "", "", ",", "", "", Regular))

    if (userObj.typeOfUser.equals(Admin)) {

      if (request.body.asJson.isEmpty) {
        logger.error("Empty Json data.")
        Future(BadRequest(jsonErrResponse("Expecting Json data")))
      } else {
        val nameOpt = (request.body.asJson.get \ "name").asOpt[String]
        val countryOpt = (request.body.asJson.get \ "country").asOpt[String]
        val descriptionOpt = (request.body.asJson.get \ "description").asOpt[String]

        (nameOpt, countryOpt, descriptionOpt) match {
          case (Some(name), Some(country), Some(description)) => {
            val newCity = City(name, country, description, Seq(Comment("", "", DateTime.now, "")))
            getServiceResponse(service.add(newCity))
          }
          case (_, _, _) => {
            logger.debug("Bad input")
            Future(BadRequest(jsonErrResponse("Bad input")))
          }
        }
      }
    } else {
      logger.debug("User have no permission to access this service");
      Future(BadRequest(jsonErrResponse("Sorry, this page isn't available")))
    }
  })

  def comment() = authorizationAction.async({ request: Request[AnyContent] =>
    if (request.body.asJson.isEmpty) {
      logger.error("Empty Json data.")
      Future(BadRequest(jsonErrResponse("Expecting Json data")))
    } else {
      val jsValueUser: JsValue = Json.parse(request.session.get("user").get)
      val userObj = Json.fromJson[User](jsValueUser).getOrElse(User(0, "", "", ",", "", "", Regular))
      val cityNameOpt = (request.body.asJson.get \ "cityName").asOpt[String]
      val commentOpt = (request.body.asJson.get \ "comment").asOpt[String]
      (cityNameOpt, commentOpt) match {
        case (Some(cityName), Some(comment)) => {
          val commentObj = Comment(userObj.name, comment, DateTime.now, cityName)
          getServiceResponse(service.comment(userObj, commentObj))
        }
        case (_,_) => {
          logger.debug("Bad input")
          Future(BadRequest(jsonErrResponse("Bad input")))
        }
      }
    }
  })

  def delete() = authorizationAction.async({ request: Request[AnyContent] =>
    if (request.body.asJson.isEmpty) {
      logger.error("Empty Json data.")
      Future(BadRequest(jsonErrResponse("Expecting Json data")))
    } else {
      val jsValueUser: JsValue = Json.parse(request.session.get("user").get)
      val userObj = Json.fromJson[User](jsValueUser).getOrElse(User(0, "", "", ",", "", "", Regular))
      val cityNameOpt = (request.body.asJson.get \ "cityName").asOpt[String]
      cityNameOpt match {
        case Some(cityName) => {
          getServiceResponse(service.delete(userObj, cityName))
        }
        case _ => {
          logger.debug("Bad input")
          Future(BadRequest(jsonErrResponse("Bad input")))
        }
      }
    }
  })

  private def getServiceResponse(serviceResponse : Future[JsObject]) = {
    serviceResponse.map(jsResponse => jsResponse.keys.contains("success") match {
      case true => Ok(jsResponse)
      case false => BadRequest(jsResponse)
    })
  }
}
