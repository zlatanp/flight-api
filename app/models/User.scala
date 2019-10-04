package models

sealed trait Type
case object Admin extends Type
case object Regular extends Type

case class User(id: Long, firstName: String, lastName: String, name: String, password: String, salt: String, typeOfUser: Type)


