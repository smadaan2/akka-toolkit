package com.akkaactor.exercises



import akka.actor.{Actor, ActorIdentity, ActorRef, ActorSystem, Identify, Props, Terminated}

class Follower extends Actor {
  val identifyId = 1
  context.actorSelection("/actorselectionsystem/user/another") ! Identify(identifyId)

  def receive = {
    case ActorIdentity(`identifyId`, Some(ref)) ⇒
      println("actoridentity ref:::" + ref.path)
      context.watch(ref)
      context.become(active(ref))
    case ActorIdentity(`identifyId`, None) ⇒
      println("no reference")
      context.stop(self)
  }

  def active(another: ActorRef): Actor.Receive = {
    case Terminated(`another`) ⇒ context.stop(self)
  }
}


object ActorSelectionApp extends App {
  val system = ActorSystem("actorselectionsystem")
  val follower = system.actorOf(Props(new Follower),"follower")
}