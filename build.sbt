name := "akka-http-from-new"
organization := "io.github.silvaren"
version := "0.0.2"
scalaVersion := "2.12.2"
scalacOptions := Seq("-unchecked", "-feature", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val catsV          = "0.9.0"
  val akkaHttpV      = "10.0.5"
  val circeV         = "0.7.0"
  val ficusV         = "1.4.0"
  val scalaMockV     = "3.5.0"
  val catsScalatestV = "2.2.0"

  Seq(
    "org.typelevel"     %% "cats-core"       % catsV,
    "com.iheart"        %% "ficus"           % ficusV,
    "com.typesafe.akka" %% "akka-http"       % akkaHttpV,
    "de.heikoseeberger" %% "akka-http-circe" % "1.15.0",
    "io.circe"          %% "circe-core"      % circeV,
    "io.circe"          %% "circe-generic"   % circeV,
    "io.circe"          %% "circe-parser"    % circeV,
    "org.scalamock"     %% "scalamock-scalatest-support" % scalaMockV     % "it,test",
    "com.ironcorelabs"  %% "cats-scalatest"              % catsScalatestV % "it,test",
    "com.typesafe.akka" %% "akka-http-testkit"           % akkaHttpV      % "it,test",
    "org.specs2"        %% "specs2-core"            % "3.8.9"        % "it,test"

  )
}

libraryDependencies += "com.amazonaws" % "aws-lambda-java-core" % "1.1.0"

lazy val root = project.in(file(".")).configs(IntegrationTest)
Defaults.itSettings
Revolver.settings
Revolver.enableDebugging(port = 5050, suspend = false)
enablePlugins(JavaAppPackaging)


initialCommands := """
import cats._
import cats.data._
import cats.implicits._
import akka.actor._
import akka.pattern._
import akka.util._
import scala.concurrent._
import scala.concurrent.duration._
""".stripMargin
