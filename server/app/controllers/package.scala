import akka.actor.{ActorRef, Address}
import akka.event.{EventStream, EventBus}
import fr.xebia.xke.akka.airport.Airport

package object controllers {

  case class TeamMail(value: String) extends AnyVal{
    override def toString = value
  }

  case class HostName(value: String) extends AnyVal

  object HostName {

    def from(address: Address): HostName =
      HostName(address.host.get)

    def from(request: play.api.mvc.Request[_]): HostName = {
      HostName(request.host.split(":").head)
    }
  }
  case class UserInfo(userId: TeamMail, airport: Airport, playerSystemAddress: Option[Address] = None){
    def airportCode = airport.code
  }

  case class GameContext(listener: ActorRef, game: ActorRef, eventBus: EventStream)

}
