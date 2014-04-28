package fr.xebia.xke.akka.infrastructure.cluster

import scala.concurrent.duration._
import org.scalatest.{ShouldMatchers, FunSpec}
import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import akka.testkit.TestProbe
import fr.xebia.xke.akka.infrastructure.cluster.RemoteProxy.{Register, Unregister}
import language.postfixOps

class RemoteProxySpec extends FunSpec with ShouldMatchers {

  describe("A remote proxy") {
    it("should forward message and reply to the actorSelection") {
      implicit val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))

      val target = TestProbe()
      val proxy = system.actorOf(RemoteProxy.props(target.ref))
      val probe = TestProbe()

      probe.send(proxy, "Hello")
      target.expectMsg("Hello")

      target.send(target.lastSender, ", World")
      probe expectMsg ", World"
    }

    it("should not forward message when unregistered") {
      implicit val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))

      val target = TestProbe()
      val proxy = system.actorOf(RemoteProxy.props(target.ref))
      val probe = TestProbe()

      probe.send(proxy, Unregister)
      probe.send(proxy, "Hello")

      target expectNoMsg (100 milliseconds)
    }

    it("should forward message to the last registered actorSelection") {
      implicit val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))

      val target1 = TestProbe()
      val target2 = TestProbe()
      val proxy = system.actorOf(RemoteProxy.props(target1.ref))
      val probe = TestProbe()

      probe.send(proxy, Register(target2.ref))
      probe.send(proxy, "Hello")

      target1 expectNoMsg (100 milliseconds)
      target2 expectMsg "Hello"
    }

    it("should forward message after registering a new target") {
      implicit val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))

      val target1 = TestProbe()
      val target2 = TestProbe()
      val proxy = system.actorOf(RemoteProxy.props(target1.ref))
      val probe = TestProbe()

      probe.send(proxy, Unregister)
      probe.send(proxy, Register(target2.ref))
      probe.send(proxy, "Hello")

      target1 expectNoMsg (100 milliseconds)
      target2 expectMsg "Hello"
    }

    it("should forward message from outbound with proxy when sender is the proxied target") {
      implicit val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))

      val targetATC = TestProbe()
      val proxy = system.actorOf(RemoteProxy.props(targetATC.ref), "proxy")
      val plane = TestProbe()
      plane.send(proxy, "Hello")
      targetATC.expectMsg("Hello")

      //When
      targetATC reply "World"

      //Then
      plane expectMsg "World"
      plane.lastSender should be(proxy)
    }

    it("should forward message from outbound with original sender when sender is not the proxied target") {
      implicit val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))

      val targetATC = TestProbe()
      val proxy = system.actorOf(RemoteProxy.props(targetATC.ref), "proxy")
      val plane = TestProbe()
      plane.send(proxy, "Hello")
      targetATC.expectMsg("Hello")

      //When
      val operator = TestProbe()
      operator.send(targetATC.lastSender, "World")

      //Then
      plane expectMsg "World"
      plane.lastSender should be(operator.ref)
    }
  }
}
