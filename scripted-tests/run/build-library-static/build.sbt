import java.nio.file.{Path, Paths}
import scala.sys.process.Process
import scala.scalanative.build.Discover

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

nativeConfig ~= {
  _.withBuildTarget(scalanative.build.BuildTarget.libraryStatic)
    .withMode(scalanative.build.Mode.releaseFast)
    .withBaseName("test")
}

val outExt = if (Platform.isWindows) "exe" else "out"

// Cannot build program written in C using static library produced by Scala Native
// Linking would fail with missing __cxa_* symbols

lazy val testCpp =
  taskKey[Unit]("Build test application using SN library for C++")
testCpp := {
  sLog.value.info("Testing dynamic library from C++")
  compileAndTest(
    Discover.clangpp(),
    libPath = crossTarget.value,
    sourcePath = baseDirectory.value / "src" / "main" / "c" / "testlib.cpp",
    outFile = baseDirectory.value / s"testCpp.$outExt"
  )
}

def discover(binaryName: String, envPath: String): Option[Path] = {
  val binaryNameOrPath = sys.env.getOrElse(envPath, binaryName)
  val which = if (Platform.isWindows) "where" else "which"
  val path = Process(s"$which $binaryNameOrPath").lineStream.map { p =>
    Paths.get(p)
  }.headOption
  path
}

def compileAndTest(
    clangPath: Path,
    libPath: File,
    sourcePath: File,
    outFile: File
): Unit = {
  val platformLibs =
    if (Platform.isWindows) Seq("Advapi32", "Userenv", "Dbghelp")
    else Seq("pthread", "dl")
  val cmd: Seq[String] =
    Seq(
      clangPath.toAbsolutePath.toString,
      sourcePath.absolutePath,
      "-o",
      outFile.absolutePath,
      s"-L${libPath.absolutePath}",
      "-ltest"
    ) ++ platformLibs.map("-l" + _)

  val res = Process(cmd, libPath).!
  assert(res == 0, "failed to compile")
  assert(outFile.setExecutable(true), "cannot add +x permission")

  val testRes = Process(outFile.absolutePath, libPath).!

  assert(testRes == 0, s"tests in ${outFile} failed with code ${testRes}")
}
