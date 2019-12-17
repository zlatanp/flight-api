package helpers

import play.api.libs.json.{JsObject, Json}

object JsonHelper {

  def jsonErrResponse(message: String): JsObject = {
    Json.obj("error" -> message)
  }

  def jsonSuccessResponse(message: String): JsObject = {
    Json.obj("success" -> message)
  }

  def getUserJsonFromRequest(jsObject: JsObject): JsObject = {
    jsObject + ("id" -> Json.toJson(0)) + ("firstName" -> Json.toJson("")) + ("lastName" -> Json.toJson("")) + ("salt" -> Json.toJson("")) + ("typeOfUser" -> Json.toJson("Regular"))
  }

}
