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

// Stack trace test runner - runs the compiled binary and checks exit code
lazy val runStackTraceTest =
  taskKey[Unit]("Run stack trace test with current configuration")
runStackTraceTest := {
  import scala.sys.process._

  val log = streams.value.log
  val config = nativeConfig.value

  log.info(s"Running stack trace test:")
  log.info(s"  mode: ${config.mode}")
  log.info(s"  sourceLevelDebugging: ${config.sourceLevelDebuggingConfig}")

  val bin = (Compile / nativeLink).value
  val result = Process(bin.getAbsolutePath).!

  if (result != 0) {
    throw new RuntimeException(
      s"Stack trace test failed with exit code $result"
    )
  }
  log.info("Stack trace test passed!")
}
