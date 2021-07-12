package junit.java.lang

import org.junit.Assert._
import org.junit.Test

class StringTestJDK11 {
  @Test def stripTest(): Unit = {
    assertEquals("", "".strip())
    assertEquals("hallo", "   hallo".stripLeading())
    assertEquals("hallo", "hallo   ".stripTrailing())
    assertEquals("hallo", "   hallo   ".strip())
    assertEquals("hallo\nwelt", "\t\t\t  hallo\nwelt\r\n  \t".strip())
  }

  @Test def isBlankTest(): Unit = {
    assertTrue("".isBlank)
    assertTrue(" \t\n\u000B\f\r\u001C\u001D\u001E\u001F".isBlank)
    assertFalse(" \t\n\u000B\f\r\u001C a \u001D\u001E\u001F".isBlank)
  }

  @Test def linesTest(): Unit = {
    import scala.collection.JavaConverters._
    assertEquals(Seq(""), "".lines.iterator.asScala.toSeq)
    assertEquals(Seq("hallo", "welt"),
                 "hallo\r\nwelt".lines.iterator.asScala.toSeq)
    assertEquals(Seq("hallo", "welt"),
                 "hallo\rwelt".lines.iterator.asScala.toSeq)
    assertEquals(Seq("hallo", "welt"),
                 "hallo\nwelt".lines.iterator.asScala.toSeq)
    assertEquals(Seq("hallo", "schöne", "welt"),
                 "hallo\rschöne\nwelt".lines.iterator.asScala.toSeq)
    assertEquals(Seq("hallo", "schöne", "große", "welt"),
                 "hallo\rschöne\r\ngroße\nwelt".lines.iterator.asScala.toSeq)
  }

  @Test def repeatTest(): Unit = {
    assertEquals("", "".repeat(10))
    assertEquals("", "hallo".repeat(0))
    assertEquals("foofoofoo", "foo".repeat(3))
  }

}
