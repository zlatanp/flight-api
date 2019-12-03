package database.templates

import slick.jdbc.H2Profile.api._

case class RouteTemplate(tag: Tag) extends Table[(String, String, String, String, String, String, String, Int, String, Double)](tag, "ROUTE") {
  def * = (airline, airlineId, sourceAirport, sourceAirportId, destinationAirpot, destinationAirportId, codeshare, stops, equipment, price)

  def airline = column[String]("AIRLINE") // This is the primary key column

  def airlineId = column[String]("AIRLINE_ID")

  def sourceAirport = column[String]("SOURCE_AIRPORT")

  def sourceAirportId = column[String]("SOURCE_AIRPORT_ID")

  def destinationAirpot = column[String]("DESTINATION_AIRPORT")

  def destinationAirportId = column[String]("DESTINATION_AIRPORT_ID")

  def codeshare = column[String]("CODESHARE")

  def stops = column[Int]("STOPS")

  def equipment = column[String]("EQUIPMENT")

  def price = column[Double]("PRICE")
}
