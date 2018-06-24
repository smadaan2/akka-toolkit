package com.akkaactor.exercises

import akka.actor.{Actor, ActorSystem, Props, Timers}

import scala.concurrent.duration._

class TimerActor extends Actor with Timers {
  import TimerActor._
  timers.startSingleTimer(TickKey, FirstTick, 500.millis)

  def receive = {
    case FirstTick ⇒
      println("FirstTick")
      timers.startPeriodicTimer(Tick,TickKey, 1.second)
    case TickKey => println("TickKey")
    case Tick ⇒ println("Tick")

  }
}


object TimerActor {
  private case object TickKey
  private case object FirstTick
  private case object Tick
}

object TimerApp extends App{
  val system = ActorSystem("timer")
  val myactor = system.actorOf(Props(new TimerActor),"myactor")

}


