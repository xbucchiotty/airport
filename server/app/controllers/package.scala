import akka.actor.{ActorRef, Address}
import akka.event.{EventStream, EventBus}

package object controllers {

  type Users = collection.immutable.Map[TeamMail, UserInfo]
  type Systems = collection.immutable.Map[HostName, Address]
  type Contexts = collection.immutable.Map[TeamMail, GameContext]

  case class TeamMail(value: String) extends AnyVal

  case class HostName(value: String) extends AnyVal

  object HostName {

    def from(address: Address): HostName =
      HostName(address.host.get)

    def from(request: play.api.mvc.Request[_]): HostName = {
      HostName(request.host.split(":").head)
    }
  }

  object Systems {

    def empty: Systems = Map.empty[HostName, Address]
  }

  object Users {

    def empty: Users = Map.empty[TeamMail, UserInfo]

  }

  object Contexts {

    def empty: Contexts = Map.empty[TeamMail, GameContext]

  }

  case class UserInfo(mail: TeamMail, host: HostName, playerSystemAddress: Option[Address] = None)

  case class GameContext(listener: ActorRef, game: ActorRef, eventBus: EventStream)

}
