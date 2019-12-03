package helpers

import play.api.libs.json.{JsObject, Json}

object JsonHelper {

  def jsonErrResponse(message: String): JsObject = {
    Json.obj("error" -> message)
  }

  def jsonSuccessResponse(message: String): JsObject = {
    Json.obj("success" -> message)
  }

}
