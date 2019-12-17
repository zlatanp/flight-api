package helpers

import helpers.JsonHelper.jsonErrResponse
import javax.inject.Inject
import play.api.Logger
import play.api.mvc.Results._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class AuthorizationAction @Inject()(parser: BodyParsers.Default)(implicit ec: ExecutionContext)
  extends ActionBuilderImpl(parser) {

  val logger: Logger = Logger(this.getClass())

  override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]) = {
    val userOpt = request.session.get("user")
    println(request.session.get("user"))
    userOpt match {
      case None => {
        logger.debug("Not connected user trying to open index page.")
        Future.successful(Unauthorized(jsonErrResponse("Oops, you are not connected")))
      }
      case Some(user) => {
        logger.debug("User exists.")
        val res: Future[Result] = block(request)
        res
      }
    }
  }
}