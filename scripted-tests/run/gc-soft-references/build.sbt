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

lazy val runSoftReferences = taskKey[Unit](
  "Run out of a heap with a hard upper bound while soft references hold most of it"
)

runSoftReferences := {
  implicit val conv: xsbti.FileConverter = Keys.fileConverter.value
  val binary = nativeExecutable((Compile / nativeLink).value)
  val proc = new ProcessBuilder(binary.getAbsolutePath)
  proc.environment().put("GC_INITIAL_HEAP_SIZE", "1M")
  // Small enough to be exhausted quickly, see Main.MaxHeapSize
  proc.environment().put("GC_MAXIMUM_HEAP_SIZE", "64M")
  proc.inheritIO()

  val running = proc.start()
  if (!running.waitFor(120, TimeUnit.SECONDS)) {
    running.destroyForcibly()
    throw new RuntimeException(
      "Soft references test did not finish within 120s"
    )
  }

  val exitCode = running.exitValue()
  if (exitCode != 0)
    throw new RuntimeException(s"Soft references test exited with $exitCode")
}
