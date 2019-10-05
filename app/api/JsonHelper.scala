package api

import models.{City, CityJson, Comment, User}

object JsonHelper {

  import play.api.libs.json._

  implicit val userWriter = new Writes[User] {
    def writes(user: User) = Json.obj(
      "id" -> user.id,
      "firstName" -> user.firstName,
      "lastName" -> user.lastName,
      "name" -> user.name,
      "password" -> user.password,
      "salt" -> user.salt,
      "typeOfUser" -> user.typeOfUser.toString
    )
  }

  implicit val commentsWriter = new Writes[Comment] {
    def writes(comment: Comment) = Json.obj(
      "user" -> comment.user,
      "content" -> comment.content,
      "timestamp" -> comment.timestamp.toString,
      "cityName" -> comment.cityName
    )
  }


  implicit val cityWriter = new Writes[City] {
    def writes(city: City) = Json.obj(
      "name" -> city.name,
      "country" -> city.country,
      "description" -> city.description
    )
  }

  implicit val cityJsonWriter = new Writes[CityJson] {
    def writes(city: CityJson) = Json.obj(
      "name" -> city.name,
      "country" -> city.country,
      "description" -> city.description,
      "comments" -> city.comments
    )
  }

  def jsonErrResponse(message: String): JsObject = {
    Json.obj("error" -> message)
  }

  def jsonSuccessResponse(message: String): JsObject = {
    Json.obj("success" -> message)
  }

}
