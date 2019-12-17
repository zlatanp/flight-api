package database.templates

import models.{Admin, Regular, UserType}
import slick.jdbc.H2Profile.api._

case class UserTemplate(tag: Tag) extends Table[(Long, String, String, String, String, String, UserType)](tag, "USER") {

  implicit val userTypeMapper = MappedColumnType.base[UserType, String](
    {
      case Admin => "admin"
      case Regular => "regular"
    },
    {
      case "admin" => Admin
      case "regular" => Regular
    })

  def * = (id, firstName, lastName, name, password, salt, typeOfUser)

  def id = column[Long]("USER_ID", O.PrimaryKey, O.AutoInc) // This is the primary key column

  def firstName = column[String]("FIRST_NAME")

  def lastName = column[String]("LAST_NAME")

  def name = column[String]("NAME")

  def password = column[String]("PASSWORD")

  def salt = column[String]("SALT")

  def typeOfUser = column[UserType]("TYPE_OF_USER")
}
