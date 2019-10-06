package api

import models.{City, CityJson, Comment, Flight, Route, User}

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

  implicit val routesWriter = new Writes[Route] {
    def writes(route: Route) = Json.obj(
      "airline" -> route.airline,
      "airlineId" -> route.airlineId,
      "sourceAirport" -> route.sourceAirport,
      "sourceAirportId" -> route.sourceAirportId,
      "destinationAirpot" -> route.destinationAirpot,
      "destinationAirportId" -> route.destinationAirportId,
      "codeshare" -> route.codeshare,
      "stops" -> route.stops,
      "equipment" -> route.equipment,
      "price" -> route.price
    )
  }

  implicit val flightWriter = new Writes[Flight] {
    def writes(flight: Flight) = Json.obj(
      "price" -> flight.price,
      "distance" -> flight.distance,
      "routes" -> flight.routes
    )
  }

  def jsonErrResponse(message: String): JsObject = {
    Json.obj("error" -> message)
  }

  def jsonSuccessResponse(message: String): JsObject = {
    Json.obj("success" -> message)
  }

}
