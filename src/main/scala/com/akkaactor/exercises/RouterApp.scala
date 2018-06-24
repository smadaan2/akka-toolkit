package com.akkaactor.exercises

import akka.actor.{Actor, ActorSystem, Props, Terminated}
import akka.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router}


class Master extends Actor {
  var router = {
    val routees = Vector.fill(5) {
      val r = context.actorOf(Props[Worker])
      context watch r
      ActorRefRoutee(r)
    }
    Router(RoundRobinRoutingLogic(), routees)
  }

  def receive = {
    case msg ⇒
      router.route(msg, sender())
    case Terminated(a) ⇒
      router = router.removeRoutee(a)
      val r = context.actorOf(Props[Worker])
      context watch r
      router = router.addRoutee(r)
  }
}

class Worker extends Actor {
  override def receive: Receive = {
    case msg => println(s"Message $msg received at this path ${self.path}")
  }
}

object RouterApp extends App{

  val system = ActorSystem("MasterApp")

  val master = system.actorOf(Props[Master], "master")
  master ! "Hii"
  master ! "Hii2"
  master ! "Hii3"
  master ! "Hii4"
  master ! "Hii5"

}
