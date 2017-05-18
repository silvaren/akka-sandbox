package io.github.silvaren

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.server.{Directives, Route, RouteResult}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import de.heikoseeberger.akkahttpcirce.CirceSupport

import scala.concurrent.ExecutionContext

trait BaseComponent extends Config {
  protected implicit def log: LoggingAdapter
  protected implicit def executor: ExecutionContext
}

trait BaseService {
  protected def routes: Route
}

object Main extends App with Config with Services {
  implicit val system       = ActorSystem()
  implicit val materializer = ActorMaterializer()

  //  override protected def log      = Logging(system, "service")
  //  override protected def executor = system.dispatcher

  val flow: Flow[HttpRequest, HttpResponse, Any] = routes

  Http().bindAndHandle(routes, httpConfig.interface, httpConfig.port)
}

trait Services extends StatusService {

}