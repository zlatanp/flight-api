package controllers

import api.DataBase
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.api.test._
import services.UserService

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 *
 * For more information, see https://www.playframework.com/documentation/latest/ScalaTestingWithScalaTest
 */
class UserControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {

  val db = new DataBase
  val userService = new UserService(db)

  "HomeController GET" should {

    "response for not connected user" in {
      val controller = new UserController(stubControllerComponents(), userService)
      val home = controller.index().apply(FakeRequest(GET, "/home"))

      status(home) mustBe BAD_REQUEST
      contentType(home) mustBe Some("application/json")
      contentAsString(home) must be("{\"error\":\"Oops, you are not connected\"}")
    }

    "response for LOG IN endpoint" in {
      val controller = new UserController(stubControllerComponents(), userService)
      val home = controller.login().apply(FakeRequest(POST, "/user/login").withJsonBody(Json.parse("""{ "name": "aaa","password": "xxx"}""")))

      status(home) mustBe OK
      contentType(home) mustBe Some("application/json")
      contentAsString(home) must include("{\"success\":\"login\",\"user\":{\"id\":1,\"firstName\":\"A\",\"lastName\":\"AA\",\"name\":\"aaa\",\"password\":\"xxx\",\"salt\":\"x1x1x1\",\"typeOfUser\":\"Admin\"}}")
    }

    "response for LOG OUT endpoint" in {
      val controller = new UserController(stubControllerComponents(), userService)
      val loggedInUser = controller.logout().apply(FakeRequest(POST, "/user/logout").withSession("connected" -> "aaa", "usertype" -> "Admin"))
      val notLoggedInUser = controller.logout().apply(FakeRequest(POST, "/user/logout"))

      status(loggedInUser) mustBe OK
      contentType(loggedInUser) mustBe Some("application/json")
      contentAsString(loggedInUser) must include("""{"success":"logout","user":"aaa"}""")

      status(notLoggedInUser) mustBe BAD_REQUEST
      contentType(notLoggedInUser) mustBe Some("application/json")
      contentAsString(notLoggedInUser) must be("{\"error\":\"You are not signed in!\"}")
    }
  }
}
