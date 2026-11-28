package org.scalanative.testsuite.javalib.io

import java.io.{Console, IOError, IOException}

import org.junit.Assert._
import org.junit.Assume._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

/* Tests run with stdin and stdout redirected (e.g. on CI) see no console.
 * Methods that would block reading the terminal are not exercised here.
 */
class ConsoleTestOnJDK25 {

  private def console(): Console = {
    val c = System.console()
    assumeTrue("Requires an interactive terminal", c != null)
    c
  }

  @Test def consoleIsCached(): Unit = {
    assertSame(System.console(), System.console())
  }

  @Test def consoleIsTerminal(): Unit = {
    assertTrue(console().isTerminal())
  }

  @Test def charsetIsDefined(): Unit = {
    assertNotNull(console().charset())
  }

  @Test def readerAndWriterAreShared(): Unit = {
    val c = console()
    assertSame(c.reader(), c.reader())
    assertSame(c.writer(), c.writer())
  }

  @Test def formatReturnsConsole(): Unit = {
    val c = console()
    assertSame(c, c.format("%s", ""))
    assertSame(c, c.printf("%s", ""))
    c.flush()
  }

  @Test def formatRejectsNullFormat(): Unit = {
    val c = console()
    assertThrows(classOf[NullPointerException], c.format(null))
    assertThrows(classOf[NullPointerException], c.readLine(null))
    assertThrows(classOf[NullPointerException], c.readPassword(null))
  }

  @Test def closingWriterKeepsConsoleUsable(): Unit = {
    val c = console()
    c.writer().close()
    c.printf("%s", "")
    assertFalse(c.writer().checkError())
  }

  @Test def ioErrorKeepsCause(): Unit = {
    val cause = new IOException("boom")
    val err = new IOError(cause)
    assertSame(cause, err.getCause())
    assertEquals(cause.toString(), err.getMessage())
  }
}
