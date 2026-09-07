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

lazy val runThreadBurst = taskKey[Unit](
  "Run a constrained-heap burst of 512 mutator threads"
)

runThreadBurst := {
  implicit val conv: xsbti.FileConverter = Keys.fileConverter.value
  val binary = nativeExecutable((Compile / nativeLink).value)
  val proc = new ProcessBuilder(binary.getAbsolutePath)
  proc.environment().put("GC_INITIAL_HEAP_SIZE", "1M")
  proc.environment().put("GC_MAXIMUM_HEAP_SIZE", "32M")
  proc.inheritIO()

  val running = proc.start()
  if (!running.waitFor(60, TimeUnit.SECONDS)) {
    running.destroyForcibly()
    throw new RuntimeException("512-thread burst did not finish within 60s")
  }

  val exitCode = running.exitValue()
  if (exitCode != 0)
    throw new RuntimeException(s"512-thread burst failed with exit $exitCode")
}
