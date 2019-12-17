package models

import play.api.libs.json._

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

  implicit val userReads = new Reads[User] {
    override def reads(json: JsValue): JsResult[User] = {
      (for {
        id <- (json \ "id").validate[Long]
        firstName <- (json \ "firstName").validate[String]
        lastName <- (json \ "lastName").validate[String]
        name <- (json \ "name").validate[String]
        password <- (json \ "password").validate[String]
        salt <- (json \ "salt").validate[String]
        typeOfUser <- (json \ "typeOfUser").validate[String]
        typeT = typeOfUser match {
          case "Admin" => Admin
          case _ => Regular
        }
      } yield User(id, firstName, lastName, name, password, salt, typeT))
    }
  }

}


