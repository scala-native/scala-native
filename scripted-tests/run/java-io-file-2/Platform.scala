// This file is used only when compiling sources using Scala Native

object PlatformInfo {
  val isWindows = scalanative.runtime.Platform.isWindows()
}
