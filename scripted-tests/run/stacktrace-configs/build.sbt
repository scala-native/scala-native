import scala.scalanative.build._

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

/** sbt 1: link output is a [[java.io.File]]; sbt 2: virtual file ref — resolve
 *  with [[xsbti.FileConverter]].
 */
def nativeExecutable(
    linkOutput: Any
)(implicit conv: xsbti.FileConverter): java.io.File =
  linkOutput match {
    case f: java.io.File           => f
    case ref: xsbti.VirtualFileRef => conv.toPath(ref).toFile()
  }

// Stack trace test runner - runs the compiled binary and checks exit code
lazy val runStackTraceTest =
  taskKey[Unit]("Run stack trace test with current configuration")
runStackTraceTest := {
  import scala.sys.process._
  implicit val conv: xsbti.FileConverter = Keys.fileConverter.value

  val log = streams.value.log
  val config = nativeConfig.value

  log.info(s"Running stack trace test:")
  log.info(s"  mode: ${config.mode}")
  log.info(s"  sourceLevelDebugging: ${config.sourceLevelDebuggingConfig}")

  val bin = (Compile / nativeLink).value
  val result = Process(nativeExecutable(bin).getAbsolutePath).!

  if (result != 0) {
    throw new RuntimeException(
      s"Stack trace test failed with exit code $result"
    )
  }
  log.info("Stack trace test passed!")
}
