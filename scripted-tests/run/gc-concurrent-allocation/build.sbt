import java.util.concurrent.TimeUnit

enablePlugins(ScalaNativePlugin)

Compile / mainClass := Some("Main")

nativeConfig ~= { config =>
  config
    .withGC(scala.scalanative.build.GC.immix)
    .withMode(scala.scalanative.build.Mode.releaseFast)
    .withMultithreading(true)
}

scalaVersion := {
  val version = System.getProperty("scala.version")
  if (version == null)
    sys.error("Specify -Dscala.version in scriptedLaunchOpts")
  else version
}

@transient lazy val prepareAllocationBinary =
  taskKey[Unit]("Link the binary before running the timed allocation checks")

@transient lazy val allocationExecutable =
  taskKey[java.io.File]("Read the path of the prepared native binary")

@transient lazy val runAllocationRegression =
  taskKey[Unit]("Check concurrent allocation recovery within one minute")

@transient lazy val runAllocationChecks =
  taskKey[Unit]("Check allocation sizes within one minute")

prepareAllocationBinary := {
  implicit val conv: xsbti.FileConverter = Keys.fileConverter.value
  val binary = nativeExecutable((Compile / nativeLink).value)
  IO.write(target.value / "allocation-executable", binary.getAbsolutePath)
}

allocationExecutable := {
  val path = target.value / "allocation-executable"
  if (!path.isFile)
    sys.error("Run prepareAllocationBinary before the allocation checks")

  val binary = file(IO.read(path).trim)
  if (!binary.isFile)
    sys.error(s"Prepared native binary does not exist: $binary")

  binary
}

runAllocationRegression := {
  runScenarios(
    allocationExecutable.value,
    Seq("plateau", "stress", "idle", "mixed"),
    streams.value.log
  )
}

runAllocationChecks := {
  runScenarios(allocationExecutable.value, Seq("mixed"), streams.value.log)
}

def nativeExecutable(
    output: Any
)(implicit conv: xsbti.FileConverter): java.io.File =
  output match {
    case file: java.io.File        => file
    case ref: xsbti.VirtualFileRef => conv.toPath(ref).toFile()
  }

def runScenarios(
    binary: java.io.File,
    scenarios: Seq[String],
    log: sbt.util.Logger
): Unit = {
  val started = System.nanoTime()
  val deadline = started + TimeUnit.SECONDS.toNanos(50)

  scenarios.foreach { scenario =>
    val process = new ProcessBuilder(binary.getAbsolutePath, scenario)
    val environment = process.environment()
    val plateau = scenario == "plateau"

    environment.put("GC_INITIAL_HEAP_SIZE", if (plateau) "256M" else "1M")
    environment.put("GC_MAXIMUM_HEAP_SIZE", if (plateau) "2G" else "512M")
    // Isolate allocation recovery from Commix's time-based growth policy.
    environment.put("GC_TIME_RATIO", "1.0")

    val remaining = deadline - System.nanoTime()
    if (remaining <= 0)
      sys.error("Allocation checks exceeded their shared 50-second budget")

    val scenarioStarted = System.nanoTime()
    val running = process.inheritIO().start()
    if (!running.waitFor(remaining, TimeUnit.NANOSECONDS)) {
      running.destroyForcibly()
      running.waitFor(5, TimeUnit.SECONDS)
      sys.error(s"GC $scenario exceeded the shared 50-second budget")
    }
    if (running.exitValue() != 0)
      sys.error(s"GC $scenario failed: ${running.exitValue()}")

    val elapsed = (System.nanoTime() - scenarioStarted) / 1e9
    log.info(f"Allocation scenario $scenario completed in $elapsed%.2f s")
  }

  val elapsed = (System.nanoTime() - started) / 1e9
  log.info(f"Allocation checks completed in $elapsed%.2f s (budget: 50 s)")
}
