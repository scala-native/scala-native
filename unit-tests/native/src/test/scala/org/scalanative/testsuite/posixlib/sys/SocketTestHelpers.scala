package org.scalanative.testsuite.posixlib
package sys

import scalanative.unsafe._
import scalanative.unsigned._

import scalanative.libc.string.strerror

import scalanative.posix.arpa.inet.{inet_addr, inet_pton}
import scalanative.posix.errno.errno
import scalanative.posix.fcntl
import scalanative.posix.fcntl.{F_SETFL, O_NONBLOCK}
import scalanative.posix.netinet.inOps._
import scalanative.posix.netdb._
import scalanative.posix.netdbOps._
import scalanative.posix.netinet.in._
import scalanative.posix.poll._
import scalanative.posix.pollEvents
import scalanative.posix.pollOps._
import scalanative.posix.sys.socket._
import scalanative.posix.unistd

import scalanative.meta.LinktimeInfo.isWindows

import scala.scalanative.windows._
import scala.scalanative.windows.WinSocketApi._
import scala.scalanative.windows.WinSocketApiExt._
import scala.scalanative.windows.WinSocketApiOps._
import scala.scalanative.windows.ErrorHandlingApi._

import org.junit.Assert._
import org.junit.Assume._

object SocketTestHelpers {

  def checkIoResult(v: CSSize, label: String): Unit = {
    if (v.toInt < 0) {
      val reason =
        if (isWindows) ErrorHandlingApiOps.errorMessage(GetLastError())
        else fromCString(strerror(errno))
      fail(s"$label failed - $reason")
    }
  }

  def closeSocket(socket: CInt): Unit = {
    if (isWindows) WinSocketApi.closeSocket(socket.toPtr[Byte])
    else unistd.close(socket)
  }

  def createAndCheckUdpSocket(domain: CInt): CInt = {
    if (isWindows) {
      val socket = WSASocketW(
        addressFamily = domain,
        socketType = SOCK_DGRAM,
        protocol = IPPROTO_UDP,
        protocolInfo = null,
        group = 0.toUInt,
        flags = WSA_FLAG_OVERLAPPED
      )
      assertNotEquals("socket create", InvalidSocket, socket)
      socket.toInt
    } else {
      val sock = socket(domain, SOCK_DGRAM, IPPROTO_UDP)
      assertNotEquals("socket create", -1, sock)
      sock
    }
  }

  /* Setting up IPv6 and IPv6 sockets is just different enough that
   * separate, near duplicate, code is easier to get write.
   * Consolidating the two without lots of hairy "if (ipv6)" and such
   * is left for the next generation.
   */

  def getUdp4LoopbackSockets()(implicit
      z: Zone
  ): Tuple3[CInt, CInt, Ptr[sockaddr]] = {
    val localhost = c"127.0.0.1"
    val localhostInetAddr = inet_addr(localhost)

    val sin: CInt = createAndCheckUdpSocket(AF_INET)

    try {
      val inAddr = alloc[sockaddr]()
      val inAddrInPtr = inAddr.asInstanceOf[Ptr[sockaddr_in]]

      inAddrInPtr.sin_family = AF_INET.toUShort
      inAddrInPtr.sin_addr.s_addr = localhostInetAddr
      // inAddrInPtr.sin_port is already the desired 0; "find a free port".

      setSocketBlocking(sin)

      // Get port for write() to use.
      val bindInStatus = bind(sin, inAddr, sizeof[sockaddr].toUInt)
      assertNotEquals(
        s"bind input socket failed,  errno: ${errno}",
        -1,
        bindInStatus
      )

      val inAddrInfo = alloc[sockaddr]()
      val gsnAddrLen = alloc[socklen_t]()
      !gsnAddrLen = sizeof[sockaddr].toUInt

      val gsnStatus = getsockname(sin, inAddrInfo, gsnAddrLen)
      assertNotEquals("getsockname", -1, gsnStatus)

      // Now use port in output socket
      val sout = createAndCheckUdpSocket(AF_INET)

      try {
        val outAddr = alloc[sockaddr]() // must be alloc, NO stackalloc
        val outAddrInPtr = outAddr.asInstanceOf[Ptr[sockaddr_in]]
        outAddrInPtr.sin_family = AF_INET.toUShort
        outAddrInPtr.sin_addr.s_addr = localhostInetAddr
        outAddrInPtr.sin_port =
          inAddrInfo.asInstanceOf[Ptr[sockaddr_in]].sin_port

        (sin, sout, outAddr)
      } catch {
        case e: Throwable =>
          SocketTestHelpers.closeSocket(sout)
          throw e
      }
    } catch {
      case e: Throwable =>
        SocketTestHelpers.closeSocket(sin)
        throw e
        (-1, -1, null) // should never get here.
    }
  }

