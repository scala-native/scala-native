package java.util

// Ported from Harmony

// Tests related to SecurityManager are removed because they doesn't exist on Scala Native

import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.Closeable
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.Flushable
import java.io.IOException
import java.io.OutputStream
import java.io.PrintStream
import java.io.UnsupportedEncodingException
import java.lang.StringBuilder
import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.nio.charset.Charset

object FormatterSuite extends tests.Suite {
  private var root: Boolean             = false
  private var notExist: File            = _
  private var fileWithContent: File     = _
  private var readOnly: File            = _
  private var defaultTimeZone: TimeZone = _

  // setup resource files for testing
  protected def setUp(): Unit = {
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

    // ignores ??? until these are implemented
    try {
      defaultTimeZone = TimeZone.getDefault()
      val cst = TimeZone.getTimeZone("Asia/Shanghai")
      TimeZone.setDefault(cst)
    } catch {
      case _: NotImplementedError =>
    }
  }

  // delete the resource files if they exist
  protected def tearDown(): Unit = {
    if (notExist.exists()) notExist.delete()
    if (fileWithContent.exists()) fileWithContent.delete()
    if (readOnly.exists()) readOnly.delete()
    // TimeZone.setDefault(defaultTimeZone)
  }

  override def test(name: String)(body: => Unit): Unit =
    super.test(name) {
      setUp()
      try {
        body
      } finally {
        tearDown()
      }
    }

  override def testFails(name: String, issue: Int)(body: => Unit): Unit =
    super.testFails(name, issue) {
      setUp()
      try {
        body
      } finally {
        tearDown()
      }
    }

  def assertNull[A](a: A): Unit =
    assert(a == null)

  def assertNotNull[A](a: A): Unit =
    assertNot(a == null)

  def assertTrue[A](a: A): Unit =
    assert(a == true)

  private class MockAppendable extends Appendable {
    def append(arg0: CharSequence): Appendable = null

    def append(arg0: Char): Appendable = null

    def append(arg0: CharSequence, arg1: Int, arg2: Int): Appendable = null
  }

  private class MockFormattable extends Formattable {
    def formatTo(formatter: Formatter,
                 flags: Int,
                 width: Int,
                 precision: Int): Unit = {
      if ((flags & FormattableFlags.UPPERCASE) != 0)
        formatter.format(
          "CUSTOMIZED FORMAT FUNCTION" + " WIDTH: " + width + " PRECISION: " + precision)
      else
        formatter.format(
          "customized format function" + " width: " + width + " precision: " + precision)
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

  test("Constructor()") {
    val f = new Formatter()
    assertNotNull(f)
    assertTrue(f.out().isInstanceOf[StringBuilder])
    assertEquals(f.locale(), Locale.getDefault())
    assertNotNull(f.toString())
  }

  test("Constructor(Appendable)") {
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

  test("Constructor(Locale)") {
    val f1 = new Formatter(Locale.FRANCE)
    assertTrue(f1.out().isInstanceOf[StringBuilder])
    assertEquals(f1.locale(), Locale.FRANCE)
    assertNotNull(f1.toString())

    val f2 = new Formatter(null.asInstanceOf[Locale])
    assertNull(f2.locale())
    assertTrue(f2.out().isInstanceOf[StringBuilder])
    assertNotNull(f2.toString())
  }

  test("Constructor(Appendable, Locale)") {
    val ma = new MockAppendable()
    val f1 = new Formatter(ma, Locale.CANADA)
    assertEquals(ma, f1.out())
    assertEquals(f1.locale(), Locale.CANADA)

    val f2 = new Formatter(ma, null)
    assertNull(f2.locale())
    assertEquals(ma, f1.out())

    val f3 = new Formatter(null, Locale.GERMAN)
    assertEquals(f3.locale(), Locale.GERMAN)
    assertTrue(f3.out().isInstanceOf[StringBuilder])
  }

  test("Constructor(String)") {
    assertThrows[NullPointerException](
      new Formatter(null.asInstanceOf[String]))

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
      assertThrows[FileNotFoundException](new Formatter(readOnly.getPath()))
    }
  }

  testFails("Constructor(String, String)", 816) {
    // OutputStreamWriter should throw UnsupportedEncodingException (NOT UnsupportedCharsetException)
    assertThrows[NullPointerException](
      new Formatter(null.asInstanceOf[String],
                    Charset.defaultCharset().name()))

    locally {
      val f =
        new Formatter(notExist.getPath(), Charset.defaultCharset().name())
      assertEquals(f.locale(), Locale.getDefault())
      f.close()
    }

    assertThrows[UnsupportedEncodingException](
      new Formatter(notExist.getPath(), "ISO 111-1")) // fails #816

    locally {
      val f = new Formatter(fileWithContent.getPath(), "UTF-16BE")
      assertEquals(0, fileWithContent.length())
      f.close()
    }

    if (!root) {
      assertThrows[FileNotFoundException](
        new Formatter(readOnly.getPath(), "UTF-16BE"))
    }
  }

  testFails("Constructor(String, String, Locale)", 816) {
    // OutputStreamWriter should throw UnsupportedEncodingException (NOT UnsupportedCharsetException)
    assertThrows[NullPointerException](
      new Formatter(null.asInstanceOf[String],
                    Charset.defaultCharset().name(),
                    Locale.KOREA))

    locally {
      val f = new Formatter(notExist.getPath(),
                            Charset.defaultCharset().name(),
                            null)
      assertNotNull(f)
      f.close()
    }

    locally {
      val f = new Formatter(notExist.getPath(),
                            Charset.defaultCharset().name(),
                            Locale.KOREA)
      assertEquals(f.locale(), Locale.KOREA)
      f.close()
    }

    assertThrows[UnsupportedEncodingException](new Formatter(
      notExist.getPath(),
      "ISO 1111-1",
      Locale.CHINA)) // fails #816

    locally {
      val f = new Formatter(fileWithContent.getPath(),
                            "UTF-16BE",
                            Locale.CANADA_FRENCH)
      assertEquals(0, fileWithContent.length())
      f.close()
    }

    if (!root) {
      assertThrows[FileNotFoundException](
        new Formatter(readOnly.getPath(),
                      Charset.defaultCharset().name(),
                      Locale.ITALY))
    }
  }

  test("Constructor(File)") {
    // segfault
    // assertThrows[NullPointerException](new Formatter(null.asInstanceOf[File]))

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
      assertThrows[FileNotFoundException](new Formatter(readOnly))
    }
  }

  testFails("Constructor(File, String)", 816) {
    // OutputStreamWriter should throw UnsupportedEncodingException (NOT UnsupportedCharsetException)

    // segfault
    // assertThrows[NullPointerException](new Formatter(null.asInstanceOf[File], Charset.defaultCharset().name()))

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
      assertThrows[FileNotFoundException](
        new Formatter(readOnly, Charset.defaultCharset().name()))
    }

    // segfault
    // try {
    //   assertThrows[NullPointerException](new Formatter(notExist, null))
    // } finally if (notExist.exists()) {
    //   // Fail on RI on Windows, because output stream is created and
    //   // not closed when exception thrown
    //   assertTrue(notExist.delete())
    // }

    try {
      assertThrows[UnsupportedEncodingException](
        new Formatter(notExist, "ISO 1111-1")) /// fails #816
    } finally if (notExist.exists()) {
      // Fail on RI on Windows, because output stream is created and
      // not closed when exception thrown
      assertTrue(notExist.delete())
    }
  }

  testFails("Constructor(File, String, Locale)", 816) {
    // OutputStreamWriter should throw UnsupportedEncodingException (NOT UnsupportedCharsetException)

    // segfault
    // assertThrows[NullPointerException](new Formatter(null.asInstanceOf[File], Charset.defaultCharset().name(), Locale.KOREA))

    // segfault
    // assertThrows[NullPointerException](new Formatter(notExist, null, Locale.KOREA))

    locally {
      val f = new Formatter(notExist, Charset.defaultCharset().name(), null)
      assertNotNull(f)
      f.close()
    }

    locally {
      val f =
        new Formatter(notExist, Charset.defaultCharset().name(), Locale.KOREA)
      assertEquals(f.locale(), Locale.KOREA)
      f.close()
    }

    assertThrows[UnsupportedEncodingException](
      new Formatter(notExist, "ISO 1111-1", Locale.CHINA)) // fails #816

    locally {
      val f = new Formatter(fileWithContent.getPath,
                            "UTF-16BE",
                            Locale.CANADA_FRENCH)
      assertEquals(0, fileWithContent.length)
      f.close()
    }

    if (!root) {
      assertThrows[FileNotFoundException](
        new Formatter(readOnly.getPath,
                      Charset.defaultCharset().name(),
                      Locale.ITALY))
    }
  }

  test("Constructor(PrintStream)") {
    assertThrows[NullPointerException](
      new Formatter(null.asInstanceOf[PrintStream]))

    val ps = new PrintStream(notExist, "UTF-16BE")
    val f  = new Formatter(ps)
    assertEquals(Locale.getDefault(), f.locale())
    f.close()
  }

  test("Constructor(OutputStream)") {
    assertThrows[NullPointerException](
      new Formatter(null.asInstanceOf[OutputStream]))

    val os = new FileOutputStream(notExist)
    val f  = new Formatter(os)
    assertEquals(Locale.getDefault(), f.locale())
    f.close()
  }

  testFails("Constructor(OutputStream, String)", 816) { // also 818
    // OutputStreamWriter should throw UnsupportedEncodingException (NOT UnsupportedCharsetException)
    // OutputStreamWriter should throw NPE if its argument is null

    assertThrows[NullPointerException](
      new Formatter(null.asInstanceOf[OutputStream],
                    Charset.defaultCharset().name())) // fails #818

    // segfault
    // locally {
    //   val os = new FileOutputStream(notExist)
    //   assertThrows[NullPointerException](new Formatter(os, null))
    //   os.close()
    // }

    locally {
      // Porting note: PipedOutputStream is not essential to this test.
      // Since it doesn't exist on Scala Native yet, it is replaced with a harmless one.
      // val os = new PipedOutputStream()
      val os = new ByteArrayOutputStream
      assertThrows[UnsupportedEncodingException](new Formatter(os, "TMP-1111")) // fails #816
    }

    locally {
      val os = new FileOutputStream(fileWithContent)
      val f  = new Formatter(os, "UTF-16BE")
      assertEquals(Locale.getDefault, f.locale())
      f.close()
    }
  }

  testFails("Constructor(OutputStream, String, Locale)", 816) { // also 818
    // OutputStreamWriter should throw UnsupportedEncodingException (NOT UnsupportedCharsetException)
    // OutputStreamWriter should throw NPE if its argument is null

    assertThrows[NullPointerException](
      new Formatter(null.asInstanceOf[OutputStream],
                    Charset.defaultCharset().name(),
                    Locale.getDefault)) // fails #818

    // segfault
    // locally {
    //   val os = new FileOutputStream(notExist)
    //   assertThrows[NullPointerException](new Formatter(os, null, Locale.getDefault))
    //   os.close()
    // }

    locally {
      val os = new FileOutputStream(notExist)
      val f  = new Formatter(os, Charset.defaultCharset().name(), null)
      f.close()
    }

    locally {
      // Porting note: PipedOutputStream is not essential to this test.
      // Since it doesn't exist on Scala Native yet, it is replaced with a harmless one.
      // val os = new PipedOutputStream()
      val os = new ByteArrayOutputStream
      assertThrows[UnsupportedEncodingException](
        new Formatter(os, "TMP-1111", Locale.getDefault)) // fails #816
    }

    locally {
      val os = new FileOutputStream(fileWithContent)
      val f  = new Formatter(os, "UTF-16BE", Locale.ENGLISH)
      assertEquals(Locale.ENGLISH, f.locale())
      f.close()
    }
  }

  test("locale()") {
    val f = new Formatter(null.asInstanceOf[Locale])
    assertNull(f.locale())

    f.close()
    assertThrows[FormatterClosedException](f.locale())
  }

  test("out()") {
    val f = new Formatter()
    assertNotNull(f.out())
    assertTrue(f.out().isInstanceOf[StringBuilder])
    f.close()
    assertThrows[FormatterClosedException](f.out())
  }

  test("flush()") {
    locally {
      val f = new Formatter(notExist)
      assertTrue(f.isInstanceOf[Flushable])
      f.close()
      assertThrows[FormatterClosedException](f.out())
    }

    locally {
      val f = new Formatter()
      // For destination that does not implement Flushable
      // No exception should be thrown
      f.flush()
    }
  }

  test("close()") {
    val f = new Formatter(notExist)
    assertTrue(f.isInstanceOf[Closeable])
    f.close()
    // close next time will not throw exception
    f.close()
    assertNull(f.ioException())
  }

  test("toString()") {
    val f = new Formatter()
    assertNotNull(f.toString())
    assertEquals(f.out().toString(), f.toString())
    f.close()
    assertThrows[FormatterClosedException](f.toString())
  }

  test("ioException()") {
    locally {
      val f = new Formatter(new MockDestination())
      assertNull(f.ioException())
      f.flush()
      assertNotNull(f.ioException())
      f.close()
    }

    locally {
      val md = new MockDestination()
      val f  = new Formatter(md)
      f.format("%s%s", "1", "2")
      // format stop working after IOException
      assertNotNull(f.ioException())
      assertEquals("", f.toString())
    }
  }

  test("format(String, Array[Object]) for null parameter") {
    locally {
      val f = new Formatter()
      // segfault
      // assertThrows[NullPointerException](f.format(null.asInstanceOf[String], "parameter"))
    }

    locally {
      val f = new Formatter()
      f.format("hello", null.asInstanceOf[Array[Object]])
      assertEquals("hello", f.toString())
    }
  }

