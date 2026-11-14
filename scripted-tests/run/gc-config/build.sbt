import scala.scalanative.build._

enablePlugins(ScalaNativePlugin)

Compile / mainClass := Some("Main")

scalaVersion := {
  val scalaVersion = System.getProperty("scala.version")
  if (scalaVersion == null)
    throw new RuntimeException(
      """|The system property 'scala.version' is not defined.
         |Specify this property using the scriptedLaunchOpts -D.""".stripMargin
    )
  else scalaVersion
}

// Legacy defines lose with the settings below
nativeConfig ~= {
  _.withCompileOptions(_ :+ "-DGC_MAXIMUM_HEAP_SIZE=32m")
    .withGarbageCollectorConfig(
      _.withInitialHeapSize(8.mb)
        .withMaxHeapSize(256.mb)
        .withLogLevel(GarbageCollectorConfig.LogLevel.Info)
    )
}

def nativeExecutable(
    linkOutput: Any
)(implicit conv: xsbti.FileConverter): java.io.File =
  linkOutput match {
    case f: java.io.File           => f
    case ref: xsbti.VirtualFileRef => conv.toPath(ref).toFile()
  }

val gcVariables = Seq(
  "GC_INITIAL_HEAP_SIZE",
  "GC_MAXIMUM_HEAP_SIZE",
  "SCALANATIVE_GC_LOG_LEVEL",
  "GC_LOG_FILE"
)

def runBinary(binary: String, env: (String, String)*): String = {
  val proc = new java.lang.ProcessBuilder(binary)
  // Start from a clean slate, CI may export some of the variables.
  gcVariables.foreach(proc.environment().remove(_))
  env.foreach { case (name, value) => proc.environment().put(name, value) }
  proc.redirectErrorStream(true)
  val running = proc.start()
  val output = scala.io.Source.fromInputStream(running.getInputStream).mkString
  val exitCode = running.waitFor()
  if (exitCode != 0)
    throw new RuntimeException(s"Exit code $exitCode with $env:\n$output")
  output
}

def expect(output: String, fragment: String, present: Boolean = true): Unit =
  if (output.contains(fragment) != present)
    throw new RuntimeException(
      s"Expected output ${if (present) "with" else "without"} '$fragment', got:\n$output"
    )

lazy val checkBuiltInSettings = taskKey[Unit](
  "Check that the settings of the build are used and that the environment of the program overrides them"
)

checkBuiltInSettings := {
  implicit val conv: xsbti.FileConverter = Keys.fileConverter.value
  val binary = nativeExecutable((Compile / nativeLink).value).getAbsolutePath
  val mb = 1024L * 1024L

  val builtIn = runBinary(binary)
  expect(builtIn, s"initialHeapSize=${8 * mb} maxHeapSize=${256 * mb}")
  // The log level of the build shows how the sizes were chosen
  expect(builtIn, s"GC_MAXIMUM_HEAP_SIZE not set, using ${256 * mb}")

  val overridden = runBinary(
    binary,
    "GC_INITIAL_HEAP_SIZE" -> "16m",
    "GC_MAXIMUM_HEAP_SIZE" -> "512m"
  )
  expect(overridden, s"initialHeapSize=${16 * mb} maxHeapSize=${512 * mb}")
  expect(overridden, "Found GC_MAXIMUM_HEAP_SIZE=512m")

  val quiet = runBinary(binary, "SCALANATIVE_GC_LOG_LEVEL" -> "warn")
  expect(quiet, s"initialHeapSize=${8 * mb} maxHeapSize=${256 * mb}")
  expect(quiet, "GC_MAXIMUM_HEAP_SIZE", present = false)

  // An invalid value is reported and the setting of the build is kept
  val invalid = runBinary(binary, "GC_MAXIMUM_HEAP_SIZE" -> "lots")
  expect(invalid, "Ignoring GC_MAXIMUM_HEAP_SIZE=lots")
  expect(invalid, s"maxHeapSize=${256 * mb}")
}