  def getUdp6LoopbackSockets()(implicit
      z: Zone
  ): Tuple3[CInt, CInt, Ptr[sockaddr]] = {
    val localhost = c"::1"

    val in6SockAddr = alloc[sockaddr_in6]()
    in6SockAddr.sin6_family = AF_INET6.toUShort

    /* Scala Native currently implements neither inaddr_loopback
     * nor IN6ADDR_LOOPBACK_INIT. When they become available,
     * this code can be simplified by using the former instead
     * of the inet_pton(code below). All things in due time.
     *
     * in6SockAddr.sin6_addr = in6addr_loopback
     */

    // sin6_port is already the desired 0; "find a free port".
    // all other fields already 0.

    val ptonStatus = inet_pton(
      AF_INET6,
      localhost,
      in6SockAddr.sin6_addr.at1.at(0).asInstanceOf[Ptr[Byte]]
    )

    assertEquals(s"inet_pton failed errno: ${errno}", ptonStatus, 1)

    val sin: CInt = createAndCheckUdpSocket(AF_INET6)

    try {
      setSocketBlocking(sin)

      // Get port for sendto() to use.
      val bindStatus = bind(
        sin,
        in6SockAddr
          .asInstanceOf[Ptr[sockaddr]],
        sizeof[sockaddr_in6].toUInt
      )

      assertNotEquals(s"bind failed,  errno: ${errno}", -1, bindStatus)

      val in6AddrInfo = alloc[sockaddr_in6]()
      val gsnAddrLen = alloc[socklen_t]()
      !gsnAddrLen = sizeof[sockaddr_in6].toUInt

      val gsnStatus = getsockname(
        sin,
        in6AddrInfo.asInstanceOf[Ptr[sockaddr]],
        gsnAddrLen
      )

      assertNotEquals("getsockname failed errno: ${errno}", -1, gsnStatus)

      // Now use port in output socket
      val sout = createAndCheckUdpSocket(AF_INET6)

      try {
        val out6Addr = alloc[sockaddr_in6]()
        out6Addr.sin6_family = AF_INET6.toUShort
        out6Addr.sin6_port = in6AddrInfo.sin6_port
        out6Addr.sin6_addr = in6SockAddr.sin6_addr

        (sin, sout, out6Addr.asInstanceOf[Ptr[sockaddr]])
      } catch {
        case e: Throwable =>
          SocketTestHelpers.closeSocket(sout)
          throw e
      }
    } catch {
      case e: Throwable =>
        SocketTestHelpers.closeSocket(sin)
        throw e
        (-1, -1, null) // should never get here.
    }
  }

  def getUdpLoopbackSockets(domain: CInt)(implicit
      z: Zone
  ): Tuple3[CInt, CInt, Ptr[sockaddr]] = {
    if (domain == AF_INET) {
      getUdp4LoopbackSockets()
    } else if (domain == AF_INET6) {
      getUdp6LoopbackSockets()
    } else {
      fail(s"getUdpLoopbackSockets: unsupported domain ${domain}")
      (-1, -1, null)
    }
  }

