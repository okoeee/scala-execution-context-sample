lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    name := """execution-context-sample""",
    organization := "com.example",
    version := "1.0-SNAPSHOT",
    scalaVersion := "2.13.12",
    libraryDependencies ++= Seq(
      guice,
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test,
      "com.typesafe.akka" %% "akka-actor-typed" % "2.6.21",
      "org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.4"
    ),
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-Xfatal-warnings"
    )
  )
