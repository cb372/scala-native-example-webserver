package webserver

import webserver.UvUtil._
import webserver.uv.{Loop, TcpHandle}

import scala.scalanative.native._

object Server {

  def _onTcpConnection(tcpHandle: Ptr[TcpHandle], status: CInt): Unit = {
    println("got a connection!")
    val loop: Ptr[Loop] = (!(tcpHandle._2)).cast[Ptr[Loop]]
    val clientTcpHandle = stdlib.malloc(sizeof[TcpHandle]).cast[Ptr[TcpHandle]] // TODO how/when do we free this?
    bailOnError(uv.tcpInit(loop, clientTcpHandle))

    bailOnError(uv.accept(tcpHandle, clientTcpHandle))

    // TODO read request

    // TODO write response

    bailOnError(uv.close(clientTcpHandle, onClose))
  }

  def _onClose(clientTcpHandle: Ptr[TcpHandle]): Unit = {
    println("Freed client handle")
    stdlib.free(clientTcpHandle.cast[Ptr[Byte]])
  }

  val onTcpConnection: CFunctionPtr2[Ptr[TcpHandle], CInt, Unit] = CFunctionPtr.fromFunction2(_onTcpConnection)
  val onClose: CFunctionPtr1[Ptr[TcpHandle], Unit] = CFunctionPtr.fromFunction1(_onClose)

}
