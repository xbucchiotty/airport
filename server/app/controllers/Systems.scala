package controllers

import akka.actor.Address

object Systems {

  def empty: Systems = Map.empty[String, Address]
}
