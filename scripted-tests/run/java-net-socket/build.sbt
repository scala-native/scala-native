import java.net.{ServerSocket}
import java.io.{PrintWriter, BufferedReader, InputStreamReader, File}
import java.nio.file.{Files, Paths}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

enablePlugins(ScalaNativePlugin)

scalaVersion := "2.11.11"

lazy val launchEchoServer =
  taskKey[Unit]("Setting up tcp echo server")

launchEchoServer := {
  val echoServer = new ServerSocket(0)
  val portFile   = Paths.get("server-port.txt")
  Files.write(portFile, echoServer.getLocalPort.toString.getBytes)
  val f = Future {
    val clientSocket = echoServer.accept
    val out          = clientSocket.getOutputStream
    val in           = clientSocket.getInputStream
    val buffer       = new Array[Byte](4)

    var count = in.read(buffer, 0, 4)
    out.write(buffer)
    in.close
    out.close
    clientSocket.close
  }
  f.onComplete {
    case _ => {
      echoServer.close
      Files.delete(portFile)
    }
  }

}
