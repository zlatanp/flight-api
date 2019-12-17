package services

import database.DataBase
import helpers.JsonHelper.{jsonErrResponse, jsonSuccessResponse}
import javax.inject.Inject
import models.{City, Comment, User}
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CityService @Inject()(db: DataBase) {

  val logger: Logger = Logger(this.getClass)

  def all(numberOfCommentsOpt: Option[Int]) = {
    val result =
      for {
        cities <- db.getAllCities()
        comments <- db.getAllComments()
      } yield db.getCitiesWithComments(cities, comments, numberOfCommentsOpt)
    result.map(responseCity => {
      logger.debug("Return list of all cities.")
      jsonSuccessResponse("getall") ++ Json.obj("cities" -> Json.toJson(responseCity))
    })
  }

  def getCity(cityName: String, numberOfComments: Option[Int]) = {
    val result =
      for {
        oneCity <- db.findCityByName(cityName)
        comments <- db.getAllComments()
      } yield db.getCitiesWithComments(Seq(oneCity.getOrElse(s"No city found for name '$cityName'", "", "", Seq(Comment("", "", DateTime.now, "")))), comments, numberOfComments)

    result.map(responseCity => {
      logger.debug(s"Return one city with name $cityName")
      jsonSuccessResponse("get") ++ Json.obj("cities" -> Json.toJson(responseCity))
    })
  }


  def add(city: City) = {
    db.addCity(city)
    logger.debug("Add new city to database")
    Future(jsonSuccessResponse("create") ++ Json.obj("city" -> Json.toJson(city)))
  }

  def comment(user: User, comment: Comment) = {
    val cityName = comment.cityName
    db.findCityByName(comment.cityName).map({
      case Some(city) => {
        db.deleteComment(user.name, cityName)
        db.addComment(comment)
        logger.debug(s"Comment added for city: $cityName")
        jsonSuccessResponse("comment")
      }
      case _ => {
        logger.debug(s"No city found for name: $cityName")
        jsonErrResponse(s"No city found for name: $cityName")
      }
    })
  }

  def delete(user: User, cityName: String) = {
    val username = user.name
    db.findCityByName(cityName).map({
      case Some(cityExist) => {
        cityExist match {
          case (name, country, description, comment) => {
            db.deleteComment(username, cityName)
            logger.debug(s"Comment from user $username deleted for city: $cityName")
            jsonSuccessResponse("delete")
          }
        }
      }
      case None => {
        logger.debug(s"City for name $cityName not found!")
        jsonErrResponse(s"City for name $cityName not found!")
      }
    })
  }
}
