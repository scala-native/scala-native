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

def nativeExecutable(
    linkOutput: Any
)(implicit conv: xsbti.FileConverter): java.io.File =
  linkOutput match {
    case f: java.io.File           => f
    case ref: xsbti.VirtualFileRef => conv.toPath(ref).toFile()
  }

lazy val checkInvalidHeapSizes = taskKey[Unit](
  "Check that invalid heap sizes are reported and do not stop the program"
)

checkInvalidHeapSizes := {
  implicit val conv: xsbti.FileConverter = Keys.fileConverter.value
  val binary = nativeExecutable((Compile / nativeLink).value).getAbsolutePath
  val heapSizeVars = Seq("GC_INITIAL_HEAP_SIZE", "GC_MAXIMUM_HEAP_SIZE")
  for {
    name <- heapSizeVars
    value <- Seq("", "abc", "G", "99999999999999999999")
  } {
    val proc = new java.lang.ProcessBuilder(binary)
    // Start from a clean slate, CI may export GC_MAXIMUM_HEAP_SIZE.
    heapSizeVars.foreach(proc.environment().remove(_))
    proc.environment().remove("SCALANATIVE_GC_LOG_LEVEL")
    proc.environment().put(name, value)
    proc.redirectErrorStream(true)
    val running = proc.start()
    val output =
      scala.io.Source.fromInputStream(running.getInputStream).mkString
    val exitCode = running.waitFor()
    val warning = s"Ignoring $name=$value,"
    if (exitCode != 0 || !output.contains(warning))
      throw new RuntimeException(
        s"""$name="$value": expected exit 0 and a warning containing '$warning', got exit $exitCode and output:\n$output"""
      )
  }
}
