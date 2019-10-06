package models

import scala.collection.mutable.ListBuffer

case class Flight(price: Double, distance: Double, routes: ListBuffer[Route])