  test("format(String, Array[Object]) for argument index") {
    locally {
      val f = new Formatter(Locale.US)
      f.format("%1$s%2$s%3$s%4$s%5$s%6$s%7$s%8$s%9$s%11$s%10$s",
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
               "11")
      assertEquals("1234567891110", f.toString())
    }

    locally {
      val f = new Formatter(Locale.JAPAN)
      f.format("%0$s", "hello")
      assertEquals("hello", f.toString())
    }

    locally {
      val f = new Formatter(Locale.US)
      assertThrows[UnknownFormatConversionException](
        f.format("%-1$s", "1", "2"))
    }

    locally {
      val f = new Formatter(Locale.US)
      assertThrows[UnknownFormatConversionException](
        f.format("%$s", "hello", "2"))
    }

    locally {
      val f = new Formatter(Locale.US)
      assertThrows[UnknownFormatConversionException](f.format("%", "string"))
    }

    locally {
      val f = new Formatter(Locale.FRANCE)
      f.format("%1$s%2$s%3$s%4$s%5$s%6$s%7$s%8$s%<s%s%s%<s",
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
               "11")
      assertEquals("123456788122", f.toString())
    }

    locally {
      val f = new Formatter(Locale.FRANCE)
      f.format("xx%1$s22%2$s%s%<s%5$s%<s&%7$h%2$s%8$s%<s%s%s%<ssuffix",
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
               "11")
      assertEquals("xx12221155&7288233suffix", f.toString())
      assertThrows[MissingFormatArgumentException](f.format("%<s", "hello"))
    }

    locally {
      val f = new Formatter(Locale.US)
      assertThrows[MissingFormatArgumentException](f.format("%123$s", "hello"))
    }

    locally {
      val f = new Formatter(Locale.US)
      // 2147483648 is the value of Integer.MAX_VALUE + 1
      assertThrows[MissingFormatArgumentException](
        f.format("%2147483648$s", "hello"))
      // 2147483647 is the value of Integer.MAX_VALUE
      assertThrows[MissingFormatArgumentException](
        f.format("%2147483647$s", "hello"))
    }

    locally {
      val f = new Formatter(Locale.US)
      assertThrows[MissingFormatArgumentException](f.format("%s%s", "hello"))
    }

    locally {
      val f = new Formatter(Locale.US)
      f.format("$100", 100.asInstanceOf[Object])
      assertEquals("$100", f.toString())
    }

    locally {
      val f = new Formatter(Locale.UK)
      f.format("%01$s", "string")
      assertEquals("string", f.toString())
    }
  }

  test("format(String, Array[Object]) for width") {
    locally {
      val f = new Formatter(Locale.US)
      f.format("%1$8s", "1")
      assertEquals("       1", f.toString())
    }

    locally {
      val f = new Formatter(Locale.US)
      f.format("%1$-1%", "string")
      assertEquals("%", f.toString())
    }

    locally {
      val f = new Formatter(Locale.ITALY)
      // 2147483648 is the value of Integer.MAX_VALUE + 1
      f.format("%2147483648s", "string")
      assertEquals("string", f.toString())
    }

    // the value of Integer.MAX_VALUE will allocate about 4G bytes of
    // memory.
    // It may cause OutOfMemoryError, so this value is not tested
  }

  test("format(String, Array[Object]) for precision") {
    locally {
      val f = new Formatter(Locale.US)
      f.format("%.5s", "123456")
      assertEquals("12345", f.toString())
    }

    locally {
      val f = new Formatter(Locale.US)
      // 2147483648 is the value of Integer.MAX_VALUE + 1
      f.format("%.2147483648s", "...")
      assertEquals("...", f.toString())
    }

    // the value of Integer.MAX_VALUE will allocate about 4G bytes of
    // memory.
    // It may cause OutOfMemoryError, so this value is not tested

    locally {
      val f = new Formatter(Locale.US)
      f.format("%10.0b", java.lang.Boolean.TRUE)
      assertEquals("          ", f.toString())
    }

    locally {
      val f = new Formatter(Locale.US)
      f.format("%10.01s", "hello")
      assertEquals("         h", f.toString())
    }

    locally {
      val f = new Formatter(Locale.US)
      assertThrows[UnknownFormatConversionException](
        f.format("%.s", "hello", "2"))
    }

    locally {
      val f = new Formatter(Locale.US)
      assertThrows[UnknownFormatConversionException](
        f.format("%.-5s", "123456"))
    }

    locally {
      val f = new Formatter(Locale.US)
      assertThrows[UnknownFormatConversionException](
        f.format("%1.s", "hello", "2"))
    }

    locally {
      val f = new Formatter(Locale.US)
      f.format("%5.1s", "hello")
      assertEquals("    h", f.toString())
    }

    locally {
      val f = new Formatter(Locale.FRANCE)
      f.format("%.0s", "hello", "2")
      assertEquals("", f.toString())
    }
  }

  test("format(String, Array[Object]) for line separator") {
    val oldSeparator = System.getProperty("line.separator")
    System.setProperty("line.separator", "!\n")
    try {
      locally {
        val f = new Formatter(Locale.US)
        f.format("%1$n", 1.asInstanceOf[Object])
        assertEquals("!\n", f.toString())
      }

      locally {
        val f = new Formatter(Locale.KOREAN)
        f.format("head%1$n%2$n", 1.asInstanceOf[Object], new Date())
        assertEquals("head!\n!\n", f.toString())
      }

      locally {
        val f = new Formatter(Locale.US)
        f.format("%n%s", "hello")
        assertEquals("!\nhello", f.toString())
      }
    } finally {
      System.setProperty("line.separator", oldSeparator)
    }

    locally {
      val f = new Formatter(Locale.US)
      assertThrows[IllegalFormatFlagsException](f.format("%-n"))
      assertThrows[IllegalFormatFlagsException](f.format("%+n"))
      assertThrows[IllegalFormatFlagsException](f.format("%#n"))
      assertThrows[IllegalFormatFlagsException](f.format("% n"))
      assertThrows[IllegalFormatFlagsException](f.format("%0n"))
      assertThrows[IllegalFormatFlagsException](f.format("%,n"))
      assertThrows[IllegalFormatFlagsException](f.format("%(n"))
    }

    locally {
      val f = new Formatter(Locale.US)
      assertThrows[IllegalFormatWidthException](f.format("%4n"))
    }

    locally {
      val f = new Formatter(Locale.US)
      assertThrows[IllegalFormatWidthException](f.format("%-4n"))
    }

    locally {
      val f = new Formatter(Locale.US)
      assertThrows[IllegalFormatPrecisionException](f.format("%.9n"))
    }

    locally {
      val f = new Formatter(Locale.US)
      assertThrows[IllegalFormatPrecisionException](f.format("%5.9n"))
    }

    System.setProperty("line.separator", oldSeparator)
  }

  test("format(String, Array[Object]) for percent") {
    locally {
      val f = new Formatter(Locale.ENGLISH)
      f.format("%1$%", 100.asInstanceOf[Object])
      assertEquals("%", f.toString())
    }

    locally {
      val f = new Formatter(Locale.CHINA)
      f.format("%1$%%%", "hello", new Object())
      assertEquals("%%", f.toString())
    }

    locally {
      val f = new Formatter(Locale.CHINA)
      f.format("%%%s", "hello")
      assertEquals("%hello", f.toString())
    }

    locally {
      val f = new Formatter(Locale.US)
      assertThrows[IllegalFormatPrecisionException](f.format("%.9%"))
    }

    locally {
      val f = new Formatter(Locale.US)
      assertThrows[IllegalFormatPrecisionException](f.format("%5.9%"))
    }

    locally {
      val f = new Formatter(Locale.US)
      assertFormatFlagsConversionMismatchException(f, "%+%")
      assertFormatFlagsConversionMismatchException(f, "%#%")
      assertFormatFlagsConversionMismatchException(f, "% %")
      assertFormatFlagsConversionMismatchException(f, "%0%")
      assertFormatFlagsConversionMismatchException(f, "%,%")
      assertFormatFlagsConversionMismatchException(f, "%(%")
    }

    locally {
      val f = new Formatter(Locale.KOREAN)
      f.format("%4%", 1.asInstanceOf[Object])
      /*
       * fail on RI the output string should be right justified by appending
       * spaces till the whole string is 4 chars width.
       */
      assertEquals("   %", f.toString())
    }

    locally {
      val f = new Formatter(Locale.US)
      f.format("%-4%", 100.asInstanceOf[Object])
      /*
       * fail on RI, throw UnknownFormatConversionException the output string
       * should be left justified by appending spaces till the whole string is
       * 4 chars width.
       */
      assertEquals("%   ", f.toString())
    }
  }

  private def assertFormatFlagsConversionMismatchException(
      f: Formatter,
      str: String): Unit = {
    /*
     * error on RI, throw IllegalFormatFlagsException specification
     * says FormatFlagsConversionMismatchException should be thrown
     */
    assertThrows[FormatFlagsConversionMismatchException](f.format(str))
  }

  test("format(String, Array[Object]) for flag") {
    locally {
      val f = new Formatter(Locale.US)
      assertThrows[DuplicateFormatFlagsException](
        f.format("%1$-#-8s", "something"))
    }

    locally {
      val chars = Array('-', '#', '+', ' ', '0', ',', '(', '%', '<')
      Arrays.sort(chars)
      val f = new Formatter(Locale.US)
      for (i <- (0 to 256).map(_.toChar)) {
        // test 8 bit character
        if (Arrays.binarySearch(chars, i) >= 0 || Character.isDigit(i) || Character
              .isLetter(i)) {
          // Do not test 0-9, a-z, A-Z and characters in the chars array.
          // They are characters used as flags, width or conversions
        } else {
          assertThrows[UnknownFormatConversionException](
            f.format("%" + i + "s", 1.asInstanceOf[Object]))
        }
      }
    }
  }

