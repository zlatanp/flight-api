package database.templates

import models.Comment
import org.joda.time.DateTime
import slick.jdbc.H2Profile.api._

case class CityTemplate(tag: Tag) extends Table[(String, String, String, Seq[Comment])](tag, "CITY") {

  implicit val userTypeMapper = MappedColumnType.base[Seq[Comment], String](
    {
      case Seq(Comment(user, content, timestamp, cityName)) => "Comment"
    },
    {
      case "Comment" => Seq(Comment("user", "content", DateTime.now, "cityName"))
    })

  def * = (name, country, description, comments)

  def name = column[String]("CITY_NAME", O.PrimaryKey) // This is the primary key column

  def country = column[String]("COUNTRY")

  def description = column[String]("DESCRIPTION")

  def comments = column[Seq[Comment]]("COMMENTS")
}
