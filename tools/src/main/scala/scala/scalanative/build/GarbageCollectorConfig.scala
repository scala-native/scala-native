package scala.scalanative.build

import java.nio.charset.StandardCharsets
import java.nio.file.{Path, Paths}

import scala.concurrent.duration._

/** Settings of the garbage collector selected with [[NativeConfig.withGC]].
 *
 *  Every setting is optional. A setting that is not defined here is taken from
 *  the environment variable of the same name in the environment of the build,
 *  then from a legacy `-D` compile option, then from the default listed below.
 *  The resulting value is built into the program, where the same environment
 *  variable can still override it when the program starts.
 *
 *  A setting that is defined here but is not supported by the selected garbage
 *  collector, or has an invalid value, fails the build.
 */
sealed trait GarbageCollectorConfig {
  import GarbageCollectorConfig.LogLevel

  /** Initial size of the heap.
   *
   *  Environment variable: `GC_INITIAL_HEAP_SIZE`
   *
   *  Supported by: Immix, Commix, Boehm, None without multithreading
   *
   *  Default: chosen by the garbage collector
   */
  def initialHeapSize: Option[MemorySize]

  /** Maximal size of the heap.
   *
   *  Environment variable: `GC_MAXIMUM_HEAP_SIZE`
   *
   *  Supported by: Immix, Commix, Boehm, None without multithreading
   *
   *  Default: the size of the physical memory
   */
  def maxHeapSize: Option[MemorySize]

  /** Size of the memory blocks each thread allocates from. A thread maps a new
   *  block when the current one is full, so the size has to be greater than the
   *  largest object or array the program allocates: a larger allocation never
   *  fits and ends with an OutOfMemoryError. Larger blocks reserve more virtual
   *  memory per thread. Sizes above the physical memory are reduced to it when
   *  the program starts.
   *
   *  Environment variable: `GC_THREAD_HEAP_BLOCK_SIZE`
   *
   *  Supported by: None with multithreading
   *
   *  Default: 64 MB
   */
  def threadHeapBlockSize: Option[MemorySize]

  /** Number of garbage collector threads. The value built into the program is
   *  capped at the number of processors - 1 when the program starts, the one
   *  from the environment variable is not.
   *
   *  Environment variable: `GC_NPROCS`
   *
   *  Supported by: Commix
   *
   *  Default: number of processors - 1, at most 8
   */
  def threads: Option[Int]

  /** Fraction of the run time, between 0 and 1, that collections may take
   *  before the heap grows.
   *
   *  Environment variable: `GC_TIME_RATIO`
   *
   *  Supported by: Immix, Commix
   *
   *  Default: 0.05
   */
  def timeRatio: Option[Double]

  /** Fraction of the heap, between 0 and 1, that should be free after a
   *  collection.
   *
   *  Environment variable: `GC_FREE_RATIO`
   *
   *  Supported by: Commix
   *
   *  Default: 0.5
   */
  def freeRatio: Option[Double]

  /** Fraction of the heap, between 0 and 1, above which a free heap stops
   *  growing for [[timeRatio]].
   *
   *  Environment variable: `GC_MAX_FREE_RATIO`
   *
   *  Supported by: Immix, Commix
   *
   *  Default: 0.8
   */
  def maxFreeRatio: Option[Double]

  /** How long a collection waits for all threads to reach a safepoint before it
   *  aborts the program, in whole milliseconds. `Duration.Zero` waits forever.
   *
   *  Environment variable: `SCALANATIVE_GC_SYNC_TIMEOUT_MS`
   *
   *  Supported by: Immix, Commix
   *
   *  Default: 60 seconds
   */
  def syncTimeout: Option[FiniteDuration]

  /** Interval between warnings printed while waiting for threads to reach a
   *  safepoint, in whole milliseconds.
   *
   *  Environment variable: `SCALANATIVE_GC_SYNC_WARNING_INTERVAL_MS`
   *
   *  Supported by: Immix, Commix
   *
   *  Default: 10 seconds
   */
  def syncWarningInterval: Option[FiniteDuration]

  /** Verbosity of the garbage collector log.
   *
   *  Environment variable: `SCALANATIVE_GC_LOG_LEVEL`
   *
   *  Supported by: Immix, Commix, Boehm, None
   *
   *  Default: [[GarbageCollectorConfig.LogLevel.Warn]]
   */
  def logLevel: Option[LogLevel]

