package api


import javax.inject.Singleton
import models.{City, _}
import org.joda.time.DateTime

import scala.collection.mutable.ListBuffer

@Singleton
class DataBase {

  val users: ListBuffer[User] = new ListBuffer[User]()
  val cities: ListBuffer[City] = new ListBuffer[City]()
  val airports: ListBuffer[Airport] = new ListBuffer[Airport]()
  val routes: ListBuffer[Route] = new ListBuffer[Route]()

  users += User(1, "A", "AA", "aaa", "xxx", "x1x1x1", Admin)
  users += User(2, "B", "BB", "bbb", "xxx", "x1x1x1", Regular)
  users += User(3, "C", "CC", "ccc", "xxx", "x1x1x1", Regular)

  var commentaries = new ListBuffer[Comment]()
  commentaries += Comment("aaa", "bas je lep ovaj", DateTime.now().plusDays(2))
  commentaries += Comment("bbb", "eee", DateTime.now().minusDays(2))
  commentaries += Comment("ccc", "neki komentar", DateTime.now())

  cities += City("Belgrade", "Serbia", "1,3 milion people", commentaries)
  cities += City("Frankfurt", "Germany", "0,7 milion people", new ListBuffer[Comment]())
  cities += City("Dubai", "United Arab Emirates", "3,1 milion people", new ListBuffer[Comment]())
  cities += City("Los Angeles", "California", "4 milion people", new ListBuffer[Comment]())

  def findUserById(userId: Long): Option[User] = {
    users.find(u => u.id == userId)
  }

  def findUserByName(name: String): Option[User] = {
    users.filter(u => u.name == name).headOption
  }

  def deleteUser(user: User) = users --= users.filter(o => o.id == user.id)

  def addUser(user: User) = users += user

  def getAllCities(): ListBuffer[City] = cities

  def addCity(city: City) = cities += city

  def updateCity(city: City) = {
    cities --= cities.filter(c => c.name == city.name)
    cities += city
  }

  def findCityByName(name: String): Option[City] = {
    cities.filter(c => c.name == name).headOption
  }

  def addAirport(airport: Airport) = {
    if (!airports.contains(airport))
      airports += airport
  }

  def getAirports(): ListBuffer[Airport] = airports

  def addRoute(route: Route) = {
    if (!routes.contains(route))
      routes += route
  }

  def getRoutes(): ListBuffer[Route] = routes

}
