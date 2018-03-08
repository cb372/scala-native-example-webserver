package webserver

import java.nio.charset.{Charset, StandardCharsets}

import webserver.UvUtil._
import webserver.uv.{Buffer, Loop, TcpHandle, Write}

import scala.scalanative.native._

object Server {

  // defining this as an extern in `uv` object didn't work
  private val UV_EOF: CSSize = -4095

  def _onTcpConnection(tcpHandle: Ptr[TcpHandle], status: CInt): Unit = {
    println("got a connection!")
    val loop: Ptr[Loop] = (!(tcpHandle._2)).cast[Ptr[Loop]]
    val clientTcpHandle = stdlib.malloc(sizeof[TcpHandle]).cast[Ptr[TcpHandle]]

    println("Initialising client handle")
    bailOnError(uv.tcpInit(loop, clientTcpHandle))

    println("Accepting connection")
    bailOnError(uv.accept(tcpHandle, clientTcpHandle))

    println("Reading request")
    bailOnError(uv.readStart(clientTcpHandle, allocateRequestBuffer, onRead))
  }

  private def _onClose(clientHandle: Ptr[TcpHandle]): Unit = {
    println("Freed client handle")
    //stdlib.free(clientHandle.cast[Ptr[Byte]])
  }

  private def _allocateRequestBuffer(clientHandle: Ptr[TcpHandle], suggestedSize: CSize, buffer: Ptr[Buffer]): Unit = {
    println(s"Allocating request buffer of size $suggestedSize")
    !buffer._1 = stdlib.malloc(suggestedSize)
    !buffer._2 = suggestedSize
  }

  private def _onRead(clientHandle: Ptr[TcpHandle], bytesRead: CSSize, buffer: Ptr[Buffer]): Unit = {
    bytesRead match {
      case UV_EOF =>
        println("Read the entire request")
        //stdlib.free(!(buffer._1))
      case n if n < 0 =>
        println(s"Error reading request: ${uv.getErrorName(n.toInt)}")
        uv.close(clientHandle, CFunctionPtr.fromFunction1(_onClose))
        //stdlib.free(!(buffer._1))
      case n =>
        println(s"Read $n bytes of the request")
        val request: String = readRequestAsString(bytesRead, buffer)
        println(request)

        //stdlib.free(!(buffer._1))

        parseAndRespond(clientHandle, request)
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

  private def parseAndRespond(clientHandle: Ptr[TcpHandle], rawRequest: String): Unit = {
    println("TODO parse request")
    // TODO parse request

    val buffer = stdlib.malloc(sizeof[uv.Buffer]).cast[Ptr[Buffer]]
    val text =
      s"""HTTP/1.1 200 OK\r
         |Connection: close\r
         |Content-Type: text/html\r
         |Content-Length: 36\r
         |\r
         |<html>
         |<body>
         |Hello
         |</body>
         |</html>""".stripMargin
    val bytes = text.getBytes(StandardCharsets.UTF_8)
    val string = stdlib.malloc(bytes.length + 1)

    var c = 0
    while (c < bytes.length) {
      string(c) = bytes(c)
      c += 1
    }

    !buffer._1 = string
    !buffer._2 = bytes.length + 1

    val req = stdlib.malloc(sizeof[uv.Write]).cast[Ptr[Write]]

    bailOnError(uv.write(req, clientHandle, buffer, 1.toUInt, onWritten))
  }

  private def _onWritten(write: Ptr[Write], status: CInt): Unit = {
    println(s"Write succeeded: ${status >= 0}")
    // TODO clean up a bunch of stuff
  }

  val onTcpConnection: CFunctionPtr2[Ptr[TcpHandle], CInt, Unit] = CFunctionPtr.fromFunction2(_onTcpConnection)

  private val onClose = CFunctionPtr.fromFunction1(_onClose)
  private val allocateRequestBuffer = CFunctionPtr.fromFunction3(_allocateRequestBuffer)
  private val onRead = CFunctionPtr.fromFunction3(_onRead)
  private val onWritten = CFunctionPtr.fromFunction2(_onWritten)

}
