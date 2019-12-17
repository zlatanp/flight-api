package database

import database.templates._
import javax.inject.Singleton
import models._
import org.joda.time.DateTime
import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class DataBase {

  val db = Database.forConfig("h2mem1")
  var users = TableQuery[UserTemplate]
  var cities = TableQuery[CityTemplate]
  var comments = TableQuery[CommentTemplate]
  var routes = TableQuery[RouteTemplate]
  var airports = TableQuery[AirportTemplate]

  val setup = DBIO.seq(
    (users.schema ++ cities.schema ++ routes.schema ++ comments.schema ++ airports.schema).create,

    users += (1, "A", "AA", "aaa", "xxx", "x1x1x1", Admin),
    users += (2, "B", "BB", "bbb", "xxx", "x1x1x1", Regular),
    users += (3, "C", "CC", "ccc", "xxx", "x1x1x1", Regular),

    cities += ("Belgrade", "Serbia", "1,3 milion people", Seq(Comment("", "", DateTime.now, ""))),
    cities += ("Frankfurt", "Germany", "0,7 milion people", Seq(Comment("", "", DateTime.now, ""))),
    cities += ("Dubai", "United Arab Emirates", "3,1 milion people", Seq(Comment("", "", DateTime.now, ""))),
    cities += ("Los Angeles", "California", "4 milion people", Seq(Comment("", "", DateTime.now, ""))),

    comments += ("aaa", "Nice City!", new DateTime("2019-10-07T15:06:15.502+02:00"), "Belgrade"),
    comments += ("bbb", "I like It!", new DateTime("2019-10-05T15:06:15.502+02:00"), "Belgrade"),
    comments += ("ccc", "Capital city of Serbia.", new DateTime("2019-10-09T15:06:15.502+02:00"), "Belgrade")
  )

  val setupFuture = db.run(setup)

  def findUserByName(username: String) = {
    db.run((for (user <- users if user.name === username) yield user).result.headOption)
  }

  def logIn(username: String, password: String) = {
    db.run((for (user <- users if (user.name === username && user.password === password)) yield user).result.headOption)
  }

  def getAllCities() = {
    db.run((for (city <- cities) yield city).result)
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
    db.run(cities += (city.name, city.country, city.description, city.comments)).map(_ => ())
  }

  def findCityByName(name: String) = {
    db.run((for (city <- cities if city.name === name) yield city).result.headOption)
  }

  def findCommentByCityName(cityName: String) = {
    db.run((for (comment <- comments if comment.cityName === cityName) yield comment).result)
  }

  def getAllComments() = {
    db.run(comments.result)
  }

  def addComment(comment: Comment) = {
    db.run(comments += (comment.user, comment.content, comment.timestamp, comment.cityName)).map(_ => ())
  }

  def deleteComment(user: String, cityName: String) = {
    db.run(comments.filter(_.cityName === cityName).filter(_.user === user).delete).map(_ => ())
  }

  def findAirportByCityName(cityName: String) = {
    db.run((for (comment <- comments if comment.cityName === cityName) yield comment).result)
  }

  def getCitiesWithComments(cities: Seq[(String, String, String, Seq[Comment])], comments: Seq[(String, String, DateTime, String)], numberOfComments: Option[Int]): List[City] = {
    implicit def dateTimeOrdering: Ordering[DateTime] = Ordering.fromLessThan(_ isAfter _)

    val realCities: Seq[City] = cities.map(c => City(c._1, c._2, c._3, c._4))
    val realComments: Seq[Comment] = comments.map(c => Comment(c._1, c._2, c._3, c._4))

    numberOfComments match {
      case Some(number) => realCities.toList.map(city => City(city.name, city.country, city.description, realComments.toList.filter(c => c.cityName == city.name).sortBy(_.timestamp).take(number)))
      case None => realCities.toList.map(city => City(city.name, city.country, city.description, realComments.toList.filter(c => c.cityName == city.name)))
    }
  }
}
