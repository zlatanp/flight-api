package services

import com.typesafe.config.ConfigFactory
import database.DataBase
import helpers.JsonHelper.{jsonErrResponse, jsonSuccessResponse}
import javax.inject.Inject
import models.{Airport, City, Flight, Route}
import play.api.Logger
import play.api.libs.json.{JsValue, Json}

import scala.collection.mutable.MutableList
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.Source

class AirportService @Inject()(db: DataBase) {

  val logger: Logger = Logger(this.getClass())

  val airportsPath = ConfigFactory.load().getString("data.airport")
  val routesPath = ConfigFactory.load().getString("data.route")

  var allAirportsCache: MutableList[Airport] = new MutableList[Airport]()
  var allRoutesCache: MutableList[Route] = new MutableList[Route]()

  var allFlightsList: MutableList[Flight] = new MutableList[Flight]()

  def importAirport(usertype: String) = {
    if (usertype.equalsIgnoreCase("admin")) {

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

      logger.info("Success import airports");
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

      logger.info("Success import routes");
      Future(jsonSuccessResponse("import"))
    } else {
      logger.warn("User have no permission to access this service");
      Future(jsonErrResponse("Sorry, this page isn't available"))
    }
  }

  def getFlights(json: Option[JsValue]) = {
    if (json.isEmpty) {
      logger.error("Empty Json data.")
      Future(jsonErrResponse("Expecting Json data"))
    } else {
      val from = (json.get \ "from").asOpt[String]
      val to = (json.get \ "to").asOpt[String]

      (from, to) match {
        case (Some(sourceCityName), Some(destinationCityName)) => {

          val cities =
            for {
              sourceOpt <- db.findCityByName(sourceCityName)
              destinationOpt <- db.findCityByName(destinationCityName)
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
        case (_, _) => Future(jsonErrResponse("Bad input"))
      }
    }
  }

  def findAllFlights(sourceCity: City, destinationCity: City) = {
    allFlightsList.clear()
    allAirportsCache.filter(airport => airport.city == sourceCity.name).foreach(sourceAirport => {
      allAirportsCache.filter(airport => airport.city == destinationCity.name).foreach(destinationAirport => {
        findFlightsBetweenAirports(sourceAirport, destinationAirport)
      })
    })
  }


  def findFlightsBetweenAirports(sourceAirport: Airport, destinationAirport: Airport): Unit = {
    val routes = allRoutesCache.filter(route => route.sourceAirportId == sourceAirport.airportId)
    routes.foreach(route => {
      getFlightsForRoute(route: Route, Flight(0, 0, new MutableList[Route]()), destinationAirport)
    })
  }

  def getFlightsForRoute(route: Route, previousFlights: Flight, destinationAirport: Airport): Unit = {
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

  def createFlight(previousFlights: Flight, route: Route): Flight = {
    val newRoutes: MutableList[Route] = previousFlights.routes += route
    val price: Double = newRoutes.map(route => route.price).sum
    val distance: Double = previousFlights.distance + flightDistance(newRoutes)

    Flight(round(price), round(distance), newRoutes)
  }

  def flightDistance(routes: MutableList[Route]): Double = {
    routes.map(route => getAirportsDistance(allAirportsCache.filter(a => a.airportId == route.sourceAirportId).head, allAirportsCache.filter(a => a.airportId == route.destinationAirportId).head)).sum
  }

  def getAirportsDistance(sourceAirport: Airport, destinationAirport: Airport): Double = {
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

  def round(number: Double): Double = {
    Math.round(number * 100.0) / 100.0
  }

  def getCheapest(allFlightsList: MutableList[Flight]): Flight = {
    allFlightsList.sortBy(_.price).head
  }
}
