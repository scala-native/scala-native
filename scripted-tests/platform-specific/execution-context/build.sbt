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

nativeConfig ~= { _.withMultithreading(false) }

/** sbt 1 returns a [[java.io.File]]; sbt 2 returns a virtual file ref — resolve
 *  via [[xsbti.FileConverter]].
 */
def nativeExecutable(
    linkOutput: Any
)(implicit conv: xsbti.FileConverter): java.io.File =
  linkOutput match {
    case f: java.io.File           => f
    case ref: xsbti.VirtualFileRef => conv.toPath(ref).toFile()
  }

import java.util.Locale
val osName = System
  .getProperty("os.name", "unknown")
  .toLowerCase(Locale.ROOT)
val isMac = osName.startsWith("mac")

lazy val testQueueExecutionContext = taskKey[Unit]("...")
testQueueExecutionContext := {
  import scala.sys.process._
  implicit val conv: xsbti.FileConverter = Keys.fileConverter.value

  val bin = (Compile / nativeLink).value
  val out = Process(nativeExecutable(bin).getAbsolutePath).lineStream_!.toList
  assert(
    out == List(
      "start main",
      "end main",
      "future 1",
      "future 2",
      "future 3",
      "result: 10"
    )
  )
}

lazy val testQueueExecutionContext2 = taskKey[Unit]("...")
testQueueExecutionContext2 := {
  import java.util.concurrent.TimeUnit
  implicit val conv: xsbti.FileConverter = Keys.fileConverter.value
  val bin = (Compile / nativeLink).value
  val proc = new ProcessBuilder(nativeExecutable(bin).getAbsolutePath).start()
  val finished = proc.waitFor(1, TimeUnit.SECONDS)
  if (!finished) proc.destroyForcibly()
  assert(finished)
}

lazy val testEventLoop = taskKey[Unit]("...")
testEventLoop := Def.taskDyn {
  // libuv is preintstalled only on MacOS GithubRunners
  if (!isMac) Def.task { println("EvenLoop test skipped") }
  else
    Def.task {
      import java.util.concurrent.TimeUnit
      implicit val conv: xsbti.FileConverter = Keys.fileConverter.value
      val bin = (Compile / nativeLink).value
      val proc =
        new ProcessBuilder(nativeExecutable(bin).getAbsolutePath).start()
      val finished = proc.waitFor(1, TimeUnit.SECONDS)
      if (!finished) proc.destroyForcibly()
      assert(finished)
    }
}.value

lazy val testIssue3859 = taskKey[Unit]("...")
testIssue3859 := {
  import java.util.concurrent.TimeUnit
  implicit val conv: xsbti.FileConverter = Keys.fileConverter.value
  val bin = (Compile / nativeLink).value
  val proc = new ProcessBuilder(nativeExecutable(bin).getAbsolutePath).start()
  val finished = proc.waitFor(1, TimeUnit.SECONDS)
  if (!finished) proc.destroyForcibly()
  assert(finished)
}
