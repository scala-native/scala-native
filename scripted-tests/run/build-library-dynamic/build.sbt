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
  _.withBuildTarget(scalanative.build.BuildTarget.libraryDynamic)
    .withMode(scalanative.build.Mode.releaseFast)
    .withBaseName("test")
}

val outExt = if (Platform.isWindows) "exe" else "out"
lazy val testC = taskKey[Unit]("Build test application using SN library for C")
testC := {
  sLog.value.info("Testing dynamic library from C")
  compileAndTest(
    Discover.clang(),
    libPath = crossTarget.value,
    sourcePath = baseDirectory.value / "src" / "main" / "c" / "testlib.c",
    outFile = baseDirectory.value / s"testC.$outExt"
  )
}

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

def compileAndTest(
    clangPath: Path,
    libPath: File,
    sourcePath: File,
    outFile: File
): Unit = {
  val cmd: Seq[String] =
    Seq(
      clangPath.toAbsolutePath.toString,
      sourcePath.absolutePath,
      "-o",
      outFile.absolutePath,
      s"-L${libPath.absolutePath}",
      "-ltest"
    )

  val ldPath = sys.env
    .get("LD_LIBRARY_PATH")
    .fold(libPath.absolutePath) { prev => s"${libPath.absolutePath}:$prev" }

  val res = Process(cmd, libPath).!
  assert(res == 0, "failed to compile")
  assert(outFile.setExecutable(true), "cannot add +x permission")

  val testRes =
    Process(outFile.absolutePath, libPath, ("LD_LIBRARY_PATH", ldPath)).!

  assert(testRes == 0, s"tests in ${outFile} failed with code ${testRes}")
}
