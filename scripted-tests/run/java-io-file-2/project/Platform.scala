// This file is used only inside sbt (JVM)
import java.util.Locale

object PlatformInfo {
  val isWindows = System
    .getProperty("os.name", "unknown")
    .toLowerCase(Locale.ROOT)
    .startsWith("windows")
}
