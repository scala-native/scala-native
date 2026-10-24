import java.util.concurrent.TimeUnit

enablePlugins(ScalaNativePlugin)
Compile / mainClass := Some("Main")
nativeConfig ~= { config =>
  config
    .withGC(scala.scalanative.build.GC.immix)
    .withMode(scala.scalanative.build.Mode.releaseFast)
    .withMultithreading(true)
    .withCompileOptions(config.compileOptions :+ "-DSCALANATIVE_GC_TESTING")
}
scalaVersion := {
  val version = System.getProperty("scala.version")
  if (version == null)
    sys.error("Specify -Dscala.version in scriptedLaunchOpts")
  else version
}

def nativeExecutable(
    output: Any
)(implicit conv: xsbti.FileConverter): java.io.File =
  output match {
    case file: java.io.File        => file
    case ref: xsbti.VirtualFileRef => conv.toPath(ref).toFile()
  }

def runRegression(binary: java.io.File, scenario: String): Unit = {
  val process = new ProcessBuilder(binary.getAbsolutePath, scenario)
  process.environment().put("GC_INITIAL_HEAP_SIZE", "1M")
  process
    .environment()
    .put("GC_MAXIMUM_HEAP_SIZE", if (scenario == "checks") "64M" else "512M")
  process.inheritIO()
  val running = process.start()
  if (!running.waitFor(120, TimeUnit.SECONDS)) {
    running.destroyForcibly()
    sys.error(s"Immix $scenario timed out")
  }
  if (running.exitValue() != 0)
    sys.error(s"Immix $scenario failed: ${running.exitValue()}")
}
lazy val runAllocationRegression =
  taskKey[Unit]("Check Immix concurrent allocation recovery")
lazy val runAllocationChecks =
  taskKey[Unit]("Check Immix reservation and reuse invariants")
runAllocationRegression := {
  implicit val conv: xsbti.FileConverter = Keys.fileConverter.value
  val binary = nativeExecutable((Compile / nativeLink).value)
  Seq("checks", "handoff", "stress", "idle").foreach(runRegression(binary, _))
}
runAllocationChecks := {
  implicit val conv: xsbti.FileConverter = Keys.fileConverter.value
  runRegression(nativeExecutable((Compile / nativeLink).value), "checks")
}
