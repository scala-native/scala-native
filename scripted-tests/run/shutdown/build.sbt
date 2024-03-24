import java.util.concurrent.TimeUnit
import java.nio.file.Files
import java.io.File

scalaVersion := {
  val scalaVersion = System.getProperty("scala.version")
  if (scalaVersion == null)
    throw new RuntimeException(
      """|The system property 'scala.version' is not defined.
         |Specify this property using the scriptedLaunchOpts -D.""".stripMargin
    )
  else scalaVersion
}

enablePlugins(ScalaNativePlugin)

val runTestDeleteOnExit =
  taskKey[Unit]("run test checking if shutdown hook is exucuted")
runTestDeleteOnExit := {
  val cmd = (Compile / nativeLink).value.toString
  val file = Files.createTempFile("foo", "")
  assert(Files.exists(file))
  val proc = new ProcessBuilder(cmd, file.toString).start()
  assert(proc.waitFor(5, TimeUnit.SECONDS))
  assert(proc.exitValue == 0)
  assert(!Files.exists(file))
}

def checkThreadsJoin(cmd: String, joinInMain: Boolean): Unit = {
  val joinArg = if (joinInMain) "--join" else ""
  val outFile = Files.createTempFile("proc-out", ".log").toFile()
  val proc =
    new ProcessBuilder(cmd, joinArg)
      .redirectOutput(outFile)
      .start()
  Thread.sleep(3000)
  assert(proc.isAlive())
  proc.destroy()
  assert(proc.waitFor(1, TimeUnit.SECONDS))
  assert(proc.exitValue != 0)
  val stdout = scala.io.Source.fromFile(outFile).mkString
  println(stdout)
  val matched = raw"On shutdown:(\d)".r.findAllMatchIn(stdout).toSeq
  assert(matched.size == 8)
  assert(matched.map(_.group(1)).distinct.size == 8)
}
val runTestThreadsJoin = taskKey[Unit]("test multithreaded shutdown")
runTestThreadsJoin := {
  val cmd = (Compile / nativeLink).value.toString
  checkThreadsJoin(cmd, joinInMain = true)
  checkThreadsJoin(cmd, joinInMain = false)
}
