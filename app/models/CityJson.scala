package models

import play.api.libs.json.{Json, Writes}

case class CityJson(name: String, country: String, description: String, comments: List[Comment])

object CityJson {
  implicit val commentWriter = new Writes[Comment] {
    def writes(comment: Comment) = Json.obj(
      "user" -> comment.user,
      "content" -> comment.content,
      "timestamp" -> comment.timestamp.toString,
      "cityName" -> comment.cityName
    )
  }
  implicit val cityJsonWrites = Json.writes[CityJson]
}