  /** File the garbage collector log is appended to. A relative path is resolved
   *  against the working directory of the running program.
   *
   *  Environment variable: `GC_LOG_FILE`
   *
   *  Supported by: Immix, Commix, None
   *
   *  Default: the standard error stream
   */
  def logFile: Option[Path]

  /** File the statistics of each collection are written to. A relative path is
   *  resolved against the working directory of the running program.
   *
   *  Environment variable: `GC_STATS_FILE`
   *
   *  Supported by: Immix, Commix, with [[statsEnabled]]
   *
   *  Default: no statistics
   */
  def statsFile: Option[Path]

  /** Compiles the support for [[statsFile]] into the garbage collector.
   *
   *  Environment variable: none, it cannot be changed when the program starts
   *
   *  Supported by: Immix, Commix
   *
   *  Default: false
   */
  def statsEnabled: Option[Boolean]

  /** Stops threads for a collection with a memory access trap instead of a
   *  checked flag. Traps are faster, but debuggers report them as memory access
   *  faults.
   *
   *  Environment variable: `SCALANATIVE_GC_TRAP_BASED_YIELDPOINTS` (`1` or
   *  `0`), read only when building
   *
   *  Supported by: Immix, Commix, with multithreading
   *
   *  Default: true in release modes, false in the debug mode
   */
  def trapBasedYieldPoints: Option[Boolean]

  def withInitialHeapSize(value: MemorySize): GarbageCollectorConfig
  def withMaxHeapSize(value: MemorySize): GarbageCollectorConfig
  def withThreadHeapBlockSize(value: MemorySize): GarbageCollectorConfig
  def withThreads(value: Int): GarbageCollectorConfig
  def withTimeRatio(value: Double): GarbageCollectorConfig
  def withFreeRatio(value: Double): GarbageCollectorConfig
  def withMaxFreeRatio(value: Double): GarbageCollectorConfig
  def withSyncTimeout(value: FiniteDuration): GarbageCollectorConfig
  def withSyncWarningInterval(value: FiniteDuration): GarbageCollectorConfig
  def withLogLevel(value: LogLevel): GarbageCollectorConfig
  def withLogFile(value: Path): GarbageCollectorConfig
  def withStatsFile(value: Path): GarbageCollectorConfig
  def withStatsEnabled(value: Boolean): GarbageCollectorConfig
  def withTrapBasedYieldPoints(value: Boolean): GarbageCollectorConfig

  private[scalanative] def show(indent: String): String
}

object GarbageCollectorConfig {

  /** A config without any setting defined. */
  val default: GarbageCollectorConfig = Impl()

  /** Verbosity of the garbage collector log. */
  sealed abstract class LogLevel private (val name: String) {
    override def toString: String = name
  }
  object LogLevel {
    case object Debug extends LogLevel("debug")
    case object Info extends LogLevel("info")
    case object Warn extends LogLevel("warn")
    case object Error extends LogLevel("error")

    /** Nothing is logged. */
    case object Off extends LogLevel("none")

    private[build] val all: Seq[LogLevel] = Seq(Debug, Info, Warn, Error, Off)

    /** Get a log level with given name, one of "debug", "info", "warn", "error"
     *  or "none", in either case.
     */
    def apply(name: String): LogLevel =
      all
        .find(_.name.equalsIgnoreCase(name))
        .getOrElse(
          throw new IllegalArgumentException(
            s"""GC log level can be either ${all
                .map("\"" + _ + "\"")
                .mkString(", ")}, not: $name"""
          )
        )
  }

