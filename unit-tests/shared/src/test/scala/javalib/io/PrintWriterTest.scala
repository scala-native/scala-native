package javalib.io

import java.io._

import org.junit.Test
import org.junit.Assert._

class PrintWriterTest {

  @Test def writeCharSequenceTest(): Unit = {

    val out = new StringWriter()
    val writer = new PrintWriter(out)

    // Use writer with as String casted as a CharSequence
    val stringAsCharSeq: CharSequence = "ABC"
    writer.print(stringAsCharSeq)

    // Use writer with as StringBuilder casted as a CharSequence
    val stingBuilderAsCharSeq = new StringBuilder("DEF")
    writer.print(stingBuilderAsCharSeq)

    // flush is optional here, as Writer calls the no-op StringWriter.flush()
    writer.flush()

    val result = out.toString

    assertEquals("ABCDEF", result)
  }
}
