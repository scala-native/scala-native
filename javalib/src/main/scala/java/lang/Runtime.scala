package java.lang

import java.io.File
import scala.collection.JavaConverters._
import scala.scalanative.native.stub
import scala.scalanative.libc.stdlib

class Runtime private () {
  import Runtime.ProcessBuilderOps
  def availableProcessors(): Int = 1
  def exit(status: Int): Unit    = stdlib.exit(status)
  def gc(): Unit                 = ()

  @stub
  def addShutdownHook(thread: java.lang.Thread): Unit = ???

  def exec(cmdarray: Array[String]): Process =
    new ProcessBuilder(cmdarray).start()
  def exec(cmdarray: Array[String], envp: Array[String]): Process =
    new ProcessBuilder(cmdarray).setEnv(envp).start()
  def exec(cmdarray: Array[String], envp: Array[String], dir: File): Process =
    new ProcessBuilder(cmdarray).setEnv(envp).directory(dir).start()
  def exec(cmd: String): Process                      = exec(Array(cmd))
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
      val env = pb.environment
      env.clear()
      envp match {
        case null =>
          System.getenv.asScala.foreach { case (k, v) => env.put(k, v) }
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
