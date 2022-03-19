import java.util.Locale

object Utils {
  def assertOsSpecific(
      pred: Boolean,
      msg: => String
  )(onUnix: Boolean, onWindows: Boolean) = {
    val expected =
      if (Platform.isWindows) onWindows
      else onUnix
    assert(pred == expected, msg)
  }
}
