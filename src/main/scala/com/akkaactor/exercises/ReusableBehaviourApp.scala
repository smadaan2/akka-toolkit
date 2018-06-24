package com.akkaactor.exercises

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import com.akkaactor.exercises.ProducerConsumer.{Give, GiveMeThings}

trait ProducerBehavior {
  this: Actor ⇒

  val producerBehavior: Receive = {
    case GiveMeThings ⇒
      sender() ! Give("thing")
  }
}

trait ConsumerBehavior {
  this: Actor with ActorLogging ⇒

  val consumerBehavior: Receive = {
    case ref: ActorRef ⇒
      ref ! GiveMeThings

    case Give(thing) ⇒
      log.info("Got a thing! It's {}", thing)
  }
}

class ProducerConsumer extends Actor with ActorLogging
  with ProducerBehavior with ConsumerBehavior {
  def receive = producerBehavior.orElse[Any, Unit](consumerBehavior)
}

object ProducerConsumer {
  case object GiveMeThings
  final case class Give(thing: Any)
  def props = Props[ProducerConsumer]
}

object ReusableBehaviourApp extends App{
  val system = ActorSystem("producerconsumer")
  val producerConsumer = system.actorOf(ProducerConsumer.props,"producerconsumer")
  producerConsumer ! producerConsumer


}
