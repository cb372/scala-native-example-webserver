package webserver

import scala.scalanative.native._
import scala.scalanative.posix.netinet.in.sockaddr_in

@link("uv")
@extern
object uv {

  type Loop = Unit // we don't care what the uv_loop_t type is, as we just need to pass around a pointer to it

  type Buffer = CStruct2[
    CString, // char* base;
    CSize    // size_t len;
  ]

  type TcpHandle = CStruct11[
    Ptr[Unit],                                              // void* data;
    Ptr[Loop],                                              // uv_loop_t* loop;
    CInt,                                                   // uv_handle_type type;
    CFunctionPtr1[Ptr[Byte], Unit],                         // uv_close_cb close_cb; (Ptr[Byte] is actually Ptr[TcpHandle] but we can't have recursive types)
    CArray[Ptr[Unit], Nat._2],                              // void* handle_queue[2];
    CArray[Ptr[Unit], Nat._4],                              // union { int fd; void* reserved[4]; } u;
    Ptr[Byte],                                              // uv_handle_t* next_closing; (Ptr[Byte] is actually Ptr[TcpHandle])
    UInt,                                                   // unsigned int flags;
    CSize,                                                  // size_t write_queue_size;
    CFunctionPtr3[Ptr[Byte], CSize, Ptr[Unit], Unit],       // uv_alloc_cb alloc_cb; (Ptr[Byte] is actually Ptr[TcpHandle])
    CFunctionPtr3[Ptr[Byte], CSSize, Ptr[Unit], Unit]       // uv_read_cb read_cb; (Ptr[Byte] is actually Ptr[TcpHandle])
  ]

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

  @name("uv_accept")
  def accept(handle: Ptr[TcpHandle], clientHandle: Ptr[TcpHandle]): CInt = extern

  @name("uv_close")
  def close(clientHandle: Ptr[TcpHandle], callback: CFunctionPtr1[Ptr[TcpHandle], Unit]): CInt = extern

  @name("uv_err_name")
  def getErrorName(errorCode: CInt): CString = extern

}
