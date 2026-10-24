import scala.scalanative.build.LTO
import scala.scalanative.build.Mode
import scala.scalanative.build.GC

lazy val commonSettings = Seq(
  scalaVersion := "3.8.4"
)

// workaround for output test result
ThisBuild / Test / testOptions += Tests.Argument(
  new TestFramework("munit.Framework"),
  "--logger=sbt"
)

lazy val root = project
  .in(file("."))
  .aggregate(app, core)

val circeVersion = "0.14.16"

lazy val app = project
  .in(file("app"))
  .enablePlugins(ScalaNativePlugin)
  .settings(
    commonSettings,
    libraryDependencies += "com.lihaoyi" %% "mainargs" % "0.7.7",
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser"
    ).map(_ % circeVersion),
    libraryDependencies += "xyz.matthieucourt" %% "layoutz" % "0.8.0"
  )
  .dependsOn(core)

lazy val core = project
  .in(file("core"))
  .enablePlugins(ScalaNativePlugin)
  .settings(
    commonSettings,
    libraryDependencies += "org.typelevel" %% "cats-parse" % "1.1.0",
    libraryDependencies += "org.scalameta" %% "munit" % "1.3.6" % Test
  )
