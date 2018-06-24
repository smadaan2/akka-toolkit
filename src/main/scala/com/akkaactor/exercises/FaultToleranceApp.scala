package com.akkaactor.exercises

import akka.actor.SupervisorStrategy._
import akka.actor.{Actor, ActorSystem, OneForOneStrategy, Props}

import scala.concurrent.duration._

class Supervisor extends Actor {
  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case _: ArithmeticException      ⇒ Resume
      case _: NullPointerException     ⇒ Restart
      case _: IllegalArgumentException ⇒ Stop
      case _: Exception                ⇒ Escalate
    }

  def receive = {
    case p: Props ⇒ sender() ! context.actorOf(p); context.watch(context.actorOf(p))
  }
}

class Child extends Actor {
  var state = 0
  def receive = {
    case ex: Exception ⇒ throw ex
    case x: Int        ⇒ state = x
    case "get"         ⇒ sender() ! state
  }
}


object FaultToleranceApp  extends App{
  val system = ActorSystem("faulttoleranceApp")
  val supervisor = system.actorOf(Props[Supervisor],"supervisor")
  val childProps = Props[Child]
  val child = system.actorOf(childProps,"child")

  supervisor ! childProps

  child ! 1
  child ! "get"
  child ! new ArithmeticException
  child ! "get"


}
