name := "TestAkkaProject"

version := "0.1"

scalaVersion := "2.12.4"

libraryDependencies ++= Seq("com.typesafe.akka" %% "akka-actor" % "2.5.13",
                         "com.typesafe.akka" %% "akka-persistence" % "2.5.13",
  "com.typesafe.akka" %% "akka-http"   % "10.1.8",
  "com.typesafe.akka" %% "akka-stream" % "2.5.19",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.3",
  "com.typesafe.akka" %% "akka-actor-typed" % "2.5.13",
  "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8",
  "com.typesafe.akka" %% "akka-testkit" % "2.5.13" % Test,
  "org.scalatest" %% "scalatest" % "3.0.5" % Test,
  "com.typesafe.slick" %% "slick" % "3.2.3",
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.2.3",
  "com.typesafe.akka" %% "akka-http-testkit" % "10.0.0"
)

scalacOptions += "-Ypartial-unification"