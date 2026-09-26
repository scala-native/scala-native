import java.util.concurrent.TimeUnit

enablePlugins(ScalaNativePlugin)

Compile / mainClass := Some("Main")

nativeConfig ~= {
  _.withMultithreading(true)
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

def nativeExecutable(
    linkOutput: Any
)(implicit conv: xsbti.FileConverter): java.io.File =
  linkOutput match {
    case f: java.io.File           => f
    case ref: xsbti.VirtualFileRef => conv.toPath(ref).toFile()
  }

lazy val runExplicitCollectGrowth = taskKey[Unit](
  "Run a System.gc() loop and fail if commix over-reserves the heap"
)

runExplicitCollectGrowth := {
  implicit val conv: xsbti.FileConverter = Keys.fileConverter.value
  val binary = nativeExecutable((Compile / nativeLink).value)
  val proc = new ProcessBuilder(binary.getAbsolutePath)
  proc.environment().put("GC_INITIAL_HEAP_SIZE", "4M")
  // Caps a runaway grow, which would otherwise reserve physical RAM.
  proc.environment().put("GC_MAXIMUM_HEAP_SIZE", "64M")
  proc.inheritIO()

  val running = proc.start()
  if (!running.waitFor(60, TimeUnit.SECONDS)) {
    running.destroyForcibly()
    throw new RuntimeException(
      "explicit-collect growth test did not finish within 60s"
    )
  }

  val exitCode = running.exitValue()
  if (exitCode != 0)
    throw new RuntimeException(
      s"explicit-collect growth test exited with $exitCode"
    )
}
