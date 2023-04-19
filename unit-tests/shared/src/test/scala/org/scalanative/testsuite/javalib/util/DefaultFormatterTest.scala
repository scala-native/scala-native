package org.scalanative.testsuite.javalib.util

import java.util._

// Ported from Harmony, modified to test without locale.

import java.io._
import java.math.{BigDecimal, BigInteger, MathContext}
import java.nio.charset.Charset
import java.lang.StringBuilder
import java.util.Formatter.BigDecimalLayoutForm
import org.junit.Assert._
import org.junit.{After, Before, Ignore, Test}
import org.scalanative.testsuite.utils.Platform.executingInJVM
import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class DefaultFormatterTest {
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
    assertNotNull(f.toString())
  }

  @Test def constructorAppendable(): Unit = {
    val ma = new MockAppendable()
    val f1 = new Formatter(ma)
    assertEquals(ma, f1.out())
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
  @Test def constructorString(): Unit = {
    assertThrows(
      classOf[NullPointerException],
      new Formatter(null.asInstanceOf[String])
    )

    locally {
      val f = new Formatter(notExist.getPath())
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

  @Test def constructorFile(): Unit = {

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

  @Test def constructorPrintStream(): Unit = {
    assertThrows(
      classOf[NullPointerException],
      new Formatter(null.asInstanceOf[PrintStream])
    )

    val ps = new PrintStream(notExist, "UTF-16BE")
    val f = new Formatter(ps)
    assertNotNull(f)
    f.close()
  }

  @Test def constructorOutputStream(): Unit = {
    assertThrows(
      classOf[NullPointerException],
      new Formatter(null.asInstanceOf[OutputStream])
    )

    val os = new FileOutputStream(notExist)
    val f = new Formatter(os)
    assertNotNull(f)
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
      assertNotNull(f)
      f.close()
    }
  }

  @Test def out(): Unit = {
    val f = new Formatter()
    assertNotNull(f.out())
    assertTrue(f.out().isInstanceOf[StringBuilder])
    f.close()
    assertThrows(classOf[FormatterClosedException], f.out())
  }

  @Test def flush(): Unit = {
    locally {
      val f = new Formatter(notExist)
      assertTrue(f.isInstanceOf[Flushable])
      f.close()
      assertThrows(classOf[FormatterClosedException], f.out())
    }

    locally {
      val f = new Formatter()
      // For destination that does not implement Flushable
      // No exception should be thrown
      f.flush()
    }
  }

  @Test def close(): Unit = {
    val f = new Formatter(notExist)
    assertTrue(f.isInstanceOf[Closeable])
    f.close()
    // close next time will not throw exception
    f.close()
    assertNull(f.ioException())
  }

  @Test def testToString(): Unit = {
    val f = new Formatter()
    assertNotNull(f.toString())
    assertEquals(f.out().toString(), f.toString())
    f.close()
    assertThrows(classOf[FormatterClosedException], f.toString())
  }

  @Test def ioException(): Unit = {
    locally {
      val f = new Formatter(new MockDestination())
      assertNull(f.ioException())
      f.flush()
      assertNotNull(f.ioException())
      f.close()
    }

    locally {
      val md = new MockDestination()
      val f = new Formatter(md)
      f.format("%s%s", "1", "2")
      // format stop working after IOException
      assertNotNull(f.ioException())
      assertEquals("", f.toString())
    }
  }

  @Test def formatForNullParameter(): Unit = {
    locally {
      val f = new Formatter()
      f.format("hello", null.asInstanceOf[Array[Object]])
      assertEquals("hello", f.toString())
    }
  }

  @Test def formatForArgumentIndex(): Unit = {
    locally {
      val f = new Formatter()
      f.format(
        "%1$s%2$s%3$s%4$s%5$s%6$s%7$s%8$s%9$s%11$s%10$s",
        "1",
        "2",
        "3",
        "4",
        "5",
        "6",
        "7",
        "8",
        "9",
        "10",
        "11"
      )
      assertEquals("1234567891110", f.toString())
    }

    locally {
      val f = new Formatter()
      f.format("%0$s", "hello")
      assertEquals("hello", f.toString())
    }

    locally {
      val f = new Formatter()
      assertThrows(
        classOf[UnknownFormatConversionException],
        f.format("%-1$s", "1", "2")
      )
    }

    locally {
      val f = new Formatter()
      assertThrows(
        classOf[UnknownFormatConversionException],
        f.format("%$s", "hello", "2")
      )
    }

    locally {
      val f = new Formatter()
      assertThrows(
        classOf[UnknownFormatConversionException],
        f.format("%", "string")
      )
    }

    locally {
      val f = new Formatter()
      f.format(
        "%1$s%2$s%3$s%4$s%5$s%6$s%7$s%8$s%<s%s%s%<s",
        "1",
        "2",
        "3",
        "4",
        "5",
        "6",
        "7",
        "8",
        "9",
        "10",
        "11"
      )
      assertEquals("123456788122", f.toString())
    }

    locally {
      val f = new Formatter()
      f.format(
        "xx%1$s22%2$s%s%<s%5$s%<s&%7$h%2$s%8$s%<s%s%s%<ssuffix",
        "1",
        "2",
        "3",
        "4",
        "5",
        "6",
        7.asInstanceOf[Object], // this is intended
        "8",
        "9",
        "10",
        "11"
      )
      assertEquals("xx12221155&7288233suffix", f.toString())
      assertThrows(
        classOf[MissingFormatArgumentException],
        f.format("%<s", "hello")
      )
    }

    locally {
      val f = new Formatter()
      assertThrows(
        classOf[MissingFormatArgumentException],
        f.format("%123$s", "hello")
      )
    }

    locally {
      val f = new Formatter()
      // 2147483648 is the value of Integer.MAX_VALUE + 1
      assertThrows(
        classOf[MissingFormatArgumentException],
        f.format("%2147483648$s", "hello")
      )
      // 2147483647 is the value of Integer.MAX_VALUE
      assertThrows(
        classOf[MissingFormatArgumentException],
        f.format("%2147483647$s", "hello")
      )
    }

    locally {
      val f = new Formatter()
      assertThrows(
        classOf[MissingFormatArgumentException],
        f.format("%s%s", "hello")
      )
    }

    locally {
      val f = new Formatter()
      f.format("$100", 100.asInstanceOf[Object])
      assertEquals("$100", f.toString())
    }

    locally {
      val f = new Formatter()
      f.format("%01$s", "string")
      assertEquals("string", f.toString())
    }
  }

  @Test def formatForWidth(): Unit = {
    locally {
      val f = new Formatter()
      f.format("%1$8s", "1")
      assertEquals("       1", f.toString())
    }

    locally {
      val f = new Formatter()
      f.format("%1$-1%", "string")
      assertEquals("%", f.toString())
    }

    locally {
      val f = new Formatter()
      // 2147483648 is the value of Integer.MAX_VALUE + 1
      f.format("%2147483648s", "string")
      assertEquals("string", f.toString())
    }
  }

  @Test def formatForPrecision(): Unit = {
    locally {
      val f = new Formatter()
      f.format("%.5s", "123456")
      assertEquals("12345", f.toString())
    }

    locally {
      val f = new Formatter()
      // 2147483648 is the value of Integer.MAX_VALUE + 1
      f.format("%.2147483648s", "...")
      assertEquals("...", f.toString())
    }

    locally {
      val f = new Formatter()
      f.format("%10.0b", java.lang.Boolean.TRUE)
      assertEquals("          ", f.toString())
    }

    locally {
      val f = new Formatter()
      f.format("%10.01s", "hello")
      assertEquals("         h", f.toString())
    }

    locally {
      val f = new Formatter()
      assertThrows(
        classOf[UnknownFormatConversionException],
        f.format("%.s", "hello", "2")
      )
    }

    locally {
      val f = new Formatter()
      assertThrows(
        classOf[UnknownFormatConversionException],
        f.format("%.-5s", "123456")
      )
    }

    locally {
      val f = new Formatter()
      assertThrows(
        classOf[UnknownFormatConversionException],
        f.format("%1.s", "hello", "2")
      )
    }

    locally {
      val f = new Formatter()
      f.format("%5.1s", "hello")
      assertEquals("    h", f.toString())
    }

    locally {
      val f = new Formatter()
      f.format("%.0s", "hello", "2")
      assertEquals("", f.toString())
    }
  }

  @Ignore("line separator is hard coded")
  def formatForCustomLineSeparator(): Unit = {
    val oldSeparator = System.getProperty("line.separator")
    System.setProperty("line.separator", "!\n")
    try {
      locally {
        val f = new Formatter()
        f.format("%1$n", 1.asInstanceOf[Object])
        assertEquals("!\n", f.toString())
      }

      locally {
        val f = new Formatter()
        f.format("head%1$n%2$n", 1.asInstanceOf[Object], new Date())
        assertEquals("head!\n!\n", f.toString())
      }

      locally {
        val f = new Formatter()
        f.format("%n%s", "hello")
        assertEquals("!\nhello", f.toString())
      }
    } finally {
      System.setProperty("line.separator", oldSeparator)
    }
  }

  @Test def formatForLineSeparator(): Unit = {
    locally {
      val f = new Formatter()
      assertThrows(classOf[IllegalFormatFlagsException], f.format("%-n"))
      assertThrows(classOf[IllegalFormatFlagsException], f.format("%+n"))
      assertThrows(classOf[IllegalFormatFlagsException], f.format("%#n"))
      assertThrows(classOf[IllegalFormatFlagsException], f.format("% n"))
      assertThrows(classOf[IllegalFormatFlagsException], f.format("%0n"))
      assertThrows(classOf[IllegalFormatFlagsException], f.format("%,n"))
      assertThrows(classOf[IllegalFormatFlagsException], f.format("%(n"))
    }

    locally {
      val f = new Formatter()
      assertThrows(classOf[IllegalFormatWidthException], f.format("%4n"))
    }

    locally {
      val f = new Formatter()
      assertThrows(classOf[IllegalFormatWidthException], f.format("%-4n"))
    }

    locally {
      val f = new Formatter()
      assertThrows(classOf[IllegalFormatPrecisionException], f.format("%.9n"))
    }

    locally {
      val f = new Formatter()
      assertThrows(classOf[IllegalFormatPrecisionException], f.format("%5.9n"))
    }
  }

  @Test def formatForPercent(): Unit = {
    locally {
      val f = new Formatter()
      f.format("%1$%", 100.asInstanceOf[Object])
      assertEquals("%", f.toString())
    }

    locally {
      val f = new Formatter()
      f.format("%1$%%%", "hello", new Object())
      assertEquals("%%", f.toString())
    }

    locally {
      val f = new Formatter()
      f.format("%%%s", "hello")
      assertEquals("%hello", f.toString())
    }

    locally {
      val f = new Formatter()
      assertThrows(classOf[IllegalFormatPrecisionException], f.format("%.9%"))
    }

    locally {
      val f = new Formatter()
      assertThrows(classOf[IllegalFormatPrecisionException], f.format("%5.9%"))
    }

    if (!executingInJVM) { // https://bugs.openjdk.java.net/browse/JDK-8260221
      val f = new Formatter()
      assertFormatFlagsConversionMismatchException(f, "%+%")
      assertFormatFlagsConversionMismatchException(f, "%#%")
      assertFormatFlagsConversionMismatchException(f, "% %")
      assertFormatFlagsConversionMismatchException(f, "%0%")
      assertFormatFlagsConversionMismatchException(f, "%,%")
      assertFormatFlagsConversionMismatchException(f, "%(%")
    }

    locally {
      val f = new Formatter()
      f.format("%4%", 1.asInstanceOf[Object])
      /*
       * fail on RI the output string should be right justified by appending
       * spaces till the whole string is 4 chars width.
       */
      assertEquals("   %", f.toString())
    }

    locally {
      val f = new Formatter()
      f.format("%-4%", 100.asInstanceOf[Object])
      /*
       * fail on RI, throw UnknownFormatConversionException the output string
       * should be left justified by appending spaces till the whole string is
       * 4 chars width.
       */
      assertEquals("%   ", f.toString())
    }
  }

  private def assertIllegalFormatFlagsExceptionException(
      f: Formatter,
      str: String
  ): Unit = {
    assertThrows(classOf[IllegalFormatFlagsException], f.format(str))
  }

  private def assertFormatFlagsConversionMismatchException(
      f: Formatter,
      str: String
  ): Unit = {
    assertThrows(classOf[FormatFlagsConversionMismatchException], f.format(str))
  }

  @Test def formatForFlag(): Unit = {
    locally {
      val f = new Formatter()
      assertThrows(
        classOf[DuplicateFormatFlagsException],
        f.format("%1$-#-8s", "something")
      )
    }

    locally {
      val chars = Array('-', '#', '+', ' ', '0', ',', '(', '%', '<')
      Arrays.sort(chars)
      val f = new Formatter()
      for (i <- (0 to 256).map(_.toChar)) {
        // test 8 bit character
        if (Arrays.binarySearch(chars, i) >= 0 || Character.isDigit(i)
            || Character.isLetter(i)) {
          // Do not test 0-9, a-z, A-Z and characters in the chars array.
          // They are characters used as flags, width or conversions
        } else {
          assertThrows(
            classOf[UnknownFormatConversionException],
            f.format("%" + i + "s", 1.asInstanceOf[Object])
          )
        }
      }
    }
  }

  @Test def formatForGeneralConversionType_bB(): Unit = {
    val triple = Array[Array[Object]](
      Array(Boolean.box(false), "%3.2b", " fa"),
      Array(Boolean.box(false), "%-4.6b", "false"),
      Array(Boolean.box(false), "%.2b", "fa"),
      Array(Boolean.box(true), "%3.2b", " tr"),
      Array(Boolean.box(true), "%-4.6b", "true"),
      Array(Boolean.box(true), "%.2b", "tr"),
      Array(Char.box('c'), "%3.2b", " tr"),
      Array(Char.box('c'), "%-4.6b", "true"),
      Array(Char.box('c'), "%.2b", "tr"),
      Array(Byte.box(0x01.toByte), "%3.2b", " tr"),
      Array(Byte.box(0x01.toByte), "%-4.6b", "true"),
      Array(Byte.box(0x01.toByte), "%.2b", "tr"),
      Array(Short.box(0x0001.toShort), "%3.2b", " tr"),
      Array(Short.box(0x0001.toShort), "%-4.6b", "true"),
      Array(Short.box(0x0001.toShort), "%.2b", "tr"),
      Array(Int.box(1), "%3.2b", " tr"),
      Array(Int.box(1), "%-4.6b", "true"),
      Array(Int.box(1), "%.2b", "tr"),
      Array(Float.box(1.1f), "%3.2b", " tr"),
      Array(Float.box(1.1f), "%-4.6b", "true"),
      Array(Float.box(1.1f), "%.2b", "tr"),
      Array(Double.box(1.1d), "%3.2b", " tr"),
      Array(Double.box(1.1d), "%-4.6b", "true"),
      Array(Double.box(1.1d), "%.2b", "tr"),
      Array("", "%3.2b", " tr"),
      Array("", "%-4.6b", "true"),
      Array("", "%.2b", "tr"),
      Array("string content", "%3.2b", " tr"),
      Array("string content", "%-4.6b", "true"),
      Array("string content", "%.2b", "tr"),
      Array(new MockFormattable(), "%3.2b", " tr"),
      Array(new MockFormattable(), "%-4.6b", "true"),
      Array(new MockFormattable(), "%.2b", "tr"),
      Array(null.asInstanceOf[Object], "%3.2b", " fa"),
      Array(null.asInstanceOf[Object], "%-4.6b", "false"),
      Array(null.asInstanceOf[Object], "%.2b", "fa")
    )

    val input = 0
    val pattern = 1
    val output = 2
    for (i <- 0 until triple.length) {
      locally {
        val f = new Formatter()
        f.format(triple(i)(pattern).asInstanceOf[String], triple(i)(input))
        assertEquals(triple(i)(output), f.toString())
      }

      locally {
        val f = new Formatter()
        f.format(
          triple(i)(pattern).asInstanceOf[String].toUpperCase(),
          triple(i)(input)
        )
        assertEquals(
          triple(i)(output).asInstanceOf[String].toUpperCase(),
          f.toString()
        )
      }
    }
  }

  @Test def formatForFloatDoubleConversionType_sS_WithExcessPrecision()
      : Unit = {
    val triple = Array[Array[Any]](
      Array(1.1f, "%-6.4s", "1.1   "),
      Array(1.1f, "%.5s", "1.1"),
      Array(1.1d, "%-6.4s", "1.1   "),
      Array(1.1d, "%.5s", "1.1")
    )

    val input = 0
    val pattern = 1
    val output = 2
    for (i <- (0 until triple.length)) {
      locally {
        val f = new Formatter()
        f.format(
          triple(i)(pattern).asInstanceOf[String],
          triple(i)(input).asInstanceOf[Object]
        )
        assertEquals(triple(i)(output), f.toString())
      }

      locally {
        val f = new Formatter()
        f.format(
          triple(i)(pattern).asInstanceOf[String].toUpperCase(),
          triple(i)(input).asInstanceOf[Object]
        )
        assertEquals(
          triple(i)(output).asInstanceOf[String].toUpperCase(),
          f.toString()
        )
      }
    }
  }

  @Test def formatForGeneralConversionType_sS(): Unit = {
    val triple = Array[Array[Object]](
      Array(Boolean.box(false), "%2.3s", "fal"),
      Array(Boolean.box(false), "%-6.4s", "fals  "),
      Array(Boolean.box(false), "%.5s", "false"),
      Array(Boolean.box(true), "%2.3s", "tru"),
      Array(Boolean.box(true), "%-6.4s", "true  "),
      Array(Boolean.box(true), "%.5s", "true"),
      Array(Char.box('c'), "%2.3s", " c"),
      Array(Char.box('c'), "%-6.4s", "c     "),
      Array(Char.box('c'), "%.5s", "c"),
      Array(Byte.box(0x01.toByte), "%2.3s", " 1"),
      Array(Byte.box(0x01.toByte), "%-6.4s", "1     "),
      Array(Byte.box(0x01.toByte), "%.5s", "1"),
      Array(Short.box(0x0001.toShort), "%2.3s", " 1"),
      Array(Short.box(0x0001.toShort), "%-6.4s", "1     "),
      Array(Short.box(0x0001.toShort), "%.5s", "1"),
      Array(Int.box(1), "%2.3s", " 1"),
      Array(Int.box(1), "%-6.4s", "1     "),
      Array(Int.box(1), "%.5s", "1"),
      Array(Float.box(1.1f), "%2.3s", "1.1"),
      Array(Float.box(1.1f), "%-6.4s", "1.1   "),
      Array(Float.box(1.1f), "%.5s", "1.1"),
      Array(Double.box(1.1d), "%2.3s", "1.1"),
      Array(Double.box(1.1d), "%-6.4s", "1.1   "),
      Array(Double.box(1.1d), "%.5s", "1.1"),
      Array("", "%2.3s", "  "),
      Array("", "%-6.4s", "      "),
      Array("", "%.5s", ""),
      Array("string content", "%2.3s", "str"),
      Array("string content", "%-6.4s", "stri  "),
      Array("string content", "%.5s", "strin"),
      Array(
        new MockFormattable(),
        "%2.3s",
        "customized format function width: 2 precision: 3"
      ),
      Array(
        new MockFormattable(),
        "%-6.4s",
        "customized format function width: 6 precision: 4"
      ),
      Array(
        new MockFormattable(),
        "%.5s",
        "customized format function width: -1 precision: 5"
      ),
      Array(null.asInstanceOf[Object], "%2.3s", "nul"),
      Array(null.asInstanceOf[Object], "%-6.4s", "null  "),
      Array(null.asInstanceOf[Object], "%.5s", "null")
    )

    val input = 0
    val pattern = 1
    val output = 2
    for (i <- (0 until triple.length)) {
      locally {
        val f = new Formatter()
        f.format(triple(i)(pattern).asInstanceOf[String], triple(i)(input))
        assertEquals(triple(i)(output), f.toString())
      }

      locally {
        val f = new Formatter()
        f.format(
          triple(i)(pattern).asInstanceOf[String].toUpperCase(),
          triple(i)(input)
        )
        assertEquals(
          triple(i)(output).asInstanceOf[String].toUpperCase(),
          f.toString()
        )
      }
    }
  }

  @Test def formatForGeneralConversionType_hH(): Unit = {
    val input = Array(
      false.asInstanceOf[Object],
      true.asInstanceOf[Object],
      'c'.asInstanceOf[Object],
      0x01.toByte.asInstanceOf[Object],
      0x0001.toShort.asInstanceOf[Object],
      1.asInstanceOf[Object],
      1.1f.asInstanceOf[Object],
      1.1d.asInstanceOf[Object],
      "",
      "string content",
      new MockFormattable(),
      null.asInstanceOf[Object]
    )

    for (i <- (0 until (input.length - 1))) { // Porting note: sic
      locally {
        val f = new Formatter()
        f.format("%h", input(i))
        assertEquals(Integer.toHexString(input(i).hashCode()), f.toString())
      }

      locally {
        val f = new Formatter()
        f.format("%H", input(i))
        assertEquals(
          Integer.toHexString(input(i).hashCode()).toUpperCase(),
          f.toString()
        )
      }
    }
  }

  @Test def formatForGeneralConversionOtherCases(): Unit = {
    val input = Array(
      false.asInstanceOf[Object],
      true.asInstanceOf[Object],
      'c'.asInstanceOf[Object],
      0x01.toByte.asInstanceOf[Object],
      0x0001.toShort.asInstanceOf[Object],
      1.asInstanceOf[Object],
      1.1f.asInstanceOf[Object],
      1.1d.asInstanceOf[Object],
      "",
      "string content",
      new MockFormattable(),
      null.asInstanceOf[Object]
    )
    val f = new Formatter()
    for (i <- (0 until input.length)) {
      if (!input(i).isInstanceOf[Formattable]) {
        /*
         * fail on RI, spec says if the '#' flag is present and the
         * argument is not a Formattable , then a
         * FormatFlagsConversionMismatchException will be thrown.
         */
        assertThrows(
          classOf[FormatFlagsConversionMismatchException],
          f.format("%#s", input(i))
        )
      } else {
        f.format("%#s%<-#8s", input(i))
        assertEquals(
          "customized format function width: -1 precision: -1customized format function width: 8 precision: -1",
          f.toString()
        )
      }
    }
  }

  @Test def formatForGeneralConversionException(): Unit = {
    locally {
      val flagMismatch = Array(
        "%#b",
        "%+b",
        "% b",
        "%03b",
        "%,b",
        "%(b",
        "%#B",
        "%+B",
        "% B",
        "%03B",
        "%,B",
        "%(B",
        "%#h",
        "%+h",
        "% h",
        "%03h",
        "%,h",
        "%(h",
        "%#H",
        "%+H",
        "% H",
        "%03H",
        "%,H",
        "%(H",
        "%+s",
        "% s",
        "%03s",
        "%,s",
        "%(s",
        "%+S",
        "% S",
        "%03S",
        "%,S",
        "%(S"
      )

      val f = new Formatter()

      for (i <- 0 until flagMismatch.length) {
        assertThrows(
          classOf[FormatFlagsConversionMismatchException],
          f.format(flagMismatch(i), "something")
        )
      }

      val missingWidth = Array("%-b", "%-B", "%-h", "%-H", "%-s", "%-S")
      for (i <- 0 until missingWidth.length) {
        assertThrows(
          classOf[MissingFormatWidthException],
          f.format(missingWidth(i), "something")
        )
      }
    }

    // Regression test
    locally {
      val f = new Formatter()
      assertThrows(
        classOf[IllegalFormatCodePointException],
        f.format("%c", -0x0001.toByte.asInstanceOf[Object])
      )
    }

    locally {
      val f = new Formatter()
      assertThrows(
        classOf[IllegalFormatCodePointException],
        f.format("%c", -0x0001.toShort.asInstanceOf[Object])
      )
    }

    locally {
      val f = new Formatter()
      assertThrows(
        classOf[IllegalFormatCodePointException],
        f.format("%c", -0x0001.asInstanceOf[Object])
      )
    }
  }

  @Test def formatForCharacterConversion(): Unit = {
    val f = new Formatter()
    val illArgs = Array(true, 1.1f, 1.1d, "string content", 1.1f, new Date())
    for (i <- (0 until illArgs.length)) {
      assertThrows(
        classOf[IllegalFormatConversionException],
        f.format("%c", illArgs(i).asInstanceOf[Object])
      )
    }

    assertThrows(
      classOf[IllegalFormatCodePointException],
      f.format("%c", Integer.MAX_VALUE.asInstanceOf[Object])
    )

    assertThrows(
      classOf[FormatFlagsConversionMismatchException],
      f.format("%#c", 'c'.asInstanceOf[Object])
    )

    val triple = Array[Array[Any]](
      Array('c', "%c", "c"),
      Array('c', "%-2c", "c "),
      Array('\u0123', "%c", "\u0123"),
      Array('\u0123', "%-2c", "\u0123 "),
      Array(0x11.toByte, "%c", "\u0011"),
      Array(0x11.toByte, "%-2c", "\u0011 "),
      Array(0x1111.toShort, "%c", "\u1111"),
      Array(0x1111.toShort, "%-2c", "\u1111 "),
      Array(0x11, "%c", "\u0011"),
      Array(0x11, "%-2c", "\u0011 ")
    )

    val input = 0
    val pattern = 1
    val output = 2
    for (i <- 0 until triple.length) {
      val f = new Formatter()
      f.format(
        triple(i)(pattern).asInstanceOf[String],
        triple(i)(input).asInstanceOf[Object]
      )
      assertEquals(triple(i)(output), f.toString())
    }

    locally {
      val f = new Formatter()
      f.format("%c", 0x10000.asInstanceOf[Object])
      assertEquals(0x10000, f.toString().codePointAt(0))

      assertThrows(
        classOf[IllegalFormatPrecisionException],
        f.format("%2.2c", 'c'.asInstanceOf[Object])
      )
    }

    locally {
      val f = new Formatter()
      f.format("%C", 'w'.asInstanceOf[Object])
      // error on RI, throw UnknownFormatConversionException
      // RI do not support converter 'C'
      assertEquals("W", f.toString())
    }

    locally {
      val f = new Formatter()
      f.format("%Ced", 0x1111.asInstanceOf[Object])
      // error on RI, throw UnknownFormatConversionException
      // RI do not support converter 'C'
      assertEquals("\u1111ed", f.toString())
    }
  }

  @Test def formatForLegalByteShortIntegerLongConversionType_d(): Unit = {
    val triple = Array[Array[Any]](
      Array(0, "%d", "0"),
      Array(0, "%10d", "         0"),
      Array(0, "%-1d", "0"),
      Array(0, "%+d", "+0"),
      Array(0, "% d", " 0"),
      Array(0, "%,d", "0"),
      Array(0, "%(d", "0"),
      Array(0, "%08d", "00000000"),
      Array(0, "%-+,(11d", "+0         "),
      Array(0, "%0 ,(11d", " 0000000000"),
      Array(0xff.toByte, "%d", "-1"),
      Array(0xff.toByte, "%10d", "        -1"),
      Array(0xff.toByte, "%-1d", "-1"),
      Array(0xff.toByte, "%+d", "-1"),
      Array(0xff.toByte, "% d", "-1"),
      Array(0xff.toByte, "%,d", "-1"),
      Array(0xff.toByte, "%(d", "(1)"),
      Array(0xff.toByte, "%08d", "-0000001"),
      Array(0xff.toByte, "%-+,(11d", "(1)        "),
      Array(0xff.toByte, "%0 ,(11d", "(000000001)"),
      Array(0xf123.toShort, "%d", "-3805"),
      Array(0xf123.toShort, "%10d", "     -3805"),
      Array(0xf123.toShort, "%-1d", "-3805"),
      Array(0xf123.toShort, "%+d", "-3805"),
      Array(0xf123.toShort, "% d", "-3805"),
      Array(0xf123.toShort, "%,d", "-3,805"),
      Array(0xf123.toShort, "%(d", "(3805)"),
      Array(0xf123.toShort, "%08d", "-0003805"),
      Array(0xf123.toShort, "%-+,(11d", "(3,805)    "),
      Array(0xf123.toShort, "%0 ,(11d", "(00003,805)"),
      Array(0x123456, "%d", "1193046"),
      Array(0x123456, "%10d", "   1193046"),
      Array(0x123456, "%-1d", "1193046"),
      Array(0x123456, "%+d", "+1193046"),
      Array(0x123456, "% d", " 1193046"),
      Array(0x123456, "%,d", "1,193,046"),
      Array(0x123456, "%(d", "1193046"),
      Array(0x123456, "%08d", "01193046"),
      Array(0x123456, "%-+,(11d", "+1,193,046 "),
      Array(0x123456, "%0 ,(11d", " 01,193,046"),
      Array(-3, "%d", "-3"),
      Array(-3, "%10d", "        -3"),
      Array(-3, "%-1d", "-3"),
      Array(-3, "%+d", "-3"),
      Array(-3, "% d", "-3"),
      Array(-3, "%,d", "-3"),
      Array(-3, "%(d", "(3)"),
      Array(-3, "%08d", "-0000003"),
      Array(-3, "%-+,(11d", "(3)        "),
      Array(-3, "%0 ,(11d", "(000000003)"),
      Array(0x7654321L, "%d", "124076833"),
      Array(0x7654321L, "%10d", " 124076833"),
      Array(0x7654321L, "%-1d", "124076833"),
      Array(0x7654321L, "%+d", "+124076833"),
      Array(0x7654321L, "% d", " 124076833"),
      Array(0x7654321L, "%,d", "124,076,833"),
      Array(0x7654321L, "%(d", "124076833"),
      Array(0x7654321L, "%08d", "124076833"),
      Array(0x7654321L, "%-+,(11d", "+124,076,833"),
      Array(0x7654321L, "%0 ,(11d", " 124,076,833"),
      Array(-1L, "%d", "-1"),
      Array(-1L, "%10d", "        -1"),
      Array(-1L, "%-1d", "-1"),
      Array(-1L, "%+d", "-1"),
      Array(-1L, "% d", "-1"),
      Array(-1L, "%,d", "-1"),
      Array(-1L, "%(d", "(1)"),
      Array(-1L, "%08d", "-0000001"),
      Array(-1L, "%-+,(11d", "(1)        "),
      Array(-1L, "%0 ,(11d", "(000000001)")
    )

    val input = 0
    val pattern = 1
    val output = 2
    for (i <- 0 until triple.length) {
      val f = new Formatter()
      f.format(
        triple(i)(pattern).asInstanceOf[String],
        triple(i)(input).asInstanceOf[Object]
      )
      assertEquals(triple(i)(output), f.toString())
    }
  }

  @Test def formatForLegalByteShortIntegerLongConversionType_o(): Unit = {
    val triple = Array[Array[Any]](
      Array(0, "%o", "0"),
      Array(0, "%-6o", "0     "),
      Array(0, "%08o", "00000000"),
      Array(0, "%#o", "00"),
      Array(0, "%0#11o", "00000000000"),
      Array(0, "%-#9o", "00       "),
      Array(0xff.toByte, "%o", "377"),
      Array(0xff.toByte, "%-6o", "377   "),
      Array(0xff.toByte, "%08o", "00000377"),
      Array(0xff.toByte, "%#o", "0377"),
      Array(0xff.toByte, "%0#11o", "00000000377"),
      Array(0xff.toByte, "%-#9o", "0377     "),
      Array(0xf123.toShort, "%o", "170443"),
      Array(0xf123.toShort, "%-6o", "170443"),
      Array(0xf123.toShort, "%08o", "00170443"),
      Array(0xf123.toShort, "%#o", "0170443"),
      Array(0xf123.toShort, "%0#11o", "00000170443"),
      Array(0xf123.toShort, "%-#9o", "0170443  "),
      Array(0x123456, "%o", "4432126"),
      Array(0x123456, "%-6o", "4432126"),
      Array(0x123456, "%08o", "04432126"),
      Array(0x123456, "%#o", "04432126"),
      Array(0x123456, "%0#11o", "00004432126"),
      Array(0x123456, "%-#9o", "04432126 "),
      Array(-3, "%o", "37777777775"),
      Array(-3, "%-6o", "37777777775"),
      Array(-3, "%08o", "37777777775"),
      Array(-3, "%#o", "037777777775"),
      Array(-3, "%0#11o", "037777777775"),
      Array(-3, "%-#9o", "037777777775"),
      Array(0x7654321L, "%o", "731241441"),
      Array(0x7654321L, "%-6o", "731241441"),
      Array(0x7654321L, "%08o", "731241441"),
      Array(0x7654321L, "%#o", "0731241441"),
      Array(0x7654321L, "%0#11o", "00731241441"),
      Array(0x7654321L, "%-#9o", "0731241441"),
      Array(-1L, "%o", "1777777777777777777777"),
      Array(-1L, "%-6o", "1777777777777777777777"),
      Array(-1L, "%08o", "1777777777777777777777"),
      Array(-1L, "%#o", "01777777777777777777777"),
      Array(-1L, "%0#11o", "01777777777777777777777"),
      Array(-1L, "%-#9o", "01777777777777777777777")
    )

    val input = 0
    val pattern = 1
    val output = 2
    for (i <- 0 until triple.length) {
      val f = new Formatter()
      f.format(
        triple(i)(pattern).asInstanceOf[String],
        triple(i)(input).asInstanceOf[Object]
      )
      assertEquals(triple(i)(output), f.toString())
    }
  }

  @Test def formatForLegalByteShortIntegerLongConversionType_xX(): Unit = {
    val triple = Array[Array[Any]](
      Array(0, "%x", "0"),
      Array(0, "%-8x", "0       "),
      Array(0, "%06x", "000000"),
      Array(0, "%#x", "0x0"),
      Array(0, "%0#12x", "0x0000000000"),
      Array(0, "%-#9x", "0x0      "),
      Array(0xff.toByte, "%x", "ff"),
      Array(0xff.toByte, "%-8x", "ff      "),
      Array(0xff.toByte, "%06x", "0000ff"),
      Array(0xff.toByte, "%#x", "0xff"),
      Array(0xff.toByte, "%0#12x", "0x00000000ff"),
      Array(0xff.toByte, "%-#9x", "0xff     "),
      Array(0xf123.toShort, "%x", "f123"),
      Array(0xf123.toShort, "%-8x", "f123    "),
      Array(0xf123.toShort, "%06x", "00f123"),
      Array(0xf123.toShort, "%#x", "0xf123"),
      Array(0xf123.toShort, "%0#12x", "0x000000f123"),
      Array(0xf123.toShort, "%-#9x", "0xf123   "),
      Array(0x123456, "%x", "123456"),
      Array(0x123456, "%-8x", "123456  "),
      Array(0x123456, "%06x", "123456"),
      Array(0x123456, "%#x", "0x123456"),
      Array(0x123456, "%0#12x", "0x0000123456"),
      Array(0x123456, "%-#9x", "0x123456 "),
      Array(-3, "%x", "fffffffd"),
      Array(-3, "%-8x", "fffffffd"),
      Array(-3, "%06x", "fffffffd"),
      Array(-3, "%#x", "0xfffffffd"),
      Array(-3, "%0#12x", "0x00fffffffd"),
      Array(-3, "%-#9x", "0xfffffffd"),
      Array(0x7654321L, "%x", "7654321"),
      Array(0x7654321L, "%-8x", "7654321 "),
      Array(0x7654321L, "%06x", "7654321"),
      Array(0x7654321L, "%#x", "0x7654321"),
      Array(0x7654321L, "%0#12x", "0x0007654321"),
      Array(0x7654321L, "%-#9x", "0x7654321"),
      Array(-1L, "%x", "ffffffffffffffff"),
      Array(-1L, "%-8x", "ffffffffffffffff"),
      Array(-1L, "%06x", "ffffffffffffffff"),
      Array(-1L, "%#x", "0xffffffffffffffff"),
      Array(-1L, "%0#12x", "0xffffffffffffffff"),
      Array(-1L, "%-#9x", "0xffffffffffffffff")
    )

    val input = 0
    val pattern = 1
    val output = 2
    for (i <- 0 until triple.length) {
      locally {
        val f = new Formatter()
        f.format(
          triple(i)(pattern).asInstanceOf[String],
          triple(i)(input).asInstanceOf[Object]
        )
        assertEquals(triple(i)(output), f.toString())
      }

      locally {
        val f = new Formatter()
        f.format(
          triple(i)(pattern).asInstanceOf[String],
          triple(i)(input).asInstanceOf[Object]
        )
        assertEquals(triple(i)(output), f.toString())
      }
    }
  }

  @Test def formatForNullArgumentForByteShortIntegerLongBigIntegerConversion()
      : Unit = {
    locally {
      val f = new Formatter()
      f.format("%d%<o%<x%<5X", null.asInstanceOf[java.lang.Integer])
      assertEquals("nullnullnull NULL", f.toString())
    }

    locally {
      val f = new Formatter()
      f.format("%d%<#03o %<0#4x%<6X", null.asInstanceOf[java.lang.Long])
      assertEquals("nullnull null  NULL", f.toString())
    }

    locally {
      val f = new Formatter()
      f.format("%(+,07d%<o %<x%<6X", null.asInstanceOf[java.lang.Byte])
      assertEquals("   nullnull null  NULL", f.toString())
    }

    locally {
      val f = new Formatter()
      f.format("%(+,07d%<o %<x%<0#6X", null.asInstanceOf[java.lang.Short])
      assertEquals("   nullnull null  NULL", f.toString())
    }

    locally {
      val f = new Formatter()
      f.format("%(+,-7d%<( o%<+(x %<( 06X", null.asInstanceOf[BigInteger])
      assertEquals("null   nullnull   NULL", f.toString())
    }
  }

  @Test def formatForLegalBigIntegerConversionType_d(): Unit = {
    val tripleD = Array(
      Array(
        new BigInteger("123456789012345678901234567890"),
        "%d",
        "123456789012345678901234567890"
      ),
      Array(
        new BigInteger("123456789012345678901234567890"),
        "%10d",
        "123456789012345678901234567890"
      ),
      Array(
        new BigInteger("123456789012345678901234567890"),
        "%-1d",
        "123456789012345678901234567890"
      ),
      Array(
        new BigInteger("123456789012345678901234567890"),
        "%+d",
        "+123456789012345678901234567890"
      ),
      Array(
        new BigInteger("123456789012345678901234567890"),
        "% d",
        " 123456789012345678901234567890"
      ),
      Array(
        new BigInteger("123456789012345678901234567890"),
        "%,d",
        "123,456,789,012,345,678,901,234,567,890"
      ),
      Array(
        new BigInteger("123456789012345678901234567890"),
        "%(d",
        "123456789012345678901234567890"
      ),
      Array(
        new BigInteger("123456789012345678901234567890"),
        "%08d",
        "123456789012345678901234567890"
      ),
      Array(
        new BigInteger("123456789012345678901234567890"),
        "%-+,(11d",
        "+123,456,789,012,345,678,901,234,567,890"
      ),
      Array(
        new BigInteger("123456789012345678901234567890"),
        "%0 ,(11d",
        " 123,456,789,012,345,678,901,234,567,890"
      ),
      Array(
        new BigInteger("-9876543210987654321098765432100000"),
        "%d",
        "-9876543210987654321098765432100000"
      ),
      Array(
        new BigInteger("-9876543210987654321098765432100000"),
        "%10d",
        "-9876543210987654321098765432100000"
      ),
      Array(
        new BigInteger("-9876543210987654321098765432100000"),
        "%-1d",
        "-9876543210987654321098765432100000"
      ),
      Array(
        new BigInteger("-9876543210987654321098765432100000"),
        "%+d",
        "-9876543210987654321098765432100000"
      ),
      Array(
        new BigInteger("-9876543210987654321098765432100000"),
        "% d",
        "-9876543210987654321098765432100000"
      ),
      Array(
        new BigInteger("-9876543210987654321098765432100000"),
        "%,d",
        "-9,876,543,210,987,654,321,098,765,432,100,000"
      ),
      Array(
        new BigInteger("-9876543210987654321098765432100000"),
        "%(d",
        "(9876543210987654321098765432100000)"
      ),
      Array(
        new BigInteger("-9876543210987654321098765432100000"),
        "%08d",
        "-9876543210987654321098765432100000"
      ),
      Array(
        new BigInteger("-9876543210987654321098765432100000"),
        "%-+,(11d",
        "(9,876,543,210,987,654,321,098,765,432,100,000)"
      ),
      Array(
        new BigInteger("-9876543210987654321098765432100000"),
        "%0 ,(11d",
        "(9,876,543,210,987,654,321,098,765,432,100,000)"
      )
    )

    val input = 0
    val pattern = 1
    val output = 2
    for (i <- 0 until tripleD.length) {
      val f = new Formatter()
      f.format(tripleD(i)(pattern).asInstanceOf[String], tripleD(i)(input))
      assertEquals(tripleD(i)(output), f.toString())
    }

    val tripleO = Array(
      Array(
        new BigInteger("123456789012345678901234567890"),
        "%o",
        "143564417755415637016711617605322"
      ),
      Array(
        new BigInteger("123456789012345678901234567890"),
        "%-6o",
        "143564417755415637016711617605322"
      ),
      Array(
        new BigInteger("123456789012345678901234567890"),
        "%08o",
        "143564417755415637016711617605322"
      ),
      Array(
        new BigInteger("123456789012345678901234567890"),
        "%#o",
        "0143564417755415637016711617605322"
      ),
      Array(
        new BigInteger("123456789012345678901234567890"),
        "%0#11o",
        "0143564417755415637016711617605322"
      ),
      Array(
        new BigInteger("123456789012345678901234567890"),
        "%-#9o",
        "0143564417755415637016711617605322"
      ),
      Array(
        new BigInteger("-9876543210987654321098765432100000"),
        "%o",
        "-36336340043453651353467270113157312240"
      ),
      Array(
        new BigInteger("-9876543210987654321098765432100000"),
        "%-6o",
        "-36336340043453651353467270113157312240"
      ),
      Array(
        new BigInteger("-9876543210987654321098765432100000"),
        "%08o",
        "-36336340043453651353467270113157312240"
      ),
      Array(
        new BigInteger("-9876543210987654321098765432100000"),
        "%#o",
        "-036336340043453651353467270113157312240"
      ),
      Array(
        new BigInteger("-9876543210987654321098765432100000"),
        "%0#11o",
        "-036336340043453651353467270113157312240"
      ),
      Array(
        new BigInteger("-9876543210987654321098765432100000"),
        "%-#9o",
        "-036336340043453651353467270113157312240"
      )
    )

    for (i <- 0 until tripleO.length) {
      val f = new Formatter()
      f.format(tripleO(i)(pattern).asInstanceOf[String], tripleO(i)(input))
      assertEquals(tripleO(i)(output), f.toString())
    }

    val tripleX = Array(
      Array(
        new BigInteger("123456789012345678901234567890"),
        "%x",
        "18ee90ff6c373e0ee4e3f0ad2"
      ),
      Array(
        new BigInteger("123456789012345678901234567890"),
        "%-8x",
        "18ee90ff6c373e0ee4e3f0ad2"
      ),
      Array(
        new BigInteger("123456789012345678901234567890"),
        "%06x",
        "18ee90ff6c373e0ee4e3f0ad2"
      ),
      Array(
        new BigInteger("123456789012345678901234567890"),
        "%#x",
        "0x18ee90ff6c373e0ee4e3f0ad2"
      ),
      Array(
        new BigInteger("123456789012345678901234567890"),
        "%0#12x",
        "0x18ee90ff6c373e0ee4e3f0ad2"
      ),
      Array(
        new BigInteger("123456789012345678901234567890"),
        "%-#9x",
        "0x18ee90ff6c373e0ee4e3f0ad2"
      ),
      Array(
        new BigInteger("-9876543210987654321098765432100000"),
        "%x",
        "-1e6f380472bd4bae6eb8259bd94a0"
      ),
      Array(
        new BigInteger("-9876543210987654321098765432100000"),
        "%-8x",
        "-1e6f380472bd4bae6eb8259bd94a0"
      ),
      Array(
        new BigInteger("-9876543210987654321098765432100000"),
        "%06x",
        "-1e6f380472bd4bae6eb8259bd94a0"
      ),
      Array(
        new BigInteger("-9876543210987654321098765432100000"),
        "%#x",
        "-0x1e6f380472bd4bae6eb8259bd94a0"
      ),
      Array(
        new BigInteger("-9876543210987654321098765432100000"),
        "%0#12x",
        "-0x1e6f380472bd4bae6eb8259bd94a0"
      ),
      Array(
        new BigInteger("-9876543210987654321098765432100000"),
        "%-#9x",
        "-0x1e6f380472bd4bae6eb8259bd94a0"
      )
    )

    for (i <- 0 until tripleX.length) {
      val f = new Formatter()
      f.format(tripleX(i)(pattern).asInstanceOf[String], tripleX(i)(input))
      assertEquals(tripleX(i)(output), f.toString())
    }

    locally {
      val f = new Formatter()
      f.format("%(+,-7d%<( o%<+(x %<( 06X", null.asInstanceOf[BigInteger])
      assertEquals("null   nullnull   NULL", f.toString())
    }
  }

  @Test def formatForPaddingOfBigIntegerConversion(): Unit = {
    val bigInt = new BigInteger("123456789012345678901234567890")
    val negBigInt = new BigInteger("-1234567890123456789012345678901234567890")

    locally {
      val f = new Formatter()
      f.format("%32d", bigInt)
      assertEquals("  123456789012345678901234567890", f.toString())
    }

    locally {
      val f = new Formatter()
      f.format("%+32x", bigInt)
      assertEquals("      +18ee90ff6c373e0ee4e3f0ad2", f.toString())
    }

    locally {
      val f = new Formatter()
      f.format("% 32o", bigInt)
      assertEquals(" 143564417755415637016711617605322", f.toString())
    }

    locally {
      val f = new Formatter()
      f.format("%( 040X", negBigInt)
      assertEquals("(000003A0C92075C0DBF3B8ACBC5F96CE3F0AD2)", f.toString())
    }

    locally {
      val f = new Formatter()
      f.format("%+(045d", negBigInt)
      assertEquals(
        "(0001234567890123456789012345678901234567890)",
        f.toString()
      )
    }

    locally {
      val f = new Formatter()
      f.format("%+,-(60d", negBigInt)
      assertEquals(
        "(1,234,567,890,123,456,789,012,345,678,901,234,567,890)     ",
        f.toString()
      )
    }
  }

  @Test def formatForBigIntegerConversionException(): Unit = {
    val flagsConversionMismatches = Array("%#d", "%,o", "%,x", "%,X")
    for (i <- 0 until flagsConversionMismatches.length) {
      val f = new Formatter()
      assertThrows(
        classOf[FormatFlagsConversionMismatchException],
        f.format(flagsConversionMismatches(i), new BigInteger("1"))
      )
    }

    val missingFormatWidths = Array(
      "%-0d",
      "%0d",
      "%-d",
      "%-0o",
      "%0o",
      "%-o",
      "%-0x",
      "%0x",
      "%-x",
      "%-0X",
      "%0X",
      "%-X"
    )
    for (i <- 0 until missingFormatWidths.length) {
      val f = new Formatter()
      assertThrows(
        classOf[MissingFormatWidthException],
        f.format(missingFormatWidths(i), new BigInteger("1"))
      )
    }

    val illFlags =
      Array("%+ d", "%-08d", "%+ o", "%-08o", "%+ x", "%-08x", "%+ X", "%-08X")
    for (i <- 0 until illFlags.length) {
      val f = new Formatter()
      assertThrows(
        classOf[IllegalFormatFlagsException],
        f.format(illFlags(i), new BigInteger("1"))
      )
    }

    val precisionExceptions = Array("%.4d", "%2.5o", "%8.6x", "%11.17X")
    for (i <- 0 until precisionExceptions.length) {
      val f = new Formatter()
      assertThrows(
        classOf[IllegalFormatPrecisionException],
        f.format(precisionExceptions(i), new BigInteger("1"))
      )
    }

    locally {
      val f = new Formatter()
      assertThrows(
        classOf[UnknownFormatConversionException],
        f.format("%D", new BigInteger("1"))
      )
    }

    locally {
      val f = new Formatter()
      assertThrows(
        classOf[UnknownFormatConversionException],
        f.format("%O", new BigInteger("1"))
      )
    }

    locally {
      val f = new Formatter()
      assertThrows(
        classOf[MissingFormatWidthException],
        f.format("%010000000000000000000000000000000001d", new BigInteger("1"))
      )
    }
  }

  @Test def formatForBigIntegerExceptionThrowingOrder(): Unit = {
    val big = new BigInteger("100")

    /*
     * Order summary: UnknownFormatConversionException >
     * MissingFormatWidthException > IllegalFormatFlagsException >
     * IllegalFormatPrecisionException > IllegalFormatConversionException >
     * FormatFlagsConversionMismatchException
     *
     */
    val f = new Formatter()
    // compare IllegalFormatConversionException and
    // FormatFlagsConversionMismatchException
    assertThrows(
      classOf[IllegalFormatConversionException],
      f.format("%(o", false.asInstanceOf[Object])
    )

    // compare IllegalFormatPrecisionException and
    // IllegalFormatConversionException
    assertThrows(
      classOf[IllegalFormatPrecisionException],
      f.format("%.4o", false.asInstanceOf[Object])
    )

    // compare IllegalFormatFlagsException and
    // IllegalFormatPrecisionException
    assertThrows(classOf[IllegalFormatFlagsException], f.format("%+ .4o", big))

    // compare MissingFormatWidthException and
    // IllegalFormatFlagsException
    assertThrows(classOf[MissingFormatWidthException], f.format("%+ -o", big))

    // compare UnknownFormatConversionException and
    // MissingFormatWidthException
    assertThrows(
      classOf[UnknownFormatConversionException],
      f.format("%-O", big)
    )
  }

  @Test def formatForFloatDoubleConversionType_eE(): Unit = {
    val tripleE = Array[Array[Any]](
      Array(0f, "%e", "0.000000e+00"),
      Array(0f, "%#.0e", "0.e+00"),
      Array(0f, "%#- (9.8e", " 0.00000000e+00"),
      Array(0f, "%#+0(8.4e", "+0.0000e+00"),
      Array(0f, "%-+(1.6e", "+0.000000e+00"),
      Array(0f, "% 0(12e", " 0.000000e+00"),
      Array(101f, "%e", "1.010000e+02"),
      Array(101f, "%#.0e", "1.e+02"),
      Array(101f, "%#- (9.8e", " 1.01000000e+02"),
      Array(101f, "%#+0(8.4e", "+1.0100e+02"),
      Array(101f, "%-+(1.6e", "+1.010000e+02"),
      Array(101f, "% 0(12e", " 1.010000e+02"),
      Array(1.0f, "%e", "1.000000e+00"),
      Array(1.0f, "%#.0e", "1.e+00"),
      Array(1.0f, "%#- (9.8e", " 1.00000000e+00"),
      Array(1.0f, "%#+0(8.4e", "+1.0000e+00"),
      Array(1.0f, "%-+(1.6e", "+1.000000e+00"),
      Array(1.0f, "% 0(12e", " 1.000000e+00"),
      Array(-98f, "%e", "-9.800000e+01"),
      Array(-98f, "%#.0e", "-1.e+02"),
      Array(-98f, "%#- (9.8e", "(9.80000000e+01)"),
      Array(-98f, "%#+0(8.4e", "(9.8000e+01)"),
      Array(-98f, "%-+(1.6e", "(9.800000e+01)"),
      Array(-98f, "% 0(12e", "(9.800000e+01)"),
      Array(1.23f, "%e", "1.230000e+00"),
      Array(1.23f, "%#.0e", "1.e+00"),
      Array(1.23f, "%#- (9.8e", " 1.23000002e+00"),
      Array(1.23f, "%#+0(8.4e", "+1.2300e+00"),
      Array(1.23f, "%-+(1.6e", "+1.230000e+00"),
      Array(1.23f, "% 0(12e", " 1.230000e+00"),
      Array(34.1234567f, "%e", "3.412346e+01"),
      Array(34.1234567f, "%#.0e", "3.e+01"),
      Array(34.1234567f, "%#- (9.8e", " 3.41234550e+01"),
      Array(34.1234567f, "%#+0(8.4e", "+3.4123e+01"),
      Array(34.1234567f, "%-+(1.6e", "+3.412346e+01"),
      Array(34.1234567f, "% 0(12e", " 3.412346e+01"),
      Array(-.12345f, "%e", "-1.234500e-01"),
      Array(-.12345f, "%#.0e", "-1.e-01"),
      Array(-.12345f, "%#- (9.8e", "(1.23450004e-01)"),
      Array(-.12345f, "%#+0(8.4e", "(1.2345e-01)"),
      Array(-.12345f, "%-+(1.6e", "(1.234500e-01)"),
      Array(-.12345f, "% 0(12e", "(1.234500e-01)"),
      Array(-9876.1234567f, "%e", "-9.876123e+03"),
      Array(-9876.1234567f, "%#.0e", "-1.e+04"),
      Array(-9876.1234567f, "%#- (9.8e", "(9.87612305e+03)"),
      Array(-9876.1234567f, "%#+0(8.4e", "(9.8761e+03)"),
      Array(-9876.1234567f, "%-+(1.6e", "(9.876123e+03)"),
      Array(-9876.1234567f, "% 0(12e", "(9.876123e+03)"),
      Array(java.lang.Float.MAX_VALUE, "%e", "3.402823e+38"),
      Array(java.lang.Float.MAX_VALUE, "%#.0e", "3.e+38"),
      Array(java.lang.Float.MAX_VALUE, "%#- (9.8e", " 3.40282347e+38"),
      Array(java.lang.Float.MAX_VALUE, "%#+0(8.4e", "+3.4028e+38"),
      Array(java.lang.Float.MAX_VALUE, "%-+(1.6e", "+3.402823e+38"),
      Array(java.lang.Float.MAX_VALUE, "% 0(12e", " 3.402823e+38"),
      Array(java.lang.Float.MIN_VALUE, "%e", "1.401298e-45"),
      Array(java.lang.Float.MIN_VALUE, "%#.0e", "1.e-45"),
      Array(java.lang.Float.MIN_VALUE, "%#- (9.8e", " 1.40129846e-45"),
      Array(java.lang.Float.MIN_VALUE, "%#+0(8.4e", "+1.4013e-45"),
      Array(java.lang.Float.MIN_VALUE, "%-+(1.6e", "+1.401298e-45"),
      Array(java.lang.Float.MIN_VALUE, "% 0(12e", " 1.401298e-45"),
      Array(java.lang.Float.NaN, "%e", "NaN"),
      Array(java.lang.Float.NaN, "%#.0e", "NaN"),
      Array(java.lang.Float.NaN, "%#- (9.8e", "NaN      "),
      Array(java.lang.Float.NaN, "%#+0(8.4e", "     NaN"),
      Array(java.lang.Float.NaN, "%-+(1.6e", "NaN"),
      Array(java.lang.Float.NaN, "% 0(12e", "         NaN"),
      Array(java.lang.Float.NEGATIVE_INFINITY, "%e", "-Infinity"),
      Array(java.lang.Float.NEGATIVE_INFINITY, "%#.0e", "-Infinity"),
      Array(java.lang.Float.NEGATIVE_INFINITY, "%#- (9.8e", "(Infinity)"),
      Array(java.lang.Float.NEGATIVE_INFINITY, "%#+0(8.4e", "(Infinity)"),
      Array(java.lang.Float.NEGATIVE_INFINITY, "%-+(1.6e", "(Infinity)"),
      Array(java.lang.Float.NEGATIVE_INFINITY, "% 0(12e", "  (Infinity)"),
      Array(java.lang.Float.NEGATIVE_INFINITY, "%e", "-Infinity"),
      Array(java.lang.Float.NEGATIVE_INFINITY, "%#.0e", "-Infinity"),
      Array(java.lang.Float.NEGATIVE_INFINITY, "%#- (9.8e", "(Infinity)"),
      Array(java.lang.Float.NEGATIVE_INFINITY, "%#+0(8.4e", "(Infinity)"),
      Array(java.lang.Float.NEGATIVE_INFINITY, "%-+(1.6e", "(Infinity)"),
      Array(java.lang.Float.NEGATIVE_INFINITY, "% 0(12e", "  (Infinity)"),
      Array(0d, "%e", "0.000000e+00"),
      Array(0d, "%#.0e", "0.e+00"),
      Array(0d, "%#- (9.8e", " 0.00000000e+00"),
      Array(0d, "%#+0(8.4e", "+0.0000e+00"),
      Array(0d, "%-+(1.6e", "+0.000000e+00"),
      Array(0d, "% 0(12e", " 0.000000e+00"),
      Array(1d, "%e", "1.000000e+00"),
      Array(1d, "%#.0e", "1.e+00"),
      Array(1d, "%#- (9.8e", " 1.00000000e+00"),
      Array(1d, "%#+0(8.4e", "+1.0000e+00"),
      Array(1d, "%-+(1.6e", "+1.000000e+00"),
      Array(1d, "% 0(12e", " 1.000000e+00"),
      Array(-1d, "%e", "-1.000000e+00"),
      Array(-1d, "%#.0e", "-1.e+00"),
      Array(-1d, "%#- (9.8e", "(1.00000000e+00)"),
      Array(-1d, "%#+0(8.4e", "(1.0000e+00)"),
      Array(-1d, "%-+(1.6e", "(1.000000e+00)"),
      Array(-1d, "% 0(12e", "(1.000000e+00)"),
      Array(.00000001d, "%e", "1.000000e-08"),
      Array(.00000001d, "%#.0e", "1.e-08"),
      Array(.00000001d, "%#- (9.8e", " 1.00000000e-08"),
      Array(.00000001d, "%#+0(8.4e", "+1.0000e-08"),
      Array(.00000001d, "%-+(1.6e", "+1.000000e-08"),
      Array(.00000001d, "% 0(12e", " 1.000000e-08"),
      Array(9122.10d, "%e", "9.122100e+03"),
      Array(9122.10d, "%#.0e", "9.e+03"),
      Array(9122.10d, "%#- (9.8e", " 9.12210000e+03"),
      Array(9122.10d, "%#+0(8.4e", "+9.1221e+03"),
      Array(9122.10d, "%-+(1.6e", "+9.122100e+03"),
      Array(9122.10d, "% 0(12e", " 9.122100e+03"),
      Array(0.1d, "%e", "1.000000e-01"),
      Array(0.1d, "%#.0e", "1.e-01"),
      Array(0.1d, "%#- (9.8e", " 1.00000000e-01"),
      Array(0.1d, "%#+0(8.4e", "+1.0000e-01"),
      Array(0.1d, "%-+(1.6e", "+1.000000e-01"),
      Array(0.1d, "% 0(12e", " 1.000000e-01"),
      Array(-2.0d, "%e", "-2.000000e+00"),
      Array(-2.0d, "%#.0e", "-2.e+00"),
      Array(-2.0d, "%#- (9.8e", "(2.00000000e+00)"),
      Array(-2.0d, "%#+0(8.4e", "(2.0000e+00)"),
      Array(-2.0d, "%-+(1.6e", "(2.000000e+00)"),
      Array(-2.0d, "% 0(12e", "(2.000000e+00)"),
      Array(-.39d, "%e", "-3.900000e-01"),
      Array(-.39d, "%#.0e", "-4.e-01"),
      Array(-.39d, "%#- (9.8e", "(3.90000000e-01)"),
      Array(-.39d, "%#+0(8.4e", "(3.9000e-01)"),
      Array(-.39d, "%-+(1.6e", "(3.900000e-01)"),
      Array(-.39d, "% 0(12e", "(3.900000e-01)"),
      Array(-1234567890.012345678d, "%e", "-1.234568e+09"),
      Array(-1234567890.012345678d, "%#.0e", "-1.e+09"),
      Array(-1234567890.012345678d, "%#- (9.8e", "(1.23456789e+09)"),
      Array(-1234567890.012345678d, "%#+0(8.4e", "(1.2346e+09)"),
      Array(-1234567890.012345678d, "%-+(1.6e", "(1.234568e+09)"),
      Array(-1234567890.012345678d, "% 0(12e", "(1.234568e+09)"),
      Array(java.lang.Double.MAX_VALUE, "%e", "1.797693e+308"),
      Array(java.lang.Double.MAX_VALUE, "%#.0e", "2.e+308"),
      Array(java.lang.Double.MAX_VALUE, "%#- (9.8e", " 1.79769313e+308"),
      Array(java.lang.Double.MAX_VALUE, "%#+0(8.4e", "+1.7977e+308"),
      Array(java.lang.Double.MAX_VALUE, "%-+(1.6e", "+1.797693e+308"),
      Array(java.lang.Double.MAX_VALUE, "% 0(12e", " 1.797693e+308"),
      Array(java.lang.Double.MIN_VALUE, "%e", "4.900000e-324"),
      Array(java.lang.Double.MIN_VALUE, "%#.0e", "5.e-324"),
      Array(java.lang.Double.MIN_VALUE, "%#- (9.8e", " 4.90000000e-324"),
      Array(java.lang.Double.MIN_VALUE, "%#+0(8.4e", "+4.9000e-324"),
      Array(java.lang.Double.MIN_VALUE, "%-+(1.6e", "+4.900000e-324"),
      Array(java.lang.Double.MIN_VALUE, "% 0(12e", " 4.900000e-324"),
      Array(java.lang.Double.NaN, "%e", "NaN"),
      Array(java.lang.Double.NaN, "%#.0e", "NaN"),
      Array(java.lang.Double.NaN, "%#- (9.8e", "NaN      "),
      Array(java.lang.Double.NaN, "%#+0(8.4e", "     NaN"),
      Array(java.lang.Double.NaN, "%-+(1.6e", "NaN"),
      Array(java.lang.Double.NaN, "% 0(12e", "         NaN"),
      Array(java.lang.Double.NEGATIVE_INFINITY, "%e", "-Infinity"),
      Array(java.lang.Double.NEGATIVE_INFINITY, "%#.0e", "-Infinity"),
      Array(java.lang.Double.NEGATIVE_INFINITY, "%#- (9.8e", "(Infinity)"),
      Array(java.lang.Double.NEGATIVE_INFINITY, "%#+0(8.4e", "(Infinity)"),
      Array(java.lang.Double.NEGATIVE_INFINITY, "%-+(1.6e", "(Infinity)"),
      Array(java.lang.Double.NEGATIVE_INFINITY, "% 0(12e", "  (Infinity)"),
      Array(java.lang.Double.POSITIVE_INFINITY, "%e", "Infinity"),
      Array(java.lang.Double.POSITIVE_INFINITY, "%#.0e", "Infinity"),
      Array(java.lang.Double.POSITIVE_INFINITY, "%#- (9.8e", " Infinity"),
      Array(java.lang.Double.POSITIVE_INFINITY, "%#+0(8.4e", "+Infinity"),
      Array(java.lang.Double.POSITIVE_INFINITY, "%-+(1.6e", "+Infinity"),
      Array(java.lang.Double.POSITIVE_INFINITY, "% 0(12e", "    Infinity")
    )

    val input = 0
    val pattern = 1
    val output = 2
    for (i <- 0 until tripleE.length) {
      locally {
        val f = new Formatter()
        f.format(
          tripleE(i)(pattern).asInstanceOf[String],
          tripleE(i)(input).asInstanceOf[Object]
        )
        assertEquals(tripleE(i)(output), f.toString())
      }

      // test for conversion type 'E'
      locally {
        val f = new Formatter()
        f.format(
          tripleE(i)(pattern).asInstanceOf[String].toUpperCase(),
          tripleE(i)(input).asInstanceOf[Object]
        )
        assertEquals(
          tripleE(i)(output).asInstanceOf[String].toUpperCase(),
          f.toString()
        )
      }
    }

    val f = new Formatter()
    f.format("%e", 1001f.asInstanceOf[Object])
    /*
     * fail on RI, spec says 'e' requires the output to be formatted in
     * general scientific notation and the localization algorithm is
     * applied. But RI format this case to 1.001000e+03, which does not
     * conform to the German Locale
     */
    assertEquals("1.001000e+03", f.toString())
  }

  @Test def formatForFloatDoubleConversionType_gG(): Unit = {
    val tripleG = Array[Array[Any]](
      Array(1001f, "%g", "1001.00"),
      Array(1001f, "%- (,9.8g", " 1,001.0000"),
      Array(1001f, "%+0(,8.4g", "+001,001"),
      Array(1001f, "%-+(,1.6g", "+1,001.00"),
      Array(1001f, "% 0(,12.0g", " 0000001e+03"),
      Array(1.0f, "%g", "1.00000"),
      Array(1.0f, "%- (,9.8g", " 1.0000000"),
      Array(1.0f, "%+0(,8.4g", "+001.000"),
      Array(1.0f, "%-+(,1.6g", "+1.00000"),
      Array(1.0f, "% 0(,12.0g", " 00000000001"),
      Array(-98f, "%g", "-98.0000"),
      Array(-98f, "%- (,9.8g", "(98.000000)"),
      Array(-98f, "%+0(,8.4g", "(098.00)"),
      Array(-98f, "%-+(,1.6g", "(98.0000)"),
      Array(-98f, "% 0(,12.0g", "(000001e+02)"),
      Array(0.000001f, "%g", "1.00000e-06"),
      Array(0.000001f, "%- (,9.8g", " 1.0000000e-06"),
      Array(0.000001f, "%+0(,8.4g", "+1.000e-06"),
      Array(0.000001f, "%-+(,1.6g", "+1.00000e-06"),
      Array(0.000001f, "% 0(,12.0g", " 0000001e-06"),
      Array(345.1234567f, "%g", "345.123"),
      Array(345.1234567f, "%- (,9.8g", " 345.12344"),
      Array(345.1234567f, "%+0(,8.4g", "+00345.1"),
      Array(345.1234567f, "%-+(,1.6g", "+345.123"),
      Array(345.1234567f, "% 0(,12.0g", " 0000003e+02"),
      Array(-.00000012345f, "%g", "-1.23450e-07"),
      Array(-.00000012345f, "%- (,9.8g", "(1.2344999e-07)"),
      Array(-.00000012345f, "%+0(,8.4g", "(1.234e-07)"),
      Array(-.00000012345f, "%-+(,1.6g", "(1.23450e-07)"),
      Array(-.00000012345f, "% 0(,12.0g", "(000001e-07)"),
      Array(-987.1234567f, "%g", "-987.123"),
      Array(-987.1234567f, "%- (,9.8g", "(987.12347)"),
      Array(-987.1234567f, "%+0(,8.4g", "(0987.1)"),
      Array(-987.1234567f, "%-+(,1.6g", "(987.123)"),
      Array(-987.1234567f, "% 0(,12.0g", "(000001e+03)"),
      Array(java.lang.Float.MAX_VALUE, "%g", "3.40282e+38"),
      Array(java.lang.Float.MAX_VALUE, "%- (,9.8g", " 3.4028235e+38"),
      Array(java.lang.Float.MAX_VALUE, "%+0(,8.4g", "+3.403e+38"),
      Array(java.lang.Float.MAX_VALUE, "%-+(,1.6g", "+3.40282e+38"),
      Array(java.lang.Float.MAX_VALUE, "% 0(,12.0g", " 0000003e+38"),
      Array(java.lang.Float.MIN_VALUE, "%g", "1.40130e-45"),
      Array(java.lang.Float.MIN_VALUE, "%- (,9.8g", " 1.4012985e-45"),
      Array(java.lang.Float.MIN_VALUE, "%+0(,8.4g", "+1.401e-45"),
      Array(java.lang.Float.MIN_VALUE, "%-+(,1.6g", "+1.40130e-45"),
      Array(java.lang.Float.MIN_VALUE, "% 0(,12.0g", " 0000001e-45"),
      Array(java.lang.Float.NaN, "%g", "NaN"),
      Array(java.lang.Float.NaN, "%- (,9.8g", "NaN      "),
      Array(java.lang.Float.NaN, "%+0(,8.4g", "     NaN"),
      Array(java.lang.Float.NaN, "%-+(,1.6g", "NaN"),
      Array(java.lang.Float.NaN, "% 0(,12.0g", "         NaN"),
      Array(java.lang.Float.NEGATIVE_INFINITY, "%g", "-Infinity"),
      Array(java.lang.Float.NEGATIVE_INFINITY, "%- (,9.8g", "(Infinity)"),
      Array(java.lang.Float.NEGATIVE_INFINITY, "%+0(,8.4g", "(Infinity)"),
      Array(java.lang.Float.NEGATIVE_INFINITY, "%-+(,1.6g", "(Infinity)"),
      Array(java.lang.Float.NEGATIVE_INFINITY, "% 0(,12.0g", "  (Infinity)"),
      Array(java.lang.Float.POSITIVE_INFINITY, "%g", "Infinity"),
      Array(java.lang.Float.POSITIVE_INFINITY, "%- (,9.8g", " Infinity"),
      Array(java.lang.Float.POSITIVE_INFINITY, "%+0(,8.4g", "+Infinity"),
      Array(java.lang.Float.POSITIVE_INFINITY, "%-+(,1.6g", "+Infinity"),
      Array(java.lang.Float.POSITIVE_INFINITY, "% 0(,12.0g", "    Infinity"),
      Array(1d, "%g", "1.00000"),
      Array(1d, "%- (,9.8g", " 1.0000000"),
      Array(1d, "%+0(,8.4g", "+001.000"),
      Array(1d, "%-+(,1.6g", "+1.00000"),
      Array(1d, "% 0(,12.0g", " 00000000001"),
      Array(-1d, "%g", "-1.00000"),
      Array(-1d, "%- (,9.8g", "(1.0000000)"),
      Array(-1d, "%+0(,8.4g", "(01.000)"),
      Array(-1d, "%-+(,1.6g", "(1.00000)"),
      Array(-1d, "% 0(,12.0g", "(0000000001)"),
      Array(.00000001d, "%g", "1.00000e-08"),
      Array(.00000001d, "%- (,9.8g", " 1.0000000e-08"),
      Array(.00000001d, "%+0(,8.4g", "+1.000e-08"),
      Array(.00000001d, "%-+(,1.6g", "+1.00000e-08"),
      Array(.00000001d, "% 0(,12.0g", " 0000001e-08"),
      Array(1912.10d, "%g", "1912.10"),
      Array(1912.10d, "%- (,9.8g", " 1,912.1000"),
      Array(1912.10d, "%+0(,8.4g", "+001,912"),
      Array(1912.10d, "%-+(,1.6g", "+1,912.10"),
      Array(1912.10d, "% 0(,12.0g", " 0000002e+03"),
      Array(0.1d, "%g", "0.100000"),
      Array(0.1d, "%- (,9.8g", " 0.10000000"),
      Array(0.1d, "%+0(,8.4g", "+00.1000"),
      Array(0.1d, "%-+(,1.6g", "+0.100000"),
      Array(0.1d, "% 0(,12.0g", " 000000000.1"),
      Array(-2.0d, "%g", "-2.00000"),
      Array(-2.0d, "%- (,9.8g", "(2.0000000)"),
      Array(-2.0d, "%+0(,8.4g", "(02.000)"),
      Array(-2.0d, "%-+(,1.6g", "(2.00000)"),
      Array(-2.0d, "% 0(,12.0g", "(0000000002)"),
      Array(-.00039d, "%g", "-0.000390000"),
      Array(-.00039d, "%- (,9.8g", "(0.00039000000)"),
      Array(-.00039d, "%+0(,8.4g", "(0.0003900)"),
      Array(-.00039d, "%-+(,1.6g", "(0.000390000)"),
      Array(-.00039d, "% 0(,12.0g", "(00000.0004)"),
      Array(-1234567890.012345678d, "%g", "-1.23457e+09"),
      Array(-1234567890.012345678d, "%- (,9.8g", "(1.2345679e+09)"),
      Array(-1234567890.012345678d, "%+0(,8.4g", "(1.235e+09)"),
      Array(-1234567890.012345678d, "%-+(,1.6g", "(1.23457e+09)"),
      Array(-1234567890.012345678d, "% 0(,12.0g", "(000001e+09)"),
      Array(java.lang.Double.MAX_VALUE, "%g", "1.79769e+308"),
      Array(java.lang.Double.MAX_VALUE, "%- (,9.8g", " 1.7976931e+308"),
      Array(java.lang.Double.MAX_VALUE, "%+0(,8.4g", "+1.798e+308"),
      Array(java.lang.Double.MAX_VALUE, "%-+(,1.6g", "+1.79769e+308"),
      Array(java.lang.Double.MAX_VALUE, "% 0(,12.0g", " 000002e+308"),
      Array(java.lang.Double.MIN_VALUE, "%g", "4.90000e-324"),
      Array(java.lang.Double.MIN_VALUE, "%- (,9.8g", " 4.9000000e-324"),
      Array(java.lang.Double.MIN_VALUE, "%+0(,8.4g", "+4.900e-324"),
      Array(java.lang.Double.MIN_VALUE, "%-+(,1.6g", "+4.90000e-324"),
      Array(java.lang.Double.MIN_VALUE, "% 0(,12.0g", " 000005e-324"),
      Array(java.lang.Double.NaN, "%g", "NaN"),
      Array(java.lang.Double.NaN, "%- (,9.8g", "NaN      "),
      Array(java.lang.Double.NaN, "%+0(,8.4g", "     NaN"),
      Array(java.lang.Double.NaN, "%-+(,1.6g", "NaN"),
      Array(java.lang.Double.NaN, "% 0(,12.0g", "         NaN"),
      Array(java.lang.Double.NEGATIVE_INFINITY, "%g", "-Infinity"),
      Array(java.lang.Double.NEGATIVE_INFINITY, "%- (,9.8g", "(Infinity)"),
      Array(java.lang.Double.NEGATIVE_INFINITY, "%+0(,8.4g", "(Infinity)"),
      Array(java.lang.Double.NEGATIVE_INFINITY, "%-+(,1.6g", "(Infinity)"),
      Array(java.lang.Double.NEGATIVE_INFINITY, "% 0(,12.0g", "  (Infinity)"),
      Array(java.lang.Double.POSITIVE_INFINITY, "%g", "Infinity"),
      Array(java.lang.Double.POSITIVE_INFINITY, "%- (,9.8g", " Infinity"),
      Array(java.lang.Double.POSITIVE_INFINITY, "%+0(,8.4g", "+Infinity"),
      Array(java.lang.Double.POSITIVE_INFINITY, "%-+(,1.6g", "+Infinity"),
      Array(java.lang.Double.POSITIVE_INFINITY, "% 0(,12.0g", "    Infinity")
    )

    val input = 0
    val pattern = 1
    val output = 2
    for (i <- 0 until tripleG.length) {
      locally {
        val f = new Formatter()
        f.format(
          tripleG(i)(pattern).asInstanceOf[String],
          tripleG(i)(input).asInstanceOf[Object]
        )
        assertEquals(tripleG(i)(output), f.toString())
      }

      // test for conversion type 'G'
      locally {
        val f = new Formatter()
        f.format(
          tripleG(i)(pattern).asInstanceOf[String].toUpperCase(),
          tripleG(i)(input).asInstanceOf[Object]
        )
        assertEquals(
          tripleG(i)(output).asInstanceOf[String].toUpperCase(),
          f.toString()
        )
      }
    }

    locally {
      val f = new Formatter()
      f.format("%.5g", 0f.asInstanceOf[Object])
      assertEquals("0.0000", f.toString())
    }

    locally {
      val f = new Formatter()
      f.format("%.0g", 0f.asInstanceOf[Object])
      /*
       * fail on RI, spec says if the precision is 0, then it is taken to be
       * 1. but RI throws ArrayIndexOutOfBoundsException.
       */
      assertEquals("0", f.toString())
    }

    locally {
      val f = new Formatter()
      f.format("%g", 1001f.asInstanceOf[Object])
      /*
       * fail on RI, spec says 'g' requires the output to be formatted in
       * general scientific notation and the localization algorithm is
       * applied. But RI format this case to 1001.00, which does not conform
       * to the German Locale
       */
      assertEquals("1001.00", f.toString())
    }
  }

  @Test def formatForFloatDoubleConversionType_gG_Overflow(): Unit = {
    locally {
      val f = new Formatter()
      f.format("%g", 999999.5.asInstanceOf[Object])
      assertEquals("1.00000e+06", f.toString())
    }

    locally {
      val f = new Formatter()
      f.format("%g", 99999.5.asInstanceOf[Object])
      assertEquals("99999.5", f.toString())
    }

    locally {
      val f = new Formatter()
      f.format("%.4g", 99.95.asInstanceOf[Object])
      assertEquals("99.95", f.toString())
    }

    locally {
      val f = new Formatter()
      f.format("%g", 99.95.asInstanceOf[Object])
      assertEquals("99.9500", f.toString())
    }

    locally {
      val f = new Formatter()
      f.format("%g", 0.9.asInstanceOf[Object])
      assertEquals("0.900000", f.toString())
    }

    locally {
      val f = new Formatter()
      f.format("%.0g", 0.000095.asInstanceOf[Object])
      assertEquals("0.0001", f.toString())
    }

    locally {
      val f = new Formatter()
      f.format("%g", 0.0999999.asInstanceOf[Object])
      assertEquals("0.0999999", f.toString())
    }

    locally {
      val f = new Formatter()
      f.format("%g", 0.00009.asInstanceOf[Object])
      assertEquals("9.00000e-05", f.toString())
    }
  }

  @Test def formatForFloatDoubleMaxValueConversionType_f(): Unit = {
    // These need a way to reproduce the same decimal representation of
    // extreme values as JVM.
    val tripleF = Array[Array[Any]](
      Array(-1234567890.012345678d, "% 0#(9.8f", "(1234567890.01234580)"),
      Array(
        java.lang.Double.MAX_VALUE,
        "%f",
        "179769313486231570000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000.000000"
      ),
      Array(
        java.lang.Double.MAX_VALUE,
        "%#.3f",
        "179769313486231570000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000.000"
      ),
      Array(
        java.lang.Double.MAX_VALUE,
        "%,5f",
        "179,769,313,486,231,570,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000.000000"
      ),
      Array(
        java.lang.Double.MAX_VALUE,
        "%- (12.0f",
        " 179769313486231570000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
      ),
      Array(
        java.lang.Double.MAX_VALUE,
        "%#+0(1.6f",
        "+179769313486231570000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000.000000"
      ),
      Array(
        java.lang.Double.MAX_VALUE,
        "%-+(8.4f",
        "+179769313486231570000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000.0000"
      ),
      Array(
        java.lang.Double.MAX_VALUE,
        "% 0#(9.8f",
        " 179769313486231570000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000.00000000"
      ),
      Array(
        java.lang.Float.MAX_VALUE,
        "%f",
        "340282346638528860000000000000000000000.000000"
      ),
      Array(
        java.lang.Float.MAX_VALUE,
        "%#.3f",
        "340282346638528860000000000000000000000.000"
      ),
      Array(
        java.lang.Float.MAX_VALUE,
        "%,5f",
        "340,282,346,638,528,860,000,000,000,000,000,000,000.000000"
      ),
      Array(
        java.lang.Float.MAX_VALUE,
        "%- (12.0f",
        " 340282346638528860000000000000000000000"
      ),
      Array(
        java.lang.Float.MAX_VALUE,
        "%#+0(1.6f",
        "+340282346638528860000000000000000000000.000000"
      ),
      Array(
        java.lang.Float.MAX_VALUE,
        "%-+(8.4f",
        "+340282346638528860000000000000000000000.0000"
      ),
      Array(
        java.lang.Float.MAX_VALUE,
        "% 0#(9.8f",
        " 340282346638528860000000000000000000000.00000000"
      )
    )

    val input: Int = 0
    val pattern: Int = 1
    val output: Int = 2
    for (i <- 0 until tripleF.length) {
      val f = new Formatter()
      f.format(
        tripleF(i)(pattern).asInstanceOf[String],
        tripleF(i)(input).asInstanceOf[Object]
      )
      assertEquals(tripleF(i)(output), f.toString)
    }
  }

  @Test def formatForFloatDoubleConversionType_f(): Unit = {
    val tripleF = Array[Array[Any]](
      Array(0f, "%f", "0.000000"),
      Array(0f, "%#.3f", "0.000"),
      Array(0f, "%,5f", "0.000000"),
      Array(0f, "%- (12.0f", " 0          "),
      Array(0f, "%#+0(1.6f", "+0.000000"),
      Array(0f, "%-+(8.4f", "+0.0000 "),
      Array(0f, "% 0#(9.8f", " 0.00000000"),
      Array(1234f, "%f", "1234.000000"),
      Array(1234f, "%#.3f", "1234.000"),
      Array(1234f, "%,5f", "1,234.000000"),
      Array(1234f, "%- (12.0f", " 1234       "),
      Array(1234f, "%#+0(1.6f", "+1234.000000"),
      Array(1234f, "%-+(8.4f", "+1234.0000"),
      Array(1234f, "% 0#(9.8f", " 1234.00000000"),
      Array(1.0f, "%f", "1.000000"),
      Array(1.0f, "%#.3f", "1.000"),
      Array(1.0f, "%,5f", "1.000000"),
      Array(1.0f, "%- (12.0f", " 1          "),
      Array(1.0f, "%#+0(1.6f", "+1.000000"),
      Array(1.0f, "%-+(8.4f", "+1.0000 "),
      Array(1.0f, "% 0#(9.8f", " 1.00000000"),
      Array(-98f, "%f", "-98.000000"),
      Array(-98f, "%#.3f", "-98.000"),
      Array(-98f, "%,5f", "-98.000000"),
      Array(-98f, "%- (12.0f", "(98)        "),
      Array(-98f, "%#+0(1.6f", "(98.000000)"),
      Array(-98f, "%-+(8.4f", "(98.0000)"),
      Array(-98f, "% 0#(9.8f", "(98.00000000)"),
      Array(0.000001f, "%f", "0.000001"),
      Array(0.000001f, "%#.3f", "0.000"),
      Array(0.000001f, "%,5f", "0.000001"),
      Array(0.000001f, "%- (12.0f", " 0          "),
      Array(0.000001f, "%#+0(1.6f", "+0.000001"),
      Array(0.000001f, "%-+(8.4f", "+0.0000 "),
      Array(0.000001f, "% 0#(9.8f", " 0.00000100"),
      Array(345.1234567f, "%f", "345.123444"),
      Array(345.1234567f, "%#.3f", "345.123"),
      Array(345.1234567f, "%,5f", "345.123444"),
      Array(345.1234567f, "%- (12.0f", " 345        "),
      Array(345.1234567f, "%#+0(1.6f", "+345.123444"),
      Array(345.1234567f, "%-+(8.4f", "+345.1234"),
      Array(345.1234567f, "% 0#(9.8f", " 345.12344360"),
      Array(-.00000012345f, "%f", "-0.000000"),
      Array(-.00000012345f, "%#.3f", "-0.000"),
      Array(-.00000012345f, "%,5f", "-0.000000"),
      Array(-.00000012345f, "%- (12.0f", "(0)         "),
      Array(-.00000012345f, "%#+0(1.6f", "(0.000000)"),
      Array(-.00000012345f, "%-+(8.4f", "(0.0000)"),
      Array(-.00000012345f, "% 0#(9.8f", "(0.00000012)"),
      Array(-987654321.1234567f, "%f", "-987654336.000000"),
      Array(-987654321.1234567f, "%#.3f", "-987654336.000"),
      Array(-987654321.1234567f, "%,5f", "-987,654,336.000000"),
      Array(-987654321.1234567f, "%- (12.0f", "(987654336) "),
      Array(-987654321.1234567f, "%#+0(1.6f", "(987654336.000000)"),
      Array(-987654321.1234567f, "%-+(8.4f", "(987654336.0000)"),
      Array(-987654321.1234567f, "% 0#(9.8f", "(987654336.00000000)"),
      Array(java.lang.Float.MIN_VALUE, "%f", "0.000000"),
      Array(java.lang.Float.MIN_VALUE, "%#.3f", "0.000"),
      Array(java.lang.Float.MIN_VALUE, "%,5f", "0.000000"),
      Array(java.lang.Float.MIN_VALUE, "%- (12.0f", " 0          "),
      Array(java.lang.Float.MIN_VALUE, "%#+0(1.6f", "+0.000000"),
      Array(java.lang.Float.MIN_VALUE, "%-+(8.4f", "+0.0000 "),
      Array(java.lang.Float.MIN_VALUE, "% 0#(9.8f", " 0.00000000"),
      Array(java.lang.Float.NaN, "%f", "NaN"),
      Array(java.lang.Float.NaN, "%#.3f", "NaN"),
      Array(java.lang.Float.NaN, "%,5f", "  NaN"),
      Array(java.lang.Float.NaN, "%- (12.0f", "NaN         "),
      Array(java.lang.Float.NaN, "%#+0(1.6f", "NaN"),
      Array(java.lang.Float.NaN, "%-+(8.4f", "NaN     "),
      Array(java.lang.Float.NaN, "% 0#(9.8f", "      NaN"),
      Array(java.lang.Float.NEGATIVE_INFINITY, "%f", "-Infinity"),
      Array(java.lang.Float.NEGATIVE_INFINITY, "%#.3f", "-Infinity"),
      Array(java.lang.Float.NEGATIVE_INFINITY, "%,5f", "-Infinity"),
      Array(java.lang.Float.NEGATIVE_INFINITY, "%- (12.0f", "(Infinity)  "),
      Array(java.lang.Float.NEGATIVE_INFINITY, "%#+0(1.6f", "(Infinity)"),
      Array(java.lang.Float.NEGATIVE_INFINITY, "%-+(8.4f", "(Infinity)"),
      Array(java.lang.Float.NEGATIVE_INFINITY, "% 0#(9.8f", "(Infinity)"),
      Array(java.lang.Float.POSITIVE_INFINITY, "%f", "Infinity"),
      Array(java.lang.Float.POSITIVE_INFINITY, "%#.3f", "Infinity"),
      Array(java.lang.Float.POSITIVE_INFINITY, "%,5f", "Infinity"),
      Array(java.lang.Float.POSITIVE_INFINITY, "%- (12.0f", " Infinity   "),
      Array(java.lang.Float.POSITIVE_INFINITY, "%#+0(1.6f", "+Infinity"),
      Array(java.lang.Float.POSITIVE_INFINITY, "%-+(8.4f", "+Infinity"),
      Array(java.lang.Float.POSITIVE_INFINITY, "% 0#(9.8f", " Infinity"),
      Array(0d, "%f", "0.000000"),
      Array(0d, "%#.3f", "0.000"),
      Array(0d, "%,5f", "0.000000"),
      Array(0d, "%- (12.0f", " 0          "),
      Array(0d, "%#+0(1.6f", "+0.000000"),
      Array(0d, "%-+(8.4f", "+0.0000 "),
      Array(0d, "% 0#(9.8f", " 0.00000000"),
      Array(1d, "%f", "1.000000"),
      Array(1d, "%#.3f", "1.000"),
      Array(1d, "%,5f", "1.000000"),
      Array(1d, "%- (12.0f", " 1          "),
      Array(1d, "%#+0(1.6f", "+1.000000"),
      Array(1d, "%-+(8.4f", "+1.0000 "),
      Array(1d, "% 0#(9.8f", " 1.00000000"),
      Array(-1d, "%f", "-1.000000"),
      Array(-1d, "%#.3f", "-1.000"),
      Array(-1d, "%,5f", "-1.000000"),
      Array(-1d, "%- (12.0f", "(1)         "),
      Array(-1d, "%#+0(1.6f", "(1.000000)"),
      Array(-1d, "%-+(8.4f", "(1.0000)"),
      Array(-1d, "% 0#(9.8f", "(1.00000000)"),
      Array(.00000001d, "%f", "0.000000"),
      Array(.00000001d, "%#.3f", "0.000"),
      Array(.00000001d, "%,5f", "0.000000"),
      Array(.00000001d, "%- (12.0f", " 0          "),
      Array(.00000001d, "%#+0(1.6f", "+0.000000"),
      Array(.00000001d, "%-+(8.4f", "+0.0000 "),
      Array(.00000001d, "% 0#(9.8f", " 0.00000001"),
      Array(1000.10d, "%f", "1000.100000"),
      Array(1000.10d, "%#.3f", "1000.100"),
      Array(1000.10d, "%,5f", "1,000.100000"),
      Array(1000.10d, "%- (12.0f", " 1000       "),
      Array(1000.10d, "%#+0(1.6f", "+1000.100000"),
      Array(1000.10d, "%-+(8.4f", "+1000.1000"),
      Array(1000.10d, "% 0#(9.8f", " 1000.10000000"),
      Array(0.1d, "%f", "0.100000"),
      Array(0.1d, "%#.3f", "0.100"),
      Array(0.1d, "%,5f", "0.100000"),
      Array(0.1d, "%- (12.0f", " 0          "),
      Array(0.1d, "%#+0(1.6f", "+0.100000"),
      Array(0.1d, "%-+(8.4f", "+0.1000 "),
      Array(0.1d, "% 0#(9.8f", " 0.10000000"),
      Array(-2.0d, "%f", "-2.000000"),
      Array(-2.0d, "%#.3f", "-2.000"),
      Array(-2.0d, "%,5f", "-2.000000"),
      Array(-2.0d, "%- (12.0f", "(2)         "),
      Array(-2.0d, "%#+0(1.6f", "(2.000000)"),
      Array(-2.0d, "%-+(8.4f", "(2.0000)"),
      Array(-2.0d, "% 0#(9.8f", "(2.00000000)"),
      Array(-.00009d, "%f", "-0.000090"),
      Array(-.00009d, "%#.3f", "-0.000"),
      Array(-.00009d, "%,5f", "-0.000090"),
      Array(-.00009d, "%- (12.0f", "(0)         "),
      Array(-.00009d, "%#+0(1.6f", "(0.000090)"),
      Array(-.00009d, "%-+(8.4f", "(0.0001)"),
      Array(-.00009d, "% 0#(9.8f", "(0.00009000)"),
      Array(-1234567890.012345678d, "%f", "-1234567890.012346"),
      Array(-1234567890.012345678d, "%#.3f", "-1234567890.012"),
      Array(-1234567890.012345678d, "%,5f", "-1,234,567,890.012346"),
      Array(-1234567890.012345678d, "%- (12.0f", "(1234567890)"),
      Array(-1234567890.012345678d, "%#+0(1.6f", "(1234567890.012346)"),
      Array(-1234567890.012345678d, "%-+(8.4f", "(1234567890.0123)"),
      Array(java.lang.Double.MIN_VALUE, "%f", "0.000000"),
      Array(java.lang.Double.MIN_VALUE, "%#.3f", "0.000"),
      Array(java.lang.Double.MIN_VALUE, "%,5f", "0.000000"),
      Array(java.lang.Double.MIN_VALUE, "%- (12.0f", " 0          "),
      Array(java.lang.Double.MIN_VALUE, "%#+0(1.6f", "+0.000000"),
      Array(java.lang.Double.MIN_VALUE, "%-+(8.4f", "+0.0000 "),
      Array(java.lang.Double.MIN_VALUE, "% 0#(9.8f", " 0.00000000"),
      Array(java.lang.Double.NaN, "%f", "NaN"),
      Array(java.lang.Double.NaN, "%#.3f", "NaN"),
      Array(java.lang.Double.NaN, "%,5f", "  NaN"),
      Array(java.lang.Double.NaN, "%- (12.0f", "NaN         "),
      Array(java.lang.Double.NaN, "%#+0(1.6f", "NaN"),
      Array(java.lang.Double.NaN, "%-+(8.4f", "NaN     "),
      Array(java.lang.Double.NaN, "% 0#(9.8f", "      NaN"),
      Array(java.lang.Double.POSITIVE_INFINITY, "%f", "Infinity"),
      Array(java.lang.Double.POSITIVE_INFINITY, "%#.3f", "Infinity"),
      Array(java.lang.Double.POSITIVE_INFINITY, "%,5f", "Infinity"),
      Array(java.lang.Double.POSITIVE_INFINITY, "%- (12.0f", " Infinity   "),
      Array(java.lang.Double.POSITIVE_INFINITY, "%#+0(1.6f", "+Infinity"),
      Array(java.lang.Double.POSITIVE_INFINITY, "%-+(8.4f", "+Infinity"),
      Array(java.lang.Double.POSITIVE_INFINITY, "% 0#(9.8f", " Infinity"),
      Array(java.lang.Double.NEGATIVE_INFINITY, "%f", "-Infinity"),
      Array(java.lang.Double.NEGATIVE_INFINITY, "%#.3f", "-Infinity"),
      Array(java.lang.Double.NEGATIVE_INFINITY, "%,5f", "-Infinity"),
      Array(java.lang.Double.NEGATIVE_INFINITY, "%- (12.0f", "(Infinity)  "),
      Array(java.lang.Double.NEGATIVE_INFINITY, "%#+0(1.6f", "(Infinity)"),
      Array(java.lang.Double.NEGATIVE_INFINITY, "%-+(8.4f", "(Infinity)"),
      Array(java.lang.Double.NEGATIVE_INFINITY, "% 0#(9.8f", "(Infinity)")
    )
    val input: Int = 0
    val pattern: Int = 1
    val output: Int = 2
    for (i <- 0 until tripleF.length) {
      val f = new Formatter()
      f.format(
        tripleF(i)(pattern).asInstanceOf[String],
        tripleF(i)(input).asInstanceOf[Object]
      )
      assertEquals(tripleF(i)(output), f.toString)
    }
  }

  @Test def formatForDoubleMinValueConversionType_aA(): Unit = {

    val tripleA = Array(
      Array(java.lang.Double.MIN_VALUE, "%a", "0x0.0000000000001p-1022"),
      Array(java.lang.Double.MIN_VALUE, "%5a", "0x0.0000000000001p-1022")
    ).asInstanceOf[Array[Array[Any]]]
    val input: Int = 0
    val pattern: Int = 1
    val output: Int = 2
    for (i <- 0 until tripleA.length) {
      locally {
        val f = new Formatter()
        f.format(
          tripleA(i)(pattern).asInstanceOf[String],
          tripleA(i)(input).asInstanceOf[Object]
        )
        assertEquals(tripleA(i)(output), f.toString)
      }
      // test for conversion type 'A'
      locally {
        val f = new Formatter()
        f.format(
          tripleA(i)(pattern).asInstanceOf[String].toUpperCase(),
          tripleA(i)(input).asInstanceOf[Object]
        )
        assertEquals(
          tripleA(i)(output).asInstanceOf[String].toUpperCase(),
          f.toString
        )
      }
    }
  }

  @Test def formatForFloatDoubleConversionType_aA(): Unit = {
    val tripleA = Array[Array[Any]](
      Array(-0f, "%a", "-0x0.0p0"),
      Array(-0f, "%#.3a", "-0x0.000p0"),
      Array(-0f, "%5a", "-0x0.0p0"),
      Array(-0f, "%- 12.0a", "-0x0.0p0    "),
      Array(-0f, "%#+01.6a", "-0x0.000000p0"),
      Array(-0f, "%-+8.4a", "-0x0.0000p0"),
      Array(0f, "%a", "0x0.0p0"),
      Array(0f, "%#.3a", "0x0.000p0"),
      Array(0f, "%5a", "0x0.0p0"),
      Array(0f, "%- 12.0a", " 0x0.0p0    "),
      Array(0f, "%#+01.6a", "+0x0.000000p0"),
      Array(0f, "%-+8.4a", "+0x0.0000p0"),
      Array(1234f, "%a", "0x1.348p10"),
      Array(1234f, "%#.3a", "0x1.348p10"),
      Array(1234f, "%5a", "0x1.348p10"),
      Array(1234f, "%- 12.0a", " 0x1.3p10   "),
      Array(1234f, "%#+01.6a", "+0x1.348000p10"),
      Array(1234f, "%-+8.4a", "+0x1.3480p10"),
      Array(1.0f, "%a", "0x1.0p0"),
      Array(1.0f, "%#.3a", "0x1.000p0"),
      Array(1.0f, "%5a", "0x1.0p0"),
      Array(1.0f, "%- 12.0a", " 0x1.0p0    "),
      Array(1.0f, "%#+01.6a", "+0x1.000000p0"),
      Array(1.0f, "%-+8.4a", "+0x1.0000p0"),
      Array(-98f, "%a", "-0x1.88p6"),
      Array(-98f, "%#.3a", "-0x1.880p6"),
      Array(-98f, "%5a", "-0x1.88p6"),
      Array(-98f, "%- 12.0a", "-0x1.8p6    "),
      Array(-98f, "%#+01.6a", "-0x1.880000p6"),
      Array(-98f, "%-+8.4a", "-0x1.8800p6"),
      Array(345.1234567f, "%a", "0x1.591f9ap8"),
      Array(345.1234567f, "%5a", "0x1.591f9ap8"),
      Array(345.1234567f, "%#+01.6a", "+0x1.591f9ap8"),
      Array(-987654321.1234567f, "%a", "-0x1.d6f346p29"),
      Array(-987654321.1234567f, "%#.3a", "-0x1.d6fp29"),
      Array(-987654321.1234567f, "%5a", "-0x1.d6f346p29"),
      Array(-987654321.1234567f, "%- 12.0a", "-0x1.dp29   "),
      Array(-987654321.1234567f, "%#+01.6a", "-0x1.d6f346p29"),
      Array(-987654321.1234567f, "%-+8.4a", "-0x1.d6f3p29"),
      Array(java.lang.Float.MAX_VALUE, "%a", "0x1.fffffep127"),
      Array(java.lang.Float.MAX_VALUE, "%5a", "0x1.fffffep127"),
      Array(java.lang.Float.MAX_VALUE, "%#+01.6a", "+0x1.fffffep127"),
      Array(java.lang.Float.NaN, "%a", "NaN"),
      Array(java.lang.Float.NaN, "%#.3a", "NaN"),
      Array(java.lang.Float.NaN, "%5a", "  NaN"),
      Array(java.lang.Float.NaN, "%- 12.0a", "NaN         "),
      Array(java.lang.Float.NaN, "%#+01.6a", "NaN"),
      Array(java.lang.Float.NaN, "%-+8.4a", "NaN     "),
      Array(java.lang.Float.NEGATIVE_INFINITY, "%a", "-Infinity"),
      Array(java.lang.Float.NEGATIVE_INFINITY, "%#.3a", "-Infinity"),
      Array(java.lang.Float.NEGATIVE_INFINITY, "%5a", "-Infinity"),
      Array(java.lang.Float.NEGATIVE_INFINITY, "%- 12.0a", "-Infinity   "),
      Array(java.lang.Float.NEGATIVE_INFINITY, "%#+01.6a", "-Infinity"),
      Array(java.lang.Float.NEGATIVE_INFINITY, "%-+8.4a", "-Infinity"),
      Array(java.lang.Float.POSITIVE_INFINITY, "%a", "Infinity"),
      Array(java.lang.Float.POSITIVE_INFINITY, "%#.3a", "Infinity"),
      Array(java.lang.Float.POSITIVE_INFINITY, "%5a", "Infinity"),
      Array(java.lang.Float.POSITIVE_INFINITY, "%- 12.0a", " Infinity   "),
      Array(java.lang.Float.POSITIVE_INFINITY, "%#+01.6a", "+Infinity"),
      Array(java.lang.Float.POSITIVE_INFINITY, "%-+8.4a", "+Infinity"),
      Array(-0d, "%a", "-0x0.0p0"),
      Array(-0d, "%#.3a", "-0x0.000p0"),
      Array(-0d, "%5a", "-0x0.0p0"),
      Array(-0d, "%- 12.0a", "-0x0.0p0    "),
      Array(-0d, "%#+01.6a", "-0x0.000000p0"),
      Array(-0d, "%-+8.4a", "-0x0.0000p0"),
      Array(0d, "%a", "0x0.0p0"),
      Array(0d, "%#.3a", "0x0.000p0"),
      Array(0d, "%5a", "0x0.0p0"),
      Array(0d, "%- 12.0a", " 0x0.0p0    "),
      Array(0d, "%#+01.6a", "+0x0.000000p0"),
      Array(0d, "%-+8.4a", "+0x0.0000p0"),
      Array(1d, "%a", "0x1.0p0"),
      Array(1d, "%#.3a", "0x1.000p0"),
      Array(1d, "%5a", "0x1.0p0"),
      Array(1d, "%- 12.0a", " 0x1.0p0    "),
      Array(1d, "%#+01.6a", "+0x1.000000p0"),
      Array(1d, "%-+8.4a", "+0x1.0000p0"),
      Array(-1d, "%a", "-0x1.0p0"),
      Array(-1d, "%#.3a", "-0x1.000p0"),
      Array(-1d, "%5a", "-0x1.0p0"),
      Array(-1d, "%- 12.0a", "-0x1.0p0    "),
      Array(-1d, "%#+01.6a", "-0x1.000000p0"),
      Array(-1d, "%-+8.4a", "-0x1.0000p0"),
      Array(.00000001d, "%a", "0x1.5798ee2308c3ap-27"),
      Array(.00000001d, "%5a", "0x1.5798ee2308c3ap-27"),
      Array(.00000001d, "%- 12.0a", " 0x1.5p-27  "),
      Array(.00000001d, "%#+01.6a", "+0x1.5798eep-27"),
      Array(1000.10d, "%a", "0x1.f40cccccccccdp9"),
      Array(1000.10d, "%5a", "0x1.f40cccccccccdp9"),
      Array(1000.10d, "%- 12.0a", " 0x1.fp9    "),
      Array(0.1d, "%a", "0x1.999999999999ap-4"),
      Array(0.1d, "%5a", "0x1.999999999999ap-4"),
      Array(-2.0d, "%a", "-0x1.0p1"),
      Array(-2.0d, "%#.3a", "-0x1.000p1"),
      Array(-2.0d, "%5a", "-0x1.0p1"),
      Array(-2.0d, "%- 12.0a", "-0x1.0p1    "),
      Array(-2.0d, "%#+01.6a", "-0x1.000000p1"),
      Array(-2.0d, "%-+8.4a", "-0x1.0000p1"),
      Array(-.00009d, "%a", "-0x1.797cc39ffd60fp-14"),
      Array(-.00009d, "%5a", "-0x1.797cc39ffd60fp-14"),
      Array(-1234567890.012345678d, "%a", "-0x1.26580b480ca46p30"),
      Array(-1234567890.012345678d, "%5a", "-0x1.26580b480ca46p30"),
      Array(-1234567890.012345678d, "%- 12.0a", "-0x1.2p30   "),
      Array(-1234567890.012345678d, "%#+01.6a", "-0x1.26580bp30"),
      Array(-1234567890.012345678d, "%-+8.4a", "-0x1.2658p30"),
      Array(java.lang.Double.MAX_VALUE, "%a", "0x1.fffffffffffffp1023"),
      Array(java.lang.Double.MAX_VALUE, "%5a", "0x1.fffffffffffffp1023"),
      Array(java.lang.Double.MIN_VALUE, "%a", "0x0.0000000000001p-1022"),
      Array(java.lang.Double.MIN_VALUE, "%5a", "0x0.0000000000001p-1022"),
      Array(java.lang.Double.NaN, "%a", "NaN"),
      Array(java.lang.Double.NaN, "%#.3a", "NaN"),
      Array(java.lang.Double.NaN, "%5a", "  NaN"),
      Array(java.lang.Double.NaN, "%- 12.0a", "NaN         "),
      Array(java.lang.Double.NaN, "%#+01.6a", "NaN"),
      Array(java.lang.Double.NaN, "%-+8.4a", "NaN     "),
      Array(java.lang.Double.NEGATIVE_INFINITY, "%a", "-Infinity"),
      Array(java.lang.Double.NEGATIVE_INFINITY, "%#.3a", "-Infinity"),
      Array(java.lang.Double.NEGATIVE_INFINITY, "%5a", "-Infinity"),
      Array(java.lang.Double.NEGATIVE_INFINITY, "%- 12.0a", "-Infinity   "),
      Array(java.lang.Double.NEGATIVE_INFINITY, "%#+01.6a", "-Infinity"),
      Array(java.lang.Double.NEGATIVE_INFINITY, "%-+8.4a", "-Infinity"),
      Array(java.lang.Double.POSITIVE_INFINITY, "%a", "Infinity"),
      Array(java.lang.Double.POSITIVE_INFINITY, "%#.3a", "Infinity"),
      Array(java.lang.Double.POSITIVE_INFINITY, "%5a", "Infinity"),
      Array(java.lang.Double.POSITIVE_INFINITY, "%- 12.0a", " Infinity   "),
      Array(java.lang.Double.POSITIVE_INFINITY, "%#+01.6a", "+Infinity"),
      Array(java.lang.Double.POSITIVE_INFINITY, "%-+8.4a", "+Infinity")
    )
    val input: Int = 0
    val pattern: Int = 1
    val output: Int = 2
    for (i <- 0 until tripleA.length) {
      locally {
        val f = new Formatter()
        f.format(
          tripleA(i)(pattern).asInstanceOf[String],
          tripleA(i)(input).asInstanceOf[Object]
        )
        assertEquals(tripleA(i)(output), f.toString)
      }
      // test for conversion type 'A'
      locally {
        val f = new Formatter()
        f.format(
          tripleA(i)(pattern).asInstanceOf[String].toUpperCase(),
          tripleA(i)(input).asInstanceOf[Object]
        )
        assertEquals(
          tripleA(i)(output).asInstanceOf[String].toUpperCase(),
          f.toString
        )
      }
    }
  }

  @Test def formatForBigDecimalConversionType_eE(): Unit = {
    val tripleE = Array(
      Array(BigDecimal.ZERO, "%e", "0.000000e+00"),
      Array(BigDecimal.ZERO, "%#.0e", "0.e+00"),
      Array(BigDecimal.ZERO, "%# 9.8e", " 0.00000000e+00"),
      Array(BigDecimal.ZERO, "%#+0(8.4e", "+0.0000e+00"),
      Array(BigDecimal.ZERO, "%-+17.6e", "+0.000000e+00    "),
      Array(BigDecimal.ZERO, "% 0(20e", " 00000000.000000e+00"),
      Array(BigDecimal.ONE, "%e", "1.000000e+00"),
      Array(BigDecimal.ONE, "%#.0e", "1.e+00"),
      Array(BigDecimal.ONE, "%# 9.8e", " 1.00000000e+00"),
      Array(BigDecimal.ONE, "%#+0(8.4e", "+1.0000e+00"),
      Array(BigDecimal.ONE, "%-+17.6e", "+1.000000e+00    "),
      Array(BigDecimal.ONE, "% 0(20e", " 00000001.000000e+00"),
      Array(BigDecimal.TEN, "%e", "1.000000e+01"),
      Array(BigDecimal.TEN, "%#.0e", "1.e+01"),
      Array(BigDecimal.TEN, "%# 9.8e", " 1.00000000e+01"),
      Array(BigDecimal.TEN, "%#+0(8.4e", "+1.0000e+01"),
      Array(BigDecimal.TEN, "%-+17.6e", "+1.000000e+01    "),
      Array(BigDecimal.TEN, "% 0(20e", " 00000001.000000e+01"),
      Array(new BigDecimal(-1), "%e", "-1.000000e+00"),
      Array(new BigDecimal(-1), "%#.0e", "-1.e+00"),
      Array(new BigDecimal(-1), "%# 9.8e", "-1.00000000e+00"),
      Array(new BigDecimal(-1), "%#+0(8.4e", "(1.0000e+00)"),
      Array(new BigDecimal(-1), "%-+17.6e", "-1.000000e+00    "),
      Array(new BigDecimal(-1), "% 0(20e", "(0000001.000000e+00)"),
      Array(new BigDecimal("5.000E999"), "%e", "5.000000e+999"),
      Array(new BigDecimal("5.000E999"), "%#.0e", "5.e+999"),
      Array(new BigDecimal("5.000E999"), "%# 9.8e", " 5.00000000e+999"),
      Array(new BigDecimal("5.000E999"), "%#+0(8.4e", "+5.0000e+999"),
      Array(new BigDecimal("5.000E999"), "%-+17.6e", "+5.000000e+999   "),
      Array(new BigDecimal("5.000E999"), "% 0(20e", " 0000005.000000e+999"),
      Array(new BigDecimal("-5.000E999"), "%e", "-5.000000e+999"),
      Array(new BigDecimal("-5.000E999"), "%#.0e", "-5.e+999"),
      Array(new BigDecimal("-5.000E999"), "%# 9.8e", "-5.00000000e+999"),
      Array(new BigDecimal("-5.000E999"), "%#+0(8.4e", "(5.0000e+999)"),
      Array(new BigDecimal("-5.000E999"), "%-+17.6e", "-5.000000e+999   "),
      Array(new BigDecimal("-5.000E999"), "% 0(20e", "(000005.000000e+999)")
    )
    val input: Int = 0
    val pattern: Int = 1
    val output: Int = 2
    for (i <- 0 until tripleE.length) {
      locally {
        val f = new Formatter()
        f.format(
          tripleE(i)(pattern).asInstanceOf[String],
          tripleE(i)(input).asInstanceOf[Object]
        )
        assertEquals(tripleE(i)(output), f.toString)
      }
      // test for conversion type 'E'
      locally {
        val f = new Formatter()
        f.format(
          tripleE(i)(pattern).asInstanceOf[String].toUpperCase(),
          tripleE(i)(input).asInstanceOf[Object]
        )
        assertEquals(
          tripleE(i)(output).asInstanceOf[String].toUpperCase(),
          f.toString
        )
      }
    }
  }

  @Test def formatForBigDecimalConversionType_gG(): Unit = {
    val tripleG = Array(
      Array(BigDecimal.ZERO, "%g", "0.00000"),
      Array(BigDecimal.ZERO, "%.5g", "0.0000"),
      Array(BigDecimal.ZERO, "%- (,9.8g", " 0.0000000"),
      Array(BigDecimal.ZERO, "%+0(,8.4g", "+000.000"),
      Array(BigDecimal.ZERO, "%-+10.6g", "+0.00000  "),
      Array(BigDecimal.ZERO, "% 0(,12.0g", " 00000000000"),
      Array(BigDecimal.ONE, "%g", "1.00000"),
      Array(BigDecimal.ONE, "%.5g", "1.0000"),
      Array(BigDecimal.ONE, "%- (,9.8g", " 1.0000000"),
      Array(BigDecimal.ONE, "%+0(,8.4g", "+001.000"),
      Array(BigDecimal.ONE, "%-+10.6g", "+1.00000  "),
      Array(BigDecimal.ONE, "% 0(,12.0g", " 00000000001"),
      Array(new BigDecimal(-1), "%g", "-1.00000"),
      Array(new BigDecimal(-1), "%.5g", "-1.0000"),
      Array(new BigDecimal(-1), "%- (,9.8g", "(1.0000000)"),
      Array(new BigDecimal(-1), "%+0(,8.4g", "(01.000)"),
      Array(new BigDecimal(-1), "%-+10.6g", "-1.00000  "),
      Array(new BigDecimal(-1), "% 0(,12.0g", "(0000000001)"),
      Array(new BigDecimal(-0.000001), "%g", "-1.00000e-06"),
      Array(new BigDecimal(-0.000001), "%.5g", "-1.0000e-06"),
      Array(new BigDecimal(-0.000001), "%- (,9.8g", "(1.0000000e-06)"),
      Array(new BigDecimal(-0.000001), "%+0(,8.4g", "(1.000e-06)"),
      Array(new BigDecimal(-0.000001), "%-+10.6g", "-1.00000e-06"),
      Array(new BigDecimal(-0.000001), "% 0(,12.0g", "(000001e-06)"),
      Array(new BigDecimal(0.0002), "%g", "0.000200000"),
      Array(new BigDecimal(0.0002), "%.5g", "0.00020000"),
      Array(new BigDecimal(0.0002), "%- (,9.8g", " 0.00020000000"),
      Array(new BigDecimal(0.0002), "%+0(,8.4g", "+0.0002000"),
      Array(new BigDecimal(0.0002), "%-+10.6g", "+0.000200000"),
      Array(new BigDecimal(0.0002), "% 0(,12.0g", " 000000.0002"),
      Array(new BigDecimal(-0.003), "%g", "-0.00300000"),
      Array(new BigDecimal(-0.003), "%.5g", "-0.0030000"),
      Array(new BigDecimal(-0.003), "%- (,9.8g", "(0.0030000000)"),
      Array(new BigDecimal(-0.003), "%+0(,8.4g", "(0.003000)"),
      Array(new BigDecimal(-0.003), "%-+10.6g", "-0.00300000"),
      Array(new BigDecimal(-0.003), "% 0(,12.0g", "(000000.003)"),
      Array(new BigDecimal("5.000E999"), "%g", "5.00000e+999"),
      Array(new BigDecimal("5.000E999"), "%.5g", "5.0000e+999"),
      Array(new BigDecimal("5.000E999"), "%- (,9.8g", " 5.0000000e+999"),
      Array(new BigDecimal("5.000E999"), "%+0(,8.4g", "+5.000e+999"),
      Array(new BigDecimal("5.000E999"), "%-+10.6g", "+5.00000e+999"),
      Array(new BigDecimal("5.000E999"), "% 0(,12.0g", " 000005e+999"),
      Array(new BigDecimal("-5.000E999"), "%g", "-5.00000e+999"),
      Array(new BigDecimal("-5.000E999"), "%.5g", "-5.0000e+999"),
      Array(new BigDecimal("-5.000E999"), "%- (,9.8g", "(5.0000000e+999)"),
      Array(new BigDecimal("-5.000E999"), "%+0(,8.4g", "(5.000e+999)"),
      Array(new BigDecimal("-5.000E999"), "%-+10.6g", "-5.00000e+999"),
      Array(new BigDecimal("-5.000E999"), "% 0(,12.0g", "(00005e+999)")
    ).asInstanceOf[Array[Array[Any]]]
    val input: Int = 0
    val pattern: Int = 1
    val output: Int = 2
    for (i <- 0 until tripleG.length) {
      locally {
        val f = new Formatter()
        f.format(
          tripleG(i)(pattern).asInstanceOf[String],
          tripleG(i)(input).asInstanceOf[Object]
        )
        assertEquals(tripleG(i)(output), f.toString)
      }
      // test for conversion type 'G'
      locally {
        val f = new Formatter()
        f.format(
          tripleG(i)(pattern).asInstanceOf[String].toUpperCase(),
          tripleG(i)(input).asInstanceOf[Object]
        )
        assertEquals(
          tripleG(i)(output).asInstanceOf[String].toUpperCase(),
          f.toString
        )
      }
    }
    val f = new Formatter()
    f.format("%- (,9.6g", new BigDecimal("4E6"))
    /*
     * fail on RI, spec says 'g' requires the output to be formatted in
     * general scientific notation and the localization algorithm is
     * applied. But RI format this case to 4.00000e+06, which does not
     * conform to the German Locale
     */
    assertEquals(" 4.00000e+06", f.toString)
  }

  @Test def formatForBigDecimalConversionType_f(): Unit = {
    val input: Int = 0
    val pattern: Int = 1
    val output: Int = 2
    val tripleF = Array(
      Array(BigDecimal.ZERO, "%f", "0.000000"),
      Array(BigDecimal.ZERO, "%#.3f", "0.000"),
      Array(BigDecimal.ZERO, "%#,5f", "0.000000"),
      Array(BigDecimal.ZERO, "%- #(12.0f", " 0.         "),
      Array(BigDecimal.ZERO, "%#+0(1.6f", "+0.000000"),
      Array(BigDecimal.ZERO, "%-+(8.4f", "+0.0000 "),
      Array(BigDecimal.ZERO, "% 0#(9.8f", " 0.00000000"),
      Array(BigDecimal.ONE, "%f", "1.000000"),
      Array(BigDecimal.ONE, "%#.3f", "1.000"),
      Array(BigDecimal.ONE, "%#,5f", "1.000000"),
      Array(BigDecimal.ONE, "%- #(12.0f", " 1.         "),
      Array(BigDecimal.ONE, "%#+0(1.6f", "+1.000000"),
      Array(BigDecimal.ONE, "%-+(8.4f", "+1.0000 "),
      Array(BigDecimal.ONE, "% 0#(9.8f", " 1.00000000"),
      Array(BigDecimal.TEN, "%f", "10.000000"),
      Array(BigDecimal.TEN, "%#.3f", "10.000"),
      Array(BigDecimal.TEN, "%#,5f", "10.000000"),
      Array(BigDecimal.TEN, "%- #(12.0f", " 10.        "),
      Array(BigDecimal.TEN, "%#+0(1.6f", "+10.000000"),
      Array(BigDecimal.TEN, "%-+(8.4f", "+10.0000"),
      Array(BigDecimal.TEN, "% 0#(9.8f", " 10.00000000"),
      Array(new BigDecimal(-1), "%f", "-1.000000"),
      Array(new BigDecimal(-1), "%#.3f", "-1.000"),
      Array(new BigDecimal(-1), "%#,5f", "-1.000000"),
      Array(new BigDecimal(-1), "%- #(12.0f", "(1.)        "),
      Array(new BigDecimal(-1), "%#+0(1.6f", "(1.000000)"),
      Array(new BigDecimal(-1), "%-+(8.4f", "(1.0000)"),
      Array(new BigDecimal(-1), "% 0#(9.8f", "(1.00000000)"),
      Array(
        new BigDecimal("9999999999999999999999999999999999999999999"),
        "%f",
        "9999999999999999999999999999999999999999999.000000"
      ),
      Array(
        new BigDecimal("9999999999999999999999999999999999999999999"),
        "%#.3f",
        "9999999999999999999999999999999999999999999.000"
      ),
      Array(
        new BigDecimal("9999999999999999999999999999999999999999999"),
        "%#,5f",
        "9,999,999,999,999,999,999,999,999,999,999,999,999,999,999.000000"
      ),
      Array(
        new BigDecimal("9999999999999999999999999999999999999999999"),
        "%- #(12.0f",
        " 9999999999999999999999999999999999999999999."
      ),
      Array(
        new BigDecimal("9999999999999999999999999999999999999999999"),
        "%#+0(1.6f",
        "+9999999999999999999999999999999999999999999.000000"
      ),
      Array(
        new BigDecimal("9999999999999999999999999999999999999999999"),
        "%-+(8.4f",
        "+9999999999999999999999999999999999999999999.0000"
      ),
      Array(
        new BigDecimal("9999999999999999999999999999999999999999999"),
        "% 0#(9.8f",
        " 9999999999999999999999999999999999999999999.00000000"
      ),
      Array(
        new BigDecimal("-9999999999999999999999999999999999999999999"),
        "%f",
        "-9999999999999999999999999999999999999999999.000000"
      ),
      Array(
        new BigDecimal("-9999999999999999999999999999999999999999999"),
        "%#.3f",
        "-9999999999999999999999999999999999999999999.000"
      ),
      Array(
        new BigDecimal("-9999999999999999999999999999999999999999999"),
        "%#,5f",
        "-9,999,999,999,999,999,999,999,999,999,999,999,999,999,999.000000"
      ),
      Array(
        new BigDecimal("-9999999999999999999999999999999999999999999"),
        "%- #(12.0f",
        "(9999999999999999999999999999999999999999999.)"
      ),
      Array(
        new BigDecimal("-9999999999999999999999999999999999999999999"),
        "%#+0(1.6f",
        "(9999999999999999999999999999999999999999999.000000)"
      ),
      Array(
        new BigDecimal("-9999999999999999999999999999999999999999999"),
        "%-+(8.4f",
        "(9999999999999999999999999999999999999999999.0000)"
      ),
      Array(
        new BigDecimal("-9999999999999999999999999999999999999999999"),
        "% 0#(9.8f",
        "(9999999999999999999999999999999999999999999.00000000)"
      )
    ).asInstanceOf[Array[Array[Any]]]
    for (i <- 0 until tripleF.length) {
      val f = new Formatter()
      f.format(
        tripleF(i)(pattern).asInstanceOf[String],
        tripleF(i)(input).asInstanceOf[Object]
      )
      assertEquals(tripleF(i)(output), f.toString)
    }
    val f = new Formatter()
    f.format("%f", new BigDecimal("5.0E9"))
    // error on RI
    // RI throw ArrayIndexOutOfBoundsException
    assertEquals("5000000000.000000", f.toString)
  }

  @Test def formatForExceptionsInFloatDoubleBigDecimalConversionType_eEgGfaA()
      : Unit = {
    val conversions: Array[Char] = Array('e', 'E', 'g', 'G', 'f', 'a', 'A')
    val illArgs: Array[Any] = Array(
      false,
      1.toByte,
      2.toShort,
      3,
      4.toLong,
      new BigInteger("5"),
      java.lang.Character.valueOf('c'),
      new Object(),
      new Date()
    )
    for {
      i <- 0 until illArgs.length
      j <- 0 until conversions.length
    } {
      val f = new Formatter()
      assertThrows(
        classOf[IllegalFormatConversionException],
        f.format("%" + conversions(j), illArgs(i).asInstanceOf[Object])
      )

    }
    locally {
      val f = new Formatter()
      assertThrows(
        classOf[IllegalFormatConversionException],
        f.format("%a", new BigDecimal(1))
      )
    }
    locally {
      val f = new Formatter()
      assertThrows(
        classOf[IllegalFormatConversionException],
        f.format("%A", new BigDecimal(1))
      )
    }

    val flagsConversionMismatches: Array[String] =
      Array("%,e", "%,E", "%#g", "%#G", "%,a", "%,A", "%(a", "%(A")
    for (i <- 0 until flagsConversionMismatches.length) {
      locally {
        val f = new Formatter()
        assertThrows(
          classOf[FormatFlagsConversionMismatchException],
          f.format(flagsConversionMismatches(i), new BigDecimal(1))
        )
      }
      locally {
        val f = new Formatter()
        assertThrows(
          classOf[FormatFlagsConversionMismatchException],
          f.format(flagsConversionMismatches(i), null.asInstanceOf[BigDecimal])
        )
      }
    }

    val missingFormatWidths: Array[String] = Array(
      "%-0e",
      "%0e",
      "%-e",
      "%-0E",
      "%0E",
      "%-E",
      "%-0g",
      "%0g",
      "%-g",
      "%-0G",
      "%0G",
      "%-G",
      "%-0f",
      "%0f",
      "%-f",
      "%-0a",
      "%0a",
      "%-a",
      "%-0A",
      "%0A",
      "%-A"
    )
    for (i <- 0 until missingFormatWidths.length) {
      locally {
        val f = new Formatter()
        assertThrows(
          classOf[MissingFormatWidthException],
          f.format(missingFormatWidths(i), 1f.asInstanceOf[Object])
        )
      }
      locally {
        val f = new Formatter()
        assertThrows(
          classOf[MissingFormatWidthException],
          f.format(missingFormatWidths(i), null.asInstanceOf[java.lang.Float])
        )
      }
    }

    val illFlags: Array[String] = Array(
      "%+ e",
      "%+ E",
      "%+ g",
      "%+ G",
      "%+ f",
      "%+ a",
      "%+ A",
      "%-03e",
      "%-03E",
      "%-03g",
      "%-03G",
      "%-03f",
      "%-03a",
      "%-03A"
    )
    for (i <- 0 until illFlags.length) {
      locally {
        val f = new Formatter()
        assertThrows(
          classOf[IllegalFormatFlagsException],
          f.format(illFlags(i), 1.23d.asInstanceOf[Object])
        )
      }
      locally {
        val f = new Formatter()
        assertThrows(
          classOf[IllegalFormatFlagsException],
          f.format(illFlags(i), null.asInstanceOf[java.lang.Double])
        )
      }
    }
    val f = new Formatter()
    assertThrows(
      classOf[UnknownFormatConversionException],
      f.format("%F", 1.asInstanceOf[Object])
    )
  }

  @Test def formatForFloatDoubleBigDecimalExceptionThrowingOrder(): Unit = {
    /*
     * Summary: UnknownFormatConversionException >
     * MissingFormatWidthException > IllegalFormatFlagsException >
     * FormatFlagsConversionMismatchException >
     * IllegalFormatConversionException
     *
     */
    locally {
      // compare FormatFlagsConversionMismatchException and
      // IllegalFormatConversionException
      val f = new Formatter()
      assertThrows(
        classOf[FormatFlagsConversionMismatchException],
        f.format("%,e", 1.toByte.asInstanceOf[Object])
      )
    }

    locally {
      // compare IllegalFormatFlagsException and
      // FormatFlagsConversionMismatchException
      val f = new Formatter()
      assertThrows(
        classOf[IllegalFormatFlagsException],
        f.format("%+ ,e", 1f.asInstanceOf[Object])
      )
    }

    locally {
      // compare MissingFormatWidthException and
      // IllegalFormatFlagsException
      val f = new Formatter()
      assertThrows(
        classOf[MissingFormatWidthException],
        f.format("%+ -e", 1f.asInstanceOf[Object])
      )
    }

    locally {
      // compare UnknownFormatConversionException and
      // MissingFormatWidthException
      val f = new Formatter()
      assertThrows(
        classOf[UnknownFormatConversionException],
        f.format("%-F", 1f.asInstanceOf[Object])
      )
    }
  }

  @Test def formatForBigDecimalExceptionThrowingOrder(): Unit = {
    val bd = new BigDecimal("1.0")
    /*
     * Summary: UnknownFormatConversionException >
     * MissingFormatWidthException > IllegalFormatFlagsException >
     * FormatFlagsConversionMismatchException >
     * IllegalFormatConversionException
     *
     */
    locally {
      // compare FormatFlagsConversionMismatchException and
      // IllegalFormatConversionException
      val f = new Formatter()
      assertThrows(
        classOf[FormatFlagsConversionMismatchException],
        f.format("%,e", 1.toByte.asInstanceOf[Object])
      )
    }

    locally {
      // compare IllegalFormatFlagsException and
      // FormatFlagsConversionMismatchException
      val f = new Formatter()
      assertThrows(classOf[IllegalFormatFlagsException], f.format("%+ ,e", bd))
    }

    locally {
      // compare MissingFormatWidthException and
      // IllegalFormatFlagsException
      val f = new Formatter()
      assertThrows(classOf[MissingFormatWidthException], f.format("%+ -e", bd))
    }

    locally {
      // compare UnknownFormatConversionException and
      // MissingFormatWidthException
      val f = new Formatter()
      assertThrows(
        classOf[UnknownFormatConversionException],
        f.format("%-F", bd)
      )
    }
  }

  @Test def formatForNullArgumentForFloatDoubleBigDecimalConversion_a()
      : Unit = {
    locally {
      val f = new Formatter()
      f.format("% .4a", null.asInstanceOf[java.lang.Float])
      assertEquals("null", f.toString)
    }

    locally {
      val f = new Formatter()
      f.format("%06A", null.asInstanceOf[java.lang.Float])
      assertEquals("  NULL", f.toString)
    }

    locally {
      val f = new Formatter()
      f.format("%06a", null.asInstanceOf[BigDecimal])
      assertEquals("  null", f.toString)
    }

    locally {
      val f = new Formatter()
      f.format("% .5A", null.asInstanceOf[BigDecimal])
      assertEquals("NULL", f.toString)
    }

    locally {
      val f = new Formatter()
      f.format("%#.6a", null.asInstanceOf[java.lang.Double])
      assertEquals("null", f.toString)
    }

    locally {
      val f = new Formatter()
      f.format("% 2.5A", null.asInstanceOf[java.lang.Double])
      assertEquals("NULL", f.toString)
    }
  }

  @Test def formatForNullArgumentForFloatDoubleBigDecimalConversion(): Unit = {
    // test (Float)null
    locally {
      val f = new Formatter()
      f.format("%#- (9.0e", null.asInstanceOf[java.lang.Float])
      assertEquals("         ", f.toString)
    }
    locally {
      val f = new Formatter()
      f.format("%-+(1.6E", null.asInstanceOf[java.lang.Float])
      assertEquals("NULL", f.toString)
    }
    locally {
      val f = new Formatter()
      f.format("%+0(,8.4g", null.asInstanceOf[java.lang.Float])
      assertEquals("    null", f.toString)
    }
    locally {
      val f = new Formatter()
      f.format("%- (9.8G", null.asInstanceOf[java.lang.Float])
      assertEquals("NULL     ", f.toString)
    }
    locally {
      val f = new Formatter()
      f.format("%- (12.1f", null.asInstanceOf[java.lang.Float])
      assertEquals("n           ", f.toString)
    }
    locally {
      val f = new Formatter()
      f.format("% .4a", null.asInstanceOf[java.lang.Float])
      assertEquals("null", f.toString)
    }
    locally {
      val f = new Formatter()
      f.format("%06A", null.asInstanceOf[java.lang.Float])
      assertEquals("  NULL", f.toString)
    }
    // test (Double)null
    locally {
      val f = new Formatter()
      f.format("%- (9e", null.asInstanceOf[java.lang.Double])
      assertEquals("null     ", f.toString)
    }
    locally {
      val f = new Formatter()
      f.format("%#-+(1.6E", null.asInstanceOf[java.lang.Double])
      assertEquals("NULL", f.toString)
    }
    locally {
      val f = new Formatter()
      f.format("%+0(6.4g", null.asInstanceOf[java.lang.Double])
      assertEquals("  null", f.toString)
    }
    locally {
      val f = new Formatter()
      f.format("%- (,5.8G", null.asInstanceOf[java.lang.Double])
      assertEquals("NULL ", f.toString)
    }
    locally {
      val f = new Formatter()
      f.format("% (.4f", null.asInstanceOf[java.lang.Double])
      assertEquals("null", f.toString)
    }
    locally {
      val f = new Formatter()
      f.format("%#.6a", null.asInstanceOf[java.lang.Double])
      assertEquals("null", f.toString)
    }
    locally {
      val f = new Formatter()
      f.format("% 2.5A", null.asInstanceOf[java.lang.Double])
      assertEquals("NULL", f.toString)
    }
    // test (BigDecimal)null
    locally {
      val f = new Formatter()
      f.format("%#- (6.2e", null.asInstanceOf[BigDecimal])
      assertEquals("nu    ", f.toString)
    }
    locally {
      val f = new Formatter()
      f.format("%-+(1.6E", null.asInstanceOf[BigDecimal])
      assertEquals("NULL", f.toString)
    }
    locally {
      val f = new Formatter()
      f.format("%+-(,5.3g", null.asInstanceOf[BigDecimal])
      assertEquals("nul  ", f.toString)
    }
    locally {
      val f = new Formatter()
      f.format("%0 3G", null.asInstanceOf[BigDecimal])
      assertEquals("NULL", f.toString)
    }
    locally {
      val f = new Formatter()
      f.format("%0 (9.0G", null.asInstanceOf[BigDecimal])
      assertEquals("         ", f.toString)
    }
    locally {
      val f = new Formatter()
      f.format("% (.5f", null.asInstanceOf[BigDecimal])
      assertEquals("null", f.toString)
    }
    locally {
      val f = new Formatter()
      f.format("%06a", null.asInstanceOf[BigDecimal])
      assertEquals("  null", f.toString)
    }
    locally {
      val f = new Formatter()
      f.format("% .5A", null.asInstanceOf[BigDecimal])
      assertEquals("NULL", f.toString)
    }
  }

  @Test def formatterBigDecimalLayoutFormValues(): Unit = {
    import Formatter.BigDecimalLayoutForm
    val vals: Array[BigDecimalLayoutForm] = BigDecimalLayoutForm.values()
    assertEquals(2, vals.length)
    assertEquals(BigDecimalLayoutForm.SCIENTIFIC, vals(0))
    assertEquals(BigDecimalLayoutForm.DECIMAL_FLOAT, vals(1))
  }

  @Test def formatterBigDecimalLayoutFormValueOfString(): Unit = {
    import Formatter.BigDecimalLayoutForm
    val sci: BigDecimalLayoutForm = BigDecimalLayoutForm.valueOf("SCIENTIFIC")
    assertEquals(BigDecimalLayoutForm.SCIENTIFIC, sci)
    val decFloat: BigDecimalLayoutForm =
      BigDecimalLayoutForm.valueOf("DECIMAL_FLOAT")
    assertEquals(BigDecimalLayoutForm.DECIMAL_FLOAT, decFloat)
  }

  /*
   * Regression test for Harmony-5845
   * test scientific notation to follow RI's behavior
   */
  @Test def scientificNotation(): Unit = {
    val f: Formatter = new Formatter()
    val mc: MathContext = new MathContext(30)
    val value: BigDecimal = new BigDecimal(0.1, mc)
    f.format("%.30G", value)

    val result: String = f.toString
    val expected: String = "0.100000000000000005551115123126"
    assertEquals(expected, result)
  }
}
