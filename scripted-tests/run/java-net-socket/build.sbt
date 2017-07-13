import java.net.{ServerSocket}
import java.io.{PrintWriter, BufferedReader, InputStreamReader}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

enablePlugins(ScalaNativePlugin)

scalaVersion := "2.11.11"

lazy val launchEchoServer = taskKey[Unit]("Setting up echo server on port 5832")

launchEchoServer := {
  val echoServer = new ServerSocket(5832)
  val f = Future { 
    val clientSocket = echoServer.accept 
    val out = clientSocket.getOutputStream
    val in = clientSocket.getInputStream
    val buffer = new Array[Byte](4)
 
    var count = in.read(buffer, 0, 4)
    out.write(buffer)
    in.close
    out.close
    clientSocket.close
  }
  f.onComplete { case _ => echoServer.close }

}
