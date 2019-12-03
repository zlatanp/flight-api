package models

import play.api.libs.json._

case class City(name: String, country: String, description: String)

object City {
  implicit val cityWrites = Json.writes[City]
}


