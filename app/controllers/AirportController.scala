package controllers

import api.DataBase
import api.JsonHelper.{jsonErrResponse, jsonSuccessResponse}
import com.typesafe.config.ConfigFactory
import javax.inject.{Inject, Singleton}
import models.{Airport, Route}
import play.api.Logger
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.Source

@Singleton
class AirportController @Inject()(cc: ControllerComponents, db: DataBase) extends AbstractController(cc) {

  val logger: Logger = Logger(this.getClass())

  val airportsPath = ConfigFactory.load().getString("data.airport")
  val routesPath = ConfigFactory.load().getString("data.route")

  def importAirport() = Action.async({ request: Request[AnyContent] =>
    request.session
      .get("usertype")
      .map { usertype =>
        if (usertype.equalsIgnoreCase("admin")) {

          Source.fromFile(getClass().getClassLoader().getResource(airportsPath).getFile).getLines.foreach { x =>
            x.replaceAll("\"", "").split(",").toVector match {
              case Vector(airportId, name, city, country, iata, icao, latitude, longitude, altitude, timezone, dst, tz, typeOfAirport, source) => {
                val airport = Airport(airportId, name, city, country, iata, icao, BigDecimal(latitude), BigDecimal(longitude), altitude.toDouble, if (timezone == "\\N") BigDecimal(0) else BigDecimal(timezone), dst, tz, typeOfAirport, source)
                db.getAllCities().map(_.foreach {
                  case (name, country, description) => {
                    if (name == city) db.addAirport(airport)
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
                val route = Route(airline, airlineId, sourceAirport, sourceAirportId, destinationAirpot, destinationAirportId, codeshare, stops.toInt, equipment, price.toDouble)
                db.getAllAirports().map(_.foreach {
                  case (airportId, name, city, country, iata, icao, latitude, longitude, altitude, timezone, dst, tz, typeOfAirport, source) => {
                    if (airportId == sourceAirportId) db.addRoute(route)
                  }
                  case _ => None
                })
              }
              case _ => None
            }
          }
          logger.info("Success import routes");
          Future(Ok(jsonSuccessResponse("import")))
        } else {
          logger.warn("User have no permission to access this service");
          Future(BadRequest(jsonErrResponse("Sorry, this page isn't available")))
        }
      }
      .getOrElse {
        logger.info("You are not signed in!")
        Future(BadRequest(jsonErrResponse("You are not signed in!")))
      }
  })
}
