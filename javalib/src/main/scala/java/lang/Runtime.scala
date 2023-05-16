package java.lang

import java.io.File
import scala.scalanative.annotation.stub
import scala.scalanative.libc.stdlib
import scala.scalanative.posix.unistd._
import scala.scalanative.windows.SysInfoApi._
import scala.scalanative.windows.SysInfoApiOps._
import scala.scalanative.unsafe._
import scala.scalanative.meta.LinktimeInfo.isWindows

class Runtime private () {
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

  @stub
  def addShutdownHook(thread: java.lang.Thread): Unit = ???

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

object Runtime {
  private val currentRuntime = new Runtime()

  def getRuntime(): Runtime = currentRuntime

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
