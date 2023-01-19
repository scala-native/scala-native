package org.scalanative.testsuite.javalib.io

import java.io._

import org.junit.Test
import org.junit.Assert._

class PushbackReaderTest {
  @Test def pushbackReaderCanUnreadCharacters(): Unit = {
    val reader =
      new InputStreamReader(new ByteArrayInputStream(Array(1, 2, 3)))
    val pushbackReader = new PushbackReader(reader)

    assertTrue(pushbackReader.read() == 1)
    pushbackReader.unread(1)
    assertTrue(pushbackReader.read() == 1)
    pushbackReader.unread(5)
    assertTrue(pushbackReader.read() == 5)
    assertTrue(pushbackReader.read() == 2)
    assertTrue(pushbackReader.read() == 3)
    pushbackReader.unread(0)
    assertTrue(pushbackReader.read() == 0)
    assertTrue(pushbackReader.read() == -1)
  }
}
