enablePlugins(ScalaNativePlugin)

import scala.sys.process._

scalaVersion := {
  val scalaVersion = System.getProperty("scala.version")
  if (scalaVersion == null)
    throw new RuntimeException(
      """|The system property 'scala.version' is not defined.
         |Specify this property using the scriptedLaunchOpts -D.""".stripMargin
    )
  else scalaVersion
}

Compile / nativeConfig := {
  val nc = nativeConfig.value
  nc.withLinkingOptions(
    nc.linkingOptions ++ Seq(s"-L${target.value.getAbsoluteFile}")
  )
}

Compile / compile := {
  val log = streams.value.log
  val cwd = target.value
  val nc = nativeConfig.value
  val compileOptions = nc.compileOptions
  val cpaths = (baseDirectory.value.getAbsoluteFile * "*.c").get
  val clangPath = nc.clang.toAbsolutePath.toString

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

  val isWindows = System
    .getProperty("os.name", "unknown")
    .toLowerCase(Locale.ROOT)
    .startsWith("windows")
  val libName =
    if (isWindows) "link-order-test.lib"
    else "liblink-order-test.a"

  val archivePath = cwd / libName
  // Windows does not have ar binary, but llvm toolchain provides llvm-ar
  // On MacOS llvm-ar might not be defined in path by default
  val archiveBin = if (isWindows) "llvm-ar" else "ar"
  val archive = Seq(archiveBin, "cr", abs(archivePath)) ++ opaths
  if (run(archive) != 0) {
    sys.error(s"Failed to create archive $archivePath")
  }

  (Compile / compile).value
}
