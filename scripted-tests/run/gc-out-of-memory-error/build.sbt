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

lazy val runHeapExhaustion = taskKey[Unit](
  "Run allocation failures against a heap with a hard upper bound"
)

runHeapExhaustion := {
  implicit val conv: xsbti.FileConverter = Keys.fileConverter.value
  val binary = nativeExecutable((Compile / nativeLink).value)
  val proc = new ProcessBuilder(binary.getAbsolutePath)
  proc.environment().put("GC_INITIAL_HEAP_SIZE", "1M")
  // Large enough to symbolicate a stack trace after the heap ran out, small
  // enough to be exhausted quickly by the test.
  proc.environment().put("GC_MAXIMUM_HEAP_SIZE", "64M")
  proc.inheritIO()

  val running = proc.start()
  if (!running.waitFor(120, TimeUnit.SECONDS)) {
    running.destroyForcibly()
    throw new RuntimeException(
      "Heap exhaustion test did not finish within 120s"
    )
  }

  val exitCode = running.exitValue()
  if (exitCode != 0)
    throw new RuntimeException(s"Heap exhaustion test exited with $exitCode")
}
