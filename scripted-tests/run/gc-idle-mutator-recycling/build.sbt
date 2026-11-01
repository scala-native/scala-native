import java.util.concurrent.TimeUnit

enablePlugins(ScalaNativePlugin)

Compile / mainClass := Some("Main")

nativeConfig ~= {
  _.withGC(scala.scalanative.build.GC.immix)
    .withMultithreading(true)
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

/* Runs the binary under a hard heap cap and fails on a non-zero exit or on
 * not finishing in time. A collector that keeps collecting at the cap instead
 * of failing shows up here as the timeout.
 */
def runCapped(
    binary: java.io.File,
    maxHeap: String,
    args: Seq[String],
    log: sbt.util.Logger,
    gcLog: Option[java.io.File] = None
): Unit = {
  val process = new ProcessBuilder((binary.getAbsolutePath +: args): _*)
  process.environment().put("GC_INITIAL_HEAP_SIZE", "1M")
  process.environment().put("GC_MAXIMUM_HEAP_SIZE", maxHeap)
  gcLog.foreach { file =>
    IO.delete(file)
    process.environment().put("SCALANATIVE_GC_LOG_LEVEL", "debug")
    process.environment().put("GC_LOG_FILE", file.getAbsolutePath)
  }
  val started = System.nanoTime()
  val running = process.inheritIO().start()
  if (!running.waitFor(60, TimeUnit.SECONDS)) {
    running.destroyForcibly()
    running.waitFor(5, TimeUnit.SECONDS)
    sys.error(s"${args.mkString(" ")} at $maxHeap did not finish within 60 s")
  }
  if (running.exitValue() != 0)
    sys.error(
      s"${args.mkString(" ")} at $maxHeap exited ${running.exitValue()}"
    )
  val elapsed = (System.nanoTime() - started) / 1e9
  log.info(f"${args.mkString(" ")} at $maxHeap completed in $elapsed%.2f s")
}

lazy val runIdleMutators = taskKey[Unit](
  "A list that fits a heap cap with no idle threads must fit it with 16"
)

lazy val runFragmentedExhaustion = taskKey[Unit](
  "Unbounded growth among garbage must end in OutOfMemoryError"
)

runIdleMutators := {
  implicit val conv: xsbti.FileConverter = Keys.fileConverter.value
  val binary = nativeExecutable((Compile / nativeLink).value)
  val log = streams.value.log
  // 300,000 kept nodes need about 96 MB once interleaved with garbage;
  // 128 MB leaves room. Partly free blocks handed to idle threads at each
  // collection used to raise the requirement to well past this cap.
  runCapped(binary, "128M", Seq("idle", "0", "300000"), log)
  runCapped(binary, "128M", Seq("idle", "16", "300000"), log)
}

runFragmentedExhaustion := {
  implicit val conv: xsbti.FileConverter = Keys.fileConverter.value
  val binary = nativeExecutable((Compile / nativeLink).value)
  val log = streams.value.log
  runCapped(binary, "64M", Seq("exhaust"), log)
  // Free chunks left in the large allocator count as free: 32 MB of 8-32 KiB
  // arrays fit an 80 MB cap, which needs 68 MB on this workload.
  runCapped(binary, "80M", Seq("medium", (32L * 1024 * 1024).toString), log)
  // A full heap that is still valid: 118 MB live in 128 MB. Collections free
  // little but cost little, so the overhead limit must not fire; a rule on
  // free space alone threw OutOfMemoryError here. The GC's debug log must
  // show low-yield recovery collections, so that completing proves the limit
  // was tested rather than never approached. It runs once with default
  // logging, the configuration programs use, and once with debug logging for
  // that check: writing the log during collections adds to their time.
  val fullHeap = Seq("full", (118L * 1024 * 1024).toString, "20000000")
  runCapped(binary, "128M", fullHeap, log)
  val gcLog = target.value / "full-heap-gc.log"
  runCapped(binary, "128M", fullHeap, log, Some(gcLog))
  val lowYield = IO
    .readLines(gcLog)
    .count(_.contains("Overhead limit: low-yield recovery collection"))
  if (lowYield == 0)
    sys.error("the full heap never reached a low-yield recovery collection")
  log.info(s"full heap: $lowYield low-yield recovery collections, no error")
}
