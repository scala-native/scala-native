package org.scalanative.testsuite.javalib.lang

import java.lang as jl
import java.nio.CharBuffer

import org.junit.Test
import org.junit.Assert.*

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class CharSequenceTestOnJDK15 {

  @Test def isEmptyCharBuffer(): Unit = {
    // check method overridden in class

    /* When trying to follow the logic of this test, recall that for
     * CharSequence "isEmpty()" is equivalent to its "remaining()" method.
     * Using flip() to enter a read state sets the sense used in, say,
     * the String class of "number of characters available to get".
     */

    val quote = "Only this and nothing more."

    // buffers are created set for writing
    val cBuf = CharBuffer.allocate(quote.length())
    assertFalse("writable CharBuffer at allocation", cBuf.isEmpty())

    cBuf.flip() // set for reading
    assertTrue("flipped allocated CharBuffer", cBuf.isEmpty())

    cBuf.clear().append(quote)
    assertTrue("zero remaining write space", cBuf.isEmpty())

    cBuf.flip()
    assertFalse("nonempty readable CharBuffer", cBuf.isEmpty())
  }

  @Test def isEmptyString(): Unit = {
    // check method overridden in class
    val emptyStringYes = ""
    val emptyStringNo = "Perched, and sat, and nothing more"

    assertTrue("empty String", emptyStringYes.isEmpty())
    assertFalse("nonempty String", emptyStringNo.isEmpty())
  }

  @Test def isEmptyStringBuilder(): Unit = {
    // check method inherited from CharSequence
    val sb = new jl.StringBuilder(64)

    assertTrue("empty StringBuilder", sb.isEmpty())

    sb.append("Quoth the Raven “Nevermore.”")
    assertFalse("nonempty StringBuilder", sb.isEmpty())
  }

}
