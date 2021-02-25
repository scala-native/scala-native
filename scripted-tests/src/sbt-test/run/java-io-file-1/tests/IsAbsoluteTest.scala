object IsAbsoluteTest {
  import Files._

  def main(args: Array[String]): Unit = {
    if (java.io.File.separator == "/") {
      assert(absoluteUnixStyle.isAbsolute)
      assert(!absoluteWinStyle0.isAbsolute)
      assert(!absoluteWinStyle1.isAbsolute)
      assert(!absoluteWinStyle2.isAbsolute)
    } else {
      assert(!absoluteUnixStyle.isAbsolute)
      assert(absoluteWinStyle0.isAbsolute)
      assert(absoluteWinStyle1.isAbsolute)
      assert(absoluteWinStyle2.isAbsolute)
    }

    assert(!relative0.isAbsolute)
    assert(!relative1.isAbsolute)
    assert(!relative2.isAbsolute)
  }
}
