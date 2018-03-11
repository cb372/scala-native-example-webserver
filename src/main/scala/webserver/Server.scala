package webserver

import java.nio.charset.{Charset, StandardCharsets}

import webserver.uv._

import scala.scalanative.native._

object Server {

  // defining this as an extern in `uv` object didn't work
  private val UV_EOF: CSSize = -4095

  def _onTcpConnection(tcpHandle: Ptr[TcpHandle], status: CInt): Unit = {
    println("Got a connection!")
    val loop: Ptr[Loop] = (!tcpHandle._2).cast[Ptr[Loop]]

    println("Allocating a client handle for the request")
    val clientTcpHandle = stdlib.malloc(sizeof[TcpHandle]).cast[Ptr[TcpHandle]]

    println("Initialising client handle")
    bailOnError(uv.tcpInit(loop, clientTcpHandle))

    println("Accepting connection")
    bailOnError(uv.accept(tcpHandle, clientTcpHandle))

    println("Reading request")
    bailOnError(uv.readStart(clientTcpHandle, allocateRequestBuffer, onRead))
  }

  private def _onClose(clientHandle: Ptr[TcpHandle]): Unit = {
    println("Freeing the client handle for the request")
    stdlib.free(clientHandle.cast[Ptr[Byte]])
  }

  private def _allocateRequestBuffer(clientHandle: Ptr[TcpHandle], suggestedSize: CSize, buffer: Ptr[Buffer]): Unit = {
    println(s"Allocating request read buffer of size $suggestedSize")
    !buffer._1 = stdlib.malloc(suggestedSize)
    !buffer._2 = suggestedSize
  }

  private def _onRead(clientHandle: Ptr[TcpHandle], bytesRead: CSSize, buffer: Ptr[Buffer]): Unit = {
    bytesRead match {
      case UV_EOF =>
        println("Finished reading the request")

        println(s"Freeing request read buffer of size ${!buffer._2}")
        stdlib.free(!buffer._1)
      case n if n < 0 =>
        println(s"Error reading request: ${uv.getErrorName(n.toInt)}")
        uv.close(clientHandle, onClose)

        println(s"Freeing request read buffer of size ${!buffer._2}")
        stdlib.free(!buffer._1)
      case n =>
        println(s"Read $n bytes of the request")

        val requestAsString: String = readRequestAsString(n, buffer)

        println(s"Freeing request read buffer of size ${!buffer._2}")
        stdlib.free(!buffer._1)

        /*
        The onRead callback can be called multiple times for a single request,
        but for simplicity we assume the request is < 64k and is thus read in a single chunk.
        As soon as we read the first chunk, we parse it as an HTTP request and write the response.
         */
        parseAndRespond(clientHandle, requestAsString)
    }
  }

  private def readRequestAsString(bytesRead: CSSize, buffer: Ptr[Buffer]): String = {
    val buf: Ptr[CChar] = !buffer._1

    val bytes = new Array[Byte](bytesRead.toInt)
    var c = 0
    while (c < bytesRead) {
      bytes(c) = !(buf + c)
      c += 1
    }
    new String(bytes, Charset.defaultCharset())
  }

  private val ErrorResponse =
    s"""HTTP/1.1 400 Bad Request\r
       |Connection: close\r
       |Content-Type: text/plain; charset=utf8\r
       |Content-Length: 35\r
       |\r
       |What on earth did you just send me?!""".stripMargin

  private def parseAndRespond(clientHandle: Ptr[TcpHandle], rawRequest: String): Unit = {
    val responseText = Http.parseRequest(rawRequest) match {
      case Right(parsedRequest) =>
        val entity =
          s"""<!DOCTYPE html>
             |<html>
             |<body>
             |  <h2>Thanks for your request!</h2>
             |  <p>Here's what you sent me.</p>
             |  <ul>
             |    <li>Method = ${parsedRequest.startLine.httpMethod}</li>
             |    <li>Target = ${parsedRequest.startLine.requestTarget}</li>
             |    <li>HTTP version = ${parsedRequest.startLine.httpVersion}</li>
             |    <li>Headers:
             |      <ul>
             |        ${parsedRequest.headers.map(h => s"<li>${h.key}: ${h.value}</li>").mkString}
             |      </ul>
             |    </li>
             |  </ul>
             |</body>
             |</html>
             |""".stripMargin

        s"""HTTP/1.1 200 OK\r
           |Connection: close\r
           |Content-Type: text/html; charset=utf8\r
           |Content-Length: ${entity.length}\r
           |\r
           |$entity""".stripMargin

      case Left(_) =>
        ErrorResponse
    }

    writeResponse(clientHandle, responseText.getBytes(StandardCharsets.UTF_8))
  }

  private def writeResponse(clientHandle: Ptr[TcpHandle], responseBytes: Array[Byte]): Unit = {
    println(s"Allocating a buffer for the response (${responseBytes.length} bytes)")
    val responseBuffer = stdlib.malloc(responseBytes.length)

    var c = 0
    while (c < responseBytes.length) {
      responseBuffer(c) = responseBytes(c)
      c += 1
    }

    println("Allocating a wrapper for the response buffer")
    val buffer = stdlib.malloc(sizeof[Buffer]).cast[Ptr[Buffer]]

    !buffer._1 = responseBuffer
    !buffer._2 = responseBytes.length

    println("Allocating a Write for the response")
    val req = stdlib.malloc(sizeof[Write]).cast[Ptr[Write]]

    // Store a pointer to the response buffer in the 'data' field to make it easy to free it later
    !req._1 = buffer.cast[Ptr[Byte]]

    bailOnError(uv.write(req, clientHandle, buffer, 1.toUInt, onWritten))
  }

  private def _onWritten(write: Ptr[Write], status: CInt): Unit = {
    println(s"Write succeeded: ${status >= 0}")

    val buffer = (!write._1).cast[Ptr[Buffer]]

    println(s"Freeing the response buffer (${(!buffer._2).cast[CSize]} bytes)")
    stdlib.free(!buffer._1)

    println("Freeing the wrapper for the response buffer")
    stdlib.free(buffer.cast[Ptr[Byte]])

    val clientHandle = (!write._6).cast[Ptr[TcpHandle]]
    uv.close(clientHandle, onClose)

    println("Freeing the Write for the response")
    stdlib.free(write.cast[Ptr[Byte]])
  }

  val onTcpConnection: CFunctionPtr2[Ptr[TcpHandle], CInt, Unit] = CFunctionPtr.fromFunction2(_onTcpConnection)

  private val onClose = CFunctionPtr.fromFunction1(_onClose)
  private val allocateRequestBuffer = CFunctionPtr.fromFunction3(_allocateRequestBuffer)
  private val onRead = CFunctionPtr.fromFunction3(_onRead)
  private val onWritten = CFunctionPtr.fromFunction2(_onWritten)

}
