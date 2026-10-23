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

lazy val app = project
  .in(file("app"))
  .enablePlugins(ScalaNativePlugin)
  .settings(
    commonSettings
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
