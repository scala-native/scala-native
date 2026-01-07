import java.util.Locale
import java.util.concurrent.TimeUnit

val osName = System
  .getProperty("os.name", "unknown")
  .toLowerCase(Locale.ROOT)
val isWindows = osName.startsWith("windows")

scalaVersion := {
  val scalaVersion = System.getProperty("scala.version")
  if (scalaVersion == null)
    throw new RuntimeException(
      """|The system property 'scala.version' is not defined.
         |Specify this property using the scriptedLaunchOpts -D.""".stripMargin
    )
  else scalaVersion
}

enablePlugins(ScalaNativePlugin)

// Base configuration - multithreading required
nativeConfig ~= { _.withMultithreading(true) }

// =============================================================================
// Test Helpers
// =============================================================================
def runScenario(binary: java.io.File, gc: scala.scalanative.build.GC)(
    scenario: String,
    timeoutSeconds: Int,
    expectNonZeroExit: Boolean = false,
    envVars: Map[String, String] = Map.empty
): Unit = {
  println(s"Running scenario: $scenario, GC=${gc}")

  val pb = new ProcessBuilder(binary.getAbsolutePath, scenario)

  // Default env vars for faster tests
  val defaultEnv = Map(
    "SCALANATIVE_GC_SYNC_TIMEOUT_MS" -> "3000",
    "SCALANATIVE_GC_SYNC_WARNING_INTERVAL_MS" -> "1000"
  )
  val allEnv = defaultEnv ++ envVars
  allEnv.foreach { case (k, v) => pb.environment().put(k, v) }

  pb.inheritIO()
  val proc = pb.start()

  val finished = proc.waitFor(timeoutSeconds, TimeUnit.SECONDS)
  if (!finished) {
    proc.destroyForcibly()
    throw new RuntimeException(
      s"FAIL: $scenario did not complete within $timeoutSeconds seconds"
    )
  }

  val exitCode = proc.exitValue()

  if (expectNonZeroExit) {
    // We expect the binary to abort() after detecting stuck thread
    println(s"$scenario exited with code: $exitCode (expected non-zero)")
  } else {
    if (exitCode != 0) {
      throw new RuntimeException(s"FAIL: $scenario exited with code $exitCode")
    }
  }

  println(s"$scenario PASSED")
}

// =============================================================================
// Individual Test Tasks
// =============================================================================

/** Test: GC works correctly with @blocking annotation (baseline) */
lazy val testCorrect = taskKey[Unit]("Test correct @blocking behavior")
testCorrect := runScenario(
  binary = (Compile / nativeLink).value,
  gc = (Compile / nativeConfig).value.gc
)("correct", timeoutSeconds = 10)

/** Test: GC timeout detection when thread blocks without @blocking */
lazy val testDeadlock = taskKey[Unit]("Test deadlock detection with timeout")
testDeadlock := runScenario(
  binary = (Compile / nativeLink).value,
  gc = (Compile / nativeConfig).value.gc
)("deadlock", timeoutSeconds = 20, expectNonZeroExit = true)

/** Test: GC timeout detection with pause() syscall - POSIX only */
lazy val testPause = taskKey[Unit]("Test pause() deadlock detection")
testPause := {
  if (isWindows)
    System.err.println("Skipping pause test on Windows - pause() is POSIX-only")
  else
    runScenario(
      binary = (Compile / nativeLink).value,
      gc = (Compile / nativeConfig).value.gc
    )("pause", timeoutSeconds = 20, expectNonZeroExit = true)
}

/** Test: GC detects zombie threads (threads that exited without cleanup) -
 *  POSIX only
 */
lazy val testZombie = taskKey[Unit]("Test zombie thread detection")
testZombie := {
  if (isWindows)
    System.err.println(
      "Skipping zombie test on Windows - uses POSIX pthread APIs"
    )
  else
    // Zombie test may abort or complete depending on timing
    runScenario(
      binary = (Compile / nativeLink).value,
      gc = (Compile / nativeConfig).value.gc
    )("zombie", timeoutSeconds = 20, expectNonZeroExit = true)
}

/** Test: GC handles multiple stuck threads */
lazy val testAllocator = taskKey[Unit]("Test multiple blocking threads")
testAllocator := runScenario(
  binary = (Compile / nativeLink).value,
  gc = (Compile / nativeConfig).value.gc
)("allocator", timeoutSeconds = 20, expectNonZeroExit = true)

/** Run all tests with current GC */
lazy val testAll = taskKey[Unit]("Run all GC deadlock detection tests")
testAll := Def
  .sequential(
    testCorrect,
    testDeadlock,
    testPause,
    testZombie,
    testAllocator
  )
  .value
