package scala.scalanative.posix
package sys

import scalanative.unsafe._
import scalanative.unsigned._

import scalanative.libc.errno
import scalanative.libc.string.strerror

import scalanative.posix.netdb._
import scalanative.posix.netdbOps._
import scalanative.posix.netinet.in._
import scalanative.posix.netinet.inOps._
import scalanative.posix.poll._
import scalanative.posix.pollOps._
import scalanative.posix.sys.socket._

import scalanative.meta.LinktimeInfo.isWindows

import scala.scalanative.windows._
import scala.scalanative.windows.WinSocketApi._
import scala.scalanative.windows.WinSocketApiExt._
import scala.scalanative.windows.WinSocketApiOps._
import scala.scalanative.windows.ErrorHandlingApi._

import org.junit.Assert._

object SocketTestHelpers {

  def hasIPv6LoopbackAddress(socktype: CInt, protocol: CInt): Boolean = {
    if (isWindows) {
      false // Not implemented on Windows; an exercise for the reader.
    } else
      Zone { implicit z =>
        /* Test where a working IPv6 network is available.
         * The Scala Native GitHub CI environment is known to have a
         * working IPv6 network. Arbitrary local systems may not.
         *
         * The JVM sets a system property "java.net.preferIPv4Stack=false"
         * when an IPv6 interface is active. Scala Native does not
         * set this property. One has to see if an IPv6 loopback address
         * can be found.
         */

        val in6SockAddr = alloc[sockaddr_in6]()
        in6SockAddr.sin6_family = AF_INET6.toUShort
        // sin6_port is already the desired 0; "find a free port".
        // all other fields, including ai_flags, are already 0.

        val localhost = c"::1"

        val hints = stackalloc[addrinfo]()
        hints.ai_family = AF_INET6
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
      val fds = stackalloc[WSAPollFd](1.toUInt)
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
      val fds = stackalloc[struct_pollfd](1.toUInt)
      (fds + 0).fd = fd
      (fds + 0).events = pollEvents.POLLIN | pollEvents.POLLRDNORM

      errno.errno = 0

      /* poll() sounds like a nasty busy wait loop, but is event driven
       * in the kernel.
       */

      val ret = poll(fds, 1.toUInt, timeout)

      if (ret == 0) {
        fail(s"poll timed out after ${timeout} milliseconds")
      } else if (ret < 0) {
        val reason = fromCString(strerror(errno.errno))
        fail(s"poll for input failed - $reason")
      }
      // else good to go
    }
  }
}
