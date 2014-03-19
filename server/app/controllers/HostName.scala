package controllers

import akka.actor.Address

case class HostName(value: String) extends AnyVal

object HostName {

  def from(address: Address): HostName =
    HostName(address.host.get)

  def from(request: play.api.mvc.Request[_]): HostName = {
    HostName(request.host.split(":").head)
  }
}