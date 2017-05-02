package io.github.silvaren

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import org.specs2.mutable.Specification

class NewHandlerSpec extends Specification {

  "NewHandler" should {

    "return a! in route A" in {
      val inputStream = new ByteArrayInputStream(InputEventStrings.inputStringForRouteA.getBytes())
      val outputStream = new ByteArrayOutputStream()
      NewHandler.handleRequest(inputStream,outputStream,null)
      outputStream.toString must contain("A!")
    }

    "return b! in route B" in {
      val inputStream = new ByteArrayInputStream(InputEventStrings.inputStringForRouteB.getBytes())
      val outputStream = new ByteArrayOutputStream()
      NewHandler.handleRequest(inputStream,outputStream,null)
      outputStream.toString must contain("B!")
    }

    "return userId from header" in {
      val inputStream = new ByteArrayInputStream(InputEventStrings.inputStringForRouteHeaders.getBytes())
      val outputStream = new ByteArrayOutputStream()
      NewHandler.handleRequest(inputStream,outputStream,null)
      outputStream.toString must contain("myuserid")
    }

    "return value from query param" in {
      val inputStream = new ByteArrayInputStream(InputEventStrings.inputStringForQueryParams.getBytes())
      val outputStream = new ByteArrayOutputStream()
      NewHandler.handleRequest(inputStream,outputStream,null)
      outputStream.toString must contain("thisvalue1")
      outputStream.toString must contain("thisvalue2")
    }

    "return values from path param" in {
      val inputStream = new ByteArrayInputStream(InputEventStrings.inputStringForPathParams.getBytes())
      val outputStream = new ByteArrayOutputStream()
      NewHandler.handleRequest(inputStream,outputStream,null)
      outputStream.toString must contain("pathvalue1")
      outputStream.toString must contain("42")
    }

    "return values from json body" in {
      val inputStream = new ByteArrayInputStream(InputEventStrings.inputStringForJsonBody.getBytes())
      val outputStream = new ByteArrayOutputStream()
      NewHandler.handleRequest(inputStream,outputStream,null)
      outputStream.toString must contain("coolValue")
      outputStream.toString must contain("anotherValue")
    }

    "return values from form-data body" in {
      val inputStream = new ByteArrayInputStream(InputEventStrings.inputStringForFormDataBody.getBytes())
      val outputStream = new ByteArrayOutputStream()
      NewHandler.handleRequest(inputStream,outputStream,null)
      outputStream.toString must contain("aFormValue")
      outputStream.toString must contain("anotherFormValue")
      outputStream.toString must contain("this is a sample file")
    }

    "return values from x-www-form-urlencoded body" in {
      val inputStream = new ByteArrayInputStream(InputEventStrings.inputStringForXWwwFormUrlencodedBody.getBytes())
      val outputStream = new ByteArrayOutputStream()
      NewHandler.handleRequest(inputStream,outputStream,null)
      outputStream.toString must contain("coolValue")
      outputStream.toString must contain("https://github.com/silvaren")
    }

    "return values from binary body" in {
      val inputStream = new ByteArrayInputStream(InputEventStrings.inputStringForBinaryBody.getBytes())
      val outputStream = new ByteArrayOutputStream()
      NewHandler.handleRequest(inputStream,outputStream,null)
      outputStream.toString must contain("this is a sample file")
    }
  }
}