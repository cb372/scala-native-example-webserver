package webserver

import utest._
import webserver.Http.{Header, StartLine}

object HttpTest extends TestSuite {

  val tests = Tests {
    'parseValidHttpRequest - {
      val raw = "GET /foo/bar?wow=yeah HTTP/1.1\r\nHost: localhost:7000\r\nUser-Agent: curl/7.54.0\r\nAccept: */*\r\n\r\n"
      val parsed = Http.parseRequest(raw).right.get

      assert(parsed.startLine == StartLine("GET", "/foo/bar?wow=yeah", "1.1"))
      assert(parsed.headers.toList == List(
        Header("Host", "localhost:7000"),
        Header("User-Agent", "curl/7.54.0"),
        Header("Accept", "*/*")
      ))
    }

    'parseInvalidHttpRequest - {
      assert(Http.parseRequest("yolo").isLeft)
    }

  }

}
