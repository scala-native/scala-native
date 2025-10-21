import java.io.{BufferedReader, InputStreamReader, PrintWriter}
import java.net.ServerSocket
import java.nio.file.{Files, Paths}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object ServerTest {

  val echoServer = new ServerSocket(0)

  def func: Unit = {
    val clientSocket = echoServer.accept
    val out = new PrintWriter(clientSocket.getOutputStream, true)
    val in =
      new BufferedReader(new InputStreamReader(clientSocket.getInputStream))

    out.println("echo")
    val unicodeLine = "♞ € ✓ a 1 %$ ∞ ☎  ௸   ኌ ᳄   🛋  "
    out.println(unicodeLine)

    assert(in.readLine == "echo")
    assert(in.readLine == unicodeLine)

    in.close
    out.close
    clientSocket.close
  }

  def main(args: Array[String]): Unit = {
    val portFile = Paths.get("server-port.txt")
    Files.write(portFile, echoServer.getLocalPort.toString.getBytes)
    echoServer.setSoTimeout(20000)
    val f = Future(func)
    f.onComplete {
      case _ => {
        echoServer.close
        Files.delete(portFile)
      }
    }
  }
}
