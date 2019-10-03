package controllers

import api.DataBase
import api.JsonHelper.{jsonErrResponse, jsonSuccessResponse, _}
import javax.inject.{Inject, Singleton}
import models.{City, Comment}
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}

import scala.collection.mutable.ListBuffer

@Singleton
class CityController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  var db = new DataBase
  val logger: Logger = Logger(this.getClass())

  implicit def dateTimeOrdering: Ordering[DateTime] = Ordering.fromLessThan(_ isAfter _)

  def all() = Action { request: Request[AnyContent] =>
    request.session
      .get("connected")
      .map { name =>
        val json = request.body.asJson
        if (json.isEmpty) {
          logger.error("Empty Json data.")
          BadRequest(jsonErrResponse("Expecting Json data"))
        } else {
          json match {
            case Some(j) => {
              val numberOfComments = (j \ "comments").asOpt[Int]
              numberOfComments match {
                case Some(number) => {
                  val cityResponse = db.getAllCities().map { city =>
                    City(city.name, city.country, city.description, city.comments.sortBy(_.timestamp).take(number))
                  }
                  logger.info("Return list of one city with limited number of comments.")
                  Ok(Json.arr(Json.obj("user" -> Json.toJson(db.findUserByName(name))), Json.obj("cities" -> Json.toJson(cityResponse))))
                }
                case None => {
                  logger.info("Return list of all cities.")
                  Ok(Json.arr(Json.obj("user" -> Json.toJson(db.findUserByName(name))), Json.obj("cities" -> Json.toJson(db.getAllCities()))))
                }
              }
            }
            case _ => {
              logger.info("Return list of all cities.")
              Ok(Json.arr(Json.obj("user" -> Json.toJson(db.findUserByName(name))), Json.obj("cities" -> Json.toJson(db.getAllCities()))))
            }
          }
        }
      }
      .getOrElse {
        logger.warn("You are not signed in!")
        BadRequest(jsonErrResponse("You are not signed in!"))
      }
  }

  def add() = Action { request: Request[AnyContent] =>
    request.session
      .get("usertype")
      .map { usertype =>
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
                val newCity = City(name, country, description, new ListBuffer[Comment]())
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

  def comment() = Action { request: Request[AnyContent] =>
    request.session
      .get("connected")
      .map { userName =>
        val json = request.body.asJson

        if (json.isEmpty) {
          logger.error("Empty Json data.")
          BadRequest(jsonErrResponse("Expecting Json data"))
        } else {
          val cityName = (json.get \ "cityName").asOpt[String]
          val comment = (json.get \ "comment").asOpt[String]

          (cityName, comment) match {
            case (Some(cityName), Some(comment)) => {
              db.findCityByName(cityName) match {
                case Some(city) => {
                  city.removeComment(userName)
                  city.addComment(Comment(userName, comment, DateTime.now))
                  db.updateCity(city)
                  logger.warn(s"Comment added for city: $cityName")
                  Ok(Json.arr(jsonSuccessResponse("comment"), Json.obj("city" -> Json.toJson(city))))
                }
                case _ => {
                  logger.warn(s"No city found for name: $cityName")
                  BadRequest(jsonErrResponse(s"No city found for name: $cityName"))
                }
              }
            }
            case (_, _) => {
              logger.warn("Bad input")
              BadRequest(jsonErrResponse("Bad input"))
            }
          }
        }
      }
      .getOrElse {
        logger.warn("You are not signed in!")
        BadRequest(jsonErrResponse("You are not signed in!"))
      }
  }

  def delete() = Action { request: Request[AnyContent] =>
    request.session
      .get("connected")
      .map { userName =>

        val json = request.body.asJson

        if (json.isEmpty) {
          logger.error("Empty Json data.")
          BadRequest(jsonErrResponse("Expecting Json data"))
        } else {

          val cityName = (json.get \ "cityName").asOpt[String]
          cityName match {
            case Some(cityName) => {
              db.findCityByName(cityName) match {
                case Some(city) => {
                  city.removeComment(userName)
                  db.updateCity(city)
                  logger.warn(s"Comment from user $userName deleted for city: $cityName")
                  Ok(Json.arr(jsonSuccessResponse("delete"), Json.obj("city" -> Json.toJson(city))))
                }
                case _ => {
                  logger.warn(s"No city found for name: $cityName")
                  BadRequest(jsonErrResponse(s"No city found for name: $cityName"))
                }
              }
            }
            case _ => {
              logger.warn("Bad input")
              BadRequest(jsonErrResponse("Bad input"))
            }
          }
        }
      }
      .getOrElse {
        logger.warn("You are not signed in!")
        BadRequest(jsonErrResponse("You are not signed in!"))
      }
  }

  def getCity() = Action { request: Request[AnyContent] =>
    request.session
      .get("connected")
      .map { name =>
        val json = request.body.asJson

        if (json.isEmpty) {
          logger.error("Empty Json data.")
          BadRequest(jsonErrResponse("Expecting Json data"))
        } else {

          val cityName = (json.get \ "cityName").asOpt[String]
          val numberOfComments = (json.get \ "comments").asOpt[Int]

          (cityName, numberOfComments) match {
            case (Some(city), None) => {
              db.findCityByName(city) match {
                case Some(c) => {
                  logger.info("Return list of one city with unlimited number of comments.")
                  Ok(Json.arr(Json.obj("user" -> Json.toJson(db.findUserByName(name))), Json.obj("cities" -> Json.toJson(c))))
                }
                case _ => {
                  logger.warn(s"No city found for name: $cityName")
                  BadRequest(jsonErrResponse(s"No city found for name: $cityName"))
                }
              }
            }
            case (Some(city), Some(number)) => {
              db.findCityByName(city) match {
                case Some(c) => {
                  val cityResponse = new City(c.name, c.country, c.description, c.comments.sortBy(_.timestamp).take(number))
                  logger.info("Return list of one city with limited number of comments.")
                  Ok(Json.arr(Json.obj("user" -> Json.toJson(db.findUserByName(name))), Json.obj("cities" -> Json.toJson(cityResponse))))
                }
                case _ => {
                  logger.warn(s"No city found for name: $cityName")
                  BadRequest(jsonErrResponse(s"No city found for name: $cityName"))
                }
              }
            }
            case (_, _) => {
              logger.warn("Bad input")
              BadRequest(jsonErrResponse("Bad input"))
            }
          }
        }
      }
      .getOrElse {
        logger.warn("You are not signed in!")
        BadRequest(jsonErrResponse("You are not signed in!"))
      }
  }
}
