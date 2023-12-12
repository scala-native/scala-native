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

  private def getFreeBSDLoopbackAddr(): String = {
    /* Standard out-of-the-box FreeBSD differs from Linux & macOS in
     * not allowing IPv4 mapped IPv6 addresses, such as :FFFF:127.0.0.1
     * or ::ffff:7f00:1.  These can be enabled by setting the line
     * ipv6_ipv4mapping="YES" in /etc/rc.conf (and rebooting).
     *
     * FreeBSD TestMain initial connections should succeed on both IPv4
     * and IPv6 systems without requiring arcane and non-standard system
     * configuration.  This method checks the protocol that Java connect()
     * is likely used and returns the corresponding loopback address.
     * Tests which use IPv4 addresses, either through hard-coding or
     * bind resolution, on IPv6 systems will still fail. This allows to
     * run the vast majority of tests which do not have this characteristic.
     *
     * Networking is complex, especially on j-random systems: full of
     * joy & lamentation.
     */

    if (!LinktimeInfo.isFreeBSD) iPv4Loopback // should never happen
    else {
      // These are the effective imports
      import scalanative.posix.sys.socket._
      import scalanative.posix.netinet.in
      import scalanative.posix.unistd

      /* These are to get this code to compile on Windows.
       * Including all of them is cargo cult programming. Someone
       * more patient or more knowledgeable about Windows may
       * be able to reduce the set.
       */
      import scala.scalanative.windows._
      import scala.scalanative.windows.WinSocketApi._
      import scala.scalanative.windows.WinSocketApiExt._
      import scala.scalanative.windows.WinSocketApiOps._
      import scala.scalanative.windows.ErrorHandlingApi._

      /* The keen observer will note that this scheme could be used
       * for Linux and macOS. That change is not introduced at this time
       * in order to preserve historical behavior.
       */
      val sd = socket(AF_INET6, SOCK_STREAM, in.IPPROTO_TCP)
      if (sd == -1) iPv4Loopback
      else {
        unistd.close(sd)
        iPv6Loopback
      }
    }
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

    val serverAddr =
      if (!LinktimeInfo.isFreeBSD) iPv4Loopback
      else getFreeBSDLoopbackAddr()
    val serverPort = args(0).toInt
    val clientSocket = new Socket(serverAddr, serverPort)
    val nativeRPC = new NativeRPC(clientSocket)(ExecutionContext.global)
    val bridge = new TestAdapterBridge(nativeRPC)

    bridge.start()

    val exitCode = nativeRPC.loop()
    sys.exit(exitCode)
  }

}
