package services

import database.DataBase
import helpers.JsonHelper.{jsonErrResponse, jsonSuccessResponse}
import javax.inject.Inject
import models._
import play.api.libs.json.Json
import play.api.{Configuration, Logger}

import scala.collection.mutable.MutableList
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.Source

class AirportService @Inject()(config: Configuration, db: DataBase) {

  val logger: Logger = Logger(this.getClass)

  val airportsPath = config.get[String]("data.airport")
  val routesPath = config.get[String]("data.route")

  var allAirportsCache: MutableList[Airport] = new MutableList[Airport]()
  var allRoutesCache: MutableList[Route] = new MutableList[Route]()

  var allFlightsList: MutableList[Flight] = new MutableList[Flight]()

  def importAirport(user: User) = {
    user.typeOfUser match {
      case Admin => {
        val allCities = for {
          cities <- db.getAllCities()
        } yield cities

        Source.fromFile(getClass().getClassLoader().getResource(airportsPath).getFile).getLines.foreach { x =>
          x.replaceAll("\"", "").split(",").toVector match {
            case Vector(airportId, name, city, country, iata, icao, latitude, longitude, altitude, timezone, dst, tz, typeOfAirport, source) => {
              val airport = Airport(airportId, name, city, country, iata, icao, BigDecimal(latitude), BigDecimal(longitude), altitude.toDouble, if (timezone == "\\N") BigDecimal(0) else BigDecimal(timezone), dst, tz, typeOfAirport, source)
              allCities.map(_.foreach {
                case (name, country, description, comments) => {
                  if (name == city) {
                    db.addAirport(airport)
                    allAirportsCache += airport
                  }
                }
                case _ => None
              })
            }
            case _ => None
          }
        }

        logger.debug("Success import airports");
        Source.fromFile(getClass().getClassLoader().getResource(routesPath).getFile).getLines.foreach { x =>
          x.split(",").toVector match {
            case Vector(airline, airlineId, sourceAirport, sourceAirportId, destinationAirpot, destinationAirportId, codeshare, stops, equipment, price) => {

              val sourceAirportDB = allAirportsCache.find(_.airportId == sourceAirportId)
              val destinationAirportDB = allAirportsCache.find(_.airportId == destinationAirportId)

              (sourceAirportDB, destinationAirportDB) match {
                case (Some(sAirport), Some(dAirport)) => {
                  val route = Route(airline, airlineId, sourceAirport, sourceAirportId, destinationAirpot, destinationAirportId, codeshare, stops.toInt, equipment, price.toDouble)
                  db.addRoute(route)
                  allRoutesCache += route
                }
                case (_, _) => None
              }
            }
            case _ => None
          }
        }
        logger.debug("Success import routes");
        Future(jsonSuccessResponse("import"))
      }
      case Regular => {
        logger.debug("User have no permission to access this service");
        Future(jsonErrResponse("Sorry, this page isn't available"))
      }
    }
  }

  def getFlights(sourceCity: String, destinationCity: String) = {
    val cities =
      for {
        sourceOpt <- db.findCityByName(sourceCity)
        destinationOpt <- db.findCityByName(destinationCity)
      } yield (sourceOpt, destinationOpt)

    cities.map({
      case (Some(sCity), Some(dCity)) => {
        findAllFlights(City(sCity._1, sCity._2, sCity._3, sCity._4), City(dCity._1, dCity._2, dCity._3, sCity._4))
        if (allFlightsList.isEmpty) {
          jsonErrResponse("No airports imported")
        } else {
          jsonSuccessResponse("flights") ++ Json.obj("flight" -> Json.toJson(getCheapest(allFlightsList)))
        }
      }
      case (Some(sCity), _) => jsonErrResponse("Destination city does not exist in database!")
      case (_, Some(sCity)) => jsonErrResponse("Source city does not exist in database!")
      case (_, _) => jsonErrResponse("Source and Destination city does not exist in database!")
    })
  }

  private def findAllFlights(sourceCity: City, destinationCity: City) = {
    allFlightsList.clear()
    allAirportsCache.filter(airport => airport.city == sourceCity.name).foreach(sourceAirport => {
      allAirportsCache.filter(airport => airport.city == destinationCity.name).foreach(destinationAirport => {
        findFlightsBetweenAirports(sourceAirport, destinationAirport)
      })
    })
  }


  private def findFlightsBetweenAirports(sourceAirport: Airport, destinationAirport: Airport): Unit = {
    val routes = allRoutesCache.filter(route => route.sourceAirportId == sourceAirport.airportId)
    routes.foreach(route => {
      getFlightsForRoute(route: Route, Flight(0, 0, new MutableList[Route]()), destinationAirport)
    })
  }

  private def getFlightsForRoute(route: Route, previousFlights: Flight, destinationAirport: Airport): Unit = {
    val visitedAirports = previousFlights.routes.map(r => r.sourceAirportId)
    if (!visitedAirports.contains(route.destinationAirportId)) {
      val flight = createFlight(previousFlights, route)
      if (route.destinationAirportId == destinationAirport.airportId) {
        allFlightsList += flight
      } else {
        val nextAirports = allAirportsCache.filter(airport => airport.airportId == route.destinationAirportId)
        nextAirports.foreach(nextAirport => {
          val nextRoutes = allRoutesCache.filter(route => route.sourceAirportId == nextAirport.airportId)
          nextRoutes.foreach(nextRoute => {
            val visited = flight.routes.map(r => r.sourceAirportId)
            if (!visited.contains(nextRoute.destinationAirportId) && !visited.contains(nextRoute.sourceAirportId))
              getFlightsForRoute(nextRoute, flight, destinationAirport)
          })
        })
      }
    }
  }

  private def createFlight(previousFlights: Flight, route: Route): Flight = {
    val newRoutes: MutableList[Route] = previousFlights.routes += route
    val price: Double = newRoutes.map(route => route.price).sum
    val distance: Double = previousFlights.distance + flightDistance(newRoutes)

    Flight(round(price), round(distance), newRoutes)
  }

  private def flightDistance(routes: MutableList[Route]): Double = {
    routes.map(route => getAirportsDistance(allAirportsCache.filter(a => a.airportId == route.sourceAirportId).head, allAirportsCache.filter(a => a.airportId == route.destinationAirportId).head)).sum
  }

  private def getAirportsDistance(sourceAirport: Airport, destinationAirport: Airport): Double = {
    val sourceLatitude: Double = sourceAirport.latitude.toDouble
    val sourceLongitude: Double = sourceAirport.longitude.toDouble
    val sourceAltitude: Double = sourceAirport.altitude

    val destinationLatitude: Double = destinationAirport.latitude.toDouble
    val destinationLongitude: Double = destinationAirport.longitude.toDouble
    val destinationAltitude: Double = destinationAirport.altitude

    val radius: Int = 6371;

    val latDistance: Double = Math.toRadians(destinationLatitude - sourceLatitude)
    val lonDistance: Double = Math.toRadians(destinationLongitude - sourceLongitude)

    val a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) + Math.cos(Math.toRadians(sourceLatitude)) * Math.cos(Math.toRadians(destinationLatitude)) * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

    val distance = radius * c * 1000;
    val height: Double = sourceAltitude - destinationAltitude

    val airportsDistance: Double = Math.pow(distance, 2) + Math.pow(height, 2)
    Math.sqrt(airportsDistance) / 1000
  }

  private def round(number: Double): Double = {
    Math.round(number * 100.0) / 100.0
  }

  private def getCheapest(allFlightsList: MutableList[Flight]): Flight = {
    allFlightsList.sortBy(_.price).head
  }
}
