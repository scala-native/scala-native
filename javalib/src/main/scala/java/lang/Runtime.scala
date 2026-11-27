package java.lang

import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import java.util.{Comparator, Set => juSet}

import scala.scalanative.libc.{signal, stdlib}
import scala.scalanative.meta.LinktimeInfo
import scala.scalanative.posix.unistd._
import scala.scalanative.runtime.GC
import scala.scalanative.runtime.javalib.Proxy
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._
import scala.scalanative.windows.SysInfoApi._
import scala.scalanative.windows.SysInfoApiOps._

class Runtime private () {
  import Runtime._
  @volatile private var shutdownStarted = false
  private lazy val hooks: juSet[Thread] = new java.util.HashSet()

  lazy val setupAtExitHandler = {
    stdlib.atexit(() => Runtime.getRuntime().runHooks())
  }

  // New signals must also be blocked during GC, see `signalsBlockedDuringGC`
  private def setupSignalHandlers(): Unit = {
    installSignalHandler(signal.SIGINT)
    installSignalHandler(signal.SIGTERM)
  }

  // Only replaces SIG_DFL, keeps handlers and SIG_IGN set by the application
  private def installSignalHandler(sig: CInt): Unit = {
    val handler: CFuncPtr1[CInt, Unit] = handleSignal(_)
    // SIG_DFL is a null pointer, boxed by the extern call as a null CFuncPtr
    def address(fn: CFuncPtr): CVoidPtr =
      if (fn == null) null else CFuncPtr.toPtr(fn)
    def isDefault(fn: CFuncPtr1[CInt, Unit]): scala.Boolean =
      address(fn) == address(signal.SIG_DFL)
    if (LinktimeInfo.isWindows) {
      val previous = signal.signal(sig, handler)
      if (!isDefault(previous) &&
          address(previous) != address(signal.SIG_ERR))
        signal.signal(sig, previous)
    } else {
      import scala.scalanative.posix.{signal => psignal}
      import scala.scalanative.posix.signalOps.sigaction_ops
      val current = stackalloc[psignal.sigaction]()
      if (psignal.sigaction(sig, null, current) == 0 &&
          isDefault(current.sa_handler)) {
        val action = stackalloc[psignal.sigaction]()
        action.sa_handler = handler
        action.sa_flags = psignal.SA_RESTART
        psignal.sigaction(sig, action, null)
      }
    }
  }

  private def ensureCanModify(hook: Thread): Unit = if (shutdownStarted) {
    throw new IllegalStateException(
      s"Shutdown sequence started, cannot add/remove hook $hook"
    )
  }

  def addShutdownHook(thread: Thread): Unit = hooks.synchronized {
    ensureCanModify(thread)
    hooks.add(thread)
    setupAtExitHandler
    setupSignalHandlers()
  }

  def removeShutdownHook(thread: Thread): scala.Boolean = hooks.synchronized {
    ensureCanModify(thread)
    hooks.remove(thread)
  }

  private def runHooksConcurrent() = {
    val hooks = this.hooks
      .toArray()
      .asInstanceOf[Array[Thread]]
      .sorted(Ordering.by[Thread, Int](-_.getPriority()))
    hooks.foreach { t =>
      t.setUncaughtExceptionHandler(ShutdownHookUncaughtExceptionHandler)
    }
    // JDK specifies that hooks might run in any order.
    // However, for Scala Native it might be beneficial to support partial ordering
    // E.g. Zone/MemoryPool shutdownHook cleaning pools should be run after DeleteOnExit using `toCString`
    // Group the hooks by priority starting with the ones with the highest priority
    val limit = hooks.size
    var idx = 0
    while (idx < limit) {
      val groupStart = idx
      val groupPriority = hooks(groupStart).getPriority()
      while (idx < limit && hooks(idx).getPriority() == groupPriority) {
        hooks(idx).startInternal()
        idx += 1
      }
      for (i <- groupStart until limit) {
        hooks(i).join()
      }
    }
  }
  private def runHooksSequential() = {
    this.hooks
      .toArray()
      .asInstanceOf[Array[Thread]]
      .sorted(Ordering.by[Thread, Int](-_.getPriority()))
      .foreach { t =>
        try t.run()
        catch {
          case ex: Throwable =>
            ShutdownHookUncaughtExceptionHandler.uncaughtException(t, ex)
        }
      }
  }
  private def runHooks() = {
    import scala.scalanative.meta.LinktimeInfo.isMultithreadingEnabled
    hooks.synchronized {
      if (!shutdownStarted) {
        shutdownStarted = true
        if (isMultithreadingEnabled) runHooksConcurrent()
        else runHooksSequential()
      }
    }
  }

