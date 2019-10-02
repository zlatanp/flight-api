package models

import org.joda.time.DateTime

import scala.collection.mutable.ListBuffer

case class City(name: String, country: String, description: String, comments: ListBuffer[Comment]) {
  def addComment(comment: Comment) = comments += comment

  def removeComment(userName: String) = {
    comments --= comments.filter(c => c.user == userName)
  }
}

case class Comment(user: String, content: String, timestamp: DateTime)
