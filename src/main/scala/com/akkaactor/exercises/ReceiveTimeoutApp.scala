package com.akkaactor.exercises


import akka.actor.{Actor, ActorSystem, Props, ReceiveTimeout}

import scala.concurrent.duration._
class MyActor extends Actor {
  // To set an initial delay
  context.setReceiveTimeout(3000000 milliseconds)
  def receive = {
    case "Hello" ⇒
      // To set in a response to a message
      context.setReceiveTimeout(3 milliseconds)
      println("abd")
      println("abd1")
    case ReceiveTimeout ⇒
      // To turn it off
      context.setReceiveTimeout(Duration.Undefined)
      self ! "AfterTimeout"

    case "AfterTimeout" => println("AfterTimeout")
  }
}

object ReceiveTimeoutApp extends App{
  val system = ActorSystem("receivetimeoutapp")
  val myactor = system.actorOf(Props(new MyActor),"myactor")
  myactor ! "Hello"
}
