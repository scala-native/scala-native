import java.io.{BufferedReader, InputStreamReader, PrintWriter}
import java.net.{InetAddress, InetSocketAddress, Socket}
import java.nio.file.{Files, Paths}

// In this test we manually do the connecting (and binding when implemented)
object EchoManualClientTest {

  def main(args: Array[String]): Unit = {
    val portFile = Paths.get("server-port.txt")
    val lines = Files.readAllLines(portFile)
    val port = lines.get(0).toInt

    val socket = new Socket()
    socket.connect(new InetSocketAddress("127.0.0.1", port), 500)
    val out = new PrintWriter(socket.getOutputStream, true)
    val in = new BufferedReader(new InputStreamReader(socket.getInputStream))

    out.println("echo")
    assert(in.readLine == "echo")
    val unicodeLine = "♞ € ✓ a 1 %$ ∞ ☎  ௸   ኌ ᳄   🛋  "
    out.println(unicodeLine)
    assert(in.readLine == unicodeLine)

    in.close
    out.close
    socket.close
  }
}
