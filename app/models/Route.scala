package models

import play.api.libs.json.Json

case class Route(airline: String, airlineId: String, sourceAirport: String, sourceAirportId: String, destinationAirpot: String, destinationAirportId: String, codeshare: String, stops: Int, equipment: String, price: Double)

object Route {
  implicit val cityWrites = Json.writes[Route]
}