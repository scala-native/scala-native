import java.io.File
import java.nio.file.Files
import java.util.Locale
import java.util.concurrent.TimeUnit

val osName = System
  .getProperty("os.name", "unknown")
  .toLowerCase(Locale.ROOT)
val isWindows = osName.startsWith("windows")

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

/** sbt 1: link output is a [[java.io.File]]; sbt 2: virtual file ref — resolve
 *  with [[xsbti.FileConverter]].
 */
def nativeExecutable(
    linkOutput: Any
)(implicit conv: xsbti.FileConverter): java.io.File =
  linkOutput match {
    case f: java.io.File           => f
    case ref: xsbti.VirtualFileRef => conv.toPath(ref).toFile()
  }

val runTestDeleteOnExit =
  taskKey[Unit]("run test checking if shutdown hook is exucuted")
runTestDeleteOnExit := {
  implicit val conv: xsbti.FileConverter = Keys.fileConverter.value
  val cmd = nativeExecutable((Compile / nativeLink).value).toString
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
  if (isWindows)
    System.err.println(
      "Not testing multithreaded shutdown on Windows - it can deadlock during the GC, due to the lack of signals blocking"
    )
  else {
    implicit val conv: xsbti.FileConverter = Keys.fileConverter.value
    val cmd = nativeExecutable((Compile / nativeLink).value).toString
    checkThreadsJoin(cmd, joinInMain = true)
    checkThreadsJoin(cmd, joinInMain = false)
  }
}

val runTestQueueWithThreads = taskKey[Unit](
  "test multithreaded shutdown in mixed environement using Queue and Threads scheduling"
)
runTestQueueWithThreads := {
  implicit val conv: xsbti.FileConverter = Keys.fileConverter.value
  val cmd = nativeExecutable((Compile / nativeLink).value).toString
  val proc = new ProcessBuilder(cmd).start()
  assert(proc.waitFor(5, TimeUnit.SECONDS))
  assert(proc.exitValue == 0)
}
