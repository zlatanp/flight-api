package models

sealed trait UserType

object UserType {
  case object Admin extends UserType
  case object Regular extends UserType
}
