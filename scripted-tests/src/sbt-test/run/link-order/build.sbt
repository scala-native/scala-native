enablePlugins(ScalaNativePlugin)

import scala.sys.process._

scalaVersion := {
  val scalaVersion = System.getProperty("scala.version")
  if (scalaVersion == null)
    throw new RuntimeException(
      """|The system property 'scala.version' is not defined.
         |Specify this property using the scriptedLaunchOpts -D.""".stripMargin)
  else scalaVersion
}

Compile / nativeLinkingOptions += s"-L${target.value.getAbsoluteFile}"

Compile / compile := {
  val log            = streams.value.log
  val cwd            = target.value
  val compileOptions = nativeCompileOptions.value
  val cpaths         = (baseDirectory.value.getAbsoluteFile * "*.c").get
  val clangPath      = nativeClang.value.toPath.toAbsolutePath.toString

  cwd.mkdirs()

  def abs(path: File): String =
    path.getAbsolutePath

  def run(command: Seq[String]): Int = {
    log.info("Running " + command.mkString(" "))

    // Use a Process() idiom that works with both sbt 0.13.n & 1.n.
    val processLog =
      ProcessLogger(line => log.info(line), line => log.error(line))

    scala.sys.process.Process(command, cwd) ! processLog
  }

  val opaths = cpaths.map { cpath =>
    val opath = abs(cwd / s"${cpath.getName}.o")
    val command = Seq(clangPath) ++ compileOptions ++
      Seq("-c", abs(cpath), "-o", opath)

    if (run(command) != 0) {
      sys.error(s"Failed to compile $cpath")
    }
    opath
  }

  val archivePath = cwd / "liblink-order-test.a"
  val archive     = Seq("ar", "cr", abs(archivePath)) ++ opaths
  if (run(archive) != 0) {
    sys.error(s"Failed to create archive $archivePath")
  }

  (Compile / compile).value
}
