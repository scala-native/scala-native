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
  "<maxHeap> [scenario]: run the binary and fail if it hangs or fails"
)

runWithMaxHeap := {
  val args = Def.spaceDelimited().parsed
  implicit val conv: xsbti.FileConverter = Keys.fileConverter.value
  val binary = nativeExecutable((Compile / nativeLink).value)
  val proc = new ProcessBuilder(binary.getAbsolutePath)
  args.tail.foreach(arg => proc.command().add(arg))
  proc.environment().put("GC_INITIAL_HEAP_SIZE", "4M")
  // Caps a runaway grow, which would otherwise reserve physical RAM. The
  // grey packet pool is also sized from it.
  proc.environment().put("GC_MAXIMUM_HEAP_SIZE", args.head)
  proc.inheritIO()

  val running = proc.start()
  if (!running.waitFor(60, TimeUnit.SECONDS)) {
    running.destroyForcibly()
    throw new RuntimeException(
      s"${args.mkString(" ")} did not finish within 60s"
    )
  }

  val exitCode = running.exitValue()
  if (exitCode != 0)
    throw new RuntimeException(
      s"${args.mkString(" ")} exited with $exitCode"
    )
}
