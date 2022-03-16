// This file is used only inside sbt (JVM)
import java.util.Locale

object Platform {
  val osName = System
    .getProperty("os.name", "unknown")
    .toLowerCase(Locale.ROOT)

  val isWindows = osName.startsWith("windows")
  val isMac = osName.startsWith("mac")
}