  private final case class Impl(
      initialHeapSize: Option[MemorySize] = None,
      maxHeapSize: Option[MemorySize] = None,
      threadHeapBlockSize: Option[MemorySize] = None,
      threads: Option[Int] = None,
      timeRatio: Option[Double] = None,
      freeRatio: Option[Double] = None,
      maxFreeRatio: Option[Double] = None,
      syncTimeout: Option[FiniteDuration] = None,
      syncWarningInterval: Option[FiniteDuration] = None,
      logLevel: Option[LogLevel] = None,
      logFile: Option[Path] = None,
      statsFile: Option[Path] = None,
      statsEnabled: Option[Boolean] = None,
      trapBasedYieldPoints: Option[Boolean] = None
  ) extends GarbageCollectorConfig {
    // format: off
    def withInitialHeapSize(value: MemorySize): GarbageCollectorConfig = copy(initialHeapSize = Some(value))
    def withMaxHeapSize(value: MemorySize): GarbageCollectorConfig = copy(maxHeapSize = Some(value))
    def withThreadHeapBlockSize(value: MemorySize): GarbageCollectorConfig = copy(threadHeapBlockSize = Some(value))
    def withThreads(value: Int): GarbageCollectorConfig = copy(threads = Some(value))
    def withTimeRatio(value: Double): GarbageCollectorConfig = copy(timeRatio = Some(value))
    def withFreeRatio(value: Double): GarbageCollectorConfig = copy(freeRatio = Some(value))
    def withMaxFreeRatio(value: Double): GarbageCollectorConfig = copy(maxFreeRatio = Some(value))
    def withSyncTimeout(value: FiniteDuration): GarbageCollectorConfig = copy(syncTimeout = Some(value))
    def withSyncWarningInterval(value: FiniteDuration): GarbageCollectorConfig = copy(syncWarningInterval = Some(value))
    def withLogLevel(value: LogLevel): GarbageCollectorConfig = copy(logLevel = Some(value))
    def withLogFile(value: Path): GarbageCollectorConfig = copy(logFile = Some(value))
    def withStatsFile(value: Path): GarbageCollectorConfig = copy(statsFile = Some(value))
    def withStatsEnabled(value: Boolean): GarbageCollectorConfig = copy(statsEnabled = Some(value))
    def withTrapBasedYieldPoints(value: Boolean): GarbageCollectorConfig = copy(trapBasedYieldPoints = Some(value))
    // format: on

    override def toString: String = show("")

    override private[scalanative] def show(indent: String): String = {
      val defined = Settings.all.flatMap { setting =>
        setting.show(this).map(value => s"$indent- ${setting.name}: $value")
      }
      if (defined.isEmpty) "[]"
      else defined.mkString("\n", "\n", "")
    }
  }

