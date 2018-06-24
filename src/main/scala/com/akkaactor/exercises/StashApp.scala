package com.akkaactor.exercises



import akka.actor.{Actor, ActorSystem, Props, Stash}
class ActorWithProtocol extends Actor with Stash {
  def receive = {
    case "open" ⇒
      println("opening")
      unstashAll()
      context.become({
        case "write" ⇒ println("writing");context.unbecome()
        case "close" ⇒
          unstashAll()
          context.unbecome()
        case msg ⇒ stash()
      }, discardOld = true) // stack on top instead of replacing
    case msg ⇒ println("stash"); stash()
  }
}

object StashApp  extends App{
  val system = ActorSystem("stashapp")
  val actorwithprotocol = system.actorOf(Props(new ActorWithProtocol),"actorwithprotocol")
  actorwithprotocol ! "msg"
  actorwithprotocol ! "open"
  actorwithprotocol ! "msg"
  actorwithprotocol ! "write"

}
