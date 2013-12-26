package fr.xebia.xke.akka.airport

import fr.xebia.xke.akka.airport.specs.ActorSpecs
import akka.testkit.{TestProbe, TestActorRef}
import akka.actor.PoisonPill
import languageFeature.postfixOps
import concurrent.duration._
import fr.xebia.xke.akka.airport.Event.Score

class GameSpec extends ActorSpecs {

  `Given an actor system` {
    implicit system =>

      "Given a game" - {

        "It should terminates if runway terminates" in {
          val gameActor = TestActorRef(new Game())
          val probe = TestProbe()
          probe watch gameActor

          probe.send(gameActor.underlyingActor.runway, PoisonPill)

          probe expectTerminated(gameActor, 100.milliseconds)
        }
      }
  }
  `Given an actor system` {
    implicit system =>

      "Given a game" - {

        "It should terminates if gate terminates" in {
          val gameActor = TestActorRef(new Game())
          val probe = TestProbe()
          probe watch gameActor

          probe.send(gameActor.underlyingActor.gate, PoisonPill)

          probe expectTerminated(gameActor, 100.milliseconds)
        }
      }
  }
  `Given an actor system` {
    implicit system =>

      "Given a game" - {

        "It should terminates if taxiway terminates" in {
          val gameActor = TestActorRef(new Game())
          val probe = TestProbe()
          probe watch gameActor

          probe.send(gameActor.underlyingActor.taxiway, PoisonPill)

          probe expectTerminated(gameActor, 100.milliseconds)
        }
      }
  }
  `Given an actor system` {
    implicit system =>

      "Given a game" - {

        "It should terminates if a non finished plane terminates" in {
          val gameActor = TestActorRef(new Game())
          val probe = TestProbe()
          probe watch gameActor

          probe.within(10 seconds) {
            probe.awaitCond(gameActor.underlyingActor.planes.nonEmpty)

            val aPlane = gameActor.underlyingActor.planes.head
            aPlane.tell(PoisonPill, sender = aPlane)

            probe expectTerminated(gameActor, 100.milliseconds)
          }
        }
      }
  }
  `Given an actor system` {
    implicit system =>

      "Given a game" - {

        "It should not terminates if a finished plane terminates" in {
          val gameActor = TestActorRef(new Game())
          val probe = TestProbe()
          probe watch gameActor

          probe.within(10 seconds) {
            probe.awaitCond(gameActor.underlyingActor.planes.nonEmpty)
            val aPlane = gameActor.underlyingActor.planes.head

            gameActor.tell(Score(10), aPlane)
            aPlane.tell(PoisonPill, sender = aPlane)

            probe expectNoMsg 100.milliseconds
          }
        }
      }
  }

  `Given an actor system` {
    implicit system =>

      "Given a game" - {

        "It should generate a plane within 1 second" in {
          val gameActor = TestActorRef(new Game())
          val probe = TestProbe()
          probe watch gameActor

          probe.send(gameActor.underlyingActor.runway, PoisonPill)

          probe expectTerminated(gameActor, 100.milliseconds)
        }
      }
  }
}
