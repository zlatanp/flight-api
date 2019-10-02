package models

case class Airport(airportId: String, name: String, city: String, country: String, iata: String, icao: String, latitude: BigDecimal, longitude: BigDecimal, altitude: Double, timezone: BigDecimal, DST: String, tz: String, typeOfAirport: String, source: String)
