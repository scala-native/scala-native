package scala.scalanative
package testinterface

import java.net.Socket

import scala.concurrent.ExecutionContext

import scala.scalanative.runtime.testinterface.signalhandling.SignalConfig
import scalanative.meta.LinktimeInfo
import scalanative.posix.netinet.in
import scalanative.posix.sys.socket._
import scalanative.posix.unistd

object TestMain {

  private val usage: String = {
    """|Usage: test-main <server_port>
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
