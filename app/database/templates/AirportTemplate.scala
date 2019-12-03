package database.templates

import slick.jdbc.H2Profile.api._

case class AirportTemplate(tag: Tag) extends Table[(String, String, String, String, String, String, BigDecimal, BigDecimal, Double, BigDecimal, String, String, String, String)](tag, "AIRPORT") {
  def * = (airportId, name, city, country, iata, icao, latitude, longitude, altitude, timezone, DST, tz, typeOfAirport, source)

  def airportId = column[String]("AIRPORT_ID", O.PrimaryKey) // This is the primary key column

  def name = column[String]("NAME")

  def city = column[String]("CITY")

  def country = column[String]("COUNTRY")

  def iata = column[String]("IATA")

  def icao = column[String]("ICAO")

  def latitude = column[BigDecimal]("LATITUDE")

  def longitude = column[BigDecimal]("LONGITUDE")

  def altitude = column[Double]("ALTITUDE")

  def timezone = column[BigDecimal]("TIMEZONE")

  def DST = column[String]("DST")

  def tz = column[String]("TZ")

  def typeOfAirport = column[String]("TYPE_OF_AIRPORT")

  def source = column[String]("SOURCE")
}
