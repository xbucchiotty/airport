package fr.xebia.xke.akka.airport

import akka.actor.{Props, ActorSystem}
import akka.testkit.TestProbe
import fr.xebia.xke.akka.airport.Event.Parked
import languageFeature.postfixOps
import org.scalatest.{FlatSpec, FreeSpec, OneInstancePerTest}
import concurrent.duration._

class GateSpec extends FreeSpec with OneInstancePerTest {

  implicit val system = ActorSystem.create("GateSpec")

  "A free gate" - {

    s"when a plane parks in" - {

      "nothing should happens" in {

        val gate = system.actorOf(Props[Gate], "gate")
        val probe = TestProbe()
        probe watch gate


        TestProbe().send(gate, Parked)
        probe.expectNoMsg(10 milliseconds)

        system.shutdown()
      }
    }
  }

  "An occupied gate " - {

    s"when a plane parks in " - {

      "it should terminates" in {
        val gate = system.actorOf(Props[Gate], "gate")
        val probe = TestProbe()
        probe watch gate

        TestProbe().send(gate, Parked)

        //s"When a plane parks in ${gate.path.name}" - {
        TestProbe().send(gate, Parked)
        probe.expectTerminated(gate)

        system.shutdown()
      }
    }

  }


}

trait MySpec extends FlatSpec {

  object Given {
    def an_occupied_gate() {

    }
  }


  trait GateFixture {

    def system: ActorSystem

    val gate = system.actorOf(Props[Gate])
  }

}