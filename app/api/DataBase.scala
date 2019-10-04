package api


import java.io.{ByteArrayInputStream, ByteArrayOutputStream, ObjectInputStream, ObjectOutputStream}

import javax.inject.Singleton
import models._
import org.joda.time.DateTime
import slick.jdbc.H2Profile.api._
import org.joda.time.DateTime

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class DataBase {

  val db = Database.forConfig("h2mem1")

  implicit val userTypeMapper = MappedColumnType.base[Type, String](
    {
      case Admin => "admin"
      case Regular => "regular"
    },
    {
      case "admin" => Admin
      case "regular" => Regular
    })

  implicit val dateTimeMapper = MappedColumnType.base[DateTime, String](
    {
      date => date.toString()
    },
    {
      string => new DateTime(string)
    })

  case class UserTemplate(tag: Tag) extends Table[(Long, String, String, String, String, String, Type)](tag, "USER") {
    def id = column[Long]("USER_ID", O.PrimaryKey, O.AutoInc) // This is the primary key column
    def firstName = column[String]("FIRST_NAME")

    def lastName = column[String]("LAST_NAME")

    def name = column[String]("NAME")

    def password = column[String]("PASSWORD")

    def salt = column[String]("SALT")

    def typeOfUser = column[Type]("TYPE_OF_USER")

    def * = (id, firstName, lastName, name, password, salt, typeOfUser)
  }

  var users = TableQuery[UserTemplate]

  case class CityTemplate(tag: Tag) extends Table[(String, String, String)](tag, "CITY") {
    def name = column[String]("CITY_NAME", O.PrimaryKey) // This is the primary key column
    def country = column[String]("COUNTRY")
    def description = column[String]("DESCRIPTION")
    def * = (name, country, description)
  }

  var cities = TableQuery[CityTemplate]


  class CommentTemplate(tag: Tag) extends Table[(String, String, DateTime, String)](tag, "COMMENT") {
    def user = column[String]("USER") // This is the primary key column
    def content = column[String]("CONTENT")
    def timestamp = column[DateTime]("TIMESTAMP")
    def cityName = column[String]("CITY_NAME")
    def * = (user, content, timestamp, cityName)
  }

  var comments = TableQuery[CommentTemplate]

    case class RouteTemplate(tag: Tag) extends Table[(String, String, String, String, String, String, String, Int, String, Double)](tag, "ROUTE") {
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

    def * = (airline, airlineId, sourceAirport, sourceAirportId, destinationAirpot, destinationAirportId, codeshare, stops, equipment, price)
  }

  var routes = TableQuery[RouteTemplate]


  case class AirportTemplate(tag: Tag) extends Table[(String, String, String, String, String, String, BigDecimal, BigDecimal, Double, BigDecimal, String, String, String, String)](tag, "AIRPORT") {
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

    def * = (airportId, name, city, country, iata, icao, latitude, longitude, altitude, timezone, DST, tz, typeOfAirport, source)
  }

  var airports = TableQuery[AirportTemplate]

  val setup = DBIO.seq(
    (users.schema ++ cities.schema ++ routes.schema ++ airports.schema).create,

    users += (1, "A", "AA", "aaa", "xxx", "x1x1x1", Admin),
    users += (2, "B", "BB", "bbb", "xxx", "x1x1x1", Regular),
    users += (3, "C", "CC", "ccc", "xxx", "x1x1x1", Regular),

    cities += ("Belgrade", "Serbia", "1,3 milion people"),
    cities += ("Frankfurt", "Germany", "0,7 milion people"),
    cities += ("Dubai", "United Arab Emirates", "3,1 milion people"),
    cities += ("Los Angeles", "California", "4 milion people"),

    comments += ("aaa", "Nice City!", DateTime.now().plusDays(2), "Belgrade"),
    comments += ("bbb", "I like It!", DateTime.now().plusDays(2), "Belgrade"),
    comments += ("ccc", "Capital city of Serbia.", DateTime.now().plusDays(2), "Belgrade")

  )

  val setupFuture = db.run(setup)

  def findUserByName(username: String) = {
    db.run((for (user <- users if user.name === username) yield user).result.headOption)
  }

  def logIn(username: String, password: String) = {
    db.run((for (user <- users if (user.name === username && user.password === password)) yield user).result.headOption)
  }

  def getAllCities() = {
    db.run(cities.result)
  }

  def getAllAirports() = {
    db.run(airports.result)
  }

  def addAirport(airport: Airport) = {
    db.run(airports += (airport.airportId, airport.name, airport.city, airport.country, airport.iata, airport.icao, airport.latitude, airport.longitude, airport.altitude, airport.timezone, airport.DST, airport.tz, airport.typeOfAirport, airport.source)).map(_ => ())
  }

  def getAllRoutes() = {
    db.run(routes.result)
  }

  def addRoute(route: Route) = {
    db.run(routes += (route.airline, route.airlineId, route.sourceAirport, route.sourceAirportId, route.destinationAirpot, route.destinationAirportId, route.codeshare, route.stops.toInt, route.equipment, route.price.toDouble)).map(_ => ())
  }

  def addCity(city: City): Unit = {
    db.run(cities += (city.name, city.country, city.description)).map(_ => ())
  }

  def findCityByName(name: String) = {
    db.run((for (city <- cities if city.name === name) yield city).result.headOption)
  }

  def addComment(comment: Comment) = {
    db.run(comments += (comment.user, comment.content, comment.timestamp, comment.cityName)).map(_ => ())
  }

  def deleteComment(user: String, cityName: String) =
    db.run(comments.filter(_.cityName === cityName).filter(_.user === user).delete).map(_ => ())

}
