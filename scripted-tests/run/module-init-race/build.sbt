import java.util.concurrent.TimeUnit

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

// The race lives on the multithreaded module-init path, so build the code path
// under test explicitly rather than relying on auto-detection.
nativeConfig ~= { _.withMultithreading(true) }

/** sbt 1 returns the link output as a [[java.io.File]]; sbt 2 returns a virtual
 *  file ref that an [[xsbti.FileConverter]] resolves to a path.
 */
def nativeExecutable(
    linkOutput: Any
)(implicit conv: xsbti.FileConverter): java.io.File =
  linkOutput match {
    case f: java.io.File           => f
    case ref: xsbti.VirtualFileRef => conv.toPath(ref).toFile()
  }

/** Run the linked binary once, capturing its combined output, and return the
 *  exit code together with that output.
 */
def runBinary(
    binary: java.io.File,
    env: Map[String, String],
    timeoutSeconds: Int
): (Int, String) = {
  val pb = new ProcessBuilder(binary.getAbsolutePath)
  env.foreach { case (k, v) => pb.environment().put(k, v) }
  pb.redirectErrorStream(true)
  val log = java.io.File.createTempFile("module-init-race-", ".log")
  pb.redirectOutput(log)
  val proc = pb.start()
  val finished = proc.waitFor(timeoutSeconds.toLong, TimeUnit.SECONDS)
  if (!finished) {
    proc.destroyForcibly()
    throw new RuntimeException(
      s"binary did not finish within $timeoutSeconds seconds"
    )
  }
  val output =
    new String(java.nio.file.Files.readAllBytes(log.toPath))
  log.delete()
  (proc.exitValue(), output)
}

/** Plain phase: pin the observable contract and act as a crash canary. Each run
 *  is a fresh process, hence a fresh concurrent first-touch of every module.
 */
lazy val stress = taskKey[Unit]("Run the module-init race stress test")
stress := {
  implicit val conv: xsbti.FileConverter = Keys.fileConverter.value
  val binary = nativeExecutable((Compile / nativeLink).value)
  val runs = 3
  (1 to runs).foreach { n =>
    val (code, output) = runBinary(binary, Map.empty, timeoutSeconds = 120)
    print(output)
    if (code != 0)
      throw new RuntimeException(
        s"module-init-race stress run $n of $runs exited with code $code"
      )
  }
  println(s"module-init-race: $runs plain runs passed")
}

/** ThreadSanitizer phase: the actual race detector. Runs only when the config
 *  carries a sanitizer (set from the `test` script on non-Windows platforms;
 *  ThreadSanitizer is unavailable on Windows).
 *
 *  TSAN_OPTIONS uses exitcode=0 so unrelated sanitizer reports (for example in
 *  GC internals) do not fail the run, and halt_on_error=0 so the program runs
 *  to completion and every report is captured. The assertion is then targeted:
 *  it fails only when a report implicates the module-initialization code under
 *  test, and when the process itself crashed. Maintainers who prefer the
 *  stricter exitcode=66 default can pair it with a GC-scoped suppressions file.
 */
lazy val stressSanitized =
  taskKey[Unit]("Run the stress test under ThreadSanitizer")
stressSanitized := {
  (Compile / nativeConfig).value.sanitizer match {
    case None =>
      println(
        "module-init-race: ThreadSanitizer phase skipped " +
          "(no sanitizer configured on this platform)"
      )
    case Some(_) =>
      implicit val conv: xsbti.FileConverter = Keys.fileConverter.value
      val binary = nativeExecutable((Compile / nativeLink).value)
      val env = Map("TSAN_OPTIONS" -> "halt_on_error=0 exitcode=0")
      val (code, output) = runBinary(binary, env, timeoutSeconds = 300)
      print(output)
      val moduleFrames = Seq(
        "module_load",
        "loadModule",
        "startAndWaitForModuleInitialization",
        "waitForModuleInitialization",
        "awaitForInitialization"
      )
      val reportedModuleRace =
        output.contains("ThreadSanitizer: data race") &&
          moduleFrames.exists(output.contains)
      if (reportedModuleRace)
        throw new RuntimeException(
          "ThreadSanitizer reported a data race in module initialization; " +
            "the publish-ordering fix in module_load.c has regressed"
        )
      if (code != 0)
        throw new RuntimeException(
          s"sanitized run exited abnormally with code $code"
        )
      println("module-init-race: ThreadSanitizer phase clean")
  }
}
