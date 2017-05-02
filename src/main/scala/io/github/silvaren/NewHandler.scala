package io.github.silvaren

import java.io.{InputStream, OutputStream}

import com.amazonaws.services.lambda.runtime.{Context, RequestStreamHandler}
import io.circe.syntax._
import io.circe.generic.auto._

case class IntegrationResponse(statusCode: Int, headers: Option[Map[String,String]], body: String)

object NewHandler extends RequestStreamHandler with Config with Services {

  val server = new Server(routes)

  def jsonStringEscape(jsonString: String): String = {
    val chars = jsonString.map(c =>
      c match {
        case '"' => "\\\""
        case '\\' => "\\\\"
        case '\b' => "\\b"
        case '\f' => "\\f"
        case '\n' => "\\n"
        case '\r' => "\\r"
        case '\t' => "\\t"
        case _ => c + ""
      }
    )
    chars.mkString
  }

  override def handleRequest(is: InputStream, os: OutputStream, context: Context): Unit = {
    val input = scala.io.Source.fromInputStream(is).mkString
    println("input: ", input)
    val response: String = server.proxy(input, context)
    println("output: ", response)
    println("escaped output: ", jsonStringEscape(response))
    val integrationResponse = IntegrationResponse(200, None, jsonStringEscape(response)).asJson.toString
    println("integrationResponse:", integrationResponse)
    os.write(integrationResponse.getBytes("UTF-8"))
    os.flush()
    println("finished!!")
  }
}

