import java.util.Locale
import java.util.concurrent.TimeUnit

val isWindows = System
  .getProperty("os.name", "unknown")
  .toLowerCase(Locale.ROOT)
  .startsWith("windows")

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

// The pool runs callbacks on its own threads.
nativeConfig ~= { _.withMultithreading(true) }

def nativeExecutable(
    linkOutput: Any
)(implicit conv: xsbti.FileConverter): java.io.File =
  linkOutput match {
    case f: java.io.File           => f
    case ref: xsbti.VirtualFileRef => conv.toPath(ref).toFile()
  }

def runScenario(
    binary: java.io.File,
    scenario: String,
    load: Int,
    timeoutSeconds: Int,
    expectedError: Option[String] = None
): Unit = {
  println(s"Running scenario: $scenario")
  val errors = java.io.File.createTempFile("thread-pool", ".err")
  val pb = new ProcessBuilder(binary.getAbsolutePath, scenario, load.toString)
  pb.redirectOutput(ProcessBuilder.Redirect.INHERIT)
  pb.redirectError(errors)
  val proc = pb.start()
  if (!proc.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
    proc.destroyForcibly()
    System.err.print(IO.read(errors))
    errors.delete()
    sys.error(s"FAIL: $scenario did not complete in $timeoutSeconds seconds")
  }
  val stderr = IO.read(errors)
  errors.delete()
  System.err.print(stderr)
  val exitCode = proc.exitValue()
  expectedError match {
    case Some(message) =>
      if (exitCode == 0 || !stderr.contains(message))
        sys.error(s"FAIL: $scenario exited with $exitCode without: $message")
    case None =>
      if (exitCode != 0) sys.error(s"FAIL: $scenario exited with $exitCode")
  }
  println(s"$scenario PASSED")
}

val scenarios = Seq("reuse", "churn")

lazy val testAll = taskKey[Unit]("Run every thread pool scenario")
testAll := Def.taskDyn {
  if (isWindows) Def.task(println("Skipping thread pool tests, requires POSIX"))
  else
    Def.task {
      implicit val conv: xsbti.FileConverter = Keys.fileConverter.value
      val binary = nativeExecutable((Compile / nativeLink).value)
      // A collector which never frees memory gets a tenth of the work.
      val load =
        if (nativeConfig.value.gc == scala.scalanative.build.GC.none) 1
        else 10
      scenarios.foreach(runScenario(binary, _, load, timeoutSeconds = 120))
    }
}.value

lazy val testSingleThreaded =
  taskKey[Unit]("A single-threaded executable rejects foreign threads")
testSingleThreaded := Def.taskDyn {
  if (isWindows) Def.task(println("Skipping thread pool tests, requires POSIX"))
  else
    Def.task {
      implicit val conv: xsbti.FileConverter = Keys.fileConverter.value
      val binary = nativeExecutable((Compile / nativeLink).value)
      Seq("reuse").foreach(
        runScenario(
          binary,
          _,
          load = 1,
          timeoutSeconds = 60,
          expectedError = Some("Enable multithreading")
        )
      )
    }
}.value
