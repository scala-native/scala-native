package scala.scalanative.build

import java.nio.file.Paths

import scala.concurrent.duration._

import org.junit.Assert._
import org.junit.Test

import scala.scalanative.build.GarbageCollectorConfig.{LogLevel, Settings}
import scala.scalanative.build.MemorySize.syntax._

class GarbageCollectorConfigTest {

  private def resolve(
      gc: GC,
      multithreading: Boolean = true,
      env: Map[String, String] = Map.empty,
      compileOptions: Seq[String] = Nil
  )(update: GarbageCollectorConfig => GarbageCollectorConfig) =
    Settings.resolve(
      NativeConfig.empty
        .withGC(gc)
        .withMultithreading(multithreading)
        .withCompileOptions(compileOptions)
        .withGarbageCollectorConfig(update),
      env.get(_)
    )

  private def assertValid(resolved: Settings.Resolved): Unit = {
    assertEquals(Nil, resolved.issues)
    assertEquals(Nil, resolved.warnings)
  }

  private def assertIssue(resolved: Settings.Resolved, fragment: String) =
    assertTrue(
      s"expected an issue containing '$fragment' in ${resolved.issues}",
      resolved.issues.exists(_.contains(fragment))
    )

  private def assertWarning(resolved: Settings.Resolved, fragment: String) = {
    assertEquals(Nil, resolved.issues)
    assertTrue(
      s"expected a warning containing '$fragment' in ${resolved.warnings}",
      resolved.warnings.exists(_.contains(fragment))
    )
  }

  private val allGCs = Seq(GC.immix, GC.commix, GC.boehm, GC.none)

  // MemorySize

  @Test def memorySizeUnits(): Unit = {
    assertEquals(1024L, 1.kb.bytes)
    assertEquals(128L * 1024 * 1024, 128.mb.bytes)
    assertEquals(4L * 1024 * 1024 * 1024, 4.gb.bytes)
    assertEquals(MemorySize.bytes(2048), 2.kb)
    assertEquals(MemorySize.mb(1024), 1.gb)
    assertTrue(1.mb < 1.gb)
  }

  @Test def memorySizeParse(): Unit = {
    assertEquals(MemorySize.bytes(5242880), MemorySize.parse("5242880"))
    assertEquals(5.mb, MemorySize.parse("5m"))
    assertEquals(5.mb, MemorySize.parse("5M"))
    assertEquals(64.kb, MemorySize.parse("64k"))
    assertEquals(2.gb, MemorySize.parse("2G"))
    for (invalid <- Seq(
          "",
          "abc",
          "G",
          "1oopsG",
          "-1",
          "1 m",
          "1mb",
          "99999999999999999999",
          "9223372036854775807k"
        ))
      assertThrows(
        invalid,
        classOf[IllegalArgumentException],
        () => MemorySize.parse(invalid)
      )
  }

  @Test def memorySizeToStringRoundTrips(): Unit =
    for (size <- Seq(0.bytes, 1.bytes, 1500.bytes, 64.kb, 128.mb, 3.gb)) {
      assertEquals(size, MemorySize.parse(size.toString))
    }

  @Test def memorySizeRejectsNegativeAndOverflow(): Unit = {
    assertThrows(classOf[IllegalArgumentException], () => MemorySize.bytes(-1))
    assertThrows(classOf[IllegalArgumentException], () => (-1).mb)
    assertThrows(
      classOf[IllegalArgumentException],
      () => MemorySize.gb(Long.MaxValue)
    )
  }

  // Defaults

  @Test def defaultsAreValidForEveryGC(): Unit =
    for {
      gc <- allGCs :+ GC.experimental
      multithreading <- Seq(true, false)
    } assertValid(resolve(gc, multithreading)(identity))

