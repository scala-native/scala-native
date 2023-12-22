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

  private def setFreeBSDWorkaround(): Unit = {
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
     */

    System.setProperty("java.net.preferIPv4Stack", "true")
  }

  /** Main method of the test runner. */
  def main(args: Array[String]): Unit = {
    if (args.length != 1) {
      System.err.println(usage)
      throw new IllegalArgumentException("One argument expected")
    }

    if (LinktimeInfo.isFreeBSD) setFreeBSDWorkaround()
    val serverPort = args(0).toInt
    val clientSocket = new Socket("127.0.0.1", serverPort)
    val nativeRPC = new NativeRPC(clientSocket)(ExecutionContext.global)
    val bridge = new TestAdapterBridge(nativeRPC)

    bridge.start()

    SignalConfig.setDefaultHandlers()

    val exitCode = nativeRPC.loop()
    sys.exit(exitCode)
  }

}
