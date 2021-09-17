import java.util.Locale

object Utils {
  val isWindows = System
    .getProperty("os.name", "unknown")
    .toLowerCase()
    .startsWith("windows")

  def assertOsSpecific(
      pred: Boolean,
      msg: => String
  )(onUnix: Boolean, onWindows: Boolean) = {
    val expected =
      if (isWindows) onWindows
      else onUnix
    assert(pred == expected, msg)
  }
}