  @Test def defaultsPerGC(): Unit = {
    def defines(gc: GC, multithreading: Boolean = true) =
      resolve(gc, multithreading)(identity).defines.toSet

    assertEquals(
      Set(
        "SCALANATIVE_GC_CONFIG_TIME_RATIO=0.05",
        "SCALANATIVE_GC_CONFIG_MAX_FREE_RATIO=0.8",
        "SCALANATIVE_GC_CONFIG_SYNC_TIMEOUT_MS=60000ULL",
        "SCALANATIVE_GC_CONFIG_SYNC_WARNING_INTERVAL_MS=10000ULL",
        "SCALANATIVE_GC_CONFIG_LOG_LEVEL=GC_LOG_LEVEL_WARN",
        "SCALANATIVE_GC_CONFIG_STATS_ENABLED=0"
      ),
      defines(GC.immix)
    )
    assertEquals(
      defines(GC.immix) + "SCALANATIVE_GC_CONFIG_FREE_RATIO=0.5",
      defines(GC.commix)
    )
    assertEquals(
      Set("SCALANATIVE_GC_CONFIG_LOG_LEVEL=GC_LOG_LEVEL_WARN"),
      defines(GC.boehm)
    )
    assertEquals(
      Set(
        "SCALANATIVE_GC_CONFIG_LOG_LEVEL=GC_LOG_LEVEL_WARN",
        "SCALANATIVE_GC_CONFIG_THREAD_HEAP_BLOCK_SIZE=67108864ULL"
      ),
      defines(GC.none)
    )
    assertEquals(
      Set("SCALANATIVE_GC_CONFIG_LOG_LEVEL=GC_LOG_LEVEL_WARN"),
      defines(GC.none, multithreading = false)
    )
  }

  // Explicit settings

  @Test def explicitSettingsAreEncoded(): Unit = {
    val resolved = resolve(GC.commix)(
      _.withInitialHeapSize(128.mb)
        .withMaxHeapSize(1.gb)
        .withThreads(4)
        .withTimeRatio(0.1)
        .withFreeRatio(0.25)
        .withMaxFreeRatio(1.0)
        .withSyncTimeout(Duration.Zero)
        .withSyncWarningInterval(2.seconds)
        .withLogLevel(LogLevel.Off)
        .withLogFile(Paths.get("gc.log"))
        .withStatsEnabled(true)
        .withStatsFile(Paths.get("a\"b"))
    )
    assertValid(resolved)
    assertEquals(
      Seq(
        "SCALANATIVE_GC_CONFIG_INITIAL_HEAP_SIZE=134217728ULL",
        "SCALANATIVE_GC_CONFIG_MAX_HEAP_SIZE=1073741824ULL",
        "SCALANATIVE_GC_CONFIG_THREADS=4",
        "SCALANATIVE_GC_CONFIG_TIME_RATIO=0.1",
        "SCALANATIVE_GC_CONFIG_FREE_RATIO=0.25",
        "SCALANATIVE_GC_CONFIG_MAX_FREE_RATIO=1.0",
        "SCALANATIVE_GC_CONFIG_SYNC_TIMEOUT_MS=0ULL",
        "SCALANATIVE_GC_CONFIG_SYNC_WARNING_INTERVAL_MS=2000ULL",
        "SCALANATIVE_GC_CONFIG_LOG_LEVEL=GC_LOG_LEVEL_NONE",
        "SCALANATIVE_GC_CONFIG_LOG_FILE={103,99,46,108,111,103,0}",
        "SCALANATIVE_GC_CONFIG_STATS_FILE={97,34,98,0}",
        "SCALANATIVE_GC_CONFIG_STATS_ENABLED=1"
      ),
      resolved.defines
    )
  }

