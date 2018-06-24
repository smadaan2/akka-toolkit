package com.akka.guidelines

import akka.actor.{Actor, ActorRef}
import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.duration._
import akka.pattern.pipe
import scala.concurrent.ExecutionContext.Implicits.global

// Messages
case object Tick
case class Add(key: String)
case class Contains(key: String)
case class Validated(key: String, isValid: Boolean)
case object Continue
case object Rejected



/***
  * Guideline1
  * In the example above the actor schedules a Tick every 3 seconds that evolves its state.
  * This is an extremely costly mistake. The actor's behavior becomes totally non-deterministic and impossible to test right.
  * If you really need to periodically do something inside an actor, then that scheduler must not be initialized inside the actor. Take it out.
  */
class SomeActor extends Actor {
  private var counter = 0
  private val scheduler = context.system.scheduler
    .schedule(3.seconds, 3.seconds, self, Tick)

  def receive = {
    case Tick =>
      counter += 1
  }
}



/***
  * Guideline2
  * SHOULD mutate state in actors only with context.become
  */

class MyActor1 extends Actor {
  val isInSet = mutable.Set.empty[String]

  def receive = {
    case Add(key) =>
      isInSet += key

    case Contains(key) =>
      sender() ! isInSet(key)
  }
}

import collection.immutable.Set

class MyActor2 extends Actor {
  def receive = active(Set.empty)

  def active(isInSet: Set[String]): Receive = {
    case Add(key) =>
      context become active(isInSet + key)

    case Contains(key) =>
      sender() ! isInSet(key)
  }
}

/***
  * Guideline3
  * MUST NOT leak the internal state of an actor in asynchronous closures
  * sending another message to our actor when our future is done
  */

class MyActor3 extends Actor {
  val isInSet = mutable.Set.empty[String]

  def receive = {
    case Add(key) =>
      for (shouldAdd <- validate(key)) {
        if (shouldAdd) isInSet += key
      }

    // ...
  }

  def validate(key: String): Future[Boolean] = ???
}


class MyActor4 extends Actor {
  val isInSet = mutable.Set.empty[String]

  def receive = {
    case Add(key) =>
      val f = for (isValid <- validate(key))
        yield Validated(key, isValid)

      // sending the result as a message back to our actor
      pipe(f) to self

    case Validated(key, isValid) =>
      if (isValid) isInSet += key
    // ...
  }
  def validate(key: String): Future[Boolean] = ???
}

//And of course, we could be modeling a state-machine that doesn't accept any more requests until the last one is done.
// Let us also get rid of that mutable collection and also introduce back-pressure (i.e. we need to tell the sender
// when it can send the next item)

class MyActor5 extends Actor {
  def receive = idle(Set.empty)

  def idle(isInSet: Set[String]): Receive = {
    case Add(key) =>
      // sending the result as a message back to our actor
      pipe(validate(key).map(Validated(key, _))) to self

      // waiting for validation
      context.become(waitForValidation(isInSet, sender()))
  }

  def waitForValidation(set: Set[String], source: ActorRef): Receive = {
    case Validated(key, isValid) =>
      val newSet = if (isValid) set + key else set
      // sending acknowledgement of completion
      source ! Continue
      // go back to idle, accepting new requests
      context.become(idle(newSet))

    case Add(key) =>
      sender() ! Rejected
  }

  def validate(key: String): Future[Boolean] = ???
}




