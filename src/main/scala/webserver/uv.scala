package webserver

import scala.scalanative.native._
import scala.scalanative.posix.netinet.in.sockaddr_in

@link("uv")
@extern
object uv {
  type Loop = Unit // we don't care what the uv_loop_t type is, as we just need to pass around a pointer to it
  type TcpHandle = Unit // same goes for uv_tcp_t

  @name("uv_ip4_addr")
  def ipv4Addr(ip: CString, port: CInt, addr: Ptr[sockaddr_in]): CInt = extern

  @name("uv_default_loop")
  def createDefaultLoop(): Ptr[Loop] = extern

  @name("uv_tcp_init")
  def tcpInit(loop: Ptr[Loop], handle: Ptr[TcpHandle]): CInt = extern

  @name("uv_tcp_bind")
  def tcpBind(handle: Ptr[TcpHandle], socketAddress: Ptr[sockaddr_in], flags: CUnsignedInt): CInt = extern

  @name("uv_listen")
  def listen(handle: Ptr[TcpHandle], connectionBacklog: CInt, onTcpConnection: CFunctionPtr2[Ptr[TcpHandle], CInt, Unit]): CInt = extern

  @name("uv_run")
  def run(loop: Ptr[Loop], runMode: CInt): CInt = extern

  @name("uv_err_name")
  def getErrorName(errorCode: CInt): CString = extern

}
