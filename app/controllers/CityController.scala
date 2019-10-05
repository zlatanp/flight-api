package controllers

import api.DataBase
import api.JsonHelper.{jsonErrResponse, jsonSuccessResponse, _}
import javax.inject.{Inject, Singleton}
import models.{City, Comment}
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class CityController @Inject()(cc: ControllerComponents, db: DataBase) extends AbstractController(cc) {

  val logger: Logger = Logger(this.getClass())

  def all() = Action.async({ request: Request[AnyContent] =>
    request.session
      .get("connected")
      .map { name =>
        val json = request.body.asJson
        if (json.isEmpty) {
          logger.error("Empty Json data.")
          Future(BadRequest(jsonErrResponse("Expecting Json data")))
        } else {
          json match {
            case Some(j) => {
              val numberOfComments = (j \ "comments").asOpt[Int]
              val result =
                for {
                  cities <- db.getAllCities()
                  comments <- db.getAllComments()
                } yield db.getCitiesWithComments(cities, comments, numberOfComments)
              result.map(responseCity => {
                logger.info("Return list of all cities.")
                Ok(Json.obj("cities" -> Json.toJson(responseCity)))
              })
            }
            case None => {
              logger.warn("Bad input")
              Future(BadRequest(jsonErrResponse("Bad input")))
            }
          }
        }
      }
      .getOrElse {
        logger.warn("You are not signed in!")
        Future(BadRequest(jsonErrResponse("You are not signed in!")))
      }
  })

  def add() = Action {
    request: Request[AnyContent] =>
      request.session
        .get("usertype")
        .map {
          usertype =>
            if (usertype.equalsIgnoreCase("admin")) {
              val json = request.body.asJson

              if (json.isEmpty) {
                logger.error("Empty Json data.")
                BadRequest(jsonErrResponse("Expecting Json data"))
              } else {
                val name = (json.get \ "name").asOpt[String]
                val country = (json.get \ "country").asOpt[String]
                val description = (json.get \ "description").asOpt[String]

                (name, country, description) match {
                  case (Some(name), Some(country), Some(description)) => {
                    val newCity = City(name, country, description)
                    db.addCity(newCity)
                    logger.info("Add new city to database")
                    Ok(Json.arr(jsonSuccessResponse("create"), Json.obj("city" -> Json.toJson(newCity))))
                  }
                  case (_, _, _) => {
                    logger.warn("Bad input")
                    BadRequest(jsonErrResponse("Bad input"))
                  }
                }
              }
            } else {
              logger.warn("User have no permission to access this service");
              BadRequest(jsonErrResponse("Sorry, this page isn't available"))
            }
        }
        .getOrElse {
          logger.info("You are not signed in!")
          BadRequest(jsonErrResponse("You are not signed in!"))
        }
  }

  def comment() = Action.async({
    request: Request[AnyContent] =>
      request.session
        .get("connected")
        .map { userName =>
          val json = request.body.asJson

          if (json.isEmpty) {
            logger.error("Empty Json data.")
            Future(BadRequest(jsonErrResponse("Expecting Json data")))
          } else {
            val cityName = (json.get \ "cityName").asOpt[String]
            val comment = (json.get \ "comment").asOpt[String]

            (cityName, comment) match {
              case (Some(cityName), Some(comment)) => {
                db.findCityByName(cityName).map({
                  case Some(city) => {
                    db.deleteComment(userName, cityName)
                    db.addComment(Comment(userName, comment, DateTime.now, cityName))
                    logger.warn(s"Comment added for city: $cityName")
                    Ok(jsonSuccessResponse("comment"))
                  }
                  case _ => {
                    logger.warn(s"No city found for name: $cityName")
                    BadRequest(jsonErrResponse(s"No city found for name: $cityName"))
                  }
                })
              }
              case (_, _) => {
                logger.warn("Bad input")
                Future(BadRequest(jsonErrResponse("Bad input")))
              }
            }
          }
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
          val json = request.body.asJson

          if (json.isEmpty) {
            logger.error("Empty Json data.")
            Future(BadRequest(jsonErrResponse("Expecting Json data")))
          } else {

            val cityName = (json.get \ "cityName").asOpt[String]
            cityName match {
              case Some(cityName) => {
                db.findCityByName(cityName).map({
                  case Some(cityExist) => {
                    cityExist match {
                      case (name, country, description) => {
                        db.deleteComment(userName, cityName)
                        logger.warn(s"Comment from user $userName deleted for city: $cityName")
                        Ok(Json.arr(jsonSuccessResponse("delete")))
                      }
                    }
                  }
                  case None => {
                    logger.warn(s"City for name $cityName not found!")
                    Unauthorized(jsonErrResponse(s"City for name $cityName not found!"))
                  }
                })
              }
              case _ => {
                logger.warn("Bad input")
                Future(BadRequest(jsonErrResponse("Bad input")))
              }
            }
          }
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
        .map {
          name =>
            val json = request.body.asJson

            if (json.isEmpty) {
              logger.error("Empty Json data.")
              Future(BadRequest(jsonErrResponse("Expecting Json data")))
            } else {
              val cityName = (json.get \ "cityName").asOpt[String]
              val numberOfComments = (json.get \ "comments").asOpt[Int]
              cityName match {
                case Some(city) => {
                  val result =
                    for {
                      oneCity <- db.findCityByName(city)
                      comments <- db.getAllComments()
                    } yield db.getCitiesWithComments(Seq(oneCity.getOrElse(s"No city found for name '$city'", "", "")), comments, numberOfComments)
                  result.map(responseCity => {
                    logger.info(s"Return one city with name $city")
                    Ok(Json.obj("cities" -> Json.toJson(responseCity)))
                  })
                }
                case _ => {
                  logger.warn("Bad input")
                  Future(BadRequest(jsonErrResponse("Bad input")))
                }
              }
            }
        }
        .getOrElse {
          logger.warn("You are not signed in!")
          Future(BadRequest(jsonErrResponse("You are not signed in!")))
        }
  })
}
