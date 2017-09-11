package fr.xebia.xke.akka.game

import org.scalatest._
import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import akka.testkit.TestProbe
import scala.concurrent.Await
import scala.concurrent.duration._
import fr.xebia.xke.akka.airport.message.PlaneEvent.Ack

class OrderSenderSpec extends FunSpec with ShouldMatchers with BeforeAndAfterEach {

  describe("An order sender") {

    it("should send message to target on initialization") {
      val to = TestProbe()

      system.actorOf(OrderSender.props(to.ref, "Hello", 250.milliseconds))

      to expectMsg "Hello"
    }

    it("should repeat message when the target does not ack the message within timeout") {
      val to = TestProbe()

      system.actorOf(OrderSender.props(to.ref, "Hello", 250.milliseconds))
      to expectMsg "Hello"

      //given nothing happens during timeout
      Thread.sleep(300)

      to expectMsg "Hello"
    }

    it("should terminate when it receives an ack") {
      val to = TestProbe()
      val orderSender = system.actorOf(OrderSender.props(to.ref, "Hello", 250.milliseconds))
      val probe = TestProbe()
      probe watch orderSender

      to.send(orderSender,Ack)

      probe expectTerminated(orderSender, 250.milliseconds)
    }

    it("should stop sending message when it receives an ack") {
      val to = TestProbe()
      val orderSender = system.actorOf(OrderSender.props(to.ref, "Hello", 250.milliseconds))
      val probe = TestProbe()
      probe watch orderSender
      to expectMsg "Hello"

      Thread.sleep(300)
      to expectMsg "Hello"
      to reply Ack

      to expectNoMsg 300.milliseconds
    }
  }

  implicit var system: ActorSystem = _

  override protected def afterEach(): Unit = {
    Await.result(system.terminate(), 10 second)
  }

  override protected def beforeEach(): Unit = {
    system = {
      ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
    }
  }

}
