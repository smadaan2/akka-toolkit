package com.akkaactor.exercises

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, PoisonPill, Props}
import com.akkaactor.exercises.Pinger.Pong
import com.akkaactor.exercises.Ponger.Ping
//import language.postfixOps
import scala.concurrent.duration._

class Pinger extends Actor with ActorLogging {
  var countDown = 10
  def receive = {
    case Pong ⇒
      log.info(s"received pong in log, count down $countDown")
      if (countDown > 0) {
        countDown -= 1
        sender() ! Ping
      } else {
        sender() ! PoisonPill
        self ! PoisonPill
      }
  }
}

object Pinger {
  case object Pong
  def props: Props = Props[Pinger]
}

class Ponger(pinger: ActorRef) extends Actor with ActorLogging {
  def receive = {
    case Ping ⇒
      log.info(s"received ping")
      pinger ! Pong
  }
}

object Ponger {
  case object Ping
  def props(pinger: ActorRef) = Props(classOf[Ponger], pinger)
  def props1(pinger: ActorRef) = Props(new Ponger(pinger))

}


object ActorSystemApp extends App {
  val system = ActorSystem("pingpong")

  val pinger = system.actorOf(Pinger.props, "pinger")
  val ponger = system.actorOf(Ponger.props(pinger), "ponger")
  import system.dispatcher

 system.scheduler.scheduleOnce(500 millis) {
   ponger ! Ping
 }

}