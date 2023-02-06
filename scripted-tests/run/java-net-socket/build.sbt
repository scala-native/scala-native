import java.net.ServerSocket
import java.io.{PrintWriter, BufferedReader, InputStreamReader, File}
import java.nio.file.{Files, Paths}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

enablePlugins(ScalaNativePlugin)

Compile / nativeConfig ~= {
  _.withMultipleMains(true)
}

scalaVersion := {
  val scalaVersion = System.getProperty("scala.version")
  if (scalaVersion == null)
    throw new RuntimeException(
      """|The system property 'scala.version' is not defined.
         |Specify this property using the scriptedLaunchOpts -D.""".stripMargin
    )
  else scalaVersion
}

lazy val launchServer = taskKey[Unit]("Setting up a server for tests")
lazy val launchTcpEchoServer =
  taskKey[Unit]("Setting up a TCP echo server")
lazy val launchSilentServer =
  taskKey[Unit]("Setting up a non responding server")

launchServer := {
  val echoServer = new ServerSocket(0)
  val portFile = Paths.get("server-port.txt")
  Files.write(portFile, echoServer.getLocalPort.toString.getBytes)
  val f = Future {
    val clientSocket = echoServer.accept
    val out = new PrintWriter(clientSocket.getOutputStream, true)
    val in =
      new BufferedReader(new InputStreamReader(clientSocket.getInputStream))

    var line = in.readLine
    while (line != null) {
      out.println(line)
      line = in.readLine
    }
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

launchTcpEchoServer := {
  val echoServer = new ServerSocket(0)
  val portFile = Paths.get("server-port.txt")
  Files.write(portFile, echoServer.getLocalPort.toString.getBytes)
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
  f.onComplete {
    case _ => {
      echoServer.close
      Files.delete(portFile)
    }
  }
}

launchSilentServer := {
  val echoServer = new ServerSocket(0)
  val portFile = Paths.get("server-port.txt")
  Files.write(portFile, echoServer.getLocalPort.toString.getBytes)
  val f = Future {
    val clientSocket = echoServer.accept
    val in =
      new BufferedReader(new InputStreamReader(clientSocket.getInputStream))

    var line = in.readLine
    while (line != null) {
      line = in.readLine
    }

    in.close
    clientSocket.close
  }
  f.onComplete {
    case _ => {
      echoServer.close
      Files.delete(portFile)
    }
  }
}