  @Test def unsupportedSettingFailsTheBuild(): Unit = {
    assertIssue(
      resolve(GC.immix)(_.withThreads(2)),
      "threads is not supported by immix GC"
    )
    assertIssue(
      resolve(GC.immix)(_.withFreeRatio(0.5)),
      "freeRatio is not supported by immix GC"
    )
    assertIssue(
      resolve(GC.none)(_.withStatsEnabled(true)),
      "statsEnabled is not supported by none GC"
    )
    assertIssue(
      resolve(GC.commix)(_.withThreadHeapBlockSize(1.mb)),
      "threadHeapBlockSize is not supported by commix GC"
    )
    for {
      update <- Seq[GarbageCollectorConfig => GarbageCollectorConfig](
        _.withTimeRatio(0.1),
        _.withSyncTimeout(1.second),
        _.withSyncWarningInterval(1.second),
        _.withLogFile(Paths.get("gc.log")),
        _.withStatsFile(Paths.get("stats.csv"))
      )
    } assertIssue(resolve(GC.boehm)(update), "is not supported by boehm GC")
    assertIssue(
      resolve(GC.none)(_.withMaxFreeRatio(0.5)),
      "maxFreeRatio is not supported by none GC"
    )
  }

  @Test def experimentalGCAcceptsEverySetting(): Unit =
    assertValid(
      resolve(GC.experimental)(
        _.withThreads(2).withThreadHeapBlockSize(1.mb).withFreeRatio(0.1)
      )
    )

  @Test def heapSizes(): Unit = {
    for (gc <- Seq(GC.immix, GC.commix, GC.boehm)) {
      assertValid(
        resolve(gc)(_.withInitialHeapSize(1.gb).withMaxHeapSize(1.gb))
      )
      assertIssue(
        resolve(gc)(_.withInitialHeapSize(2.gb).withMaxHeapSize(1.gb)),
        "initialHeapSize of 2g is greater than maxHeapSize of 1g"
      )
      assertIssue(
        resolve(gc)(_.withMaxHeapSize(0.bytes)),
        "maxHeapSize has to be greater than 0"
      )
    }
  }

  @Test def heapSizesOn32BitTargets(): Unit = {
    def resolved(triple: String) = Settings.resolve(
      NativeConfig.empty
        .withTargetTriple(triple)
        .withGarbageCollectorConfig(_.withMaxHeapSize(4.gb)),
      _ => None
    )
    assertIssue(
      resolved("armv7-unknown-linux-gnueabihf"),
      "maxHeapSize of 4g does not fit the address space of a 32-bit target"
    )
    assertValid(resolved("x86_64-unknown-linux-gnu"))
  }

  @Test def noneGCHeapSettingsDependOnMultithreading(): Unit = {
    assertValid(
      resolve(GC.none, multithreading = false)(
        _.withInitialHeapSize(1.mb).withMaxHeapSize(64.mb)
      )
    )
    assertValid(
      resolve(GC.none, multithreading = true)(_.withThreadHeapBlockSize(32.mb))
    )
    assertIssue(
      resolve(GC.none, multithreading = true)(_.withMaxHeapSize(64.mb)),
      "maxHeapSize is not used by none GC with multithreading"
    )
    assertIssue(
      resolve(GC.none, multithreading = true)(_.withInitialHeapSize(1.mb)),
      "initialHeapSize is not used by none GC with multithreading"
    )
    assertIssue(
      resolve(GC.none, multithreading = false)(_.withThreadHeapBlockSize(1.mb)),
      "threadHeapBlockSize is not used by none GC without multithreading"
    )
    assertIssue(
      resolve(GC.none, multithreading = true)(_.withThreadHeapBlockSize(0.mb)),
      "threadHeapBlockSize has to be greater than 0"
    )
  }

  @Test def ratiosHaveToBeBetween0And1(): Unit = {
    for (invalid <- Seq(-0.1, 1.1, Double.NaN, Double.PositiveInfinity)) {
      assertIssue(resolve(GC.immix)(_.withTimeRatio(invalid)), "timeRatio")
      assertIssue(
        resolve(GC.immix)(_.withMaxFreeRatio(invalid)),
        "maxFreeRatio has to be a number between 0 and 1"
      )
      assertIssue(resolve(GC.commix)(_.withFreeRatio(invalid)), "freeRatio")
    }
    for (valid <- Seq(0.0, 0.5, 1.0))
      assertValid(
        resolve(GC.commix)(
          _.withTimeRatio(valid).withFreeRatio(valid).withMaxFreeRatio(valid)
        )
      )
  }

