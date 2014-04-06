package fr.xebia.xke.akka.infrastructure.cluster

import org.scalatest.{ShouldMatchers, FunSpec}
import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import akka.testkit.TestProbe

class RemoteProxySpec extends FunSpec with ShouldMatchers {

  describe("A remote proxy") {
    it("should forward message and reply to the actorSelection") {
      implicit val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))

      val target = TestProbe()

      val proxy = system.actorOf(RemoteProxy.props(system.actorSelection(target.ref.path)))

      val probe = TestProbe()

      probe.send(proxy, "Hello")

      target.expectMsg("Hello")
      target.send(target.lastSender, ", World")

      probe expectMsg ", World"
    }
  }
}
