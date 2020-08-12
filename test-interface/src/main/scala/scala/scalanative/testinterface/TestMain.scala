package scala.scalanative
package testinterface

import java.net.Socket

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

  /** Main method of the test runner. */
  def main(args: Array[String]): Unit = {
    if (args.length != 1) {
      System.err.println(usage)
      throw new IllegalArgumentException("One argument expected")
    }

    val serverPort   = args(0).toInt
    val clientSocket = new Socket("127.0.0.1", serverPort)
    val nativeRPC    = new NativeRPC(clientSocket)
    val bridge       = new TestAdapterBridge(nativeRPC)

    bridge.start()

    val exitCode = nativeRPC.loop()
    sys.exit(exitCode)
  }

}
