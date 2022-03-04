import java.nio.file.{Path, Paths}
import scala.sys.process.Process

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

nativeConfig := {
  val prev = nativeConfig.value
  val outPath = crossTarget.value / "libtest.so"

  prev
    .withBuildTarget(scalanative.build.BuildTarget.libraryDynamic)
    .withLinkingOptions(Seq("-o", outPath.absolutePath))
}

lazy val testC = taskKey[Unit]("Build test application using SN library for C")
testC := {
  discover("clang", "CLANG_PATH").fold {
    sLog.value.info("clang not found, skipping test")
  } {
    compileAndTest(
      _,
      libPath = crossTarget.value,
      sourcePath = baseDirectory.value / "src" / "main" / "c" / "testlib.c",
      outFile = baseDirectory.value / "testC.out"
    )
  }
}

lazy val testCpp =
  taskKey[Unit]("Build test application using SN library for C++")
testCpp := {
  discover("clang++", "CLANGPP_PATH").fold {
    sLog.value.info("clang not found, skipping test")
  } {

    compileAndTest(
      _,
      libPath = crossTarget.value,
      sourcePath = baseDirectory.value / "src" / "main" / "c" / "testlib.cpp",
      outFile = baseDirectory.value / "testCpp.out"
    )
  }
}

def discover(binaryName: String, envPath: String): Option[Path] = {
  val binaryNameOrPath = sys.env.getOrElse(envPath, binaryName)
  val path = Process(s"which $binaryNameOrPath").lineStream.map { p =>
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
  val cmd: Seq[String] =
    Seq(
      clangPath.toAbsolutePath.toString,
      sourcePath.absolutePath,
      "-o",
      outFile.absolutePath,
      //"/Users/adpauls/sm/git/scorch/exec.out",
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
