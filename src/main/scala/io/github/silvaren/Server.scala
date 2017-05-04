package io.github.silvaren

import akka.pattern.ask
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.{Route, RouteResult}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Sink, Source}
import com.amazonaws.services.lambda.runtime.Context

import scala.concurrent.Await
import scala.concurrent.duration._
import io.circe.generic.auto._
import io.circe.parser._
import io.github.silvaren.Main.routes
import scala.concurrent.ExecutionContext.Implicits.global

case class LambdaProxyEvent(resource: String,
                            path: String,
                            httpMethod: String,
                            headers: Option[Map[String,String]],
                            queryStringParameters: Option[Map[String,String]],
                            pathParameters: Map[String,String],
                            stageVariables: Option[Map[String,String]],
                            requestContext: RequestContext,
                            body: Option[String],
                            isBase64Encoded: Boolean)

case class RequestContext(accountId: String,
                          resourceId: String,
                          stage: String,
                          requestId: String,
                          identity: Identity,
                          resourcePath: String,
                          httpMethod: String,
                          apiId: String)

case class Identity(cognitoIdentityPoolId: Option[String],
                    accountId: Option[String],
                    cognitoIdentityId: Option[String],
                    caller: Option[String],
                    apiKey: Option[String],
                    sourceIp: String,
                    cognitoAuthenticationType: Option[String],
                    cognitoAuthenticationProvider: Option[String],
                    userArn: Option[String],
                    userAgent: String,
                    user: Option[String])

class Server (routes: Route) {

  def getMultiPartFormDataMediaType(contentType: String): MediaType.Multipart =
    MediaTypes.`multipart/form-data`.withBoundary(contentType.drop(contentType.indexOf("boundary=") + 9))

  def getEntity(contentType: String, event: LambdaProxyEvent): RequestEntity = {
    println(contentType)
    contentType match {
      case str if str contains "application/json" => event.body.map(body => HttpEntity(ContentTypes.`application/json`, body)).
        getOrElse(HttpEntity.Empty)
      case str if str contains "form-data" => event.body.map(body =>
        HttpEntity(ContentType(getMultiPartFormDataMediaType(str)), body.getBytes)).getOrElse(HttpEntity.Empty)
      case str if str contains "application/x-www-form-urlencoded" => event.body.map(body =>
        HttpEntity(ContentType(MediaTypes.`application/x-www-form-urlencoded`, HttpCharsets.`UTF-8`), body)).getOrElse(HttpEntity.Empty)
      case _ => event.body.map(body => HttpEntity(ContentTypes.NoContentType, body.getBytes)).getOrElse(HttpEntity.Empty)
    }

  }

  implicit val system       = ActorSystem()
  implicit val materializer = ActorMaterializer()

  def proxy(input: String, context: Context): String = {
    val event = decode[LambdaProxyEvent](input).right.get
    println(event)
    val handlerFlow = RouteResult.route2HandlerFlow(routes)
    println(getEntity(event.headers.getOrElse(Map()).toSeq.find(
      keyValue => keyValue._1 == "content-type").map(_._2).getOrElse("binary"), event))
    val request =
      HttpRequest(
        HttpMethods.getForKey(event.httpMethod).get,
        Uri(event.path).withQuery(Query(event.queryStringParameters.getOrElse(Map()))),
        event.headers.getOrElse(Map()).toList.map(x => RawHeader(x._1, x._2)),
        getEntity(event.headers.getOrElse(Map()).toSeq.find(
          keyValue => keyValue._1 == "content-type").map(_._2).getOrElse("binary"), event)
      )
    val response = Source.single(request).via(handlerFlow).toMat(Sink.head)(Keep.right).run()
    val responseString = response.map(_.entity.toString)
    Await.result(responseString, 30 seconds)
  }

}
