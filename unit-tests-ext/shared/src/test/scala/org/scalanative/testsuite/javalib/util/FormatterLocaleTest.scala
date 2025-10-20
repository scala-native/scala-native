package org.scalanative.testsuite.javalib.util

import java.util.*

// Extracted from port of Apache Harmony tests.
// All actual formatting tests are in core `DefaultFormatterTest` and `FormatterExtTest`

import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.Flushable
import java.io.IOException
import java.io.OutputStream
import java.io.PrintStream
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.lang.StringBuilder
import org.junit.Test
import org.junit.Before
import org.junit.After
import org.junit.Assert.*

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class FormatterLocaleTest {
  private var root: Boolean = false
  private var notExist: File = _
  private var fileWithContent: File = _
  private var readOnly: File = _

  // setup resource files for testing
  @Before
  def setUp(): Unit = {
    // disabled, doesn't work on Scala Native right now
    // root = System.getProperty("user.name").equalsIgnoreCase("root")
    notExist = File.createTempFile("notexist", null)
    notExist.delete()

    fileWithContent = File.createTempFile("filewithcontent", null)
    val bw = new BufferedOutputStream(new FileOutputStream(fileWithContent))
    bw.write(1); // write something into the file
    bw.close()

    readOnly = File.createTempFile("readonly", null)
    readOnly.setReadOnly()
  }

  // delete the resource files if they exist
  @After
  def tearDown(): Unit = {
    if (notExist.exists()) notExist.delete()
    if (fileWithContent.exists()) fileWithContent.delete()
    if (readOnly.exists()) readOnly.delete()
  }

  private class MockAppendable extends Appendable {
    def append(arg0: CharSequence): Appendable = null

    def append(arg0: Char): Appendable = null

    def append(arg0: CharSequence, arg1: Int, arg2: Int): Appendable = null
  }

  private class MockFormattable extends Formattable {
    def formatTo(
        formatter: Formatter,
        flags: Int,
        width: Int,
        precision: Int
    ): Unit = {
      if ((flags & FormattableFlags.UPPERCASE) != 0)
        formatter.format(
          "CUSTOMIZED FORMAT FUNCTION" + " WIDTH: " + width + " PRECISION: " + precision
        )
      else
        formatter.format(
          "customized format function" + " width: " + width + " precision: " + precision
        )
    }

    override def toString(): String = "formattable object"

    override def hashCode(): Int = 0xf
  }

  private class MockDestination extends Appendable with Flushable {
    // Porting note: the content of MockDestination was stripped because it was no-op.
    def append(c: Char): Appendable = throw new IOException()

    def append(csq: CharSequence): Appendable = throw new IOException()

    def append(csq: CharSequence, start: Int, end: Int): Appendable =
      throw new IOException()

    def flush(): Unit = throw new IOException("Always throw IOException")

    override def toString(): String = ""
  }

  @Test def constructorDefault(): Unit = {
    val f = new Formatter()
    assertNotNull(f)
    assertTrue(f.out().isInstanceOf[StringBuilder])
    assertEquals(f.locale(), Locale.getDefault())
    assertNotNull(f.toString())
  }

  @Test def constructorAppendable(): Unit = {
    val ma = new MockAppendable()
    val f1 = new Formatter(ma)
    assertEquals(ma, f1.out())
    assertEquals(f1.locale(), Locale.getDefault())
    assertNotNull(f1.toString())

    val f2 = new Formatter(null.asInstanceOf[Appendable])
    /*
     * If a(the input param) is null then a StringBuilder will be created
     * and the output can be attained by invoking the out() method. But RI
     * raises an error of FormatterClosedException when invoking out() or
     * toString().
     */
    val sb = f2.out()
    assertTrue(sb.isInstanceOf[StringBuilder])
    assertNotNull(f2.toString())
  }

  @Test def constructorLocale(): Unit = {
    val f1 = new Formatter(Locale.US)
    assertTrue(f1.out().isInstanceOf[StringBuilder])
    assertEquals(f1.locale(), Locale.US)
    assertNotNull(f1.toString())

    val f2 = new Formatter(null.asInstanceOf[Locale])
    assertNull(f2.locale())
    assertTrue(f2.out().isInstanceOf[StringBuilder])
    assertNotNull(f2.toString())
  }

  @Test def constructorAppendableLocale(): Unit = {
    val ma = new MockAppendable()
    val f1 = new Formatter(ma, Locale.US)
    assertEquals(ma, f1.out())
    assertEquals(f1.locale(), Locale.US)

    val f2 = new Formatter(ma, null)
    assertNull(f2.locale())
    assertEquals(ma, f1.out())

    val f3 = new Formatter(null, Locale.US)
    assertEquals(f3.locale(), Locale.US)
    assertTrue(f3.out().isInstanceOf[StringBuilder])
  }

  @Test def constructorString(): Unit = {
    assertThrows(
      classOf[NullPointerException],
      new Formatter(null.asInstanceOf[String])
    )

    locally {
      val f = new Formatter(notExist.getPath())
      assertEquals(f.locale(), Locale.getDefault())
      f.close()
    }

    locally {
      val f = new Formatter(fileWithContent.getPath())
      assertEquals(0, fileWithContent.length())
      f.close()
    }

    if (!root) {
      assertThrows(
        classOf[FileNotFoundException],
        new Formatter(readOnly.getPath())
      )
    }
  }

  @Test def constructorStringString(): Unit = {
    assertThrows(
      classOf[NullPointerException],
      new Formatter(null.asInstanceOf[String], Charset.defaultCharset().name())
    )

    locally {
      val f =
        new Formatter(notExist.getPath(), Charset.defaultCharset().name())
      assertEquals(f.locale(), Locale.getDefault())
      f.close()
    }

    assertThrows(
      classOf[UnsupportedEncodingException],
      new Formatter(notExist.getPath(), "ISO 111-1")
    )

    locally {
      val f = new Formatter(fileWithContent.getPath(), "UTF-16BE")
      assertEquals(0, fileWithContent.length())
      f.close()
    }

    if (!root) {
      assertThrows(
        classOf[FileNotFoundException],
        new Formatter(readOnly.getPath(), "UTF-16BE")
      )
    }
  }

  @Test def constructorStringStringLocale(): Unit = {
    assertThrows(
      classOf[NullPointerException],
      new Formatter(
        null.asInstanceOf[String],
        Charset.defaultCharset().name(),
        Locale.US
      )
    )

    locally {
      val f =
        new Formatter(notExist.getPath(), Charset.defaultCharset().name(), null)
      assertNotNull(f)
      f.close()
    }

    locally {
      val f = new Formatter(
        notExist.getPath(),
        Charset.defaultCharset().name(),
        Locale.US
      )
      assertEquals(f.locale(), Locale.US)
      f.close()
    }

    assertThrows(
      classOf[UnsupportedEncodingException],
      new Formatter(notExist.getPath(), "ISO 1111-1", Locale.US)
    )

    locally {
      val f = new Formatter(fileWithContent.getPath(), "UTF-16BE", Locale.US)
      assertEquals(0, fileWithContent.length())
      f.close()
    }

    if (!root) {
      assertThrows(
        classOf[FileNotFoundException],
        new Formatter(
          readOnly.getPath(),
          Charset.defaultCharset().name(),
          Locale.US
        )
      )
    }
  }

  @Test def constructorFile(): Unit = {
    locally {
      val f = new Formatter(notExist)
      assertEquals(f.locale(), Locale.getDefault())
      f.close()
    }

    locally {
      val f = new Formatter(fileWithContent)
      assertEquals(0, fileWithContent.length())
      f.close()
    }

    if (!root) {
      assertThrows(classOf[FileNotFoundException], new Formatter(readOnly))
    }
  }

  @Test def constructorFileString(): Unit = {

    locally {
      val f = new Formatter(notExist, Charset.defaultCharset().name())
      assertEquals(f.locale(), Locale.getDefault)
      f.close()
    }

    locally {
      val f = new Formatter(fileWithContent, "UTF-16BE")
      assertEquals(0, fileWithContent.length)
      f.close()
    }

    if (!root) {
      assertThrows(
        classOf[FileNotFoundException],
        new Formatter(readOnly, Charset.defaultCharset().name())
      )
    }

    try {
      assertThrows(
        classOf[UnsupportedEncodingException],
        new Formatter(notExist, "ISO 1111-1")
      )
    } finally
      if (notExist.exists()) {
        // Fail on RI on Windows, because output stream is created and
        // not closed when exception thrown
        assertTrue(notExist.delete())
      }
  }

  @Test def constructorFileStringLocale(): Unit = {

    locally {
      val f = new Formatter(notExist, Charset.defaultCharset().name(), null)
      assertNotNull(f)
      f.close()
    }

    locally {
      val f =
        new Formatter(notExist, Charset.defaultCharset().name(), Locale.US)
      assertEquals(f.locale(), Locale.US)
      f.close()
    }

    assertThrows(
      classOf[UnsupportedEncodingException],
      new Formatter(notExist, "ISO 1111-1", Locale.US)
    )

    locally {
      val f = new Formatter(fileWithContent.getPath, "UTF-16BE", Locale.US)
      assertEquals(0, fileWithContent.length)
      f.close()
    }

    if (!root) {
      assertThrows(
        classOf[FileNotFoundException],
        new Formatter(
          readOnly.getPath,
          Charset.defaultCharset().name(),
          Locale.US
        )
      )
    }
  }

  @Test def constructorPrintStream(): Unit = {
    assertThrows(
      classOf[NullPointerException],
      new Formatter(null.asInstanceOf[PrintStream])
    )

    val ps = new PrintStream(notExist, "UTF-16BE")
    val f = new Formatter(ps)
    assertEquals(Locale.getDefault(), f.locale())
    f.close()
  }

  @Test def constructorOutputStream(): Unit = {
    assertThrows(
      classOf[NullPointerException],
      new Formatter(null.asInstanceOf[OutputStream])
    )

    val os = new FileOutputStream(notExist)
    val f = new Formatter(os)
    assertEquals(Locale.getDefault(), f.locale())
    f.close()
  }

  @Test def constructorOutputStreamString(): Unit = {

    assertThrows(
      classOf[NullPointerException],
      new Formatter(
        null.asInstanceOf[OutputStream],
        Charset.defaultCharset().name()
      )
    )

    locally {
      // Porting note: PipedOutputStream is not essential to this test.
      // Since it doesn't exist on Scala Native yet, it is replaced with
      // a harmless one.
      // val os = new PipedOutputStream()
      val os = new ByteArrayOutputStream
      assertThrows(
        classOf[UnsupportedEncodingException],
        new Formatter(os, "TMP-1111")
      )
    }

    locally {
      val os = new FileOutputStream(fileWithContent)
      val f = new Formatter(os, "UTF-16BE")
      assertEquals(Locale.getDefault, f.locale())
      f.close()
    }
  }

  @Test def constructorOutputStreamStringLocale(): Unit = {

    assertThrows(
      classOf[NullPointerException],
      new Formatter(
        null.asInstanceOf[OutputStream],
        Charset.defaultCharset().name(),
        Locale.getDefault
      )
    )

    locally {
      val os = new FileOutputStream(notExist)
      val f = new Formatter(os, Charset.defaultCharset().name(), null)
      f.close()
    }

    locally {
      // Porting note: PipedOutputStream is not essential to this test.
      // Since it doesn't exist on Scala Native yet, it is replaced with
      // a harmless one.
      // val os = new PipedOutputStream()
      val os = new ByteArrayOutputStream
      assertThrows(
        classOf[UnsupportedEncodingException],
        new Formatter(os, "TMP-1111", Locale.getDefault)
      )
    }

    locally {
      val os = new FileOutputStream(fileWithContent)
      val f = new Formatter(os, "UTF-16BE", Locale.US)
      assertEquals(Locale.US, f.locale())
      f.close()
    }
  }

  @Test def locale(): Unit = {
    val f = new Formatter(null.asInstanceOf[Locale])
    assertNull(f.locale())

    f.close()
    assertThrows(classOf[FormatterClosedException], f.locale())
  }
}
