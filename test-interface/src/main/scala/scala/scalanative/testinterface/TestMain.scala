package scala.scalanative
package testinterface

import scalanative.meta.LinktimeInfo

import java.net.Socket
import scala.scalanative.runtime.testinterface.signalhandling.SignalConfig

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

  private def maybeSetPreferIPv4Stack(): Unit = {
    /* Standard out-of-the-box FreeBSD differs from Linux & macOS in
     * not allowing IPv4-mapped IPv6 addresses, such as :FFFF:127.0.0.1
     * or ::ffff:7f00:1.
     *
     * Another difference is that Java versions >= 11 on FreeBSD set
     * java.net.preferIPv4Stack=true by default, so the sbt server
     * listens only on a tcp4 socket.
     *
     * Even if IPv4-mapped IPv6 addresses can be enabled (via the
     * net.inet6.ip6.v6only=0 sysctl and/or via the ipv6_ipv4mapping="YES"
     * rc.conf variable) and sbt can be instructed to listen on an IPv6
     * socket (via the java.net.preferIPv4Stack=false system property),
     * the easiest way to make TestMain to work on most FreeBSD machines,
     * with different Java versions, is to set
     * java.net.preferIPv4Stack=true in Scala Native, before the first
     * Java network call, in order to always use an AF_INET IPv4 socket.
     *
     * Thus, OpenBSD has the same behaviour as well.
     *
     * See: https://github.com/scala-native/scala-native/issues/3630
     */

    if (!LinktimeInfo.isFreeBSD && !LinktimeInfo.isOpenBSD && !LinktimeInfo.isNetBSD)
      return

    System.setProperty("java.net.preferIPv4Stack", "true")
  }

  /** Main method of the test runner. */
  def main(args: Array[String]): Unit = {
    if (args.length != 1) {
      System.err.println(usage)
      throw new IllegalArgumentException("One argument expected")
    }

    locally {
      val shouldSetupSignalHandlers = sys.env
        .get("SCALANATIVE_TEST_DEBUG_SIGNALS")
        .exists(v => v.isEmpty() || v == "1")
      if (shouldSetupSignalHandlers)
        SignalConfig.setDefaultHandlers()
    }

    maybeSetPreferIPv4Stack()

    val serverPort = args(0).toInt
    val clientSocket = new Socket("127.0.0.1", serverPort)
    val nativeRPC = new NativeRPC(clientSocket)(ExecutionContext.global)
    val bridge = new TestAdapterBridge(nativeRPC)

    // Loading debug metadata can take up to few seconds which might mess up timeout specific tests
    // Prefetch the debug metadata before the actual tests do start
    // Execute after creating connection with the TestRunnner server
    if (LinktimeInfo.sourceLevelDebuging.generateFunctionSourcePositions) {
      val shouldPrefetch =
        sys.env
          .get("SCALANATIVE_TEST_PREFETCH_DEBUG_INFO")
          .exists(v => v.isEmpty() || v == "1")
      if (shouldPrefetch)
        new RuntimeException().fillInStackTrace().ensuring(_ != null)
    }

    bridge.start()

    val exitCode = nativeRPC.loop()
    sys.exit(exitCode)
  }

}
