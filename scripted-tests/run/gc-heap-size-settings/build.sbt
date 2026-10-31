import scala.sys.process._

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
  "Check that empty or non-numeric heap sizes fall back to the defaults"
)

checkInvalidHeapSizes := {
  implicit val conv: xsbti.FileConverter = Keys.fileConverter.value
  val binary = nativeExecutable((Compile / nativeLink).value).getAbsolutePath
  val log = streams.value.log

  def heapSizes(env: (String, String)*): String =
    Process(Seq(binary), None, env: _*).!!.trim

  val defaults = heapSizes()
  log.info(s"defaults: $defaults")
  for {
    name <- Seq("GC_INITIAL_HEAP_SIZE", "GC_MAXIMUM_HEAP_SIZE")
    value <- Seq("", "abc", "G")
  } {
    val sizes = heapSizes(name -> value)
    if (sizes != defaults)
      throw new RuntimeException(
        s"""$name="$value" gave '$sizes', expected the defaults '$defaults'"""
      )
  }
}
