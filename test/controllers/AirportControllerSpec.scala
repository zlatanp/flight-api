package controllers

import api.DataBase
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.test._
import play.api.test.Helpers._

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 *
 * For more information, see https://www.playframework.com/documentation/latest/ScalaTestingWithScalaTest
 */
class AirportControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {

  val db = new DataBase

  "AirportController GET" should {

    "return bad request for regular and not logged in users" in {
      val controller = new AirportController(stubControllerComponents(), db)
      val notLoggedInUser = controller.importAirport().apply(FakeRequest(POST, "/import"))
      val regularUserRequest = controller.importAirport().apply(FakeRequest(POST, "/import").withSession("connected" -> "bbb", "usertype" -> "Regular"))

      status(notLoggedInUser) mustBe BAD_REQUEST
      contentType(notLoggedInUser) mustBe Some("application/json")
      contentAsString(notLoggedInUser) must be("{\"error\":\"You are not signed in!\"}")

      status(regularUserRequest) mustBe BAD_REQUEST
      contentType(regularUserRequest) mustBe Some("application/json")
      contentAsString(regularUserRequest) must be("{\"error\":\"Sorry, this page isn't available\"}")
    }

    "return status 200 for admin user" in {
      val controller = new AirportController(stubControllerComponents(), db)
      val adminUserRequest = controller.importAirport().apply(FakeRequest(POST, "/import").withSession("connected" -> "aaa", "usertype" -> "Admin"))

      status(adminUserRequest) mustBe OK
      contentType(adminUserRequest) mustBe Some("application/json")
      contentAsString(adminUserRequest) must be("{\"success\":\"import\"}")
    }
  }
}