  @Test def threadsHaveToBePositive(): Unit = {
    assertValid(resolve(GC.commix)(_.withThreads(1)))
    assertIssue(
      resolve(GC.commix)(_.withThreads(0)),
      "threads has to be at least 1, not 0"
    )
    assertIssue(resolve(GC.commix)(_.withThreads(-2)), "threads")
  }

  @Test def durations(): Unit = {
    assertValid(resolve(GC.immix)(_.withSyncTimeout(Duration.Zero)))
    assertIssue(
      resolve(GC.immix)(_.withSyncTimeout(-1.second)),
      "syncTimeout cannot be negative"
    )
    assertIssue(
      resolve(GC.immix)(_.withSyncTimeout(1500.micros)),
      "syncTimeout has to be a whole number of milliseconds"
    )
    assertIssue(
      resolve(GC.immix)(_.withSyncWarningInterval(Duration.Zero)),
      "syncWarningInterval has to be greater than 0"
    )
  }

  @Test def statsFileRequiresStatsEnabled(): Unit = {
    val statsFile = Paths.get("stats.csv")
    for (gc <- Seq(GC.immix, GC.commix)) {
      assertValid(resolve(gc)(_.withStatsEnabled(true)))
      assertValid(
        resolve(gc)(_.withStatsFile(statsFile).withStatsEnabled(true))
      )
      assertIssue(
        resolve(gc)(_.withStatsFile(statsFile)),
        "statsFile requires statsEnabled"
      )
      assertIssue(
        resolve(gc)(_.withStatsFile(statsFile).withStatsEnabled(false)),
        "statsFile requires statsEnabled"
      )
    }
  }

  @Test def trapBasedYieldPoints(): Unit = {
    def traps(
        gc: GC,
        mode: Mode = Mode.debug,
        multithreading: Boolean = true,
        env: Map[String, String] = Map.empty
    )(update: GarbageCollectorConfig => GarbageCollectorConfig) = {
      val config = Config.empty.withCompilerConfig(
        NativeConfig.empty
          .withGC(gc)
          .withMode(mode)
          .withMultithreading(multithreading)
          .withGarbageCollectorConfig(update)
      )
      config.useTrapBasedGCYieldPoints
    }
    // Derived from the build mode unless set
    assertFalse(traps(GC.immix)(identity))
    assertTrue(traps(GC.immix, Mode.releaseFast)(identity))
    assertTrue(traps(GC.commix)(_.withTrapBasedYieldPoints(true)))
    assertFalse(
      traps(GC.immix, Mode.releaseFull)(_.withTrapBasedYieldPoints(false))
    )
    // Never without multithreading or with other collectors
    assertFalse(
      traps(GC.immix, Mode.releaseFast, multithreading = false)(identity)
    )
    assertFalse(traps(GC.boehm, Mode.releaseFast)(identity))

    assertValid(resolve(GC.immix)(_.withTrapBasedYieldPoints(true)))
    assertValid(
      resolve(GC.immix, multithreading = false)(
        _.withTrapBasedYieldPoints(false)
      )
    )
    assertIssue(
      resolve(GC.immix, multithreading = false)(
        _.withTrapBasedYieldPoints(true)
      ),
      "trapBasedYieldPoints requires multithreading"
    )
    assertIssue(
      resolve(GC.boehm)(_.withTrapBasedYieldPoints(true)),
      "trapBasedYieldPoints is not supported by boehm GC"
    )
    val env = Map("SCALANATIVE_GC_TRAP_BASED_YIELDPOINTS" -> "1")
    assertEquals(
      Some(true),
      resolve(GC.immix, env = env)(identity).values.trapBasedYieldPoints
    )
    assertEquals(
      Some(false),
      resolve(GC.immix, env = env)(
        _.withTrapBasedYieldPoints(false)
      ).values.trapBasedYieldPoints
    )
    // It is not a define of the C sources
    assertFalse(
      resolve(GC.immix)(_.withTrapBasedYieldPoints(true)).defines
        .exists(_.contains("YIELD"))
    )
  }

