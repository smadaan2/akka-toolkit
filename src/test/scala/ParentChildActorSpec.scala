import SomeActor.{SendToChild, SendToParent}
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.event.LoggingReceive
import akka.testkit.{TestActorRef, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, FreeSpecLike, GivenWhenThen, Matchers}

import scala.concurrent.duration.DurationInt

class ParentChildActorSpec extends TestKit(ActorSystem("some-test-system"))
  with GivenWhenThen
  with FreeSpecLike
  with Matchers
  with BeforeAndAfterAll {

  override def afterAll(): Unit = {
//    system.shutdown()
//    system.awaitTermination(5.seconds)
  }

  "The Actor" - {

    "Should send SendToChild messages to the child" in {

      Given("We an instance of SomeActor using a child forwarder")
      //Create our child probe which we'll use to assert messages against
      val childProbe = TestProbe()

      val someActor: ActorRef = system.actorOf(
        Props(classOf[SomeActor],
          Props(new  ChildForwarder(childProbe.ref))
        )
      )

      When("We send a SendToChild message")
      someActor ! SendToChild("hi child!")


      Then("The child actor probe receives the message")
      childProbe.expectMsg("hi child!")

    }

    "Should send SendToParent messages to the parent" in {

      Given("We an instance of SomeActor using a parent forwarder")
      //Create our parent probe which we'll use to assert messages against
      val parentProbe = TestProbe()
      val actorProps = Props(classOf[SomeActor], Props(classOf[RealChild]))
      val someActor = createParentForwarder(parentProbe, actorProps)

      When("We send a SendToParent message")
      someActor ! SendToParent("hi parent!")

      Then("The child actor probe receives the message")
      parentProbe.expectMsg("hi parent!")
    }

    "Should send SendToParent messages to the parent AND SendToChild message to the child" in {

      Given("We an instance of SomeActor using a parent AND child forwarder")
      //Create our child probe which we'll use to assert messages against
      val parentProbe = TestProbe()
      val childProbe = TestProbe()

      val actorProps = Props(classOf[SomeActor], Props(new  ChildForwarder(childProbe.ref)))
      val someActor = createParentForwarder(parentProbe, actorProps)

      When("We send a SendToChild AND a SendToParent message")
      someActor ! SendToChild("hi child!")
      someActor ! SendToParent("hi parent!")


      Then("The child actor probe receives the message")
      childProbe.expectMsg("hi child!")

      And("The parent actor probe receives the message")
      parentProbe.expectMsg("hi parent!")
    }
  }

  /**
    * Forwards any messages received by this actor to the passed probe.
    * This is used to allow us to pass a props to an actor whilst still monitoring the messages.
    */
  private class ChildForwarder(probe: ActorRef) extends Actor {

    def receive: PartialFunction[Any, Unit] = {
      case x => probe forward x
    }

    override def postStop(): Unit = {
      //We want to forward death onto the probe to track lifecycle
      probe ! "Actor Killed!"
    }
  }

  /**
    * Creates an Actor which is the parent of the actorProps.
    * Any messages received by the parent are forwarded to the probe.
    * Returns actor created using the props.
    */
  private def createParentForwarder(probe: TestProbe, actorProps: Props): ActorRef = {
    val parent = TestActorRef(new Actor {

      val someActor = context.actorOf(actorProps, "someActor")
      def receive = {
        case x =>
          probe.ref forward x

      }
    })

    parent.underlying.child("someActor").get
  }

}

object SomeActor {

  case class SendToChild(msg: String)

  case class SendToParent(msg: String)

}

class SomeActor(someActor: Props) extends Actor {

  val childActor = context.actorOf(someActor)

  override def receive: Receive = LoggingReceive {

    case SendToChild(msg) => childActor ! msg

    case SendToParent(msg) => context.parent ! msg
  }
}

class RealChild extends Actor {

  override def receive: Receive = LoggingReceive {

    case msg => println(s"received $msg")
  }
}