  def hasLoopbackAddress(
      family: CInt,
      socktype: CInt,
      protocol: CInt
  ): Boolean = {
    if (isWindows) {
      /* Discovery is not implemented on Windows; an exercise for the reader.
       *
       * IPv6 is known to be available on Scala Native Continuous Integration
       * (CI) systems.  It is also usually present, at least for loopback,
       * on Windows systems.
       *
       * Until IPv6 discovery is implemented, enable the test unconditionally,
       * knowing that it will give impolite errors on some Windows systems
       * in the wild. Such people can change the 'true' below to 'false'.
       */

      true
    } else {
      /* Test where a working IPv6 or IPv4 network is available.
       * The Scala Native GitHub CI environment is known to have a
       * working IPv6 network. Arbitrary local systems may not.
       *
       * The JVM sets a system property "java.net.preferIPv4Stack=false"
       * when an IPv6 interface is active. Scala Native does not
       * set this property. One has to see if an IPv6 loopback address
       * can be found.
       */

      assumeTrue(
        s"Address family ${family} is not supported",
        (family == AF_INET6) || (family == AF_INET)
      )
      assumeTrue(
        s"Socket type ${socktype} is not supported",
        (socktype == SOCK_DGRAM) || (socktype == SOCK_STREAM)
      )
      assumeTrue(
        s"IP protocol ${protocol} is not supported",
        (protocol == IPPROTO_UDP) || (protocol == IPPROTO_TCP)
      )

      val localhost =
        if (family == AF_INET) c"127.0.0.1"
        else c"::1"

      val hints = stackalloc[addrinfo]()
      hints.ai_family = family
      hints.ai_socktype = socktype
      hints.ai_protocol = protocol
      hints.ai_flags = AI_NUMERICHOST

      val resultPtr = stackalloc[Ptr[addrinfo]]()

      val status = getaddrinfo(localhost, null, hints, resultPtr);

      if (status == 0) {
        freeaddrinfo(!resultPtr) // leak not, want not!
      } else if ((status != EAI_FAMILY) && (status != EAI_SOCKTYPE)) {
        val msg = s"getaddrinfo failed: ${fromCString(gai_strerror(status))}"
        assertEquals(msg, 0, status)
      }

      /* status 0 means 'found'
       * status EAI_FAMILY means 'not found'.
       * status EAI_SOCKTYPE means not only 'not found' but not even
       *          supported. i.e. Looking for IPv6 with IPv4 single stack.
       */

      status == 0
    }
  }

  def pollReadyToRecv(fd: CInt, timeout: CInt): Unit = {
    // timeout is in milliseconds.

    if (isWindows) {
      val fds = stackalloc[WSAPollFd](1)
      fds.socket = fd.toPtr[Byte]
      fds.events = WinSocketApiExt.POLLIN

      val ret = WSAPoll(fds, 1.toUInt, timeout)

      if (ret == 0) {
        fail(s"poll timed out after ${timeout} milliseconds")
      } else if (ret < 0) {
        val reason = ErrorHandlingApiOps.errorMessage(GetLastError())
        fail(s"poll for input failed - $reason")
      }
    } else {
      val fds = stackalloc[struct_pollfd](1)
      (fds + 0).fd = fd
      (fds + 0).events = pollEvents.POLLIN | pollEvents.POLLRDNORM

      errno = 0

      /* poll() sounds like a nasty busy wait loop, but is event driven
       * in the kernel.
       */

      val ret = poll(fds, 1.toUInt, timeout)

      if (ret == 0) {
        fail(s"poll timed out after ${timeout} milliseconds")
      } else if (ret < 0) {
        val reason = fromCString(strerror(errno))
        fail(s"poll for input failed - $reason")
      }
      // else good to go
    }
  }

  // For some unknown reason inlining content of this method leads to failures
  // on Unix, probably due to bug in linktime conditions.
  def setSocketBlocking(socket: CInt): Unit = {
    if (isWindows) {
      val mode = stackalloc[CInt]()
      !mode = 1
      assertNotEquals(
        "iotctl setBLocking",
        -1,
        ioctlSocket(socket.toPtr[Byte], FIONBIO, mode)
      )
    } else {
      assertNotEquals(
        s"fcntl set blocking",
        -1,
        fcntl.fcntl(socket, F_SETFL, O_NONBLOCK)
      )
    }
  }
}
