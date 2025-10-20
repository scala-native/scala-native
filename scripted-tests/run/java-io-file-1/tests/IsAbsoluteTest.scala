object IsAbsoluteTest {
  import Files.*
  import Utils.*

  def main(args: Array[String]): Unit = {
    assertOsSpecific(
      absoluteUnixStyle.isAbsolute,
      "absoluteUnixStyle.isAbsolute"
    )(onUnix = true, onWindows = false)

    assertOsSpecific(
      absoluteWinStyle0.isAbsolute,
      "absoluteWinStyle0.isAbsolute"
    )(onUnix = false, onWindows = true)

    assertOsSpecific(
      absoluteWinStyle1.isAbsolute,
      "absoluteWinStyle1.isAbsolute"
    )(onUnix = false, onWindows = true)

    assertOsSpecific(
      absoluteWinStyle2.isAbsolute,
      "absoluteWinStyle2.isAbsolute"
    )(onUnix = false, onWindows = true)

    assert(!relative0.isAbsolute)
    assert(!relative1.isAbsolute)
    assert(!relative2.isAbsolute)
  }
}
