import akka.actor.Address

package object controllers {

  type Users = collection.immutable.Map[String, UserInfo]
  type Systems = collection.immutable.Map[String, Address]

}
