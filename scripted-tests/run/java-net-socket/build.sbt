import java.net.{ServerSocket}
import java.io.{PrintWriter, BufferedReader, InputStreamReader}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

enablePlugins(ScalaNativePlugin)

scalaVersion := "2.11.11"

lazy val launchEchoServer = taskKey[Unit]("Setting up echo server")

launchEchoServer := {
  val echoServer = new ServerSocket(5832)
  val f = Future { 
    val clientSocket = echoServer.accept 
    val out = new PrintWriter(clientSocket.getOutputStream, true)
    val in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream))
 
    var line = in.readLine
    while(line != null) {
      out.println(line)
      line = in.readLine
    }
    in.close
    out.close
    clientSocket.close
  }
  f.onComplete { case _ => echoServer.close }
}