  test("format(String, Array[Object]) for general conversion b/B") {
    val triple = Array(
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

    val input   = 0
    val pattern = 1
    val output  = 2
    for (i <- 0 until triple.length) {
      locally {
        val f = new Formatter(Locale.FRANCE)
        f.format(triple(i)(pattern).asInstanceOf[String], triple(i)(input))
        assertEquals(triple(i)(output), f.toString())
      }

      locally {
        val f = new Formatter(Locale.GERMAN)
        f.format(
          triple(i)(pattern).asInstanceOf[String].toUpperCase(Locale.US),
          triple(i)(input))
        assertEquals(
          triple(i)(output).asInstanceOf[String].toUpperCase(Locale.US),
          f.toString())
      }
    }
  }

  testFails(
    "format(String, Array[Object]) for general conversion type 's' and 'S'",
    481) {
    // 1.1.toString = "1.1" on Scala JVM, "1.100000" on Scala Native
    // Formatter$.Transformer.padding trims to `precision` number of chars, and then pads up to `width`.
    // The excess '0's lead to test failure.
    val triple = Array(
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
      Array(Float.box(1.1f), "%-6.4s", "1.1   "), // fails #481
      Array(Float.box(1.1f), "%.5s", "1.1"), // fails #481
      Array(Double.box(1.1d), "%2.3s", "1.1"),
      Array(Double.box(1.1d), "%-6.4s", "1.1   "), // fails #481
      Array(Double.box(1.1d), "%.5s", "1.1"), // fails #481
      Array("", "%2.3s", "  "),
      Array("", "%-6.4s", "      "),
      Array("", "%.5s", ""),
      Array("string content", "%2.3s", "str"),
      Array("string content", "%-6.4s", "stri  "),
      Array("string content", "%.5s", "strin"),
      Array(new MockFormattable(),
            "%2.3s",
            "customized format function width: 2 precision: 3"),
      Array(new MockFormattable(),
            "%-6.4s",
            "customized format function width: 6 precision: 4"),
      Array(new MockFormattable(),
            "%.5s",
            "customized format function width: -1 precision: 5"),
      Array(null.asInstanceOf[Object], "%2.3s", "nul"),
      Array(null.asInstanceOf[Object], "%-6.4s", "null  "),
      Array(null.asInstanceOf[Object], "%.5s", "null")
    )

    val input   = 0
    val pattern = 1
    val output  = 2
    for (i <- (0 until triple.length)) {
      locally {
        val f = new Formatter(Locale.FRANCE)
        f.format(triple(i)(pattern).asInstanceOf[String], triple(i)(input))
        assertEquals(triple(i)(output), f.toString())
      }

      locally {
        val f = new Formatter(Locale.GERMAN)
        f.format(
          triple(i)(pattern).asInstanceOf[String].toUpperCase(Locale.US),
          triple(i)(input))
        assertEquals(
          triple(i)(output).asInstanceOf[String].toUpperCase(Locale.US),
          f.toString())
      }
    }
  }

  test("format(String, Array[Object]) for general conversion type 'h' and 'H'") {
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
        val f = new Formatter(Locale.FRANCE)
        f.format("%h", input(i))
        assertEquals(Integer.toHexString(input(i).hashCode()), f.toString())
      }

      locally {
        val f = new Formatter(Locale.GERMAN)
        f.format("%H", input(i))
        assertEquals(
          Integer.toHexString(input(i).hashCode()).toUpperCase(Locale.US),
          f.toString())
      }
    }
  }

  test("format(String, Array[Object]) for general conversion other cases") {
    locally {
      /*
       * In Turkish locale, the upper case of '\u0069' is '\u0130'. The
       * following test indicate that '\u0069' is coverted to upper case
       * without using the turkish locale.
       */
      val f = new Formatter(new Locale("tr"))
      f.format("%S", "\u0069")
      assertEquals("\u0049", f.toString())
    }

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
    val f = new Formatter(Locale.GERMAN)
    for (i <- (0 until input.length)) {
      if (!input(i).isInstanceOf[Formattable]) {
        /*
         * fail on RI, spec says if the '#' flag is present and the
         * argument is not a Formattable , then a
         * FormatFlagsConversionMismatchException will be thrown.
         */
        assertThrows[FormatFlagsConversionMismatchException](
          f.format("%#s", input(i)))
      } else {
        f.format("%#s%<-#8s", input(i))
        assertEquals(
          "customized format function width: -1 precision: -1customized format function width: 8 precision: -1",
          f.toString())
      }
    }
  }

  test("format(String, Array[Object]) for general conversion exception") {
    locally {
      val flagMismatch = Array(
        "%#b",
        "%+b",
        "% b",
        "%0b",
        "%,b",
        "%(b",
        "%#B",
        "%+B",
        "% B",
        "%0B",
        "%,B",
        "%(B",
        "%#h",
        "%+h",
        "% h",
        "%0h",
        "%,h",
        "%(h",
        "%#H",
        "%+H",
        "% H",
        "%0H",
        "%,H",
        "%(H",
        "%+s",
        "% s",
        "%0s",
        "%,s",
        "%(s",
        "%+S",
        "% S",
        "%0S",
        "%,S",
        "%(S"
      )

      val f = new Formatter(Locale.US)

      for (i <- 0 until flagMismatch.length) {
        assertThrows[FormatFlagsConversionMismatchException](
          f.format(flagMismatch(i), "something"))
      }

      val missingWidth = Array("%-b", "%-B", "%-h", "%-H", "%-s", "%-S")
      for (i <- 0 until missingWidth.length) {
        assertThrows[MissingFormatWidthException](
          f.format(missingWidth(i), "something"))
      }
    }

    // Regression test
    locally {
      val f = new Formatter()
      assertThrows[IllegalFormatCodePointException](
        f.format("%c", -0x0001.toByte.asInstanceOf[Object]))
    }

    locally {
      val f = new Formatter()
      assertThrows[IllegalFormatCodePointException](
        f.format("%c", -0x0001.toShort.asInstanceOf[Object]))
    }

    locally {
      val f = new Formatter()
      assertThrows[IllegalFormatCodePointException](
        f.format("%c", -0x0001.asInstanceOf[Object]))
    }
  }

  test("format(String, Array[Object]) for Character conversion") {
    val f       = new Formatter(Locale.US)
    val illArgs = Array(true, 1.1f, 1.1d, "string content", 1.1f, new Date())
    for (i <- (0 until illArgs.length)) {
      assertThrows[IllegalFormatConversionException](
        f.format("%c", illArgs(i).asInstanceOf[Object]))
    }

    assertThrows[IllegalFormatCodePointException](
      f.format("%c", Integer.MAX_VALUE.asInstanceOf[Object]))

    assertThrows[FormatFlagsConversionMismatchException](
      f.format("%#c", 'c'.asInstanceOf[Object]))

    val triple = Array(
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

    val input   = 0
    val pattern = 1
    val output  = 2
    for (i <- 0 until triple.length) {
      val f = new Formatter(Locale.US)
      f.format(triple(i)(pattern).asInstanceOf[String],
               triple(i)(input).asInstanceOf[Object])
      assertEquals(triple(i)(output), f.toString())
    }

    locally {
      val f = new Formatter(Locale.US)
      f.format("%c", 0x10000.asInstanceOf[Object])
      assertEquals(0x10000, f.toString().codePointAt(0))

      assertThrows[IllegalFormatPrecisionException](
        f.format("%2.2c", 'c'.asInstanceOf[Object]))
    }

    locally {
      val f = new Formatter(Locale.US)
      f.format("%C", 'w'.asInstanceOf[Object])
      // error on RI, throw UnknownFormatConversionException
      // RI do not support converter 'C'
      assertEquals("W", f.toString())
    }

    locally {
      val f = new Formatter(Locale.JAPAN)
      f.format("%Ced", 0x1111.asInstanceOf[Object])
      // error on RI, throw UnknownFormatConversionException
      // RI do not support converter 'C'
      assertEquals("\u1111ed", f.toString())
    }
  }

  testFails(
    "format(String, Array[Object]) for legal Byte/Short/Integer/Long conversion type 'd'",
    0) { // issue not filed yet
    val triple = Array(
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
      Array(0xf123.toShort, "%,d", "-3.805"),
      Array(0xf123.toShort, "%(d", "(3805)"),
      Array(0xf123.toShort, "%08d", "-0003805"),
      Array(0xf123.toShort, "%-+,(11d", "(3.805)    "),
      Array(0xf123.toShort, "%0 ,(11d", "(00003.805)"),
      Array(0x123456, "%d", "1193046"),
      Array(0x123456, "%10d", "   1193046"),
      Array(0x123456, "%-1d", "1193046"),
      Array(0x123456, "%+d", "+1193046"),
      Array(0x123456, "% d", " 1193046"),
      Array(0x123456, "%,d", "1.193.046"),
      Array(0x123456, "%(d", "1193046"),
      Array(0x123456, "%08d", "01193046"),
      Array(0x123456, "%-+,(11d", "+1.193.046 "),
      Array(0x123456, "%0 ,(11d", " 01.193.046"),
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
      Array(0x7654321L, "%,d", "124.076.833"),
      Array(0x7654321L, "%(d", "124076833"),
      Array(0x7654321L, "%08d", "124076833"),
      Array(0x7654321L, "%-+,(11d", "+124.076.833"),
      Array(0x7654321L, "%0 ,(11d", " 124.076.833"),
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

    val input   = 0
    val pattern = 1
    val output  = 2
    for (i <- 0 until triple.length) {
      val f = new Formatter(Locale.GERMAN)
      f.format(triple(i)(pattern).asInstanceOf[String],
               triple(i)(input).asInstanceOf[Object])
      assertEquals(triple(i)(output), f.toString())
    }
  }

  test(
    "format(String, Array[Object]) for legal Byte/Short/Integer/Long conversion type 'o'") {
    val triple = Array(
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

    val input   = 0
    val pattern = 1
    val output  = 2
    for (i <- 0 until triple.length) {
      val f = new Formatter(Locale.ITALY)
      f.format(triple(i)(pattern).asInstanceOf[String],
               triple(i)(input).asInstanceOf[Object])
      assertEquals(triple(i)(output), f.toString())
    }
  }

  test(
    "format(String, Array[Object]) for legal Byte/Short/Integer/Long conversion type 'x' and 'X'") {
    val triple = Array(
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

    val input   = 0
    val pattern = 1
    val output  = 2
    for (i <- 0 until triple.length) {
      locally {
        val f = new Formatter(Locale.FRANCE)
        f.format(triple(i)(pattern).asInstanceOf[String],
                 triple(i)(input).asInstanceOf[Object])
        assertEquals(triple(i)(output), f.toString())
      }

      locally {
        val f = new Formatter(Locale.FRANCE)
        f.format(triple(i)(pattern).asInstanceOf[String],
                 triple(i)(input).asInstanceOf[Object])
        assertEquals(triple(i)(output), f.toString())
      }
    }
  }

  testFails("format(String, Array[Object]) for Date/Time conversion", 0) { // issue not filed yet
    // calls to the methods of Calender throws NotImplementedError
    // even if we comment out all of `paris` and `china` below,
    // Calendar$.getInstance still gets called from
    // Formatter$Transformer.transformFromDateTime
    val now = new Date(1147327147578L)

    val paris =
      Calendar.getInstance(TimeZone.getTimeZone("Europe/Paris"), Locale.FRANCE)
    paris.set(2006, 4, 8, 12, 0, 0)
    paris.set(Calendar.MILLISECOND, 453)
    val china =
      Calendar.getInstance(TimeZone.getTimeZone("GMT-08:00"), Locale.CHINA)
    china.set(2006, 4, 8, 12, 0, 0)
    china.set(Calendar.MILLISECOND, 609)

    val lowerCaseGermanTriple = Array(
      Array(0L, 'a', "Do."),
      Array(java.lang.Long.MAX_VALUE, 'a', "So."),
      Array(-1000L, 'a', "Do."),
      Array(new Date(1147327147578L), 'a', "Do."),
      Array(paris, 'a', "Mo."),
      Array(china, 'a', "Mo."),
      Array(0L, 'b', "Jan"),
      Array(java.lang.Long.MAX_VALUE, 'b', "Aug"),
      Array(-1000L, 'b', "Jan"),
      Array(new Date(1147327147578L), 'b', "Mai"),
      Array(paris, 'b', "Mai"),
      Array(china, 'b', "Mai"),
      Array(0L, 'c', "Do. Jan 01 08:00:00 GMT+08:00 1970"),
      Array(java.lang.Long.MAX_VALUE,
            'c',
            "So. Aug 17 15:18:47 GMT+08:00 292278994"),
      Array(-1000L, 'c', "Do. Jan 01 07:59:59 GMT+08:00 1970"),
      Array(new Date(1147327147578L),
            'c',
            "Do. Mai 11 13:59:07 GMT+08:00 2006"),
      Array(paris, 'c', "Mo. Mai 08 12:00:00 MESZ 2006"),
      Array(china, 'c', "Mo. Mai 08 12:00:00 GMT-08:00 2006"),
      Array(0L, 'd', "01"),
      Array(java.lang.Long.MAX_VALUE, 'd', "17"),
      Array(-1000L, 'd', "01"),
      Array(new Date(1147327147578L), 'd', "11"),
      Array(paris, 'd', "08"),
      Array(china, 'd', "08"),
      Array(0L, 'e', "1"),
      Array(java.lang.Long.MAX_VALUE, 'e', "17"),
      Array(-1000L, 'e', "1"),
      Array(new Date(1147327147578L), 'e', "11"),
      Array(paris, 'e', "8"),
      Array(china, 'e', "8"),
      Array(0L, 'h', "Jan"),
      Array(java.lang.Long.MAX_VALUE, 'h', "Aug"),
      Array(-1000L, 'h', "Jan"),
      Array(new Date(1147327147578L), 'h', "Mai"),
      Array(paris, 'h', "Mai"),
      Array(china, 'h', "Mai"),
      Array(0L, 'j', "001"),
      Array(java.lang.Long.MAX_VALUE, 'j', "229"),
      Array(-1000L, 'j', "001"),
      Array(new Date(1147327147578L), 'j', "131"),
      Array(paris, 'j', "128"),
      Array(china, 'j', "128"),
      Array(0L, 'k', "8"),
      Array(java.lang.Long.MAX_VALUE, 'k', "15"),
      Array(-1000L, 'k', "7"),
      Array(new Date(1147327147578L), 'k', "13"),
      Array(paris, 'k', "12"),
      Array(china, 'k', "12"),
      Array(0L, 'l', "8"),
      Array(java.lang.Long.MAX_VALUE, 'l', "3"),
      Array(-1000L, 'l', "7"),
      Array(new Date(1147327147578L), 'l', "1"),
      Array(paris, 'l', "12"),
      Array(china, 'l', "12"),
      Array(0L, 'm', "01"),
      Array(java.lang.Long.MAX_VALUE, 'm', "08"),
      Array(-1000L, 'm', "01"),
      Array(new Date(1147327147578L), 'm', "05"),
      Array(paris, 'm', "05"),
      Array(china, 'm', "05"),
      Array(0L, 'p', "vorm."),
      Array(java.lang.Long.MAX_VALUE, 'p', "nachm."),
      Array(-1000L, 'p', "vorm."),
      Array(new Date(1147327147578L), 'p', "nachm."),
      Array(paris, 'p', "nachm."),
      Array(china, 'p', "nachm."),
      Array(0L, 'r', "08:00:00 vorm."),
      Array(java.lang.Long.MAX_VALUE, 'r', "03:18:47 nachm."),
      Array(-1000L, 'r', "07:59:59 vorm."),
      Array(new Date(1147327147578L), 'r', "01:59:07 nachm."),
      Array(paris, 'r', "12:00:00 nachm."),
      Array(china, 'r', "12:00:00 nachm."),
      Array(0L, 's', "0"),
      Array(java.lang.Long.MAX_VALUE, 's', "9223372036854775"),
      Array(-1000L, 's', "-1"),
      Array(new Date(1147327147578L), 's', "1147327147"),
      Array(paris, 's', "1147082400"),
      Array(china, 's', "1147118400"),
      Array(0L, 'y', "70"),
      Array(java.lang.Long.MAX_VALUE, 'y', "94"),
      Array(-1000L, 'y', "70"),
      Array(new Date(1147327147578L), 'y', "06"),
      Array(paris, 'y', "06"),
      Array(china, 'y', "06"),
      Array(0L, 'z', "+0800"),
      Array(java.lang.Long.MAX_VALUE, 'z', "+0800"),
      Array(-1000L, 'z', "+0800"),
      Array(new Date(1147327147578L), 'z', "+0800"),
      Array(paris, 'z', "+0100"),
      Array(china, 'z', "-0800")
    )

    val lowerCaseFranceTriple = Array(
      Array(0L, 'a', "jeu."),
      Array(java.lang.Long.MAX_VALUE, 'a', "dim."),
      Array(-1000L, 'a', "jeu."),
      Array(new Date(1147327147578L), 'a', "jeu."),
      Array(paris, 'a', "lun."),
      Array(china, 'a', "lun."),
      Array(0L, 'b', "janv."),
      Array(java.lang.Long.MAX_VALUE, 'b', "ao\u00fbt"),
      Array(-1000L, 'b', "janv."),
      Array(new Date(1147327147578L), 'b', "mai"),
      Array(paris, 'b', "mai"),
      Array(china, 'b', "mai"),
      Array(0L, 'c', "jeu. janv. 01 08:00:00 UTC+08:00 1970"),
      Array(java.lang.Long.MAX_VALUE,
            'c',
            "dim. ao\u00fbt 17 15:18:47 UTC+08:00 292278994"),
      Array(-1000L, 'c', "jeu. janv. 01 07:59:59 UTC+08:00 1970"),
      Array(new Date(1147327147578L),
            'c',
            "jeu. mai 11 13:59:07 UTC+08:00 2006"),
      Array(paris, 'c', "lun. mai 08 12:00:00 HAEC 2006"),
      Array(china, 'c', "lun. mai 08 12:00:00 UTC-08:00 2006"),
      Array(0L, 'd', "01"),
      Array(java.lang.Long.MAX_VALUE, 'd', "17"),
      Array(-1000L, 'd', "01"),
      Array(new Date(1147327147578L), 'd', "11"),
      Array(paris, 'd', "08"),
      Array(china, 'd', "08"),
      Array(0L, 'e', "1"),
      Array(java.lang.Long.MAX_VALUE, 'e', "17"),
      Array(-1000L, 'e', "1"),
      Array(new Date(1147327147578L), 'e', "11"),
      Array(paris, 'e', "8"),
      Array(china, 'e', "8"),
      Array(0L, 'h', "janv."),
      Array(java.lang.Long.MAX_VALUE, 'h', "ao\u00fbt"),
      Array(-1000L, 'h', "janv."),
      Array(new Date(1147327147578L), 'h', "mai"),
      Array(paris, 'h', "mai"),
      Array(china, 'h', "mai"),
      Array(0L, 'j', "001"),
      Array(java.lang.Long.MAX_VALUE, 'j', "229"),
      Array(-1000L, 'j', "001"),
      Array(new Date(1147327147578L), 'j', "131"),
      Array(paris, 'j', "128"),
      Array(china, 'j', "128"),
      Array(0L, 'k', "8"),
      Array(java.lang.Long.MAX_VALUE, 'k', "15"),
      Array(-1000L, 'k', "7"),
      Array(new Date(1147327147578L), 'k', "13"),
      Array(paris, 'k', "12"),
      Array(china, 'k', "12"),
      Array(0L, 'l', "8"),
      Array(java.lang.Long.MAX_VALUE, 'l', "3"),
      Array(-1000L, 'l', "7"),
      Array(new Date(1147327147578L), 'l', "1"),
      Array(paris, 'l', "12"),
      Array(china, 'l', "12"),
      Array(0L, 'm', "01"),
      Array(java.lang.Long.MAX_VALUE, 'm', "08"),
      Array(-1000L, 'm', "01"),
      Array(new Date(1147327147578L), 'm', "05"),
      Array(paris, 'm', "05"),
      Array(china, 'm', "05"),
      Array(0L, 'p', "am"),
      Array(java.lang.Long.MAX_VALUE, 'p', "pm"),
      Array(-1000L, 'p', "am"),
      Array(new Date(1147327147578L), 'p', "pm"),
      Array(paris, 'p', "pm"),
      Array(china, 'p', "pm"),
      Array(0L, 'r', "08:00:00 AM"),
      Array(java.lang.Long.MAX_VALUE, 'r', "03:18:47 PM"),
      Array(-1000L, 'r', "07:59:59 AM"),
      Array(new Date(1147327147578L), 'r', "01:59:07 PM"),
      Array(paris, 'r', "12:00:00 PM"),
      Array(china, 'r', "12:00:00 PM"),
      Array(0L, 's', "0"),
      Array(java.lang.Long.MAX_VALUE, 's', "9223372036854775"),
      Array(-1000L, 's', "-1"),
      Array(new Date(1147327147578L), 's', "1147327147"),
      Array(paris, 's', "1147082400"),
      Array(china, 's', "1147118400"),
      Array(0L, 'y', "70"),
      Array(java.lang.Long.MAX_VALUE, 'y', "94"),
      Array(-1000L, 'y', "70"),
      Array(new Date(1147327147578L), 'y', "06"),
      Array(paris, 'y', "06"),
      Array(china, 'y', "06"),
      Array(0L, 'z', "+0800"),
      Array(java.lang.Long.MAX_VALUE, 'z', "+0800"),
      Array(-1000L, 'z', "+0800"),
      Array(new Date(1147327147578L), 'z', "+0800"),
      Array(paris, 'z', "+0100"),
      Array(china, 'z', "-0800")
    )

    val lowerCaseJapanTriple = Array(
      Array(0L, 'a', "\u6728"),
      Array(java.lang.Long.MAX_VALUE, 'a', "\u65e5"),
      Array(-1000L, 'a', "\u6728"),
      Array(new Date(1147327147578L), 'a', "\u6728"),
      Array(paris, 'a', "\u6708"),
      Array(china, 'a', "\u6708"),
      Array(0L, 'b', "1\u6708"),
      Array(java.lang.Long.MAX_VALUE, 'b', "8\u6708"),
      Array(-1000L, 'b', "1\u6708"),
      Array(new Date(1147327147578L), 'b', "5\u6708"),
      Array(paris, 'b', "5\u6708"),
      Array(china, 'b', "5\u6708"),
      Array(0L, 'c', "\u6728 1\u6708 01 08:00:00 GMT+08:00 1970"),
      Array(java.lang.Long.MAX_VALUE,
            'c',
            "\u65e5 8\u6708 17 15:18:47 GMT+08:00 292278994"),
      Array(-1000L, 'c', "\u6728 1\u6708 01 07:59:59 GMT+08:00 1970"),
      Array(new Date(1147327147578L),
            'c',
            "\u6728 5\u6708 11 13:59:07 GMT+08:00 2006"),
      Array(paris, 'c', "\u6708 5\u6708 08 12:00:00 GMT+02:00 2006"),
      Array(china, 'c', "\u6708 5\u6708 08 12:00:00 GMT-08:00 2006"),
      Array(0L, 'd', "01"),
      Array(java.lang.Long.MAX_VALUE, 'd', "17"),
      Array(-1000L, 'd', "01"),
      Array(new Date(1147327147578L), 'd', "11"),
      Array(paris, 'd', "08"),
      Array(china, 'd', "08"),
      Array(0L, 'e', "1"),
      Array(java.lang.Long.MAX_VALUE, 'e', "17"),
      Array(-1000L, 'e', "1"),
      Array(new Date(1147327147578L), 'e', "11"),
      Array(paris, 'e', "8"),
      Array(china, 'e', "8"),
      Array(0L, 'h', "1\u6708"),
      Array(java.lang.Long.MAX_VALUE, 'h', "8\u6708"),
      Array(-1000L, 'h', "1\u6708"),
      Array(new Date(1147327147578L), 'h', "5\u6708"),
      Array(paris, 'h', "5\u6708"),
      Array(china, 'h', "5\u6708"),
      Array(0L, 'j', "001"),
      Array(java.lang.Long.MAX_VALUE, 'j', "229"),
      Array(-1000L, 'j', "001"),
      Array(new Date(1147327147578L), 'j', "131"),
      Array(paris, 'j', "128"),
      Array(china, 'j', "128"),
      Array(0L, 'k', "8"),
      Array(java.lang.Long.MAX_VALUE, 'k', "15"),
      Array(-1000L, 'k', "7"),
      Array(new Date(1147327147578L), 'k', "13"),
      Array(paris, 'k', "12"),
      Array(china, 'k', "12"),
      Array(0L, 'l', "8"),
      Array(java.lang.Long.MAX_VALUE, 'l', "3"),
      Array(-1000L, 'l', "7"),
      Array(new Date(1147327147578L), 'l', "1"),
      Array(paris, 'l', "12"),
      Array(china, 'l', "12"),
      Array(0L, 'm', "01"),
      Array(java.lang.Long.MAX_VALUE, 'm', "08"),
      Array(-1000L, 'm', "01"),
      Array(new Date(1147327147578L), 'm', "05"),
      Array(paris, 'm', "05"),
      Array(china, 'm', "05"),
      Array(0L, 'p', "\u5348\u524d"),
      Array(java.lang.Long.MAX_VALUE, 'p', "\u5348\u5f8c"),
      Array(-1000L, 'p', "\u5348\u524d"),
      Array(new Date(1147327147578L), 'p', "\u5348\u5f8c"),
      Array(paris, 'p', "\u5348\u5f8c"),
      Array(china, 'p', "\u5348\u5f8c"),
      Array(0L, 'r', "08:00:00 \u5348\u524d"),
      Array(java.lang.Long.MAX_VALUE, 'r', "03:18:47 \u5348\u5f8c"),
      Array(-1000L, 'r', "07:59:59 \u5348\u524d"),
      Array(new Date(1147327147578L), 'r', "01:59:07 \u5348\u5f8c"),
      Array(paris, 'r', "12:00:00 \u5348\u5f8c"),
      Array(china, 'r', "12:00:00 \u5348\u5f8c"),
      Array(0L, 's', "0"),
      Array(java.lang.Long.MAX_VALUE, 's', "9223372036854775"),
      Array(-1000L, 's', "-1"),
      Array(new Date(1147327147578L), 's', "1147327147"),
      Array(paris, 's', "1147082400"),
      Array(china, 's', "1147118400"),
      Array(0L, 'y', "70"),
      Array(java.lang.Long.MAX_VALUE, 'y', "94"),
      Array(-1000L, 'y', "70"),
      Array(new Date(1147327147578L), 'y', "06"),
      Array(paris, 'y', "06"),
      Array(china, 'y', "06"),
      Array(0L, 'z', "+0800"),
      Array(java.lang.Long.MAX_VALUE, 'z', "+0800"),
      Array(-1000L, 'z', "+0800"),
      Array(new Date(1147327147578L), 'z', "+0800"),
      Array(paris, 'z', "+0100"),
      Array(china, 'z', "-0800")
    )

    val input   = 0
    val pattern = 1
    val output  = 2
    for (i <- 0 until 90) {
      // go through legal conversion
      val formatSpecifier      = "%t" + lowerCaseGermanTriple(i)(pattern)
      val formatSpecifierUpper = "%T" + lowerCaseGermanTriple(i)(pattern)
      // test '%t'
      locally {
        val f = new Formatter(Locale.GERMAN)
        f.format(formatSpecifier,
                 lowerCaseGermanTriple(i)(input).asInstanceOf[Object])
        assertEquals(lowerCaseGermanTriple(i)(output), f.toString())
      }

      locally {
        val f = new Formatter(Locale.GERMAN)
        f.format(Locale.FRANCE,
                 formatSpecifier,
                 lowerCaseFranceTriple(i)(input).asInstanceOf[Object])
        assertEquals(lowerCaseFranceTriple(i)(output), f.toString())
      }

      locally {
        val f = new Formatter(Locale.GERMAN)
        f.format(Locale.JAPAN,
                 formatSpecifier,
                 lowerCaseJapanTriple(i)(input).asInstanceOf[Object])
        assertEquals(lowerCaseJapanTriple(i)(output), f.toString())
      }

      // test '%T'
      locally {
        val f = new Formatter(Locale.GERMAN)
        f.format(formatSpecifierUpper,
                 lowerCaseGermanTriple(i)(input).asInstanceOf[Object])
        assertEquals(lowerCaseGermanTriple(i)(output)
                       .asInstanceOf[String]
                       .toUpperCase(Locale.US),
                     f.toString())
      }

      locally {
        val f = new Formatter(Locale.GERMAN)
        f.format(Locale.FRANCE,
                 formatSpecifierUpper,
                 lowerCaseFranceTriple(i)(input).asInstanceOf[Object])
        assertEquals(lowerCaseFranceTriple(i)(output)
                       .asInstanceOf[String]
                       .toUpperCase(Locale.US),
                     f.toString())
      }

      locally {
        val f = new Formatter(Locale.GERMAN)
        f.format(Locale.JAPAN,
                 formatSpecifierUpper,
                 lowerCaseJapanTriple(i)(input).asInstanceOf[Object])
        assertEquals(lowerCaseJapanTriple(i)(output)
                       .asInstanceOf[String]
                       .toUpperCase(Locale.US),
                     f.toString())
      }
    }

    val upperCaseGermanTriple = Array(
      Array(0L, 'A', "Donnerstag"),
      Array(java.lang.Long.MAX_VALUE, 'A', "Sonntag"),
      Array(-1000L, 'A', "Donnerstag"),
      Array(new Date(1147327147578L), 'A', "Donnerstag"),
      Array(paris, 'A', "Montag"),
      Array(china, 'A', "Montag"),
      Array(0L, 'B', "Januar"),
      Array(java.lang.Long.MAX_VALUE, 'B', "August"),
      Array(-1000L, 'B', "Januar"),
      Array(new Date(1147327147578L), 'B', "Mai"),
      Array(paris, 'B', "Mai"),
      Array(china, 'B', "Mai"),
      Array(0L, 'C', "19"),
      Array(java.lang.Long.MAX_VALUE, 'C', "2922789"),
      Array(-1000L, 'C', "19"),
      Array(new Date(1147327147578L), 'C', "20"),
      Array(paris, 'C', "20"),
      Array(china, 'C', "20"),
      Array(0L, 'D', "01/01/70"),
      Array(java.lang.Long.MAX_VALUE, 'D', "08/17/94"),
      Array(-1000L, 'D', "01/01/70"),
      Array(new Date(1147327147578L), 'D', "05/11/06"),
      Array(paris, 'D', "05/08/06"),
      Array(china, 'D', "05/08/06"),
      Array(0L, 'F', "1970-01-01"),
      Array(java.lang.Long.MAX_VALUE, 'F', "292278994-08-17"),
      Array(-1000L, 'F', "1970-01-01"),
      Array(new Date(1147327147578L), 'F', "2006-05-11"),
      Array(paris, 'F', "2006-05-08"),
      Array(china, 'F', "2006-05-08"),
      Array(0L, 'H', "08"),
      Array(java.lang.Long.MAX_VALUE, 'H', "15"),
      Array(-1000L, 'H', "07"),
      Array(new Date(1147327147578L), 'H', "13"),
      Array(paris, 'H', "12"),
      Array(china, 'H', "12"),
      Array(0L, 'I', "08"),
      Array(java.lang.Long.MAX_VALUE, 'I', "03"),
      Array(-1000L, 'I', "07"),
      Array(new Date(1147327147578L), 'I', "01"),
      Array(paris, 'I', "12"),
      Array(china, 'I', "12"),
      Array(0L, 'L', "000"),
      Array(java.lang.Long.MAX_VALUE, 'L', "807"),
      Array(-1000L, 'L', "000"),
      Array(new Date(1147327147578L), 'L', "578"),
      Array(paris, 'L', "453"),
      Array(china, 'L', "609"),
      Array(0L, 'M', "00"),
      Array(java.lang.Long.MAX_VALUE, 'M', "18"),
      Array(-1000L, 'M', "59"),
      Array(new Date(1147327147578L), 'M', "59"),
      Array(paris, 'M', "00"),
      Array(china, 'M', "00"),
      Array(0L, 'N', "000000000"),
      Array(java.lang.Long.MAX_VALUE, 'N', "807000000"),
      Array(-1000L, 'N', "000000000"),
      Array(new Date(1147327147578L), 'N', "578000000"),
      Array(paris, 'N', "609000000"),
      Array(china, 'N', "609000000"),
      Array(0L, 'Q', "0"),
      Array(java.lang.Long.MAX_VALUE, 'Q', "9223372036854775807"),
      Array(-1000L, 'Q', "-1000"),
      Array(new Date(1147327147578L), 'Q', "1147327147578"),
      Array(paris, 'Q', "1147082400453"),
      Array(china, 'Q', "1147118400609"),
      Array(0L, 'R', "08:00"),
      Array(java.lang.Long.MAX_VALUE, 'R', "15:18"),
      Array(-1000L, 'R', "07:59"),
      Array(new Date(1147327147578L), 'R', "13:59"),
      Array(paris, 'R', "12:00"),
      Array(china, 'R', "12:00"),
      Array(0L, 'S', "00"),
      Array(java.lang.Long.MAX_VALUE, 'S', "47"),
      Array(-1000L, 'S', "59"),
      Array(new Date(1147327147578L), 'S', "07"),
      Array(paris, 'S', "00"),
      Array(china, 'S', "00"),
      Array(0L, 'T', "08:00:00"),
      Array(java.lang.Long.MAX_VALUE, 'T', "15:18:47"),
      Array(-1000L, 'T', "07:59:59"),
      Array(new Date(1147327147578L), 'T', "13:59:07"),
      Array(paris, 'T', "12:00:00"),
      Array(china, 'T', "12:00:00"),
      Array(0L, 'Y', "1970"),
      Array(java.lang.Long.MAX_VALUE, 'Y', "292278994"),
      Array(-1000L, 'Y', "1970"),
      Array(new Date(1147327147578L), 'Y', "2006"),
      Array(paris, 'Y', "2006"),
      Array(china, 'Y', "2006"),
      Array(0L, 'Z', "CST"),
      Array(java.lang.Long.MAX_VALUE, 'Z', "CST"),
      Array(-1000L, 'Z', "CST"),
      Array(new Date(1147327147578L), 'Z', "CST"),
      Array(paris, 'Z', "CEST"),
      Array(china, 'Z', "GMT-08:00")
    )

    val upperCaseFranceTriple = Array(
      Array(0L, 'A', "jeudi"),
      Array(java.lang.Long.MAX_VALUE, 'A', "dimanche"),
      Array(-1000L, 'A', "jeudi"),
      Array(new Date(1147327147578L), 'A', "jeudi"),
      Array(paris, 'A', "lundi"),
      Array(china, 'A', "lundi"),
      Array(0L, 'B', "janvier"),
      Array(java.lang.Long.MAX_VALUE, 'B', "ao\u00fbt"),
      Array(-1000L, 'B', "janvier"),
      Array(new Date(1147327147578L), 'B', "mai"),
      Array(paris, 'B', "mai"),
      Array(china, 'B', "mai"),
      Array(0L, 'C', "19"),
      Array(java.lang.Long.MAX_VALUE, 'C', "2922789"),
      Array(-1000L, 'C', "19"),
      Array(new Date(1147327147578L), 'C', "20"),
      Array(paris, 'C', "20"),
      Array(china, 'C', "20"),
      Array(0L, 'D', "01/01/70"),
      Array(java.lang.Long.MAX_VALUE, 'D', "08/17/94"),
      Array(-1000L, 'D', "01/01/70"),
      Array(new Date(1147327147578L), 'D', "05/11/06"),
      Array(paris, 'D', "05/08/06"),
      Array(china, 'D', "05/08/06"),
      Array(0L, 'F', "1970-01-01"),
      Array(java.lang.Long.MAX_VALUE, 'F', "292278994-08-17"),
      Array(-1000L, 'F', "1970-01-01"),
      Array(new Date(1147327147578L), 'F', "2006-05-11"),
      Array(paris, 'F', "2006-05-08"),
      Array(china, 'F', "2006-05-08"),
      Array(0L, 'H', "08"),
      Array(java.lang.Long.MAX_VALUE, 'H', "15"),
      Array(-1000L, 'H', "07"),
      Array(new Date(1147327147578L), 'H', "13"),
      Array(paris, 'H', "12"),
      Array(china, 'H', "12"),
      Array(0L, 'I', "08"),
      Array(java.lang.Long.MAX_VALUE, 'I', "03"),
      Array(-1000L, 'I', "07"),
      Array(new Date(1147327147578L), 'I', "01"),
      Array(paris, 'I', "12"),
      Array(china, 'I', "12"),
      Array(0L, 'L', "000"),
      Array(java.lang.Long.MAX_VALUE, 'L', "807"),
      Array(-1000L, 'L', "000"),
      Array(new Date(1147327147578L), 'L', "578"),
      Array(paris, 'L', "453"),
      Array(china, 'L', "609"),
      Array(0L, 'M', "00"),
      Array(java.lang.Long.MAX_VALUE, 'M', "18"),
      Array(-1000L, 'M', "59"),
      Array(new Date(1147327147578L), 'M', "59"),
      Array(paris, 'M', "00"),
      Array(china, 'M', "00"),
      Array(0L, 'N', "000000000"),
      Array(java.lang.Long.MAX_VALUE, 'N', "807000000"),
      Array(-1000L, 'N', "000000000"),
      Array(new Date(1147327147578L), 'N', "578000000"),
      Array(paris, 'N', "453000000"),
      Array(china, 'N', "468000000"),
      Array(0L, 'Q', "0"),
      Array(java.lang.Long.MAX_VALUE, 'Q', "9223372036854775807"),
      Array(-1000L, 'Q', "-1000"),
      Array(new Date(1147327147578L), 'Q', "1147327147578"),
      Array(paris, 'Q', "1147082400453"),
      Array(china, 'Q', "1147118400609"),
      Array(0L, 'R', "08:00"),
      Array(java.lang.Long.MAX_VALUE, 'R', "15:18"),
      Array(-1000L, 'R', "07:59"),
      Array(new Date(1147327147578L), 'R', "13:59"),
      Array(paris, 'R', "12:00"),
      Array(china, 'R', "12:00"),
      Array(0L, 'S', "00"),
      Array(java.lang.Long.MAX_VALUE, 'S', "47"),
      Array(-1000L, 'S', "59"),
      Array(new Date(1147327147578L), 'S', "07"),
      Array(paris, 'S', "00"),
      Array(china, 'S', "00"),
      Array(0L, 'T', "08:00:00"),
      Array(java.lang.Long.MAX_VALUE, 'T', "15:18:47"),
      Array(-1000L, 'T', "07:59:59"),
      Array(new Date(1147327147578L), 'T', "13:59:07"),
      Array(paris, 'T', "12:00:00"),
      Array(china, 'T', "12:00:00"),
      Array(0L, 'Y', "1970"),
      Array(java.lang.Long.MAX_VALUE, 'Y', "292278994"),
      Array(-1000L, 'Y', "1970"),
      Array(new Date(1147327147578L), 'Y', "2006"),
      Array(paris, 'Y', "2006"),
      Array(china, 'Y', "2006"),
      Array(0L, 'Z', "CST"),
      Array(java.lang.Long.MAX_VALUE, 'Z', "CST"),
      Array(-1000L, 'Z', "CST"),
      Array(new Date(1147327147578L), 'Z', "CST"),
      Array(paris, 'Z', "CEST"),
      Array(china, 'Z', "GMT-08:00")
    )

    val upperCaseJapanTriple = Array(
      Array(0L, 'A', "\u6728\u66dc\u65e5"),
      Array(java.lang.Long.MAX_VALUE, 'A', "\u65e5\u66dc\u65e5"),
      Array(-1000L, 'A', "\u6728\u66dc\u65e5"),
      Array(new Date(1147327147578L), 'A', "\u6728\u66dc\u65e5"),
      Array(paris, 'A', "\u6708\u66dc\u65e5"),
      Array(china, 'A', "\u6708\u66dc\u65e5"),
      Array(0L, 'B', "1\u6708"),
      Array(java.lang.Long.MAX_VALUE, 'B', "8\u6708"),
      Array(-1000L, 'B', "1\u6708"),
      Array(new Date(1147327147578L), 'B', "5\u6708"),
      Array(paris, 'B', "5\u6708"),
      Array(china, 'B', "5\u6708"),
      Array(0L, 'C', "19"),
      Array(java.lang.Long.MAX_VALUE, 'C', "2922789"),
      Array(-1000L, 'C', "19"),
      Array(new Date(1147327147578L), 'C', "20"),
      Array(paris, 'C', "20"),
      Array(china, 'C', "20"),
      Array(0L, 'D', "01/01/70"),
      Array(java.lang.Long.MAX_VALUE, 'D', "08/17/94"),
      Array(-1000L, 'D', "01/01/70"),
      Array(new Date(1147327147578L), 'D', "05/11/06"),
      Array(paris, 'D', "05/08/06"),
      Array(china, 'D', "05/08/06"),
      Array(0L, 'F', "1970-01-01"),
      Array(java.lang.Long.MAX_VALUE, 'F', "292278994-08-17"),
      Array(-1000L, 'F', "1970-01-01"),
      Array(new Date(1147327147578L), 'F', "2006-05-11"),
      Array(paris, 'F', "2006-05-08"),
      Array(china, 'F', "2006-05-08"),
      Array(0L, 'H', "08"),
      Array(java.lang.Long.MAX_VALUE, 'H', "15"),
      Array(-1000L, 'H', "07"),
      Array(new Date(1147327147578L), 'H', "13"),
      Array(paris, 'H', "12"),
      Array(china, 'H', "12"),
      Array(0L, 'I', "08"),
      Array(java.lang.Long.MAX_VALUE, 'I', "03"),
      Array(-1000L, 'I', "07"),
      Array(new Date(1147327147578L), 'I', "01"),
      Array(paris, 'I', "12"),
      Array(china, 'I', "12"),
      Array(0L, 'L', "000"),
      Array(java.lang.Long.MAX_VALUE, 'L', "807"),
      Array(-1000L, 'L', "000"),
      Array(new Date(1147327147578L), 'L', "578"),
      Array(paris, 'L', "453"),
      Array(china, 'L', "609"),
      Array(0L, 'M', "00"),
      Array(java.lang.Long.MAX_VALUE, 'M', "18"),
      Array(-1000L, 'M', "59"),
      Array(new Date(1147327147578L), 'M', "59"),
      Array(paris, 'M', "00"),
      Array(china, 'M', "00"),
      Array(0L, 'N', "000000000"),
      Array(java.lang.Long.MAX_VALUE, 'N', "807000000"),
      Array(-1000L, 'N', "000000000"),
      Array(new Date(1147327147578L), 'N', "578000000"),
      Array(paris, 'N', "453000000"),
      Array(china, 'N', "468000000"),
      Array(0L, 'Q', "0"),
      Array(java.lang.Long.MAX_VALUE, 'Q', "9223372036854775807"),
      Array(-1000L, 'Q', "-1000"),
      Array(new Date(1147327147578L), 'Q', "1147327147578"),
      Array(paris, 'Q', "1147082400453"),
      Array(china, 'Q', "1147118400609"),
      Array(0L, 'R', "08:00"),
      Array(java.lang.Long.MAX_VALUE, 'R', "15:18"),
      Array(-1000L, 'R', "07:59"),
      Array(new Date(1147327147578L), 'R', "13:59"),
      Array(paris, 'R', "12:00"),
      Array(china, 'R', "12:00"),
      Array(0L, 'S', "00"),
      Array(java.lang.Long.MAX_VALUE, 'S', "47"),
      Array(-1000L, 'S', "59"),
      Array(new Date(1147327147578L), 'S', "07"),
      Array(paris, 'S', "00"),
      Array(china, 'S', "00"),
      Array(0L, 'T', "08:00:00"),
      Array(java.lang.Long.MAX_VALUE, 'T', "15:18:47"),
      Array(-1000L, 'T', "07:59:59"),
      Array(new Date(1147327147578L), 'T', "13:59:07"),
      Array(paris, 'T', "12:00:00"),
      Array(china, 'T', "12:00:00"),
      Array(0L, 'Y', "1970"),
      Array(java.lang.Long.MAX_VALUE, 'Y', "292278994"),
      Array(-1000L, 'Y', "1970"),
      Array(new Date(1147327147578L), 'Y', "2006"),
      Array(paris, 'Y', "2006"),
      Array(china, 'Y', "2006"),
      Array(0L, 'Z', "CST"),
      Array(java.lang.Long.MAX_VALUE, 'Z', "CST"),
      Array(-1000L, 'Z', "CST"),
      Array(new Date(1147327147578L), 'Z', "CST"),
      Array(paris, 'Z', "CEST"),
      Array(china, 'Z', "GMT-08:00")
    )

    for (i <- 0 until 90) {
      val formatSpecifier      = "%t" + upperCaseGermanTriple(i)(pattern)
      val formatSpecifierUpper = "%T" + upperCaseGermanTriple(i)(pattern)
      if (upperCaseGermanTriple(i)(pattern).asInstanceOf[Char] == 'N') {
        // result can't be predicted on RI, so skip this test
        // continue
      } else {
        // test '%t'
        locally {
          val f = new Formatter(Locale.JAPAN)
          f.format(formatSpecifier,
                   upperCaseJapanTriple(i)(input).asInstanceOf[Object])
          assertEquals(upperCaseJapanTriple(i)(output), f.toString())
        }

        locally {
          val f = new Formatter(Locale.JAPAN)
          f.format(Locale.GERMAN,
                   formatSpecifier,
                   upperCaseGermanTriple(i)(input).asInstanceOf[Object])
          assertEquals(upperCaseGermanTriple(i)(output), f.toString())
        }

        locally {
          val f = new Formatter(Locale.JAPAN)
          f.format(Locale.FRANCE,
                   formatSpecifier,
                   upperCaseFranceTriple(i)(input).asInstanceOf[Object])
          assertEquals(upperCaseFranceTriple(i)(output), f.toString())
        }

        // test '%T'
        locally {
          val f = new Formatter(Locale.GERMAN)
          f.format(formatSpecifierUpper,
                   upperCaseGermanTriple(i)(input).asInstanceOf[Object])
          assertEquals(upperCaseGermanTriple(i)(output)
                         .asInstanceOf[String]
                         .toUpperCase(Locale.US),
                       f.toString())
        }

        locally {
          val f = new Formatter(Locale.GERMAN)
          f.format(Locale.JAPAN,
                   formatSpecifierUpper,
                   upperCaseJapanTriple(i)(input).asInstanceOf[Object])
          assertEquals(upperCaseJapanTriple(i)(output)
                         .asInstanceOf[String]
                         .toUpperCase(Locale.US),
                       f.toString())
        }

        locally {
          val f = new Formatter(Locale.GERMAN)
          f.format(Locale.FRANCE,
                   formatSpecifierUpper,
                   upperCaseFranceTriple(i)(input).asInstanceOf[Object])
          assertEquals(upperCaseFranceTriple(i)(output)
                         .asInstanceOf[String]
                         .toUpperCase(Locale.US),
                       f.toString())
        }
      }
    }

    locally {
      val f = new Formatter(Locale.US)
      f.format("%-10ta", now)
      assertEquals("Thu       ", f.toString())
    }

    locally {
      val f = new Formatter(Locale.US)
      f.format("%10000000000000000000000000000000001ta", now)
      assertEquals("Thu", f.toString().trim())
    }
  }

  test(
    "format(String, Array[Object]) for null argument for Byte/Short/Integer/Long/BigInteger conversion") {
    locally {
      val f = new Formatter(Locale.FRANCE)
      f.format("%d%<o%<x%<5X", null.asInstanceOf[java.lang.Integer])
      assertEquals("nullnullnull NULL", f.toString())
    }

    locally {
      val f = new Formatter(Locale.GERMAN)
      f.format("%d%<#03o %<0#4x%<6X", null.asInstanceOf[java.lang.Long])
      assertEquals("nullnull null  NULL", f.toString())
    }

    locally {
      val f = new Formatter(Locale.GERMAN)
      f.format("%(+,07d%<o %<x%<6X", null.asInstanceOf[java.lang.Byte])
      assertEquals("   nullnull null  NULL", f.toString())
    }

    locally {
      val f = new Formatter(Locale.ITALY)
      f.format("%(+,07d%<o %<x%<0#6X", null.asInstanceOf[java.lang.Short])
      assertEquals("   nullnull null  NULL", f.toString())
    }

    locally {
      val f = new Formatter(Locale.GERMAN)
      f.format("%(+,-7d%<( o%<+(x %<( 06X", null.asInstanceOf[BigInteger])
      assertEquals("null   nullnull   NULL", f.toString())
    }
  }

  testFails(
    "format(String, Array[Object]) for legal BigInteger conversion type 'd'",
    0) { // issue not filed yet
    val tripleD = Array(
      Array(new BigInteger("123456789012345678901234567890"),
            "%d",
            "123456789012345678901234567890"),
      Array(new BigInteger("123456789012345678901234567890"),
            "%10d",
            "123456789012345678901234567890"),
      Array(new BigInteger("123456789012345678901234567890"),
            "%-1d",
            "123456789012345678901234567890"),
      Array(new BigInteger("123456789012345678901234567890"),
            "%+d",
            "+123456789012345678901234567890"),
      Array(new BigInteger("123456789012345678901234567890"),
            "% d",
            " 123456789012345678901234567890"),
      Array(new BigInteger("123456789012345678901234567890"),
            "%,d",
            "123.456.789.012.345.678.901.234.567.890"),
      Array(new BigInteger("123456789012345678901234567890"),
            "%(d",
            "123456789012345678901234567890"),
      Array(new BigInteger("123456789012345678901234567890"),
            "%08d",
            "123456789012345678901234567890"),
      Array(new BigInteger("123456789012345678901234567890"),
            "%-+,(11d",
            "+123.456.789.012.345.678.901.234.567.890"),
      Array(new BigInteger("123456789012345678901234567890"),
            "%0 ,(11d",
            " 123.456.789.012.345.678.901.234.567.890"),
      Array(new BigInteger("-9876543210987654321098765432100000"),
            "%d",
            "-9876543210987654321098765432100000"),
      Array(new BigInteger("-9876543210987654321098765432100000"),
            "%10d",
            "-9876543210987654321098765432100000"),
      Array(new BigInteger("-9876543210987654321098765432100000"),
            "%-1d",
            "-9876543210987654321098765432100000"),
      Array(new BigInteger("-9876543210987654321098765432100000"),
            "%+d",
            "-9876543210987654321098765432100000"),
      Array(new BigInteger("-9876543210987654321098765432100000"),
            "% d",
            "-9876543210987654321098765432100000"),
      Array(new BigInteger("-9876543210987654321098765432100000"),
            "%,d",
            "-9.876.543.210.987.654.321.098.765.432.100.000"),
      Array(new BigInteger("-9876543210987654321098765432100000"),
            "%(d",
            "(9876543210987654321098765432100000)"),
      Array(new BigInteger("-9876543210987654321098765432100000"),
            "%08d",
            "-9876543210987654321098765432100000"),
      Array(new BigInteger("-9876543210987654321098765432100000"),
            "%-+,(11d",
            "(9.876.543.210.987.654.321.098.765.432.100.000)"),
      Array(new BigInteger("-9876543210987654321098765432100000"),
            "%0 ,(11d",
            "(9.876.543.210.987.654.321.098.765.432.100.000)")
    )

    val input   = 0
    val pattern = 1
    val output  = 2
    for (i <- 0 until tripleD.length) {
      val f = new Formatter(Locale.GERMAN)
      f.format(tripleD(i)(pattern).asInstanceOf[String], tripleD(i)(input))
      assertEquals(tripleD(i)(output), f.toString())
    }

    val tripleO = Array(
      Array(new BigInteger("123456789012345678901234567890"),
            "%o",
            "143564417755415637016711617605322"),
      Array(new BigInteger("123456789012345678901234567890"),
            "%-6o",
            "143564417755415637016711617605322"),
      Array(new BigInteger("123456789012345678901234567890"),
            "%08o",
            "143564417755415637016711617605322"),
      Array(new BigInteger("123456789012345678901234567890"),
            "%#o",
            "0143564417755415637016711617605322"),
      Array(new BigInteger("123456789012345678901234567890"),
            "%0#11o",
            "0143564417755415637016711617605322"),
      Array(new BigInteger("123456789012345678901234567890"),
            "%-#9o",
            "0143564417755415637016711617605322"),
      Array(new BigInteger("-9876543210987654321098765432100000"),
            "%o",
            "-36336340043453651353467270113157312240"),
      Array(new BigInteger("-9876543210987654321098765432100000"),
            "%-6o",
            "-36336340043453651353467270113157312240"),
      Array(new BigInteger("-9876543210987654321098765432100000"),
            "%08o",
            "-36336340043453651353467270113157312240"),
      Array(new BigInteger("-9876543210987654321098765432100000"),
            "%#o",
            "-036336340043453651353467270113157312240"),
      Array(new BigInteger("-9876543210987654321098765432100000"),
            "%0#11o",
            "-036336340043453651353467270113157312240"),
      Array(new BigInteger("-9876543210987654321098765432100000"),
            "%-#9o",
            "-036336340043453651353467270113157312240")
    )

    for (i <- 0 until tripleO.length) {
      val f = new Formatter(Locale.ITALY)
      f.format(tripleO(i)(pattern).asInstanceOf[String], tripleO(i)(input))
      assertEquals(tripleO(i)(output), f.toString())
    }

    val tripleX = Array(
      Array(new BigInteger("123456789012345678901234567890"),
            "%x",
            "18ee90ff6c373e0ee4e3f0ad2"),
      Array(new BigInteger("123456789012345678901234567890"),
            "%-8x",
            "18ee90ff6c373e0ee4e3f0ad2"),
      Array(new BigInteger("123456789012345678901234567890"),
            "%06x",
            "18ee90ff6c373e0ee4e3f0ad2"),
      Array(new BigInteger("123456789012345678901234567890"),
            "%#x",
            "0x18ee90ff6c373e0ee4e3f0ad2"),
      Array(new BigInteger("123456789012345678901234567890"),
            "%0#12x",
            "0x18ee90ff6c373e0ee4e3f0ad2"),
      Array(new BigInteger("123456789012345678901234567890"),
            "%-#9x",
            "0x18ee90ff6c373e0ee4e3f0ad2"),
      Array(new BigInteger("-9876543210987654321098765432100000"),
            "%x",
            "-1e6f380472bd4bae6eb8259bd94a0"),
      Array(new BigInteger("-9876543210987654321098765432100000"),
            "%-8x",
            "-1e6f380472bd4bae6eb8259bd94a0"),
      Array(new BigInteger("-9876543210987654321098765432100000"),
            "%06x",
            "-1e6f380472bd4bae6eb8259bd94a0"),
      Array(new BigInteger("-9876543210987654321098765432100000"),
            "%#x",
            "-0x1e6f380472bd4bae6eb8259bd94a0"),
      Array(new BigInteger("-9876543210987654321098765432100000"),
            "%0#12x",
            "-0x1e6f380472bd4bae6eb8259bd94a0"),
      Array(new BigInteger("-9876543210987654321098765432100000"),
            "%-#9x",
            "-0x1e6f380472bd4bae6eb8259bd94a0")
    )

    for (i <- 0 until tripleX.length) {
      val f = new Formatter(Locale.FRANCE)
      f.format(tripleX(i)(pattern).asInstanceOf[String], tripleX(i)(input))
      assertEquals(tripleX(i)(output), f.toString())
    }

    locally {
      val f = new Formatter(Locale.GERMAN)
      f.format("%(+,-7d%<( o%<+(x %<( 06X", null.asInstanceOf[BigInteger])
      assertEquals("null   nullnull   NULL", f.toString())
    }
  }

  testFails(
    "format(String, Array[Object]) for padding of BigInteger conversion",
    0) { // issue not filed yet
    val bigInt = new BigInteger("123456789012345678901234567890")
    locally {
      val f = new Formatter(Locale.GERMAN)
      f.format("%32d", bigInt)
      assertEquals("  123456789012345678901234567890", f.toString())
    }

    locally {
      val f = new Formatter(Locale.GERMAN)
      f.format("%+32x", bigInt)
      assertEquals("      +18ee90ff6c373e0ee4e3f0ad2", f.toString())
    }

    locally {
      val f = new Formatter(Locale.GERMAN)
      f.format("% 32o", bigInt)
      assertEquals(" 143564417755415637016711617605322", f.toString())
    }

    val negBigInt = new BigInteger("-1234567890123456789012345678901234567890")
    locally {
      val f = new Formatter(Locale.GERMAN)
      f.format("%( 040X", negBigInt)
      assertEquals("(000003A0C92075C0DBF3B8ACBC5F96CE3F0AD2)", f.toString())
    }

    locally {
      val f = new Formatter(Locale.GERMAN)
      f.format("%+(045d", negBigInt)
      assertEquals("(0001234567890123456789012345678901234567890)",
                   f.toString())
    }

    locally {
      val f = new Formatter(Locale.GERMAN)
      f.format("%+,-(60d", negBigInt)
      assertEquals(
        "(1.234.567.890.123.456.789.012.345.678.901.234.567.890)     ",
        f.toString())
    }
  }

  test("format(String, Array[Object]) for BigInteger conversion exception") {
    val flagsConversionMismatches = Array("%#d", "%,o", "%,x", "%,X")
    for (i <- 0 until flagsConversionMismatches.length) {
      val f = new Formatter(Locale.CHINA)
      assertThrows[FormatFlagsConversionMismatchException](
        f.format(flagsConversionMismatches(i), new BigInteger("1")))
    }

    val missingFormatWidths = Array("%-0d",
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
                                    "%-X")
    for (i <- 0 until missingFormatWidths.length) {
      val f = new Formatter(Locale.KOREA)
      assertThrows[MissingFormatWidthException](
        f.format(missingFormatWidths(i), new BigInteger("1")))
    }

    val illFlags =
      Array("%+ d", "%-08d", "%+ o", "%-08o", "%+ x", "%-08x", "%+ X", "%-08X")
    for (i <- 0 until illFlags.length) {
      val f = new Formatter(Locale.CANADA)
      assertThrows[IllegalFormatFlagsException](
        f.format(illFlags(i), new BigInteger("1")))
    }

    val precisionExceptions = Array("%.4d", "%2.5o", "%8.6x", "%11.17X")
    for (i <- 0 until precisionExceptions.length) {
      val f = new Formatter(Locale.US)
      assertThrows[IllegalFormatPrecisionException](
        f.format(precisionExceptions(i), new BigInteger("1")))
    }

    locally {
      val f = new Formatter(Locale.US)
      assertThrows[UnknownFormatConversionException](
        f.format("%D", new BigInteger("1")))
    }

    locally {
      val f = new Formatter(Locale.US)
      assertThrows[UnknownFormatConversionException](
        f.format("%O", new BigInteger("1")))
    }

    locally {
      val f = new Formatter()
      assertThrows[MissingFormatWidthException](
        f.format("%010000000000000000000000000000000001d",
                 new BigInteger("1")))
    }
  }

  test("format(String, Array[Object]) for BigInteger exception throwing order") {
    val big = new BigInteger("100")

    /*
     * Order summary: UnknownFormatConversionException >
     * MissingFormatWidthException > IllegalFormatFlagsException >
     * IllegalFormatPrecisionException > IllegalFormatConversionException >
     * FormatFlagsConversionMismatchException
     *
     */
    val f = new Formatter(Locale.US)
    // compare IllegalFormatConversionException and
    // FormatFlagsConversionMismatchException
    assertThrows[IllegalFormatConversionException](
      f.format("%(o", false.asInstanceOf[Object]))

    // compare IllegalFormatPrecisionException and
    // IllegalFormatConversionException
    assertThrows[IllegalFormatPrecisionException](
      f.format("%.4o", false.asInstanceOf[Object]))

    // compare IllegalFormatFlagsException and
    // IllegalFormatPrecisionException
    assertThrows[IllegalFormatFlagsException](f.format("%+ .4o", big))

    // compare MissingFormatWidthException and
    // IllegalFormatFlagsException
    assertThrows[MissingFormatWidthException](f.format("%+ -o", big))

    // compare UnknownFormatConversionException and
    // MissingFormatWidthException
    assertThrows[UnknownFormatConversionException](f.format("%-O", big))
  }

  testFails(
    "format(String, Array[Object]) for Float/Double conversion type 'e' and 'E'",
    0) { // issue not filed yet
    val tripleE = Array(
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

    val input   = 0
    val pattern = 1
    val output  = 2
    for (i <- 0 until tripleE.length) {
      locally {
        val f = new Formatter(Locale.US)
        f.format(tripleE(i)(pattern).asInstanceOf[String],
                 tripleE(i)(input).asInstanceOf[Object])
        assertEquals(tripleE(i)(output), f.toString())
      }

      // test for conversion type 'E'
      locally {
        val f = new Formatter(Locale.US)
        f.format(tripleE(i)(pattern).asInstanceOf[String].toUpperCase(),
                 tripleE(i)(input).asInstanceOf[Object])
        assertEquals(
          tripleE(i)(output).asInstanceOf[String].toUpperCase(Locale.UK),
          f.toString())
      }
    }

    val f = new Formatter(Locale.GERMAN)
    f.format("%e", 1001f.asInstanceOf[Object])
    /*
     * fail on RI, spec says 'e' requires the output to be formatted in
     * general scientific notation and the localization algorithm is
     * applied. But RI format this case to 1.001000e+03, which does not
     * conform to the German Locale
     */
    assertEquals("1,001000e+03", f.toString())
  }

  testFails(
    "format(String, Array[Object]) for Float/Double conversion type 'g' and 'G'",
    0) { // issue not filed yet
    val tripleG = Array(
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

    val input   = 0
    val pattern = 1
    val output  = 2
    for (i <- 0 until tripleG.length) {
      locally {
        val f = new Formatter(Locale.US)
        f.format(tripleG(i)(pattern).asInstanceOf[String],
                 tripleG(i)(input).asInstanceOf[Object])
        assertEquals(tripleG(i)(output), f.toString())
      }

      // test for conversion type 'G'
      locally {
        val f = new Formatter(Locale.US)
        f.format(tripleG(i)(pattern).asInstanceOf[String].toUpperCase(),
                 tripleG(i)(input).asInstanceOf[Object])
        assertEquals(
          tripleG(i)(output).asInstanceOf[String].toUpperCase(Locale.UK),
          f.toString())
      }
    }

    locally {
      val f = new Formatter(Locale.US)
      f.format("%.5g", 0f.asInstanceOf[Object])
      assertEquals("0.0000", f.toString())
    }

    locally {
      val f = new Formatter(Locale.US)
      f.format("%.0g", 0f.asInstanceOf[Object])
      /*
       * fail on RI, spec says if the precision is 0, then it is taken to be
       * 1. but RI throws ArrayIndexOutOfBoundsException.
       */
      assertEquals("0", f.toString())
    }

    locally {
      val f = new Formatter(Locale.GERMAN)
      f.format("%g", 1001f.asInstanceOf[Object])
      /*
       * fail on RI, spec says 'g' requires the output to be formatted in
       * general scientific notation and the localization algorithm is
       * applied. But RI format this case to 1001.00, which does not conform
       * to the German Locale
       */
      assertEquals("1001,00", f.toString())
    }
  }

  test(
    "format(String, Array[Object]) for Float/Double conversion type 'g' and 'G' overflow") {
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

  testFails(
    "format(String, Array[Object]) for Float/Double conversion type 'f'",
    0) { // issue not filed yet
    val tripleF: Array[Array[Any]] = Array(
      Array(0f, "%f", "0,000000"),
      Array(0f, "%#.3f", "0,000"),
      Array(0f, "%,5f", "0,000000"),
      Array(0f, "%- (12.0f", " 0          "),
      Array(0f, "%#+0(1.6f", "+0,000000"),
      Array(0f, "%-+(8.4f", "+0,0000 "),
      Array(0f, "% 0#(9.8f", " 0,00000000"),
      Array(1234f, "%f", "1234,000000"),
      Array(1234f, "%#.3f", "1234,000"),
      Array(1234f, "%,5f", "1.234,000000"),
      Array(1234f, "%- (12.0f", " 1234       "),
      Array(1234f, "%#+0(1.6f", "+1234,000000"),
      Array(1234f, "%-+(8.4f", "+1234,0000"),
      Array(1234f, "% 0#(9.8f", " 1234,00000000"),
      Array(1.0f, "%f", "1,000000"),
      Array(1.0f, "%#.3f", "1,000"),
      Array(1.0f, "%,5f", "1,000000"),
      Array(1.0f, "%- (12.0f", " 1          "),
      Array(1.0f, "%#+0(1.6f", "+1,000000"),
      Array(1.0f, "%-+(8.4f", "+1,0000 "),
      Array(1.0f, "% 0#(9.8f", " 1,00000000"),
      Array(-98f, "%f", "-98,000000"),
      Array(-98f, "%#.3f", "-98,000"),
      Array(-98f, "%,5f", "-98,000000"),
      Array(-98f, "%- (12.0f", "(98)        "),
      Array(-98f, "%#+0(1.6f", "(98,000000)"),
      Array(-98f, "%-+(8.4f", "(98,0000)"),
      Array(-98f, "% 0#(9.8f", "(98,00000000)"),
      Array(0.000001f, "%f", "0,000001"),
      Array(0.000001f, "%#.3f", "0,000"),
      Array(0.000001f, "%,5f", "0,000001"),
      Array(0.000001f, "%- (12.0f", " 0          "),
      Array(0.000001f, "%#+0(1.6f", "+0,000001"),
      Array(0.000001f, "%-+(8.4f", "+0,0000 "),
      Array(0.000001f, "% 0#(9.8f", " 0,00000100"),
      Array(345.1234567f, "%f", "345,123444"),
      Array(345.1234567f, "%#.3f", "345,123"),
      Array(345.1234567f, "%,5f", "345,123444"),
      Array(345.1234567f, "%- (12.0f", " 345        "),
      Array(345.1234567f, "%#+0(1.6f", "+345,123444"),
      Array(345.1234567f, "%-+(8.4f", "+345,1234"),
      Array(345.1234567f, "% 0#(9.8f", " 345,12344360"),
      Array(-.00000012345f, "%f", "-0,000000"),
      Array(-.00000012345f, "%#.3f", "-0,000"),
      Array(-.00000012345f, "%,5f", "-0,000000"),
      Array(-.00000012345f, "%- (12.0f", "(0)         "),
      Array(-.00000012345f, "%#+0(1.6f", "(0,000000)"),
      Array(-.00000012345f, "%-+(8.4f", "(0,0000)"),
      Array(-.00000012345f, "% 0#(9.8f", "(0,00000012)"),
      Array(-987654321.1234567f, "%f", "-987654336,000000"),
      Array(-987654321.1234567f, "%#.3f", "-987654336,000"),
      Array(-987654321.1234567f, "%,5f", "-987.654.336,000000"),
      Array(-987654321.1234567f, "%- (12.0f", "(987654336) "),
      Array(-987654321.1234567f, "%#+0(1.6f", "(987654336,000000)"),
      Array(-987654321.1234567f, "%-+(8.4f", "(987654336,0000)"),
      Array(-987654321.1234567f, "% 0#(9.8f", "(987654336,00000000)"),
      Array(java.lang.Float.MAX_VALUE,
            "%f",
            "340282346638528860000000000000000000000,000000"),
      Array(java.lang.Float.MAX_VALUE,
            "%#.3f",
            "340282346638528860000000000000000000000,000"),
      Array(java.lang.Float.MAX_VALUE,
            "%,5f",
            "340.282.346.638.528.860.000.000.000.000.000.000.000,000000"),
      Array(java.lang.Float.MAX_VALUE,
            "%- (12.0f",
            " 340282346638528860000000000000000000000"),
      Array(java.lang.Float.MAX_VALUE,
            "%#+0(1.6f",
            "+340282346638528860000000000000000000000,000000"),
      Array(java.lang.Float.MAX_VALUE,
            "%-+(8.4f",
            "+340282346638528860000000000000000000000,0000"),
      Array(java.lang.Float.MAX_VALUE,
            "% 0#(9.8f",
            " 340282346638528860000000000000000000000,00000000"),
      Array(java.lang.Float.MIN_VALUE, "%f", "0,000000"),
      Array(java.lang.Float.MIN_VALUE, "%#.3f", "0,000"),
      Array(java.lang.Float.MIN_VALUE, "%,5f", "0,000000"),
      Array(java.lang.Float.MIN_VALUE, "%- (12.0f", " 0          "),
      Array(java.lang.Float.MIN_VALUE, "%#+0(1.6f", "+0,000000"),
      Array(java.lang.Float.MIN_VALUE, "%-+(8.4f", "+0,0000 "),
      Array(java.lang.Float.MIN_VALUE, "% 0#(9.8f", " 0,00000000"),
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
      Array(0d, "%f", "0,000000"),
      Array(0d, "%#.3f", "0,000"),
      Array(0d, "%,5f", "0,000000"),
      Array(0d, "%- (12.0f", " 0          "),
      Array(0d, "%#+0(1.6f", "+0,000000"),
      Array(0d, "%-+(8.4f", "+0,0000 "),
      Array(0d, "% 0#(9.8f", " 0,00000000"),
      Array(1d, "%f", "1,000000"),
      Array(1d, "%#.3f", "1,000"),
      Array(1d, "%,5f", "1,000000"),
      Array(1d, "%- (12.0f", " 1          "),
      Array(1d, "%#+0(1.6f", "+1,000000"),
      Array(1d, "%-+(8.4f", "+1,0000 "),
      Array(1d, "% 0#(9.8f", " 1,00000000"),
      Array(-1d, "%f", "-1,000000"),
      Array(-1d, "%#.3f", "-1,000"),
      Array(-1d, "%,5f", "-1,000000"),
      Array(-1d, "%- (12.0f", "(1)         "),
      Array(-1d, "%#+0(1.6f", "(1,000000)"),
      Array(-1d, "%-+(8.4f", "(1,0000)"),
      Array(-1d, "% 0#(9.8f", "(1,00000000)"),
      Array(.00000001d, "%f", "0,000000"),
      Array(.00000001d, "%#.3f", "0,000"),
      Array(.00000001d, "%,5f", "0,000000"),
      Array(.00000001d, "%- (12.0f", " 0          "),
      Array(.00000001d, "%#+0(1.6f", "+0,000000"),
      Array(.00000001d, "%-+(8.4f", "+0,0000 "),
      Array(.00000001d, "% 0#(9.8f", " 0,00000001"),
      Array(1000.10d, "%f", "1000,100000"),
      Array(1000.10d, "%#.3f", "1000,100"),
      Array(1000.10d, "%,5f", "1.000,100000"),
      Array(1000.10d, "%- (12.0f", " 1000       "),
      Array(1000.10d, "%#+0(1.6f", "+1000,100000"),
      Array(1000.10d, "%-+(8.4f", "+1000,1000"),
      Array(1000.10d, "% 0#(9.8f", " 1000,10000000"),
      Array(0.1d, "%f", "0,100000"),
      Array(0.1d, "%#.3f", "0,100"),
      Array(0.1d, "%,5f", "0,100000"),
      Array(0.1d, "%- (12.0f", " 0          "),
      Array(0.1d, "%#+0(1.6f", "+0,100000"),
      Array(0.1d, "%-+(8.4f", "+0,1000 "),
      Array(0.1d, "% 0#(9.8f", " 0,10000000"),
      Array(-2.0d, "%f", "-2,000000"),
      Array(-2.0d, "%#.3f", "-2,000"),
      Array(-2.0d, "%,5f", "-2,000000"),
      Array(-2.0d, "%- (12.0f", "(2)         "),
      Array(-2.0d, "%#+0(1.6f", "(2,000000)"),
      Array(-2.0d, "%-+(8.4f", "(2,0000)"),
      Array(-2.0d, "% 0#(9.8f", "(2,00000000)"),
      Array(-.00009d, "%f", "-0,000090"),
      Array(-.00009d, "%#.3f", "-0,000"),
      Array(-.00009d, "%,5f", "-0,000090"),
      Array(-.00009d, "%- (12.0f", "(0)         "),
      Array(-.00009d, "%#+0(1.6f", "(0,000090)"),
      Array(-.00009d, "%-+(8.4f", "(0,0001)"),
      Array(-.00009d, "% 0#(9.8f", "(0,00009000)"),
      Array(-1234567890.012345678d, "%f", "-1234567890,012346"),
      Array(-1234567890.012345678d, "%#.3f", "-1234567890,012"),
      Array(-1234567890.012345678d, "%,5f", "-1.234.567.890,012346"),
      Array(-1234567890.012345678d, "%- (12.0f", "(1234567890)"),
      Array(-1234567890.012345678d, "%#+0(1.6f", "(1234567890,012346)"),
      Array(-1234567890.012345678d, "%-+(8.4f", "(1234567890,0123)"),
      Array(-1234567890.012345678d, "% 0#(9.8f", "(1234567890,01234580)"),
      Array(
        java.lang.Double.MAX_VALUE,
        "%f",
        "179769313486231570000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000,000000"
      ),
      Array(
        java.lang.Double.MAX_VALUE,
        "%#.3f",
        "179769313486231570000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000,000"
      ),
      Array(
        java.lang.Double.MAX_VALUE,
        "%,5f",
        "179.769.313.486.231.570.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000,000000"
      ),
      Array(
        java.lang.Double.MAX_VALUE,
        "%- (12.0f",
        " 179769313486231570000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
      ),
      Array(
        java.lang.Double.MAX_VALUE,
        "%#+0(1.6f",
        "+179769313486231570000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000,000000"
      ),
      Array(
        java.lang.Double.MAX_VALUE,
        "%-+(8.4f",
        "+179769313486231570000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000,0000"
      ),
      Array(
        java.lang.Double.MAX_VALUE,
        "% 0#(9.8f",
        " 179769313486231570000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000,00000000"
      ),
      Array(java.lang.Double.MIN_VALUE, "%f", "0,000000"),
      Array(java.lang.Double.MIN_VALUE, "%#.3f", "0,000"),
      Array(java.lang.Double.MIN_VALUE, "%,5f", "0,000000"),
      Array(java.lang.Double.MIN_VALUE, "%- (12.0f", " 0          "),
      Array(java.lang.Double.MIN_VALUE, "%#+0(1.6f", "+0,000000"),
      Array(java.lang.Double.MIN_VALUE, "%-+(8.4f", "+0,0000 "),
      Array(java.lang.Double.MIN_VALUE, "% 0#(9.8f", " 0,00000000"),
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
    val input: Int   = 0
    val pattern: Int = 1
    val output: Int  = 2
    for (i <- 0 until tripleF.length) {
      val f = new Formatter(Locale.GERMAN)
      f.format(tripleF(i)(pattern).asInstanceOf[String],
               tripleF(i)(input).asInstanceOf[Object])
      assertEquals(tripleF(i)(output), f.toString)
    }
  }

  testFails(
    "format(String, Array[Object]) for Float/Double conversion type 'a' and 'A'",
    0) { // issue not filed yet
    val tripleA: Array[Array[Any]] = Array(
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
    val input: Int   = 0
    val pattern: Int = 1
    val output: Int  = 2
    for (i <- 0 until tripleA.length) {
      locally {
        val f = new Formatter(Locale.UK)
        f.format(tripleA(i)(pattern).asInstanceOf[String],
                 tripleA(i)(input).asInstanceOf[Object])
        assertEquals(tripleA(i)(output), f.toString)
      }
      // test for conversion type 'A'
      locally {
        val f = new Formatter(Locale.UK)
        f.format(tripleA(i)(pattern).asInstanceOf[String].toUpperCase(),
                 tripleA(i)(input).asInstanceOf[Object])
        assertEquals(
          tripleA(i)(output).asInstanceOf[String].toUpperCase(Locale.UK),
          f.toString)
      }
    }
  }

  test(
    "format(String, Array[Object]) for BigDecimal conversion type 'e' and 'E'") {
    val tripleE: Array[Array[Any]] = Array(
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
    val input: Int   = 0
    val pattern: Int = 1
    val output: Int  = 2
    for (i <- 0 until tripleE.length) {
      locally {
        val f = new Formatter(Locale.US)
        f.format(tripleE(i)(pattern).asInstanceOf[String],
                 tripleE(i)(input).asInstanceOf[Object])
        assertEquals(tripleE(i)(output), f.toString)
      }
      // test for conversion type 'E'
      locally {
        val f = new Formatter(Locale.US)
        f.format(tripleE(i)(pattern).asInstanceOf[String].toUpperCase(),
                 tripleE(i)(input).asInstanceOf[Object])
        assertEquals(
          tripleE(i)(output).asInstanceOf[String].toUpperCase(Locale.US),
          f.toString)
      }
    }
  }

  testFails(
    "format(String, Array[Object]) for BigDecimal conversion type 'g' and 'G'",
    0) { // issue not filed yet
    val tripleG: Array[Array[Any]] = Array(
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
    )
    val input: Int   = 0
    val pattern: Int = 1
    val output: Int  = 2
    for (i <- 0 until tripleG.length) {
      locally {
        val f = new Formatter(Locale.US)
        f.format(tripleG(i)(pattern).asInstanceOf[String],
                 tripleG(i)(input).asInstanceOf[Object])
        assertEquals(tripleG(i)(output), f.toString)
      }
      // test for conversion type 'G'
      locally {
        val f = new Formatter(Locale.US)
        f.format(tripleG(i)(pattern).asInstanceOf[String].toUpperCase(),
                 tripleG(i)(input).asInstanceOf[Object])
        assertEquals(
          tripleG(i)(output).asInstanceOf[String].toUpperCase(Locale.US),
          f.toString)
      }
    }
    val f = new Formatter(Locale.GERMAN)
    f.format("%- (,9.6g", new BigDecimal("4E6"))
    /*
     * fail on RI, spec says 'g' requires the output to be formatted in
     * general scientific notation and the localization algorithm is
     * applied. But RI format this case to 4.00000e+06, which does not
     * conform to the German Locale
     */
    assertEquals(" 4,00000e+06", f.toString)
  }

  test("format(String, Array[Object]) for BigDecimal conversion type 'f'") {
    val input: Int   = 0
    val pattern: Int = 1
    val output: Int  = 2
    val tripleF: Array[Array[Any]] = Array(
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
      Array(new BigDecimal("9999999999999999999999999999999999999999999"),
            "%f",
            "9999999999999999999999999999999999999999999.000000"),
      Array(new BigDecimal("9999999999999999999999999999999999999999999"),
            "%#.3f",
            "9999999999999999999999999999999999999999999.000"),
      Array(
        new BigDecimal("9999999999999999999999999999999999999999999"),
        "%#,5f",
        "9,999,999,999,999,999,999,999,999,999,999,999,999,999,999.000000"),
      Array(new BigDecimal("9999999999999999999999999999999999999999999"),
            "%- #(12.0f",
            " 9999999999999999999999999999999999999999999."),
      Array(new BigDecimal("9999999999999999999999999999999999999999999"),
            "%#+0(1.6f",
            "+9999999999999999999999999999999999999999999.000000"),
      Array(new BigDecimal("9999999999999999999999999999999999999999999"),
            "%-+(8.4f",
            "+9999999999999999999999999999999999999999999.0000"),
      Array(new BigDecimal("9999999999999999999999999999999999999999999"),
            "% 0#(9.8f",
            " 9999999999999999999999999999999999999999999.00000000"),
      Array(new BigDecimal("-9999999999999999999999999999999999999999999"),
            "%f",
            "-9999999999999999999999999999999999999999999.000000"),
      Array(new BigDecimal("-9999999999999999999999999999999999999999999"),
            "%#.3f",
            "-9999999999999999999999999999999999999999999.000"),
      Array(
        new BigDecimal("-9999999999999999999999999999999999999999999"),
        "%#,5f",
        "-9,999,999,999,999,999,999,999,999,999,999,999,999,999,999.000000"),
      Array(new BigDecimal("-9999999999999999999999999999999999999999999"),
            "%- #(12.0f",
            "(9999999999999999999999999999999999999999999.)"),
      Array(new BigDecimal("-9999999999999999999999999999999999999999999"),
            "%#+0(1.6f",
            "(9999999999999999999999999999999999999999999.000000)"),
      Array(new BigDecimal("-9999999999999999999999999999999999999999999"),
            "%-+(8.4f",
            "(9999999999999999999999999999999999999999999.0000)"),
      Array(new BigDecimal("-9999999999999999999999999999999999999999999"),
            "% 0#(9.8f",
            "(9999999999999999999999999999999999999999999.00000000)")
    )
    for (i <- 0 until tripleF.length) {
      val f = new Formatter(Locale.US)
      f.format(tripleF(i)(pattern).asInstanceOf[String],
               tripleF(i)(input).asInstanceOf[Object])
      assertEquals(tripleF(i)(output), f.toString)
    }
    val f = new Formatter(Locale.US)
    f.format("%f", new BigDecimal("5.0E9"))
    // error on RI
    // RI throw ArrayIndexOutOfBoundsException
    assertEquals("5000000000.000000", f.toString)
  }

  test(
    "format(String, Array[Object]) for exceptions in Float/Double/BigDecimal conversion type 'e', 'E', 'g', 'G', 'f', 'a', 'A'") {
    val conversions: Array[Char] = Array('e', 'E', 'g', 'G', 'f', 'a', 'A')
    val illArgs: Array[Any] = Array(false,
                                    1.toByte,
                                    2.toShort,
                                    3,
                                    4.toLong,
                                    new BigInteger("5"),
                                    new java.lang.Character('c'),
                                    new Object(),
                                    new Date())
    for {
      i <- 0 until illArgs.length
      j <- 0 until conversions.length
    } {
      val f = new Formatter(Locale.UK)
      assertThrows[IllegalFormatConversionException](
        f.format("%" + conversions(j), illArgs(i).asInstanceOf[Object]))

    }
    locally {
      val f = new Formatter(Locale.UK)
      assertThrows[IllegalFormatConversionException](
        f.format("%a", new BigDecimal(1)))
    }
    locally {
      val f = new Formatter(Locale.UK)
      assertThrows[IllegalFormatConversionException](
        f.format("%A", new BigDecimal(1)))
    }

    val flagsConversionMismatches: Array[String] =
      Array("%,e", "%,E", "%#g", "%#G", "%,a", "%,A", "%(a", "%(A")
    for (i <- 0 until flagsConversionMismatches.length) {
      locally {
        val f = new Formatter(Locale.CHINA)
        assertThrows[FormatFlagsConversionMismatchException](
          f.format(flagsConversionMismatches(i), new BigDecimal(1)))
      }
      locally {
        val f = new Formatter(Locale.JAPAN)
        assertThrows[FormatFlagsConversionMismatchException](
          f.format(flagsConversionMismatches(i),
                   null.asInstanceOf[BigDecimal]))
      }
    }

    val missingFormatWidths: Array[String] = Array("%-0e",
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
                                                   "%-A")
    for (i <- 0 until missingFormatWidths.length) {
      locally {
        val f = new Formatter(Locale.KOREA)
        assertThrows[MissingFormatWidthException](
          f.format(missingFormatWidths(i), 1f.asInstanceOf[Object]))
      }
      locally {
        val f = new Formatter(Locale.KOREA)
        assertThrows[MissingFormatWidthException](
          f.format(missingFormatWidths(i), null.asInstanceOf[java.lang.Float]))
      }
    }

    val illFlags: Array[String] = Array("%+ e",
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
                                        "%-03A")
    for (i <- 0 until illFlags.length) {
      locally {
        val f = new Formatter(Locale.CANADA)
        assertThrows[IllegalFormatFlagsException](
          f.format(illFlags(i), 1.23d.asInstanceOf[Object]))
      }
      locally {
        val f = new Formatter(Locale.CANADA)
        assertThrows[IllegalFormatFlagsException](
          f.format(illFlags(i), null.asInstanceOf[java.lang.Double]))
      }
    }
    val f = new Formatter(Locale.US)
    assertThrows[UnknownFormatConversionException](
      f.format("%F", 1.asInstanceOf[Object]))
  }

  test(
    "format(String, Array[Object]) for Float/Double/BigDecimal exception throwing order") { // Porting note: sic
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
      val f = new Formatter(Locale.US)
      assertThrows[FormatFlagsConversionMismatchException](
        f.format("%,e", 1.toByte.asInstanceOf[Object]))
    }

    locally {
      // compare IllegalFormatFlagsException and
      // FormatFlagsConversionMismatchException
      val f = new Formatter(Locale.US)
      assertThrows[IllegalFormatFlagsException](
        f.format("%+ ,e", 1f.asInstanceOf[Object]))
    }

    locally {
      // compare MissingFormatWidthException and
      // IllegalFormatFlagsException
      val f = new Formatter(Locale.US)
      assertThrows[MissingFormatWidthException](
        f.format("%+ -e", 1f.asInstanceOf[Object]))
    }

    locally {
      // compare UnknownFormatConversionException and
      // MissingFormatWidthException
      val f = new Formatter(Locale.US)
      assertThrows[UnknownFormatConversionException](
        f.format("%-F", 1f.asInstanceOf[Object]))
    }
  }

  test("format(String, Array[Object]) for BigDecimal exception throwing order") { // Porting note: sic
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
      val f = new Formatter(Locale.US)
      assertThrows[FormatFlagsConversionMismatchException](
        f.format("%,e", 1.toByte.asInstanceOf[Object]))
    }

    locally {
      // compare IllegalFormatFlagsException and
      // FormatFlagsConversionMismatchException
      val f = new Formatter(Locale.US)
      assertThrows[IllegalFormatFlagsException](f.format("%+ ,e", bd))
    }

    locally {
      // compare MissingFormatWidthException and
      // IllegalFormatFlagsException
      val f = new Formatter(Locale.US)
      assertThrows[MissingFormatWidthException](f.format("%+ -e", bd))
    }

    locally {
      // compare UnknownFormatConversionException and
      // MissingFormatWidthException
      val f = new Formatter(Locale.US)
      assertThrows[UnknownFormatConversionException](f.format("%-F", bd))
    }
  }

  test(
    "format(String, Array[Object]) for null argument for Float/Double/BigDecimal conversion") {
    // test (Float)null
    locally {
      val f = new Formatter(Locale.FRANCE)
      f.format("%#- (9.0e", null.asInstanceOf[java.lang.Float])
      assertEquals("         ", f.toString)
    }
    locally {
      val f = new Formatter(Locale.GERMAN)
      f.format("%-+(1.6E", null.asInstanceOf[java.lang.Float])
      assertEquals("NULL", f.toString)
    }
    locally {
      val f = new Formatter(Locale.UK)
      f.format("%+0(,8.4g", null.asInstanceOf[java.lang.Float])
      assertEquals("    null", f.toString)
    }
    locally {
      val f = new Formatter(Locale.FRANCE)
      f.format("%- (9.8G", null.asInstanceOf[java.lang.Float])
      assertEquals("NULL     ", f.toString)
    }
    locally {
      val f = new Formatter(Locale.FRANCE)
      f.format("%- (12.1f", null.asInstanceOf[java.lang.Float])
      assertEquals("n           ", f.toString)
    }
    locally {
      val f = new Formatter(Locale.FRANCE)
      f.format("% .4a", null.asInstanceOf[java.lang.Float])
      assertEquals("null", f.toString)
    }
    locally {
      val f = new Formatter(Locale.FRANCE)
      f.format("%06A", null.asInstanceOf[java.lang.Float])
      assertEquals("  NULL", f.toString)
    }
    // test (Double)null
    locally {
      val f = new Formatter(Locale.GERMAN)
      f.format("%- (9e", null.asInstanceOf[java.lang.Double])
      assertEquals("null     ", f.toString)
    }
    locally {
      val f = new Formatter(Locale.GERMAN)
      f.format("%#-+(1.6E", null.asInstanceOf[java.lang.Double])
      assertEquals("NULL", f.toString)
    }
    locally {
      val f = new Formatter(Locale.GERMAN)
      f.format("%+0(6.4g", null.asInstanceOf[java.lang.Double])
      assertEquals("  null", f.toString)
    }
    locally {
      val f = new Formatter(Locale.GERMAN)
      f.format("%- (,5.8G", null.asInstanceOf[java.lang.Double])
      assertEquals("NULL ", f.toString)
    }
    locally {
      val f = new Formatter(Locale.GERMAN)
      f.format("% (.4f", null.asInstanceOf[java.lang.Double])
      assertEquals("null", f.toString)
    }
    locally {
      val f = new Formatter(Locale.GERMAN)
      f.format("%#.6a", null.asInstanceOf[java.lang.Double])
      assertEquals("null", f.toString)
    }
    locally {
      val f = new Formatter(Locale.GERMAN)
      f.format("% 2.5A", null.asInstanceOf[java.lang.Double])
      assertEquals("NULL", f.toString)
    }
    // test (BigDecimal)null
    locally {
      val f = new Formatter(Locale.UK)
      f.format("%#- (6.2e", null.asInstanceOf[BigDecimal])
      assertEquals("nu    ", f.toString)
    }
    locally {
      val f = new Formatter(Locale.UK)
      f.format("%-+(1.6E", null.asInstanceOf[BigDecimal])
      assertEquals("NULL", f.toString)
    }
    locally {
      val f = new Formatter(Locale.UK)
      f.format("%+-(,5.3g", null.asInstanceOf[BigDecimal])
      assertEquals("nul  ", f.toString)
    }
    locally {
      val f = new Formatter(Locale.UK)
      f.format("%0 3G", null.asInstanceOf[BigDecimal])
      assertEquals("NULL", f.toString)
    }
    locally {
      val f = new Formatter(Locale.UK)
      f.format("%0 (9.0G", null.asInstanceOf[BigDecimal])
      assertEquals("         ", f.toString)
    }
    locally {
      val f = new Formatter(Locale.UK)
      f.format("% (.5f", null.asInstanceOf[BigDecimal])
      assertEquals("null", f.toString)
    }
    locally {
      val f = new Formatter(Locale.UK)
      f.format("%06a", null.asInstanceOf[BigDecimal])
      assertEquals("  null", f.toString)
    }
    locally {
      val f = new Formatter(Locale.UK)
      f.format("% .5A", null.asInstanceOf[BigDecimal])
      assertEquals("NULL", f.toString)
    }
  }

  testFails("Formatter.BigDecimalLayoutForm.values()", 0) { // issue not filed yet
    // BigDecimalLayoutForm.values() segfaults for unknown reason.
    throw new NullPointerException() // to prevent segfault
    import Formatter.BigDecimalLayoutForm
    val vals: Array[BigDecimalLayoutForm] = BigDecimalLayoutForm.values()
    assertEquals(2, vals.length)
    assertEquals(BigDecimalLayoutForm.SCIENTIFIC, vals(0))
    assertEquals(BigDecimalLayoutForm.DECIMAL_FLOAT, vals(1))
  }

  testFails("Formatter.BigDecimalLayoutForm.valueOf(String)", 0) { // issue not filed yet
    // the line `val sci: ...` segfaults for unknown reason.
    throw new NullPointerException() // to prevent segfault
    import Formatter.BigDecimalLayoutForm
    val sci: BigDecimalLayoutForm = BigDecimalLayoutForm.valueOf("SCIENTIFIC")
    assertEquals(BigDecimalLayoutForm.SCIENTIFIC, sci)
    val decFloat: BigDecimalLayoutForm =
      BigDecimalLayoutForm.valueOf("DECIMAL_FLOAT")
    assertEquals(BigDecimalLayoutForm.DECIMAL_FLOAT, decFloat)
  }

  /*
   * Regression test for Harmony-5845
   * test the short name for timezone whether uses DaylightTime or not
   */
  testFails("DaylightTime", 0) { // issue not filed yet
    // java.util.TimeZone$.getAvailableIDs throws NotImplementedError
    val c1: Calendar = new GregorianCalendar(2007, 0, 1)
    val c2: Calendar = new GregorianCalendar(2007, 7, 1)
    for (tz <- TimeZone.getAvailableIDs) {
      if (tz == "America/Los_Angeles") {
        c1.setTimeZone(TimeZone.getTimeZone(tz))
        c2.setTimeZone(TimeZone.getTimeZone(tz))
        assertTrue(String.format("%1$tZ%2$tZ", c1, c2) == "PSTPDT")
      }
      if (tz == "America/Panama") {
        c1.setTimeZone(TimeZone.getTimeZone(tz))
        c2.setTimeZone(TimeZone.getTimeZone(tz))
        assertTrue(String.format("%1$tZ%2$tZ", c1, c2) == "ESTEST")
      }
    }
  }

  /*
   * Regression test for Harmony-5845
   * test scientific notation to follow RI's behavior
   */
  test("ScientificNotation") {
    val f: Formatter      = new Formatter()
    val mc: MathContext   = new MathContext(30)
    val value: BigDecimal = new BigDecimal(0.1, mc)
    f.format("%.30G", value)

    val result: String   = f.toString
    val expected: String = "0.100000000000000005551115123126"
    assertEquals(expected, result)
  }
}
