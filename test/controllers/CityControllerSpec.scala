package controllers

import api.DataBase
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.libs.json.Json
import play.api.test._
import play.api.test.Helpers._

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 *
 * For more information, see https://www.playframework.com/documentation/latest/ScalaTestingWithScalaTest
 */
class CityControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {

  val db = new DataBase

  "CityController GET" should {

    "return list of all cities and comments" in {
      val controller = new CityController(stubControllerComponents(), db)
      val home = controller.all.apply(FakeRequest(GET, "/city/all").withJsonBody(Json.parse("""{}""")).withSession("connected" -> "aaa", "usertype" -> "Admin"))

      status(home) mustBe OK
      contentType(home) mustBe Some("application/json")
      contentAsString(home) must include ("""{"name":"Belgrade","country":"Serbia","description":"1,3 milion people","comments":[{"user":"aaa","content":"Nice City!","timestamp":"2019-10-07T15:06:15.502+02:00","cityName":"Belgrade"},{"user":"bbb","content":"I like It!","timestamp":"2019-10-05T15:06:15.502+02:00","cityName":"Belgrade"},{"user":"ccc","content":"Capital city of Serbia.","timestamp":"2019-10-09T15:06:15.502+02:00","cityName":"Belgrade"}]}""")
    }

    "return list of all cities with limited number of comments" in {
      val controller = new CityController(stubControllerComponents(), db)
      val home = controller.all.apply(FakeRequest(GET, "/city/all").withJsonBody(Json.parse("""{"comments" : 2}""")).withSession("connected" -> "aaa", "usertype" -> "Admin"))

      status(home) mustBe OK
      contentType(home) mustBe Some("application/json")
      contentAsString(home) must include ("""{"name":"Belgrade","country":"Serbia","description":"1,3 milion people","comments":[{"user":"ccc","content":"Capital city of Serbia.","timestamp":"2019-10-09T15:06:15.502+02:00","cityName":"Belgrade"},{"user":"aaa","content":"Nice City!","timestamp":"2019-10-07T15:06:15.502+02:00","cityName":"Belgrade"}]}""")
    }

    "return list of one city with comments" in {
      val controller = new CityController(stubControllerComponents(), db)
      val home = controller.getCity.apply(FakeRequest(GET, "city/one").withJsonBody(Json.parse("""{"cityName" : "Belgrade"}""")).withSession("connected" -> "aaa", "usertype" -> "Admin"))

      status(home) mustBe OK
      contentType(home) mustBe Some("application/json")
      contentAsString(home) must include ("""{"cities":[{"name":"Belgrade","country":"Serbia","description":"1,3 milion people","comments":[{"user":"aaa","content":"Nice City!","timestamp":"2019-10-07T15:06:15.502+02:00","cityName":"Belgrade"},{"user":"bbb","content":"I like It!","timestamp":"2019-10-05T15:06:15.502+02:00","cityName":"Belgrade"},{"user":"ccc","content":"Capital city of Serbia.","timestamp":"2019-10-09T15:06:15.502+02:00","cityName":"Belgrade"}]}]}""")
    }

    "return list of one city with limited number of comments" in {
      val controller = new CityController(stubControllerComponents(), db)
      val home = controller.getCity.apply(FakeRequest(GET, "city/one").withJsonBody(Json.parse("""{"cityName" : "Belgrade", "comments" : 1 }""")).withSession("connected" -> "aaa", "usertype" -> "Admin"))

      status(home) mustBe OK
      contentType(home) mustBe Some("application/json")
      contentAsString(home) must include ("""{"cities":[{"name":"Belgrade","country":"Serbia","description":"1,3 milion people","comments":[{"user":"ccc","content":"Capital city of Serbia.","timestamp":"2019-10-09T15:06:15.502+02:00","cityName":"Belgrade"}]}]}""")
    }

    "admin create new city " in {
      val controller = new CityController(stubControllerComponents(), db)
      val home = controller.add.apply(FakeRequest(POST, "/city/add").withJsonBody(Json.parse("""{ "name": "Podgorica", "country": "Montenegro", "description": "0,5 milion people" }""")).withSession("connected" -> "aaa", "usertype" -> "Admin"))

      status(home) mustBe OK
      contentType(home) mustBe Some("application/json")
      contentAsString(home) must include ("""[{"success":"create"},{"city":{"name":"Podgorica","country":"Montenegro","description":"0,5 milion people"}}]""")
    }
  }
}
