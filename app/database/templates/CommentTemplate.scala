package database.templates

import org.joda.time.DateTime
import slick.jdbc.H2Profile.api._

class CommentTemplate(tag: Tag) extends Table[(String, String, DateTime, String)](tag, "COMMENT") {

  implicit val dateTimeMapper = MappedColumnType.base[DateTime, String](
    {
      date => date.toString()
    },
    {
      string => new DateTime(string)
    })

  def * = (user, content, timestamp, cityName)

  def user = column[String]("USER") // This is the primary key column

  def content = column[String]("CONTENT")

  def timestamp = column[DateTime]("TIMESTAMP")

  def cityName = column[String]("CITY_NAME")
}
