package java.lang

import scala.scalanative.native.{stdlib, stub, sysinfo}

class Runtime private () {
  def availableProcessors(): Int = sysinfo.get_nprocs
  def exit(status: Int): Unit    = stdlib.exit(status)
  def gc(): Unit                 = ()

  @stub
  def addShutdownHook(thread: java.lang.Thread): Unit = ???
}

object Runtime {
  private val currentRuntime = new Runtime()

  def getRuntime(): Runtime = currentRuntime
}
