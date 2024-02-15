package java.lang

import java.io.File
import java.util.{Vector => juVector}
import java.util.Comparator
import scala.scalanative.libc.stdlib
import scala.scalanative.posix.unistd._
import scala.scalanative.windows.SysInfoApi._
import scala.scalanative.windows.SysInfoApiOps._
import scala.scalanative.unsafe._
import scala.scalanative.meta.LinktimeInfo.isWindows

class Runtime private () {
  import Runtime._
  @volatile private var shutdownStarted = false
  private val hooks: juVector[Thread] = new juVector(8)

  lazy val setupAtExitHandler = {
    stdlib.atexit(() => Runtime.getRuntime().runHooks())
  }
  private def ensureCanModify(hook: Thread): Unit = if (shutdownStarted) {
    throw new IllegalStateException(
      s"Shutdown sequence started, cannot add/remove hook $hook"
    )
  }

  def addShutdownHook(thread: Thread): Unit = {
    ensureCanModify(thread)
    hooks.add(thread)
    setupAtExitHandler
  }
  def removeShutdownHook(thread: Thread): Boolean = {
    ensureCanModify(thread)
    hooks.remove(thread)
  }

  private def runHooksConcurrent() = {
    // assume: hooks sorted by -priority
    hooks.forEach { t =>
      t.setUncaughtExceptionHandler(ShutdownHookUncoughExceptionHandler)
    }
    val limit = hooks.size()
    var idx = 0
    while (idx < limit) {
      val groupStart = idx
      val groupPriority = hooks.get(groupStart).getPriority()
      while (idx < limit && hooks.get(idx).getPriority() == groupPriority) {
        hooks.get(idx).start()
        idx += 1
      }
      for (i <- groupStart until limit) {
        hooks.get(i).join()
      }
    }
  }
  private def runHooksSequential() = {
    // assume: hooks sorted by -priority
    hooks
      .forEach { t =>
        try t.run()
        catch {
          case ex: Throwable =>
            ShutdownHookUncoughExceptionHandler.uncaughtException(t, ex)
        }
      }
  }
  private def runHooks() = {
    import scala.scalanative.meta.LinktimeInfo.isMultithreadingEnabled
    shutdownStarted = true
    hooks.sort(Comparator.comparingInt(-_.getPriority()))
    if (isMultithreadingEnabled) runHooksConcurrent()
    else runHooksSequential()
  }

  import Runtime.ProcessBuilderOps
  def availableProcessors(): Int = {
    val available = if (isWindows) {
      val sysInfo = stackalloc[SystemInfo]()
      GetSystemInfo(sysInfo)
      sysInfo.numberOfProcessors.toInt
    } else sysconf(_SC_NPROCESSORS_ONLN).toInt
    // By contract returned value cannot be lower then 1
    available max 1
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

private object ShutdownHookUncoughExceptionHandler
    extends Thread.UncaughtExceptionHandler {
  def uncaughtException(t: Thread, e: Throwable): Unit =
    System.err.println(s"Shutdown hook $t failed, reason: $e")
    t.getThreadGroup().uncaughtException(t, e)
}

object Runtime extends Runtime() {
  def getRuntime(): Runtime = this

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
            case a =>
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
