package fr.xebia.xke.akka.infrastructure.cluster

import org.scalatest.{ShouldMatchers, FunSpec}
import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import akka.testkit.TestProbe

class SimpleProxySpec extends FunSpec with ShouldMatchers {

  describe("A simple proxy") {

    it("should send message to the outbound target") {
      implicit val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
      val simpleProxy = system.actorOf(SimpleProxy.props(TestProbe().ref, TestProbe().ref))

      //when
      val outbound = TestProbe()
      TestProbe().send(simpleProxy, SimpleProxy.Send("Hello", outbound.ref))

      //Then
      outbound expectMsg "Hello"
    }


    it("should forward message from outbound to upperProxy") {
      implicit val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))

      val upperProxy = TestProbe()
      val simpleProxy = system.actorOf(SimpleProxy.props(TestProbe().ref, upperProxy.ref))

      //when
      val outboundMessageSender = TestProbe()
      outboundMessageSender.send(simpleProxy, "Back")

      //Then
      upperProxy expectMsg SimpleProxy.Reply("Back", outboundMessageSender.ref)
    }
  }
}
