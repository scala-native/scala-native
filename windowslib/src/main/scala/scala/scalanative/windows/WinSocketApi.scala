package scala.scalanative.windows

import scala.scalanative.unsafe._
import scala.scalanative.unsigned._
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

  @blocking
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
  @blocking
  def closeSocket(socket: Socket): CInt = extern

  @name("scalanative_winsock_fionbio")
  final def FIONBIO: CInt = extern

  @name("scalanative_winsock_wsadata_size")
  final def WSADataSize: CSize = extern

  @name("scalanative_winsock_invalid_socket")
  final def InvalidSocket: Socket = extern
}

object WinSocketApiExt {
  final val WinSocketVersion: (Byte, Byte) = (2, 2)

  // WSASocket flags
  final val WSA_FLAG_OVERLAPPED = 0x01.toUInt
  final val WSA_FLAG_MULTIPOINT_C_ROOT = 0x02.toUInt
  final val WSA_FLAG_MULTIPOINT_C_LEAF = 0x04.toUInt
  final val WSA_FLAG_MULTIPOINT_D_ROOT = 0x08.toUInt
  final val WSA_FLAG_MULTIPOINT_D_LEAF = 0x10.toUInt
  final val WSA_FLAG_ACCESS_SYSTEM_SECURITY = 0x40.toUInt
  final val WSA_FLAG_NO_HANDLE_INHERIT = 0x80.toUInt

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

  // Error codes
  final val WSA_INVALID_HANDLE = 6
  final val WSA_NOT_ENOUGH_MEMORY = 8
  final val WSA_INVALID_PARAMETER = 87
  final val WSA_OPERATION_ABORTED = 995
  final val WSA_IO_INCOMPLETE = 996
  final val WSA_IO_PENDING = 997
  final val WSAEINTR = 10004
  final val WSAEBADF = 10009
  final val WSAEACCES = 10013
  final val WSAEFAULT = 10014
  final val WSAEINVAL = 10022
  final val WSAEMFILE = 10024
  final val WSAEWOULDBLOCK = 10035
  final val WSAEINPROGRESS = 10036
  final val WSAEALREADY = 10037
  final val WSAENOTSOCK = 10038
  final val WSAEDESTADDRREQ = 10039
  final val WSAEMSGSIZE = 10040
  final val WSAEPROTOTYPE = 10041
  final val WSAENOPROTOOPT = 10042
  final val WSAEPROTONOSUPPORT = 10043
  final val WSAESOCKTNOSUPPORT = 10044
  final val WSAEOPNOTSUPP = 10045
  final val WSAEPFNOSUPPORT = 10046
  final val WSAEAFNOSUPPORT = 10047
  final val WSAEADDRINUSE = 10048
  final val WSAEADDRNOTAVAIL = 10049
  final val WSAENETDOWN = 10050
  final val WSAENETUNREACH = 10051
  final val WSAENETRESET = 10052
  final val WSAECONNABORTED = 10053
  final val WSAECONNRESET = 10054
  final val WSAENOBUFS = 10055
  final val WSAEISCONN = 10056
  final val WSAENOTCONN = 10057
  final val WSAESHUTDOWN = 10058
  final val WSAETOOMANYREFS = 10059
  final val WSAETIMEDOUT = 10060
  final val WSAECONNREFUSED = 10061
  final val WSAELOOP = 10062
  final val WSAENAMETOOLONG = 10063
  final val WSAEHOSTDOWN = 10064
  final val WSAEHOSTUNREACH = 10065
  final val WSAENOTEMPTY = 10066
  final val WSAEPROCLIM = 10067
  final val WSAEUSERS = 10068
  final val WSAEDQUOT = 10069
  final val WSAESTALE = 10070
  final val WSAEREMOTE = 10071
  final val WSASYSNOTREADY = 10091
  final val WSAVERNOTSUPPORTED = 10092
  final val WSANOTINITIALISED = 10093
  final val WSAEDISCON = 10101
  final val WSAENOMORE = 10102
  final val WSAECANCELLED = 10103
  final val WSAEINVALIDPROCTABLE = 10104
  final val WSAEINVALIDPROVIDER = 10105
  final val WSAEPROVIDERFAILEDINIT = 10106
  final val WSASYSCALLFAILURE = 10107
  final val WSASERVICE_NOT_FOUND = 10108
  final val WSATYPE_NOT_FOUND = 10109
  final val WSA_E_NO_MORE = 10110
  final val WSA_E_CANCELLED = 10111
  final val WSAEREFUSED = 10112
  final val WSAHOST_NOT_FOUND = 11001
  final val WSATRY_AGAIN = 11002
  final val WSANO_RECOVERY = 11003
  final val WSANO_DATA = 11004
  final val WSA_QOS_RECEIVERS = 11005
  final val WSA_QOS_SENDERS = 11006
  final val WSA_QOS_NO_SENDERS = 11007
  final val WSA_QOS_NO_RECEIVERS = 11008
  final val WSA_QOS_REQUEST_CONFIRMED = 11009
  final val WSA_QOS_ADMISSION_FAILURE = 11010
  final val WSA_QOS_POLICY_FAILURE = 11011
  final val WSA_QOS_BAD_STYLE = 11012
  final val WSA_QOS_BAD_OBJECT = 11013
  final val WSA_QOS_TRAFFIC_CTRL_ERROR = 11014
  final val WSA_QOS_GENERIC_ERROR = 11015
  final val WSA_QOS_ESERVICETYPE = 11016
  final val WSA_QOS_EFLOWSPEC = 11017
  final val WSA_QOS_EPROVSPECBUF = 11018
  final val WSA_QOS_EFILTERSTYLE = 11019
  final val WSA_QOS_EFILTERTYPE = 11020
  final val WSA_QOS_EFILTERCOUNT = 11021
  final val WSA_QOS_EOBJLENGTH = 11022
  final val WSA_QOS_EFLOWCOUNT = 11023
  final val WSA_QOS_EUNKOWNPSOBJ = 11024
  final val WSA_QOS_EPOLICYOBJ = 11025
  final val WSA_QOS_EFLOWDESC = 11026
  final val WSA_QOS_EPSFLOWSPEC = 11027
  final val WSA_QOS_EPSFILTERSPEC = 11028
  final val WSA_QOS_ESDMODEOBJ = 11029
  final val WSA_QOS_ESHAPERATEOBJ = 11030
  final val WSA_QOS_RESERVED_PETYPE = 11031
}

object WinSocketApiOps {
  import WinSocketApi._
  import WinSocketApiExt._
  import util.Conversion._

  private var winSocketsInitialized = false

  final def init(): Unit = {
    if (!winSocketsInitialized) {
      val requiredVersion = (wordFromBytes _).tupled(WinSocketVersion)
      val winSocketData: Ptr[WSAData] =
        stackalloc[Byte](WinSocketApi.WSADataSize)
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
      winSocketsInitialized = true
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
