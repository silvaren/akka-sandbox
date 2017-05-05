package io.github.silvaren


import java.io.File

import akka.actor.ActorSystem
import akka.http.scaladsl.model.Multipart.FormData.BodyPart
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.Materializer
import akka.stream.scaladsl.FileIO

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.concurrent.ExecutionContext.Implicits.global

case class CoolClass(coolProperty: String, anotherProperty: String)

trait StatusService extends BaseService {
  import Directives._
  import io.circe.generic.auto._

  implicit val system: ActorSystem

  implicit val materializer: Materializer

  protected case class Status(uptime: String)

  override protected def routes: Route =
    path("a") {
      get {
        complete {
          HttpEntity(ContentTypes.`text/html(UTF-8)`,
            """
              <html>
              <body>
                <h1>A!</h1>
              </body>
            </html>
            """)
        }
      }
    } ~
      path("b") {
        get {
          complete {
            HttpEntity(ContentTypes.`text/html(UTF-8)`,
              """
              <html>
                <body>
                  <h1>B!</h1>
                </body>
              </html>
            """)
          }
        }
      } ~
      path("headers") {
        get {
          headerValueByName("X-User-Id") { userId =>
            complete(s"The user is $userId")
          }
        }
      } ~
      path("query") {
        get {
          parameters("thisparam1", "thisparam2") { (thisparam1, thisparam2) =>
            complete(s"The first query param is '$thisparam1' and the second one is '$thisparam2'")
          }
        }
      } ~
      path("pathparams" / Segment / "andanother" / IntNumber) {(param1, param2) =>
        get {
          complete(s"The first path param is '$param1' and the second one is '$param2'")
        }
      } ~
      path("jsonbody") {
        post {
          entity(as[CoolClass]) { coolClass =>
            complete(s"The first path param is '${coolClass.coolProperty}' and the second one is '${coolClass.anotherProperty}'")
          }
        }
      } ~
      path("formdata") {
        (post & entity(as[Multipart.FormData])){ formData =>
//          formFields('aFormKey, 'anotherFormKey, 'aFormFileKey) { (formKey, anotherFormKey, aFormFileKey) =>
//            complete(s"The first path param is '${formKey}' and the second one is '${anotherFormKey}' and $aFormFileKey")
//          }
          complete {
            val extractedData: Future[Map[String, Any]] = formData.parts.mapAsync[(String, Any)](1) {

              case file: BodyPart if file.name == "file" => val tempFile = File.createTempFile("process", "file")
                file.entity.dataBytes.runWith(FileIO.toPath(tempFile.toPath)).map { ioResult =>
                  s"file ${file.filename.fold("Unknown")(identity)}" -> s"${ioResult.count} bytes"
                }

              case data: BodyPart => data.toStrict(2.seconds).map(strict => data.name -> strict.entity.data.utf8String)
            }.runFold(Map.empty[String, Any])((map, tuple) => map + tuple)
            extractedData.map { data => HttpResponse(StatusCodes.OK, entity = s"Data : ${data.mkString(", ")} has been successfully saved.")}
          }
        }
      } ~
      path("wwwformurlencoded") {
        post {
          formFields('coolKey, 'urlKey) { (coolKey, urlKey) =>
            complete(s"The first path param is '${coolKey}' and the second one is '${urlKey}'")
          }
        }
      } ~
      path("binary") {
        post {
          entity(as[String]) { bodyContent =>
            complete(s"The binary content is '$bodyContent'")
          }
        }
      }
}
