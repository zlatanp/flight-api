package models

case class City(name: String, country: String, description: String)
case class CityJson(name: String, country: String, description: String, comments: List[Comment])


