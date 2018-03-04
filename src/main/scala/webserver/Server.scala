package webserver

import webserver.uv.TcpHandle

import scala.scalanative.native._

object Server {

  val onTcpConnection = CFunctionPtr.fromFunction2[Ptr[TcpHandle], CInt, Unit] {
    case (tcpHandle, status) =>
      println("received a connection!")
      // TODO accept
  }

}
