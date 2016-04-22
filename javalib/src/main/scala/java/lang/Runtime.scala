package java.lang

class Runtime private {
}

object Runtime {
  private val currentRuntime = new Runtime

  def getRuntime(): Runtime = currentRuntime
}
