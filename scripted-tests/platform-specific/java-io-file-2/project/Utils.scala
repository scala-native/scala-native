object Utils {
  def assertOsSpecific(
      pred: Boolean,
      msg: => String
  )(onUnix: Boolean, onWindows: Boolean) = {
    val expected =
      if (PlatformInfo.isWindows) onWindows
      else onUnix
    assert(pred == expected, msg)
  }
}
