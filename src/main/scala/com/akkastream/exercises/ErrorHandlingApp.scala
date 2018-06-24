package com.akkastream.exercises

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import akka.stream.scaladsl.Source

object ErrorHandlingApp extends App{

  implicit val system = ActorSystem("ErrorHandlingApp")
  implicit val materializer = ActorMaterializer(ActorMaterializerSettings(system).withSupervisionStrategy(decider))

  def decider: Supervision.Decider = {
    case _: RuntimeException ⇒ Supervision.Restart
    case _                   ⇒ Supervision.Stop
  }

  val m = Source(0 to 6).map(n ⇒
    if (n == 3) throw new RuntimeException("Boom!")
    else n
  ).runForeach(println)


//  val planB = Source(List("five", "six", "seven", "eight"))
//
//  Source(0 to 10).map(n ⇒
//    if (n < 5) n.toString
//    else throw new RuntimeException("Boom!")
//  ).recoverWithRetries(attempts = 1, {
//    case _: RuntimeException ⇒ planB
//  }).runForeach(println)

}
