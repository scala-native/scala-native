package java.lang

class Runtime private () {
  def availableProcessors(): Int = 1
  def gc(): Unit                 = ()
}

object Runtime {
  private val currentRuntime = new Runtime()

  def getRuntime(): Runtime = currentRuntime
}