  @Test def allIssuesAreReported(): Unit = {
    val resolved = resolve(GC.immix)(
      _.withThreads(0).withTimeRatio(2).withLogFile(Paths.get(""))
    )
    assertEquals(resolved.issues.toString, 3, resolved.issues.size)
  }

  // Environment of the build

  @Test def environmentOfTheBuildIsUsedWhenNotSetExplicitly(): Unit = {
    val env = Map(
      "GC_MAXIMUM_HEAP_SIZE" -> "512m",
      "GC_TIME_RATIO" -> "0.2",
      "SCALANATIVE_GC_SYNC_TIMEOUT_MS" -> "5000",
      "SCALANATIVE_GC_LOG_LEVEL" -> "DEBUG",
      "GC_LOG_FILE" -> "gc.log"
    )
    val fromEnv = resolve(GC.immix, env = env)(identity)
    assertValid(fromEnv)
    assertEquals(Some(512.mb), fromEnv.values.maxHeapSize)
    assertEquals(Some(0.2), fromEnv.values.timeRatio)
    assertEquals(Some(5.seconds), fromEnv.values.syncTimeout)
    assertEquals(Some(LogLevel.Debug), fromEnv.values.logLevel)
    assertEquals(Some(Paths.get("gc.log")), fromEnv.values.logFile)

    val explicit = resolve(GC.immix, env = env)(
      _.withMaxHeapSize(1.gb).withLogLevel(LogLevel.Error)
    )
    assertValid(explicit)
    assertEquals(Some(1.gb), explicit.values.maxHeapSize)
    assertEquals(Some(LogLevel.Error), explicit.values.logLevel)
    assertEquals(Some(0.2), explicit.values.timeRatio)
  }

  @Test def unsupportedEnvironmentVariablesAreSkipped(): Unit = {
    val env = Map(
      "GC_NPROCS" -> "4",
      "GC_FREE_RATIO" -> "0.1",
      "GC_THREAD_HEAP_BLOCK_SIZE" -> "1m",
      "GC_LOG_FILE" -> ""
    )
    val resolved = resolve(GC.immix, env = env)(identity)
    assertValid(resolved)
    assertEquals(None, resolved.values.threads)
    assertEquals(None, resolved.values.freeRatio)
    assertEquals(None, resolved.values.threadHeapBlockSize)
    assertEquals(None, resolved.values.logFile)

    val noneGC = resolve(GC.none, env = Map("GC_MAXIMUM_HEAP_SIZE" -> "1g"))(
      identity
    )
    assertValid(noneGC)
    assertEquals(None, noneGC.values.maxHeapSize)
  }

  @Test def invalidEnvironmentVariablesAreIgnoredWithWarning(): Unit = {
    def fromEnv(gc: GC, env: (String, String)*) =
      resolve(gc, env = env.toMap)(identity)

    val size = fromEnv(GC.immix, "GC_MAXIMUM_HEAP_SIZE" -> "lots")
    assertWarning(size, "Ignoring environment variable GC_MAXIMUM_HEAP_SIZE")
    assertEquals(None, size.values.maxHeapSize)

    val ratio = fromEnv(GC.immix, "GC_TIME_RATIO" -> "7")
    assertWarning(ratio, "Ignoring environment variable GC_TIME_RATIO")
    assertEquals(Some(0.05), ratio.values.timeRatio)

    val threads = fromEnv(GC.commix, "GC_NPROCS" -> "0")
    assertWarning(threads, "Ignoring environment variable GC_NPROCS")
    assertEquals(None, threads.values.threads)

    val level = fromEnv(GC.immix, "SCALANATIVE_GC_LOG_LEVEL" -> "verbose")
    assertWarning(level, "Ignoring environment variable SCALANATIVE_GC_LOG")
    assertEquals(Some(LogLevel.Warn), level.values.logLevel)

    val stats = fromEnv(GC.commix, "GC_STATS_FILE" -> "stats.csv")
    assertWarning(stats, "statsFile requires statsEnabled")
    assertEquals(None, stats.values.statsFile)
  }

