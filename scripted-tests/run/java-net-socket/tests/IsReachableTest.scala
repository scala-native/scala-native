package java.net

import java.nio.file.{Paths, Files}

object IsReachableTest {

  def main(args: Array[String]): Unit = {
    val portFile = Paths.get("server-port.txt")
    val lines    = Files.readAllLines(portFile)
    val port     = lines.get(0).toInt
    assert(SocketHelpers.isReachableByEcho("127.0.0.1", 1000, port) == true)
  }

}
