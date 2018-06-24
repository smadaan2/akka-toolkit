package com.akkastream.exercises
import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.util.ByteString

import scala.concurrent._
import scala.concurrent.duration._
import java.nio.file.Paths

import akka.stream.{ActorMaterializer, IOResult}
import akka.stream.scaladsl.{FileIO, Flow, Keep, Sink, Source}

import scala.util.{Failure, Success}

object AkkStreamApp extends App {

  implicit val system = ActorSystem("AkkStreamApp")
  implicit val materializer = ActorMaterializer()
  val source: Source[Int, NotUsed] = Source(1 to 100)

  val factorials = source.scan(BigInt(1))((acc, next) ⇒ acc * next)

//  val result: Future[IOResult] =
//    factorials
//      .map(num ⇒ ByteString(s"$num\n"))
//      .runWith(FileIO.toPath(Paths.get("factorials.txt")))

  def lineSink(filename: String): Sink[String, Future[IOResult]] =
    Flow[String]
      .map(s ⇒ ByteString(s + "\n"))
      .toMat(FileIO.toPath(Paths.get(filename)))(Keep.right)

  val result = factorials.map(_.toString).runWith(lineSink("factorial2.txt"))
  implicit val ec = system.dispatcher

  result.onComplete {
    case Success(a) =>  println(s"succeeded:::$a")
    case Failure(ex) => println("failure")
  }

  factorials
    .zipWith(Source(0 to 100))((num, idx) ⇒ s"$idx! = $num")
    .throttle(1, 1.second)
    .runForeach(println)



}
