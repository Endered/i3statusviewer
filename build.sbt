import scala.scalanative.build.LTO
import scala.scalanative.build.Mode
import scala.scalanative.build.GC
import scala.util.Try
import java.nio.file.Paths

lazy val commonSettings = Seq(
  scalaVersion := "3.8.4",

  // workaround for output test result
  Test / testOptions += Tests.Argument(
    new TestFramework("munit.Framework"),
    "--logger=sbt"
  )
)

lazy val updateLicensesFile = taskKey[Boolean]("update license file")
lazy val ensureLicensesFileUpdated =
  taskKey[Unit]("check license file is up to dated")

lazy val rootPath = Paths.get(".")

lazy val root = project
  .in(file("."))
  .settings(
    licenseReportTitle := "Third Party Licenses",
    updateLicensesFile := Def.uncached {
      val licensesDir = dumpLicenseReportAnyProject.value
      val licenseFile = licensesDir / "Third Party Licenses.md"
      val outputPath = rootPath / "THIRD_PARTY_LICENSE.md"

      val current = Try(IO.read(outputPath.toFile())).toOption
      val next = Try(IO.read(licenseFile)).toOption

      IO.copyFile(licenseFile, outputPath.toFile())

      current == next
    },
    ensureLicensesFileUpdated := {
      assert(updateLicensesFile.value)
    }
  )
  .aggregate(app, core)

lazy val circeVersion = "0.14.16"
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
