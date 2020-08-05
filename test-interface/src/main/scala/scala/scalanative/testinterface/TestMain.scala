package scala.scalanative
package testinterface

import java.net.Socket

object TestMain extends {

  private val usage: String = {
    """usage: test-main <server_port>
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
    if (args.length < 1) {
      System.err.println(usage)
      throw new IllegalArgumentException("missing arguments")
    }

    val serverPort   = args(0).toInt
    val clientSocket = new Socket("127.0.0.1", serverPort)

    TestAdapterBridge.start()
    val res = NativeRPC.loop(clientSocket)
    sys.exit(res)
  }

}
