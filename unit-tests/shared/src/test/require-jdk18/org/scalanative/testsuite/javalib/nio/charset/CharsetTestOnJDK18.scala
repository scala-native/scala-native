package org.scalanative.testsuite.javalib.nio.charset

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets._

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class CharsetTestOnJDK18 {
  @Test def supportedNamesAndAliasesIgnoreFallback(): Unit = {
    for ((name, expected) <- Array(
          ("uTf8", UTF_8),
          ("Latin1", ISO_8859_1),
          ("UnicodeBigUnmarked", UTF_16BE)
        )) {
      assertSame(expected, Charset.forName(name, US_ASCII))
      assertSame(expected, Charset.forName(name, null))
    }
  }

  @Test def unsupportedAndIllegalNamesReturnFallback(): Unit = {
    for (name <- Array("", " ", "-UTF8", "UTF/8", "é", "S10-no-such-charset")) {
      assertSame(UTF_16LE, Charset.forName(name, UTF_16LE))
      assertNull(Charset.forName(name, null))
    }
  }

  @Test def nullNameIsRejectedRegardlessOfFallback(): Unit = {
    assertThrows(
      classOf[IllegalArgumentException],
      Charset.forName(null, UTF_8)
    )
    assertThrows(classOf[IllegalArgumentException], Charset.forName(null, null))
  }
}
