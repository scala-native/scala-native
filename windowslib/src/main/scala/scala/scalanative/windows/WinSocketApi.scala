package scala.scalanative.windows

import scala.scalanative.unsafe._
import scalanative.windows.{Word => WinWord}

@link("ws2_32")
@extern
object WinSocketApi {

  type Socket = Ptr[Byte]
  type Group = DWord
  type WSAProtocolInfoW = Ptr[Byte]
  type WSAPollFd = CStruct3[Socket, CShort, CShort]
  // This structures contains additional 5 fields with different order in Win_64 and others
  // Should only be treated as read-only and never allocated in ScalaNative.
  type WSAData = CStruct2[WinWord, WinWord]

  def WSAStartup(versionRequested: WinWord, data: Ptr[WSAData]): CInt = extern

  def WSACleanup(): CInt = extern

  def WSASocketW(
      addressFamily: CInt,
      socketType: CInt,
      protocol: CInt,
      protocolInfo: Ptr[WSAProtocolInfoW],
      group: Group,
      flags: DWord
  ): Socket = extern

  def WSAPoll(
      fds: Ptr[WSAPollFd],
      nfds: CUnsignedLongInt,
      timeout: CInt
  ): CInt =
    extern

  def WSAGetLastError(): CInt = extern

  @name("ioctlsocket")
  def ioctlSocket(socket: Socket, cmd: CInt, argp: Ptr[CInt]): CInt = extern

  @name("closesocket")
  def closeSocket(socket: Socket): CInt = extern

  @name("scalanative_winsocket_fionbio")
  final def FIONBIO: CInt = extern

  @name("scalanative_winsock_wsadata_size")
  final def WSADataSize: CSize = extern

  @name("scalanative_winsock_invalid_socket")
  final def InvalidSocket: Socket = extern
}

object WinSocketApiExt {
  final val WinSocketVersion: (Byte, Byte) = (2, 2)

  /* Event flag definitions for WSAPoll(). Their values might differ from Posix constants */
  final val POLLRDNORM = 0x0100
  final val POLLRDBAND = 0x0200
  final val POLLIN = POLLRDNORM | POLLRDBAND
  final val POLLPRI = 0x0400

  final val POLLWRNORM = 0x0010
  final val POLLOUT = POLLWRNORM
  final val POLLWRBAND = 0x0020

  final val POLLERR = 0x0001
  final val POLLHUP = 0x0002
  final val POLLNVAL = 0x0004

}

object WinSocketApiOps {
  import WinSocketApi._
  import WinSocketApiExt._
  import util.Conversion._

  final def init(): Unit = {
    val requiredVersion = (wordFromBytes _).tupled(WinSocketVersion)
    val winSocketData = stackalloc[Byte](WinSocketApi.WSADataSize)
      .asInstanceOf[Ptr[WSAData]]

    val initError = WinSocketApi.WSAStartup(requiredVersion, winSocketData)
    if (initError != 0) {
      throw new RuntimeException(
        s"Failed to initialize socket support, error code $initError "
      )
    }
    val receivedVersion = wordToBytes(winSocketData.version)
    if (WinSocketVersion != receivedVersion) {
      WinSocketApi.WSACleanup()
      throw new RuntimeException(
        s"Could not find a usable version of WinSock.dll, expected $WinSocketVersion, got $receivedVersion"
      )
    }
  }

  implicit class WSADataOps(val ref: Ptr[WSAData]) extends AnyVal {
    def version: WinWord = ref._1
    def highVersion: WinWord = ref._2
  }

  implicit class WSAPollFdOps(val ref: Ptr[WSAPollFd]) extends AnyVal {
    def socket: Socket = ref._1
    def events: CShort = ref._2
    def revents: CShort = ref._3

    def socket_=(v: Socket): Unit = ref._1 = v
    def events_=(v: CShort): Unit = ref._2 = v
    def revents_=(v: CShort): Unit = ref._3 = v
  }
}
