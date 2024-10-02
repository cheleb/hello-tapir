val tapirVersion = "1.10.5"

lazy val rootProject = (project in file(".")).settings(
  Seq(
    name := "hello-tapir",
    version := "0.1.0-SNAPSHOT",
    organization := "dev.cheleb",
    scalaVersion := "3.5.1",
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.tapir" %% "tapir-zio-http-server" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-prometheus-metrics" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirVersion,
      "dev.zio" %% "zio-logging" % "2.1.15",
      "dev.zio" %% "zio-logging-slf4j" % "2.1.15",
      "ch.qos.logback" % "logback-classic" % "1.4.14",
      "com.softwaremill.sttp.tapir" %% "tapir-sttp-stub-server" % tapirVersion % Test,
      "org.scalatest" %% "scalatest" % "3.2.16" % Test,
      "com.softwaremill.sttp.client3" %% "circe" % "3.8.15" % Test
    )
  )
)
