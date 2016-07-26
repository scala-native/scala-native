package java.lang

import scala.scalanative.native.stdlib

class Runtime private () {
  def availableProcessors(): Int = 1
  def exit(status: Int): Unit    = stdlib.exit(status)
  def gc(): Unit                 = ()
}

object Runtime {
  private val currentRuntime = new Runtime()

  def getRuntime(): Runtime = currentRuntime
}
