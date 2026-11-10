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

lazy val runWithMaxHeap = inputKey[Unit](
  "<maxHeap>: run the binary and fail if it hangs or exits with an error"
)

runWithMaxHeap := {
  val args = Def.spaceDelimited().parsed
  implicit val conv: xsbti.FileConverter = Keys.fileConverter.value
  val binary = nativeExecutable((Compile / nativeLink).value)
  val proc = new ProcessBuilder(binary.getAbsolutePath)
  // The grey packet pool is a fixed fraction of the maximum heap size.
  proc.environment().put("GC_MAXIMUM_HEAP_SIZE", args.head)
  proc.inheritIO()

  val running = proc.start()
  if (!running.waitFor(120, TimeUnit.SECONDS)) {
    running.destroyForcibly()
    throw new RuntimeException("grey packet overflow test hung")
  }

  val exitCode = running.exitValue()
  if (exitCode != 0)
    throw new RuntimeException(
      s"grey packet overflow test exited with $exitCode"
    )
}