  /** Return the positive number of logical processors on which the process may
   *  run. At least 1 is returned.
   *
   *  On Linux, there are a number of ways (taskset, cpuset, etc.) to set the
   *  number less than sysconf(_SC_NPROCESSORS_ONLN). The underlying C code
   *  currently (2024) will return -1, if there are more than 1024 logical
   *  processors in the cpuset.
   *
   *  Windows also documents some conditions which may lower the number of
   *  available processors.
   *
   *  macOS is culturally adverse to an application lowering the number of
   *  processors below sysctl "hw.logicalcpu". There appear to be ways to
   *  accomplish such a reduction, but they are not programmatic.
   *
   *  FreeBSD and NetBSD use the _SC_NPROCESSORS_ONLN path in this code. Someday
   *  they could use os specific code to get finer granularity. FreeBSD has
   *  "cpuset_getaffinity". NetBSD has sched_getaffinity_np. Implementations for
   *  these operating systems are left as an exercise for the reader.
   */

  import Runtime.ProcessBuilderOps
  def availableProcessors(): Int = {
    val available = if (LinktimeInfo.isWindows) {
      val sysInfo = stackalloc[SystemInfo]()
      GetSystemInfo(sysInfo)
      sysInfo.numberOfProcessors.toInt
    } else {
      val nLogicalCPUs =
        if (LinktimeInfo.isLinux)
          RuntimeLinuxOsSpecific.sched_cpuset_cardinality()
        else -1

      if (nLogicalCPUs > 0) nLogicalCPUs
      else sysconf(_SC_NPROCESSORS_ONLN).toInt
    }

    // By contract returned value cannot be lower then 1
    available max 1
  }

  /** The memory held by the heap, never more than `maxMemory()`. */
  def totalMemory(): scala.Long = GC.getTotalHeapSize().toLong

  /** The part of `totalMemory()` not taken by objects. */
  def freeMemory(): scala.Long = GC.getFreeHeapSize().toLong

  /** The size the heap does not grow beyond, `Long.MAX_VALUE` if unlimited. */
  def maxMemory(): scala.Long = {
    val limit = GC.getHeapLimit()
    if (limit == ~0.toUSize) scala.Long.MaxValue else limit.toLong
  }

  def exit(status: Int): Unit = stdlib.exit(status)
  def gc(): Unit = System.gc()

  def exec(cmdarray: Array[String]): Process =
    new ProcessBuilder(cmdarray).start()
  def exec(cmdarray: Array[String], envp: Array[String]): Process =
    new ProcessBuilder(cmdarray).setEnv(envp).start()
  def exec(cmdarray: Array[String], envp: Array[String], dir: File): Process =
    new ProcessBuilder(cmdarray).setEnv(envp).directory(dir).start()
  def exec(cmd: String): Process = exec(Array(cmd))
  def exec(cmd: String, envp: Array[String]): Process = exec(Array(cmd), envp)
  def exec(cmd: String, envp: Array[String], dir: File): Process =
    exec(Array(cmd), envp, dir)
}

private object ShutdownHookUncaughtExceptionHandler
    extends Thread.UncaughtExceptionHandler {
  def uncaughtException(t: Thread, e: Throwable): Unit = {
    System.err.println(s"Shutdown hook $t failed, reason: $e")
    t.getThreadGroup().uncaughtException(t, e)
  }
}

object Runtime extends Runtime() {
  def getRuntime(): Runtime = this

  private val signalShutdownStarted = new AtomicBoolean(false)

  private def handleSignal(sig: CInt): Unit = {
    Proxy.disableGracefullShutdown()
    def runHooksAndExit(): Unit =
      try Runtime.getRuntime().runHooks()
      finally exit(128 + sig)

    // On Windows the handler runs on its own thread, it never interrupts a park
    if (!LinktimeInfo.isMultithreadingEnabled || LinktimeInfo.isWindows)
      runHooksAndExit()
    // Hooks are already run by exit, possibly by the interrupted thread
    else if (!getRuntime().shutdownStarted || signalShutdownStarted.get()) {
      // The handler might have interrupted a thread in the middle of a park or
      // a monitor wait, joining the hooks on it would corrupt these operations
      if (signalShutdownStarted.compareAndSet(false, true)) {
        val shutdownThread = new Thread(() => runHooksAndExit())
        shutdownThread.setDaemon(true)
        shutdownThread.startInternal()
      }
      // Returning could let the main thread finish with its own exit status
      while (true) pause()
    }
  }

  private implicit class ProcessBuilderOps(val pb: ProcessBuilder)
      extends AnyVal {
    def setEnv(envp: Array[String]): ProcessBuilder = {
      val env = pb.environment()
      env.clear()
      envp match {
        case null =>
          env.putAll(System.getenv())
        case a =>
          envp.foreach {
            case null =>
            case a    =>
              a.split("=") match {
                case Array(k, v) => env.put(k, v)
                case _           =>
              }
          }
      }
      pb
    }
  }
}
