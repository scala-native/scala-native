package java.net

import java.nio.file.{Paths, Files}
import java.io.{BufferedReader, InputStreamReader}

object TimeoutTest {

  def main(args: Array[String]): Unit = {
    val portFile = Paths.get("server-port.txt")
    val lines    = Files.readAllLines(portFile)
    val port     = lines.get(0).toInt

    val socket = new Socket("127.0.0.1", port)
    val in     = new BufferedReader(new InputStreamReader(socket.getInputStream))
    socket.setSoTimeout(500)
    try {
      in.readLine
    } catch {
      case e: SocketTimeoutException => {}
      case e: Throwable              => throw e
    } finally {
      in.close
      socket.close
    }
  }
}
