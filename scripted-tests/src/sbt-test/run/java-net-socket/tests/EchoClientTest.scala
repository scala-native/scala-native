import java.net.{Socket, InetSocketAddress}
import java.io.{PrintWriter, BufferedReader, InputStreamReader}
import java.nio.file.{Paths, Files}

object EchoClientTest {

  def main(args: Array[String]): Unit = {
    val portFile = Paths.get("server-port.txt")
    val lines    = Files.readAllLines(portFile)
    val port     = lines.get(0).toInt

    val socket = new Socket("127.0.0.1", port)
    val out    = new PrintWriter(socket.getOutputStream, true)
    val in     = new BufferedReader(new InputStreamReader(socket.getInputStream))

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
