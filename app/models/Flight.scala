package models

import play.api.libs.json.Json

import scala.collection.mutable

case class Flight(price: Double, distance: Double, routes: mutable.MutableList[Route])

object Flight {
  implicit val cityWrites = Json.writes[Route]
  implicit val flightWrites = Json.writes[Flight]
}
