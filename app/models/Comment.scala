package models

import org.joda.time.DateTime

case class Comment(user: String, content: String, timestamp: DateTime, cityName: String)