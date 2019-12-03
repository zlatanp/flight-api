package database.templates

import slick.jdbc.H2Profile.api._

case class CityTemplate(tag: Tag) extends Table[(String, String, String)](tag, "CITY") {
  def * = (name, country, description)

  def name = column[String]("CITY_NAME", O.PrimaryKey) // This is the primary key column

  def country = column[String]("COUNTRY")

  def description = column[String]("DESCRIPTION")
}
