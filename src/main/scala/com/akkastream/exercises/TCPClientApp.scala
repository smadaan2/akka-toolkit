package com.akkastream.exercises
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Tcp.{IncomingConnection, ServerBinding}
import akka.stream.scaladsl.{Flow, Framing, Source, Tcp}
import akka.util.ByteString

import scala.concurrent.Future

object TCPClientApp extends App{

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val connection = Tcp().outgoingConnection("127.0.0.1", 9001)

  val replParser =
    Flow[String].takeWhile(_ != "q")
      .concat(Source.single("BYE"))
      .map(elem ⇒ ByteString(s"$elem\n"))

  val repl = Flow[ByteString]
    .via(Framing.delimiter(
      ByteString("\n"),
      maximumFrameLength = 256,
      allowTruncation = true))
    .map(_.utf8String)
    .map(text ⇒ println("Server: " + text))
    .map(_ ⇒ readLine("> "))
    .via(replParser)

  val connected = connection.join(repl).run()

}
