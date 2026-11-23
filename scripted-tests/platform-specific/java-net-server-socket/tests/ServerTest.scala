import java.io.{BufferedReader, InputStreamReader, PrintWriter}
import java.net.ServerSocket
import java.nio.file.{Files, Paths, StandardCopyOption}

object ServerTest {

  def main(args: Array[String]): Unit = {
    val portFile = Paths.get("server-port.txt")
    val tmpPortFile = Paths.get("server-port.txt.tmp")
    val echoServer = new ServerSocket(0)
    try {
      echoServer.setSoTimeout(30000)
      Files.write(tmpPortFile, echoServer.getLocalPort.toString.getBytes)
      Files.move(tmpPortFile, portFile, StandardCopyOption.ATOMIC_MOVE)

      val clientSocket = echoServer.accept()
      try {
        clientSocket.setSoTimeout(30000)
        val out = new PrintWriter(clientSocket.getOutputStream, true)
        val in = new BufferedReader(
          new InputStreamReader(clientSocket.getInputStream)
        )

        val unicodeLine = "♞ € ✓ a 1 %$ ∞ ☎  ௸   ኌ ᳄   🛋  "
        out.println("echo")
        out.println(unicodeLine)

        expectLine(in, "echo")
        expectLine(in, unicodeLine)
      } finally clientSocket.close()
    } finally {
      echoServer.close()
      Files.deleteIfExists(portFile)
    }
  }

  private def expectLine(in: BufferedReader, expected: String): Unit = {
    val line = in.readLine()
    if (line != expected)
      throw new AssertionError(s"expected '$expected', got '$line'")
  }
}
