package models

import org.joda.time.DateTime
import play.api.libs.json._

case class Comment(user: String, content: String, timestamp: DateTime, cityName: String)

object Comment {
  implicit val commentWriter = new Writes[Comment] {
    def writes(comment: Comment) = Json.obj(
      "user" -> comment.user,
      "content" -> comment.content,
      "timestamp" -> comment.timestamp.toString,
      "cityName" -> comment.cityName
    )
  }
}