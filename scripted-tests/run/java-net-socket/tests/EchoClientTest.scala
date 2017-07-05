import java.net.Socket
import java.io.{PrintWriter, BufferedReader, InputStreamReader}

object EchoClientTest {

  def main(args: Array[String]): Unit = {
    println("w main")
    val socket = new Socket("127.0.0.1", 5832)
    val out = new PrintWriter(socket.getOutputStream, true)
    val in = new BufferedReader(new InputStreamReader(socket.getInputStream))

    out.println("echo")
    assert(in.readLine == "echo")
    val unicodeLine =  "♞ € ✓ a 1 %$ ∞ ☎  ௸   ኌ ᳄   🛋  "
    out.println(unicodeLine)
    assert(in.readLine == unicodeLine)
    
    in.close
    out.close
    socket.close
  }
}
