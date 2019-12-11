package models

import play.api.libs.json._

case class City(name: String, country: String, description: String, comments: Seq[Comment])

object City {
  implicit val commentWriter = new Writes[Comment] {
    def writes(comment: Comment) = Json.obj(
      "user" -> comment.user,
      "content" -> comment.content,
      "timestamp" -> comment.timestamp.toString,
      "cityName" -> comment.cityName
    )
  }
  implicit val cityJsonWrites = Json.writes[City]
}

