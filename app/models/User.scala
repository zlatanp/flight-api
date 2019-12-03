package models

import play.api.libs.json.{Json, Writes}

case class User(id: Long, firstName: String, lastName: String, name: String, password: String, salt: String, typeOfUser: UserType)

object User{
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
}