  @Test def explicitSettingConflictingWithEnvironment(): Unit = {
    val env = Map("GC_MAXIMUM_HEAP_SIZE" -> "1g")
    assertIssue(
      resolve(GC.immix, env = env)(_.withInitialHeapSize(2.gb)),
      "initialHeapSize of 2g is greater than maxHeapSize of 1g"
    )
    val initialFromEnv =
      resolve(GC.immix, env = Map("GC_INITIAL_HEAP_SIZE" -> "2g"))(
        _.withMaxHeapSize(1.gb)
      )
    assertWarning(initialFromEnv, "GC_INITIAL_HEAP_SIZE")
    assertEquals(None, initialFromEnv.values.initialHeapSize)
    assertEquals(Some(1.gb), initialFromEnv.values.maxHeapSize)
  }

  // Legacy compile options

  @Test def legacyDefinesAreUsedWhenNothingElseIsSet(): Unit = {
    val options = Seq(
      "-DGC_INITIAL_HEAP_SIZE=5m",
      "-DGC_MAXIMUM_HEAP_SIZE=6m",
      "-DGC_NPROCS=3",
      "-DGC_ENABLE_STATS"
    )
    val legacy = resolve(GC.commix, compileOptions = options)(identity)
    assertValid(legacy)
    assertEquals(Some(5.mb), legacy.values.initialHeapSize)
    assertEquals(Some(6.mb), legacy.values.maxHeapSize)
    assertEquals(Some(3), legacy.values.threads)
    assertEquals(Some(true), legacy.values.statsEnabled)

    val overridden = resolve(
      GC.commix,
      env = Map("GC_MAXIMUM_HEAP_SIZE" -> "7m"),
      compileOptions = options
    )(_.withThreads(1).withStatsEnabled(false))
    assertValid(overridden)
    assertEquals(Some(5.mb), overridden.values.initialHeapSize)
    assertEquals(Some(7.mb), overridden.values.maxHeapSize)
    assertEquals(Some(1), overridden.values.threads)
    assertEquals(Some(false), overridden.values.statsEnabled)

    val blockSize = resolve(
      GC.none,
      compileOptions = Seq("-DGC_THREAD_HEAP_BLOCK_SIZE=32m")
    )(identity)
    assertValid(blockSize)
    assertEquals(Some(32.mb), blockSize.values.threadHeapBlockSize)
  }

  // NativeConfig

  @Test def nativeConfigHoldsTheSettings(): Unit = {
    val config = NativeConfig.empty
    assertEquals(GarbageCollectorConfig.default, config.garbageCollectorConfig)
    val updated = config
      .withGarbageCollectorConfig(_.withMaxHeapSize(1.gb))
      .withGarbageCollectorConfig(_.withLogLevel(LogLevel.Info))
    assertEquals(Some(1.gb), updated.garbageCollectorConfig.maxHeapSize)
    assertEquals(Some(LogLevel.Info), updated.garbageCollectorConfig.logLevel)
    assertNotEquals(config, updated)
    assertNotEquals(config.##, updated.##)
    assertTrue(updated.toString.contains("maxHeapSize: 1g"))
    assertEquals(
      GarbageCollectorConfig.default,
      updated
        .withGarbageCollectorConfig(GarbageCollectorConfig.default)
        .garbageCollectorConfig
    )
  }

  @Test def logLevelByName(): Unit = {
    assertEquals(LogLevel.Debug, LogLevel("debug"))
    assertEquals(LogLevel.Off, LogLevel("NONE"))
    assertThrows(classOf[IllegalArgumentException], () => LogLevel("trace"))
  }
}
