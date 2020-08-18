import java.net.{Socket, InetSocketAddress}
import java.io.{PrintWriter, BufferedReader, InputStreamReader, File}
import java.nio.file.{Files, Paths}

enablePlugins(ScalaNativePlugin)

scalaVersion := "2.12.12"

lazy val launchClient = taskKey[Unit]("Launching a client for tests")

launchClient := {
  new java.util.Timer().schedule(
    new java.util.TimerTask() {
      def run = {
        val portFile = Paths.get("server-port.txt")
        val lines    = Files.readAllLines(portFile)
        val port     = lines.get(0).toInt

        val socket = new Socket
        socket.connect(new InetSocketAddress("127.0.0.1", port), 1000)
        val out = new PrintWriter(socket.getOutputStream, true)
        val in =
          new BufferedReader(new InputStreamReader(socket.getInputStream))

        var line = in.readLine
        while (line != null) {
          out.println(line)
          line = in.readLine
        }

        in.close
        out.close
        socket.close
      }
    },
    10000
  )
}