  /** The single description of every setting: its name, environment variable,
   *  default, the garbage collectors supporting it, its validation and the way
   *  it is passed to the C sources of the garbage collectors.
   */
  private[scalanative] object Settings {

    /** What a value is validated against. */
    final case class Context(
        gc: GC,
        multithreading: Boolean,
        is32BitPlatform: Boolean,
        /** Values of all the settings, including these not set explicitly. */
        values: GarbageCollectorConfig
    )

    /** Where a value of a setting has been taken from. */
    sealed abstract class Source(val describe: String => String)
    object Source {
      case object Config
          extends Source(name => s"$name in GarbageCollectorConfig")
      final case class Environment(variable: String)
          extends Source(_ => s"environment variable $variable")
      final case class CompileOption(define: String)
          extends Source(_ => s"compile option -D$define")
      case object Default extends Source(name => s"default of $name")
    }

    /** Result of [[resolve]]. */
    final case class Resolved(
        gc: GC,
        values: GarbageCollectorConfig,
        /** Problems with explicit settings, these fail the build. */
        issues: Seq[String],
        /** Ignored values coming from the environment or compile options. */
        warnings: Seq[String]
    ) {

      /** Defines passed to the C sources of the garbage collector. */
      def defines: Seq[String] =
        all.flatMap(s => s.defineOf(values).toList)
    }

    private def edit(config: GarbageCollectorConfig)(
        f: Impl => Impl
    ): GarbageCollectorConfig = config match { case impl: Impl => f(impl) }

    type Check[T] = (T, Context) => Seq[String]
    private def valid[T]: Check[T] = (_, _) => Nil

    /** @param name
     *    name of the field of [[GarbageCollectorConfig]]
     *  @param envVar
     *    environment variable read when building and when the program starts
     *  @param define
     *    suffix of the `SCALANATIVE_GC_CONFIG_` define seen by the C sources,
     *    None if the build passes the setting in another way
     *  @param supportedBy
     *    garbage collectors honouring the setting, [[GC.experimental]] is
     *    assumed to honour all of them
     *  @param default
     *    the default built into the program, None if the garbage collector
     *    computes it when the program starts
     *  @param legacyDefine
     *    name of the `-D` compile option that used to set it
     *  @param applicable
     *    a reason why the setting has no effect in given context, if any, in
     *    addition to `supportedBy`. Such a setting fails the build when set
     *    explicitly and is silently skipped otherwise
     *  @param validate
     *    problems with the value, given all the other settings
     */
    final class Setting[T](
        val name: String,
        val envVar: Option[String],
        val define: Option[String],
        val supportedBy: Set[GC],
        val default: Option[T],
        val legacyDefine: Option[String] = None,
        get: GarbageCollectorConfig => Option[T],
        set: (GarbageCollectorConfig, Option[T]) => GarbageCollectorConfig,
        parse: String => Either[String, T],
        encode: T => String,
        render: T => String = (_: T).toString,
        applicable: Context => Option[String] = _ => None,
        validate: Check[T] = valid[T]
    ) {
      def show(config: GarbageCollectorConfig): Option[String] =
        get(config).map(render)

      def defineOf(config: GarbageCollectorConfig): Option[String] =
        for (name <- define; value <- get(config))
          yield s"SCALANATIVE_GC_CONFIG_$name=${encode(value)}"

      private def unsupported(ctx: Context): Option[String] =
        if (ctx.gc != GC.experimental && !supportedBy.contains(ctx.gc))
          Some(s"is not supported by ${ctx.gc} GC")
        else applicable(ctx)

      private def update(
          config: GarbageCollectorConfig,
          value: Option[T]
      ): GarbageCollectorConfig = set(config, value)

      /** Picks the value with the highest precedence that can be used. */
      private[Settings] def pick(
          state: State,
          explicit: GarbageCollectorConfig,
          env: String => Option[String],
          legacyDefines: Map[String, String]
      ): Unit = {
        val ctx = state.context
        val candidates: List[(Source, Either[String, T])] =
          get(explicit).map(v => Source.Config -> Right(v)).toList ++
            envVar.flatMap { name =>
              // An empty variable is treated as one that is not set
              env(name)
                .filter(_.nonEmpty)
                .map(v => Source.Environment(name) -> parse(v))
            } ++
            legacyDefine.flatMap { name =>
              legacyDefines
                .get(name)
                .map(v => Source.CompileOption(name) -> parse(v))
            } ++
            default.map(v => Source.Default -> Right(v))

        val picked = candidates.iterator
          .map {
            case (source, parsed) =>
              (unsupported(ctx), parsed) match {
                case (Some(reason), _) =>
                  if (source == Source.Config)
                    state.issues += s"$name $reason"
                  None
                case (_, Left(reason)) =>
                  state.warnings +=
                    s"Ignoring ${source.describe(name)}: $reason"
                  None
                case (_, Right(value)) => Some(source -> value)
              }
          }
          .collectFirst { case Some(found) => found }
        picked.foreach {
          case (source, value) =>
            state.values = update(state.values, Some(value))
            state.sources += (name -> source)
        }
      }

      /** Validates the picked value against all the picked values. A wrong
       *  value that was not set explicitly is replaced by the default.
       */
      private[Settings] def check(state: State, picked: Context): Unit =
        for {
          value <- get(picked.values)
          source <- state.sources.get(name)
          problems = validate(value, picked)
          if problems.nonEmpty
        } source match {
          case Source.Config | Source.Default =>
            state.issues ++= problems.map(p => s"$name $p")
          case _ =>
            state.warnings ++= problems.map { p =>
              s"Ignoring ${source.describe(name)}: $name $p"
            }
            state.values = update(state.values, default)
        }
    }

    private final class State(gc: GC, mt: Boolean, is32Bit: Boolean) {
      var values: GarbageCollectorConfig = GarbageCollectorConfig.default
      var sources = Map.empty[String, Source]
      val issues = List.newBuilder[String]
      val warnings = List.newBuilder[String]
      def context = Context(gc, mt, is32Bit, values)
    }

    /** Determines the value of every setting for given build.
     *
     *  @param env
     *    the environment of the build
     */
    def resolve(
        config: NativeConfig,
        env: String => Option[String]
    ): Resolved = {
      val state = new State(
        config.gc,
        config.multithreadingSupport,
        config.targetTriple.isDefined && config.is32BitPlatform
      )
      val explicit = config.garbageCollectorConfig
      val defines = legacyDefines(config.compileOptions ++ config.cOptions)
      all.foreach(_.pick(state, explicit, env, defines))
      val picked = state.context
      all.foreach(_.check(state, picked))
      Resolved(
        config.gc,
        state.values,
        state.issues.result(),
        state.warnings.result()
      )
    }

    def resolve(config: NativeConfig): Resolved =
      resolve(config, name => Option(System.getenv(name)))

    /** `-DNAME` and `-DNAME=value` among the options, the last one wins. */
    private def legacyDefines(options: Seq[String]): Map[String, String] =
      options.iterator
        .filter(_.startsWith("-D"))
        .map(_.stripPrefix("-D").split("=", 2))
        .map(kv => kv(0) -> (if (kv.length > 1) kv(1) else "1"))
        .toMap

    // Values
    private def parseSize(value: String) = MemorySize.tryParse(value)
    private def encodeSize(value: MemorySize) = s"${value.bytes}ULL"

    private def parseInt(value: String): Either[String, Int] =
      try Right(value.trim.toInt)
      catch {
        case _: NumberFormatException => Left(s"'$value' is not a number")
      }

    private def parseRatio(value: String): Either[String, Double] =
      try Right(value.trim.toDouble)
      catch {
        case _: NumberFormatException => Left(s"'$value' is not a number")
      }

    private def parseMillis(value: String): Either[String, FiniteDuration] =
      try {
        val millis = value.trim.toLong
        if (millis < 0) Left(s"'$value' is negative")
        else Right(millis.millis)
      } catch {
        case _: NumberFormatException =>
          Left(s"'$value' is not a number of milliseconds")
      }
    private def encodeMillis(value: FiniteDuration) = s"${value.toMillis}ULL"

    private def parseLogLevel(value: String): Either[String, LogLevel] =
      LogLevel.all
        .find(_.name.equalsIgnoreCase(value))
        .toRight(s"'$value' is not a log level")
    private def encodeLogLevel(value: LogLevel) =
      s"GC_LOG_LEVEL_${value.name.toUpperCase}"

    private def parsePath(value: String): Either[String, Path] =
      if (value.isEmpty) Left("it is empty")
      else
        try Right(Paths.get(value))
        catch {
          case _: java.nio.file.InvalidPathException =>
            Left(s"'$value' is not a path")
        }

    /* A brace-enclosed initializer of a char array holding the UTF-8 bytes of
     * the path. Unlike a string literal it needs no quoting or escaping on the
     * command line of any platform.
     */
    private def encodePath(value: Path) =
      (value.toString.getBytes(StandardCharsets.UTF_8) :+ 0.toByte)
        .mkString("{", ",", "}")

    // Validation
    private val heapSize: Check[MemorySize] = (size, ctx) =>
      if (ctx.is32BitPlatform && size.bytes > 0xffffffffL)
        Seq(s"of $size does not fit the address space of a 32-bit target")
      else Nil

    private val positiveSize: Check[MemorySize] = (size, ctx) =>
      if (size.bytes == 0) Seq("has to be greater than 0")
      else heapSize(size, ctx)

    private val ratio: Check[Double] = (value, _) =>
      if (value >= 0.0 && value <= 1.0) Nil
      else Seq(s"has to be a number between 0 and 1, not $value")

    private def wholeMillis(allowZero: Boolean): Check[FiniteDuration] =
      (value, _) =>
        if (value < Duration.Zero) Seq(s"cannot be negative: $value")
        else if (value == Duration.Zero && !allowZero)
          Seq("has to be greater than 0")
        else if (value.toMillis.millis != value)
          Seq(s"has to be a whole number of milliseconds, not $value")
        else Nil

    private val nonEmptyPath: Check[Path] = (path, _) =>
      if (path.toString.isEmpty) Seq("cannot be an empty path") else Nil

    private def noneGCRequires(multithreading: Boolean, instead: String)(
        ctx: Context
    ): Option[String] =
      if (ctx.gc == GC.none && ctx.multithreading != multithreading) {
        val (state, other) =
          if (multithreading) ("without", "with") else ("with", "without")
        Some(
          s"is not used by none GC $state multithreading, set $instead instead or build $other multithreading"
        )
      } else None

    private val tracing = Set[GC](GC.immix, GC.commix)

    val initialHeapSize = new Setting[MemorySize](
      name = "initialHeapSize",
      envVar = Some("GC_INITIAL_HEAP_SIZE"),
      define = Some("INITIAL_HEAP_SIZE"),
      supportedBy = tracing + GC.boehm + GC.none,
      default = None,
      legacyDefine = Some("GC_INITIAL_HEAP_SIZE"),
      get = _.initialHeapSize,
      set = (c, v) => edit(c)(_.copy(initialHeapSize = v)),
      parse = parseSize,
      encode = encodeSize,
      applicable =
        noneGCRequires(multithreading = false, "threadHeapBlockSize"),
      validate = (size, ctx) =>
        heapSize(size, ctx) ++
          ctx.values.maxHeapSize.filter(_ < size).map { max =>
            s"of $size is greater than maxHeapSize of $max"
          }
    )

    val maxHeapSize = new Setting[MemorySize](
      name = "maxHeapSize",
      envVar = Some("GC_MAXIMUM_HEAP_SIZE"),
      define = Some("MAX_HEAP_SIZE"),
      supportedBy = tracing + GC.boehm + GC.none,
      default = None,
      legacyDefine = Some("GC_MAXIMUM_HEAP_SIZE"),
      get = _.maxHeapSize,
      set = (c, v) => edit(c)(_.copy(maxHeapSize = v)),
      parse = parseSize,
      encode = encodeSize,
      applicable =
        noneGCRequires(multithreading = false, "threadHeapBlockSize"),
      validate = positiveSize
    )

    val threadHeapBlockSize = new Setting[MemorySize](
      name = "threadHeapBlockSize",
      envVar = Some("GC_THREAD_HEAP_BLOCK_SIZE"),
      define = Some("THREAD_HEAP_BLOCK_SIZE"),
      supportedBy = Set(GC.none),
      default = Some(MemorySize.mb(64)),
      legacyDefine = Some("GC_THREAD_HEAP_BLOCK_SIZE"),
      get = _.threadHeapBlockSize,
      set = (c, v) => edit(c)(_.copy(threadHeapBlockSize = v)),
      parse = parseSize,
      encode = encodeSize,
      applicable = noneGCRequires(multithreading = true, "maxHeapSize"),
      validate = positiveSize
    )

    val threads = new Setting[Int](
      name = "threads",
      envVar = Some("GC_NPROCS"),
      define = Some("THREADS"),
      supportedBy = Set(GC.commix),
      default = None,
      legacyDefine = Some("GC_NPROCS"),
      get = _.threads,
      set = (c, v) => edit(c)(_.copy(threads = v)),
      parse = parseInt,
      encode = _.toString,
      validate = (count, _) =>
        if (count < 1) Seq(s"has to be at least 1, not $count") else Nil
    )

    val timeRatio = new Setting[Double](
      name = "timeRatio",
      envVar = Some("GC_TIME_RATIO"),
      define = Some("TIME_RATIO"),
      supportedBy = tracing,
      default = Some(0.05),
      get = _.timeRatio,
      set = (c, v) => edit(c)(_.copy(timeRatio = v)),
      parse = parseRatio,
      encode = _.toString,
      validate = ratio
    )

    val freeRatio = new Setting[Double](
      name = "freeRatio",
      envVar = Some("GC_FREE_RATIO"),
      define = Some("FREE_RATIO"),
      supportedBy = Set(GC.commix),
      default = Some(0.5),
      get = _.freeRatio,
      set = (c, v) => edit(c)(_.copy(freeRatio = v)),
      parse = parseRatio,
      encode = _.toString,
      validate = ratio
    )

    val maxFreeRatio = new Setting[Double](
      name = "maxFreeRatio",
      envVar = Some("GC_MAX_FREE_RATIO"),
      define = Some("MAX_FREE_RATIO"),
      supportedBy = tracing,
      default = Some(0.8),
      get = _.maxFreeRatio,
      set = (c, v) => edit(c)(_.copy(maxFreeRatio = v)),
      parse = parseRatio,
      encode = _.toString,
      validate = ratio
    )

    val syncTimeout = new Setting[FiniteDuration](
      name = "syncTimeout",
      envVar = Some("SCALANATIVE_GC_SYNC_TIMEOUT_MS"),
      define = Some("SYNC_TIMEOUT_MS"),
      supportedBy = tracing,
      default = Some(60.seconds),
      get = _.syncTimeout,
      set = (c, v) => edit(c)(_.copy(syncTimeout = v)),
      parse = parseMillis,
      encode = encodeMillis,
      validate = wholeMillis(allowZero = true)
    )

    val syncWarningInterval = new Setting[FiniteDuration](
      name = "syncWarningInterval",
      envVar = Some("SCALANATIVE_GC_SYNC_WARNING_INTERVAL_MS"),
      define = Some("SYNC_WARNING_INTERVAL_MS"),
      supportedBy = tracing,
      default = Some(10.seconds),
      get = _.syncWarningInterval,
      set = (c, v) => edit(c)(_.copy(syncWarningInterval = v)),
      parse = parseMillis,
      encode = encodeMillis,
      validate = wholeMillis(allowZero = false)
    )

    val logLevel = new Setting[LogLevel](
      name = "logLevel",
      envVar = Some("SCALANATIVE_GC_LOG_LEVEL"),
      define = Some("LOG_LEVEL"),
      supportedBy = tracing + GC.boehm + GC.none,
      default = Some(LogLevel.Warn),
      get = _.logLevel,
      set = (c, v) => edit(c)(_.copy(logLevel = v)),
      parse = parseLogLevel,
      encode = encodeLogLevel
    )

    val logFile = new Setting[Path](
      name = "logFile",
      envVar = Some("GC_LOG_FILE"),
      define = Some("LOG_FILE"),
      supportedBy = tracing + GC.none,
      default = None,
      get = _.logFile,
      set = (c, v) => edit(c)(_.copy(logFile = v)),
      parse = parsePath,
      encode = encodePath,
      validate = nonEmptyPath
    )

    val statsFile = new Setting[Path](
      name = "statsFile",
      envVar = Some("GC_STATS_FILE"),
      define = Some("STATS_FILE"),
      supportedBy = tracing,
      default = None,
      get = _.statsFile,
      set = (c, v) => edit(c)(_.copy(statsFile = v)),
      parse = parsePath,
      encode = encodePath,
      validate = (path, ctx) =>
        nonEmptyPath(path, ctx) ++ {
          if (!ctx.values.statsEnabled.contains(true))
            Seq("requires statsEnabled to be set to true")
          else Nil
        }
    )

    val statsEnabled = new Setting[Boolean](
      name = "statsEnabled",
      envVar = None,
      define = Some("STATS_ENABLED"),
      supportedBy = tracing,
      default = Some(false),
      legacyDefine = Some("GC_ENABLE_STATS"),
      get = _.statsEnabled,
      set = (c, v) => edit(c)(_.copy(statsEnabled = v)),
      // Only parsed from -DGC_ENABLE_STATS, where any value enables it
      parse = _ => Right(true),
      encode = enabled => if (enabled) "1" else "0"
    )

    val trapBasedYieldPoints = new Setting[Boolean](
      name = "trapBasedYieldPoints",
      envVar = Some("SCALANATIVE_GC_TRAP_BASED_YIELDPOINTS"),
      // Affects also the generated code, see Config.useTrapBasedGCYieldPoints
      define = None,
      supportedBy = tracing,
      // Depends on the build mode
      default = None,
      get = _.trapBasedYieldPoints,
      set = (c, v) => edit(c)(_.copy(trapBasedYieldPoints = v)),
      parse = value => Right(value == "1" || value.equalsIgnoreCase("true")),
      encode = _.toString,
      validate = (enabled, ctx) =>
        if (enabled && !ctx.multithreading) Seq("requires multithreading")
        else Nil
    )

    val all: Seq[Setting[_]] = Seq(
      initialHeapSize,
      maxHeapSize,
      threadHeapBlockSize,
      threads,
      timeRatio,
      freeRatio,
      maxFreeRatio,
      syncTimeout,
      syncWarningInterval,
      logLevel,
      logFile,
      statsFile,
      statsEnabled,
      trapBasedYieldPoints
    )
  }
}
