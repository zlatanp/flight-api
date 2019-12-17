package models

sealed trait UserType

case object Admin extends UserType
case object Regular extends UserType

