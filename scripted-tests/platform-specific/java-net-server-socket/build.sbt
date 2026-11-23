import java.io.{BufferedReader, InputStreamReader, PrintWriter}
import java.net.{InetSocketAddress, Socket}
import java.nio.file.Files

enablePlugins(ScalaNativePlugin)

scalaVersion := {
  val scalaVersion = System.getProperty("scala.version")
  if (scalaVersion == null)
    throw new RuntimeException(
      """|The system property 'scala.version' is not defined.
         |Specify this property using the scriptedLaunchOpts -D.""".stripMargin
    )
  else scalaVersion
}

lazy val launchClient = taskKey[Unit]("Launching a client for tests")

// Echoes every line received from the server started by `run`.
// The server writes its port to `server-port.txt` once it is listening.
launchClient := {
  val portFile = (baseDirectory.value / "server-port.txt").toPath
  Files.deleteIfExists(portFile)
  val client = new Thread(new Runnable {
    def run(): Unit = {
      val deadline = System.nanoTime() + 600L * 1000 * 1000 * 1000
      var port = -1
      while (port < 0 && System.nanoTime() < deadline) {
        if (Files.exists(portFile))
          port = new String(Files.readAllBytes(portFile)).trim.toInt
        else Thread.sleep(100)
      }
      if (port < 0) sys.error("Server port file was not created in time")

      val socket = new Socket
      try {
        socket.connect(new InetSocketAddress("127.0.0.1", port), 10000)
        socket.setSoTimeout(30000)
        val out = new PrintWriter(socket.getOutputStream, true)
        val in =
          new BufferedReader(new InputStreamReader(socket.getInputStream))

        var line = in.readLine
        while (line != null) {
          out.println(line)
          line = in.readLine
        }
      } finally socket.close()
    }
  })
  client.setDaemon(true)
  client.start()
}
