package java.niocharset

import java.nio.charset._

// Ported from Scala.js

object CharsetSuite extends tests.Suite {
  test("Default charset") {
    assertEquals("UTF-8", Charset.defaultCharset().name())
  }

  test("forName") {
    assertEquals("ISO-8859-1", Charset.forName("ISO-8859-1").name())
    assertEquals("ISO-8859-1", Charset.forName("Iso8859-1").name())
    assertEquals("ISO-8859-1", Charset.forName("iso_8859_1").name())
    assertEquals("ISO-8859-1", Charset.forName("LaTin1").name())
    assertEquals("ISO-8859-1", Charset.forName("l1").name())

    assertEquals("US-ASCII", Charset.forName("US-ASCII").name())
    assertEquals("US-ASCII", Charset.forName("Default").name())

    assertEquals("UTF-8", Charset.forName("UTF-8").name())
    assertEquals("UTF-8", Charset.forName("utf-8").name())
    assertEquals("UTF-8", Charset.forName("UtF8").name())

    assertEquals("UTF-16BE", Charset.forName("UTF-16BE").name())
    assertEquals("UTF-16BE", Charset.forName("Utf_16BE").name())
    assertEquals("UTF-16BE", Charset.forName("UnicodeBigUnmarked").name())

    assertEquals("UTF-16LE", Charset.forName("UTF-16le").name())
    assertEquals("UTF-16LE", Charset.forName("Utf_16le").name())
    assertEquals("UTF-16LE", Charset.forName("UnicodeLittleUnmarked").name())

    assertEquals("UTF-16", Charset.forName("UTF-16").name())
    assertEquals("UTF-16", Charset.forName("Utf_16").name())
    assertEquals("UTF-16", Charset.forName("unicode").name())
    assertEquals("UTF-16", Charset.forName("UnicodeBig").name())

    expectThrows(classOf[UnsupportedCharsetException], Charset.forName("UTF_8"))

    expectThrows(classOf[UnsupportedCharsetException],
      Charset.forName("this-charset-does-not-exist"))
  }

  test("isSupported") {
    assert(Charset.isSupported("ISO-8859-1"))
    assert(Charset.isSupported("US-ASCII"))
    assert(Charset.isSupported("Default"))
    assert(Charset.isSupported("utf-8"))
    assert(Charset.isSupported("UnicodeBigUnmarked"))
    assert(Charset.isSupported("Utf_16le"))
    assert(Charset.isSupported("UTF-16"))
    assert(Charset.isSupported("unicode"))

    assertNot(Charset.isSupported("this-charset-does-not-exist"))
  }
}
