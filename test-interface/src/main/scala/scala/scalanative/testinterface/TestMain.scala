package scala.scalanative
package testinterface

import scalanative.meta.LinktimeInfo

import java.net.Socket
import signalhandling.SignalConfig

import scalanative.posix.sys.socket._
import scalanative.posix.netinet.in
import scalanative.posix.unistd
import scala.concurrent.ExecutionContext

object TestMain {

  private val usage: String = {
    """Usage: test-main <server_port>
      |
      |arguments:
      |  server_port             -  the sbt test server port to use (required)
      |
      |*** Warning - dragons ahead ***
      |
      |This binary is meant to be executed by the Scala Native
      |testing framework and not standalone.
      |
      |Execute at your own risk!
      |""".stripMargin
  }

  final val iPv4Loopback = "127.0.0.1"
  final val iPv6Loopback = "::1"

  private def setFreeBSDWorkaround(): Unit = {
    /* Standard out-of-the-box FreeBSD differs from Linux & macOS in
     * not allowing IPv4 mapped IPv6 addresses, such as :FFFF:127.0.0.1
     * or ::ffff:7f00:1.  These can be enabled at runtime by running
     *
     *   sysctl net.inet6.ip6.v6only=0
     *
     * To make it persistent on reboot it is possible to set the line
     * ipv6_ipv4mapping="YES" in /etc/rc.conf.
     *
     * Another difference is that Java versions >= 11 on FreeBSD set
     * java.net.preferIPv4Stack=true by default, so the sbt server
     * listens only on a tcp4 socket.
     *
     * The easiest way to make TestMain to work on most FreeBSD machines
     * with different Java versions without changing system defaults is
     * to set java.net.preferIPv4Stack=true in Scala Native in order to
     * always use AF_INET IPv4 socket.
     */

    System.setProperty("java.net.preferIPv4Stack", "true")
  }

  /** Main method of the test runner. */
  def main(args: Array[String]): Unit = {
    if (args.length != 1) {
      System.err.println(usage)
      throw new IllegalArgumentException("One argument expected")
    }

    SignalConfig.setDefaultHandlers()

    // Loading debug metadata can take up to few seconds which might mess up timeout specific tests
    // Prefetch the debug metadata before the actual tests do start
    if (LinktimeInfo.hasDebugMetadata) {
      val shouldPrefetch =
        sys.env
          .get("SCALANATIVE_TEST_PREFETCH_DEBUG_INFO")
          .exists(v => v.isEmpty() || v == "1")
      if (shouldPrefetch)
        new RuntimeException().fillInStackTrace().ensuring(_ != null)
    }

    if (LinktimeInfo.isFreeBSD) setFreeBSDWorkaround()
    val serverAddr = iPv4Loopback
    val serverPort = args(0).toInt
    val clientSocket = new Socket(serverAddr, serverPort)
    val nativeRPC = new NativeRPC(clientSocket)(ExecutionContext.global)
    val bridge = new TestAdapterBridge(nativeRPC)

    bridge.start()

    val exitCode = nativeRPC.loop()
    sys.exit(exitCode)
  }

}
