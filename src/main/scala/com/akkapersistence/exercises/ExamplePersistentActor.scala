package com.akkapersistence.exercises

import akka.actor._
import akka.persistence._

case class Cmd(data: String)
case class Evt(data: String)

case class ExampleState(events: List[String] = Nil) {
  def updated(evt: Evt): ExampleState = copy(evt.data :: events)
  def size: Int = events.length
  override def toString: String = events.reverse.toString
}

class ExamplePersistentActor extends PersistentActor {
  override def persistenceId = "sample-id-1"

  var state = ExampleState()

  def updateState(event: Evt): Unit =
    state = state.updated(event)

  def numEvents =
    state.size

  val receiveRecover: Receive = {
    case evt: Evt                                 ⇒
      println(s"Event::::$evt")
      updateState(evt)
    case SnapshotOffer(_, snapshot: ExampleState) ⇒ println(s"snapshot"); state = snapshot
  }

  val snapShotInterval = 5
  val receiveCommand: Receive = {
    case Cmd(data) ⇒
      persist(Evt(s"${data}-${numEvents}")) { event ⇒
        updateState(event)
        //context.system.eventStream.publish(event)
        if (lastSequenceNr % snapShotInterval == 0 && lastSequenceNr != 0)
          saveSnapshot(state)
      }
    case "print" ⇒ println(state)
  }

}

object ExamplePersistentApp extends App {

  val system = ActorSystem("ExamplePersistentApp")
  val per = system.actorOf(Props[ExamplePersistentActor], "persistence")
  per ! Cmd("shikha")
}




