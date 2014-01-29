package controllers

import akka.actor.Address

case class UserInfo(mail: String, host: String, playerSystemAddress: Option[Address])

object UserInfo {
  def apply(mail: String, remoteAddress: String): UserInfo = new UserInfo(mail: String, remoteAddress, None)
}
