import scala.sys.process.Process

import scala.scalanative.build.{BuildTarget, Discover, LTO, Mode}

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

val osName = System.getProperty("os.name").toLowerCase
val isMac = osName.startsWith("mac")
val isWindows = osName.startsWith("windows")

// Regression: LTO with multithreading made libraries crash when loaded with dlopen
nativeConfig ~= {
  _.withBuildTarget(BuildTarget.libraryDynamic)
    .withMode(Mode.releaseFast)
    .withMultithreading(true)
    .withLTO(if (isMac) LTO.full else LTO.thin)
}

lazy val testDlopen = taskKey[Unit]("Load the library with dlopen from C")
testDlopen := Def.taskDyn {
  if (isWindows) Def.task(streams.value.log.info("No dlopen on Windows"))
  else testDlopenUnix
}.value

lazy val testDlopenUnix = Def.task {
  val conv = Keys.fileConverter.value
  // sbt 1 links to a File, sbt 2 to a virtual file
  val library = ((Compile / nativeLink).value: Any) match {
    case f: File                   => f
    case ref: xsbti.VirtualFileRef => conv.toPath(ref).toFile
  }
  val host = target.value / "dlopen.out"
  val main = baseDirectory.value / "src" / "main" / "c" / "main.c"
  val compile =
    Seq(Discover.clang().toString, main.getPath, "-o", host.getPath, "-ldl")
  assert(Process(compile).! == 0, "failed to compile")
  val res = Process(Seq(host.getPath, library.getAbsolutePath)).!
  assert(res == 0, s"$host failed with code $res")
}
