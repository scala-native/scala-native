/**
 * Ported from Scala.js and Harmony
 */
package java.util

import java.io._
import java.{util => ju}

import org.junit.Test
import org.junit.Ignore
import org.junit.Assert._
import org.junit.Assume._

import scala.scalanative.junit.utils.AssertThrows._
import scala.scalanative.junit.utils.Utils._

class PropertiesTest {
  // remove when Platform is implemented
  val hasCompliantAsInstanceOfs = true

  // ported from Scala.js
  @Test def setProperty(): Unit = {
    val prop = new Properties()
    prop.setProperty("a", "A")
    assertEquals("A", prop.get("a"))
    prop.setProperty("a", "AA")
    prop.setProperty("b", "B")
    assertEquals("AA", prop.get("a"))
    assertEquals("B", prop.get("b"))

    val prop2 = new Properties(prop)
    prop2.setProperty("a", "AAA")
    assertEquals("AAA", prop2.get("a"))
  }

  @Test def getProperty(): Unit = {
    val prop = new Properties()

    assertNull(prop.getProperty("a"))
    prop.setProperty("a", "A")
    assertEquals("A", prop.getProperty("a"))
    assertNull(prop.getProperty("aa"))

    assertEquals("A", prop.getProperty("a", "B"))
    assertEquals("B", prop.getProperty("b", "B"))

    // Tests with default properties
    prop.setProperty("b", "B")

    val prop2 = new Properties(prop)
    prop2.setProperty("b", "BB")
    prop2.setProperty("c", "C")
    assertEquals("A", prop2.getProperty("a"))
    assertEquals("BB", prop2.getProperty("b"))
    assertEquals("C", prop2.getProperty("c"))
  }

  @Test def propertyNames(): Unit = {
    val prop = new Properties()
    assertTrue(enumerationIsEmpty(prop.propertyNames()))
    prop.setProperty("a", "A")
    prop.setProperty("b", "B")
    prop.setProperty("c", "C")
    assertEquals(3, enumerationSize(prop.propertyNames()))
    assertEnumSameElementsAsSet[Any]("a", "b", "c")(prop.propertyNames())

    val prop2 = new Properties(prop)
    prop.setProperty("c", "CC")
    prop.setProperty("d", "D")
    assertEquals(4, enumerationSize(prop2.propertyNames()))
    assertEnumSameElementsAsSet[Any]("a", "b", "c", "d")(prop2.propertyNames())
  }

  @Test def propertyNamesIsNotAffectedByOverriddenPropertyNamesInDefaults()
      : Unit = {
    val defaults = new java.util.Properties {
      override def propertyNames(): ju.Enumeration[_] =
        ju.Collections.emptyEnumeration[String]()
    }
    defaults.setProperty("foo", "bar")

    val props = new Properties(defaults)
    props.setProperty("foobar", "babar")
    assertEnumSameElementsAsSet[Any]("foo", "foobar")(props.propertyNames())
  }

  @Test def propertyNamesWithBadContents(): Unit = {
    assumeTrue("Assumed compliant asInstanceOf", hasCompliantAsInstanceOfs)

    val prop = new Properties()
    prop.setProperty("a", "A")
    prop.setProperty("b", "B")
    prop.setProperty("c", "C")

    prop.put(1.asInstanceOf[AnyRef], "2")
    assertThrows(classOf[Throwable], prop.propertyNames())
    prop.remove(1.asInstanceOf[AnyRef])

    prop.put("1", 1.asInstanceOf[AnyRef])
    assertEnumSameElementsAsSet[Any]("a", "b", "c", "1")(prop.propertyNames())
    prop.remove("1")

    val prop2 = new Properties(prop)
    prop.setProperty("c", "CC")
    prop.setProperty("d", "D")

    prop2.put(1.asInstanceOf[AnyRef], "2")
    assertThrows(classOf[Throwable], prop2.propertyNames())
    prop2.remove(1.asInstanceOf[AnyRef])

    prop2.put("1", 1.asInstanceOf[AnyRef])
    assertEnumSameElementsAsSet[Any]("a", "b", "c", "d", "1")(
      prop2.propertyNames())
  }

  @Test def stringPropertyNames(): Unit = {
    val prop = new Properties()
    assertEquals(0, prop.stringPropertyNames().size)
    prop.setProperty("a", "A")
    prop.setProperty("b", "B")
    prop.setProperty("c", "C")
    assertEquals(3, prop.stringPropertyNames().size)
    assertCollSameElementsAsSet("a", "b", "c")(prop.stringPropertyNames())

    val prop2 = new Properties(prop)
    prop.setProperty("c", "CC")
    prop.setProperty("d", "D")
    assertEquals(4, prop2.stringPropertyNames().size)
    assertCollSameElementsAsSet("a", "b", "c", "d")(prop2.stringPropertyNames())
  }

  @Test def stringPropertyNamesIsNotAffectedByOverriddenStringPropertyNamesInDefaults()
      : Unit = {
    val defaults = new java.util.Properties {
      override def stringPropertyNames(): ju.Set[String] =
        ju.Collections.emptySet[String]()
    }
    defaults.setProperty("foo", "bar")

    val props = new Properties(defaults)
    props.setProperty("foobar", "babar")
    assertCollSameElementsAsSet("foo", "foobar")(props.stringPropertyNames())
  }

  @Test def stringPropertyNamesWithBadContents(): Unit = {
    assumeTrue("Assumed compliant asInstanceOf", hasCompliantAsInstanceOfs)

    val prop = new Properties()
    prop.setProperty("a", "A")
    prop.setProperty("b", "B")
    prop.setProperty("c", "C")

    prop.put(1.asInstanceOf[AnyRef], "2")
    assertCollSameElementsAsSet("a", "b", "c")(prop.stringPropertyNames())
    prop.remove(1.asInstanceOf[AnyRef])

    prop.put("1", 1.asInstanceOf[AnyRef])
    assertCollSameElementsAsSet("a", "b", "c")(prop.stringPropertyNames())
    prop.remove("1")

    val prop2 = new Properties(prop)
    prop.setProperty("c", "CC")
    prop.setProperty("d", "D")

    prop2.put(1.asInstanceOf[AnyRef], "2")
    assertCollSameElementsAsSet("a", "b", "c", "d")(prop2.stringPropertyNames())
    prop2.remove(1.asInstanceOf[AnyRef])

    prop2.put("1", 1.asInstanceOf[AnyRef])
    assertCollSameElementsAsSet("a", "b", "c", "d")(prop2.stringPropertyNames())
  }

  // ported from Harmony
  @Test def put_on_null_key_or_null_value(): Unit = {
    val properties = new Properties
    assertThrows(classOf[NullPointerException], properties.put(null, "any"))
    assertThrows(classOf[NullPointerException], properties.put("any", null))
  }

  @Test def non_string_values(): Unit = {
    val properties = new Properties

    properties.put("age", Int.box(18))
    assertNull(properties.getProperty("age"))
    assertThrows(classOf[ClassCastException],
                 properties.list(new PrintWriter(new ByteArrayOutputStream)))
  }

  @Test def list(): Unit = {
    val properties = new Properties

    def assertResult(result: String): Unit = {
      val buffer4stream = new ByteArrayOutputStream
      val stream        = new PrintStream(buffer4stream)
      properties.list(stream)
      assertEquals(buffer4stream.toString.trim, result.trim)
      stream.flush()
    }

    assertResult("-- listing properties --\n")

    properties.put("name", "alice")
    val result1 =
      """
        |-- listing properties --
        |name=alice
      """.stripMargin
    assertResult(result1)

    properties.put("p0000000000111111111122222222223333333333", "40")
    val result2 =
      """-- listing properties --
        |name=alice
        |p000000000011111111112222222222333333...=40
      """.stripMargin
    assertResult(result2)
  }

  private val dummyProps = {
    val bout = new ByteArrayOutputStream()
    val ps   = new PrintStream(bout)
    ps.println("#commented.key=dummy_value")
    ps.println("key1=value1")
    ps.println("key2=value1")
    ps.close()
    bout.toByteArray
  }

  @Test def load_InputStream_with_null_input(): Unit = {
    val prop = new java.util.Properties()
    assertThrows(classOf[NullPointerException],
                 prop.load(null: java.io.InputStream))
  }

  @Test def load_InputStream(): Unit = {
    val is: InputStream = new ByteArrayInputStream(dummyProps)
    val prop            = new Properties()
    prop.load(is)
    is.close()

    assertEquals("value1", prop.getProperty("key1"))
    assertNull(prop.getProperty("commented.key"))
    assertEquals("default_value",
                 prop.getProperty("commented.key", "default_value"))
  }

  @Test def load_InputStream_for_empty_keys(): Unit = {
    var prop = new java.util.Properties()
    prop.load(new ByteArrayInputStream("=".getBytes()))
    assertEquals("", prop.get(""))

    prop = new java.util.Properties()
    prop.load(new ByteArrayInputStream(" = ".getBytes()))
    assertEquals("", prop.get(""))
  }

  @Test def load_InputStream_handle_whitespace(): Unit = {
    var prop = new java.util.Properties()
    prop.load(new ByteArrayInputStream(" a= b".getBytes()))
    assertEquals("b", prop.get("a"))

    prop = new java.util.Properties()
    prop.load(new ByteArrayInputStream(" a b".getBytes()))
    assertEquals("b", prop.get("a"))
  }

  @Test def load_InputStream_handle_special_chars(): Unit = {
    var prop = new java.util.Properties()
    prop.load(
      new ByteArrayInputStream(
        "#\u008d\u00d2\na=\u008d\u00d3".getBytes("ISO8859_1")))
    assertEquals("\u008d\u00d3", prop.get("a"))

    prop = new java.util.Properties()
    prop.load(
      new ByteArrayInputStream(
        "#properties file\r\nfred=1\r\n#last comment".getBytes("ISO8859_1")))
    assertEquals("1", prop.get("fred"))
  }

  def checkLoadFromFile(prop: Properties): Unit = {
    assertEquals("\n \t \f", prop.getProperty(" \r"))
    assertEquals("a", prop.getProperty("a"))
    assertEquals("bb as,dn   ", prop.getProperty("b"))
    assertEquals(":: cu", prop.getProperty("c\r \t\nu"))
    assertEquals("bu", prop.getProperty("bu"))
    assertEquals("d\r\ne=e", prop.getProperty("d"))
    assertEquals("fff", prop.getProperty("f"))
    assertEquals("g", prop.getProperty("g"))
    assertEquals("", prop.getProperty("h h"))
    assertEquals("i=i", prop.getProperty(" "))
    assertEquals("   j", prop.getProperty("j"))
    assertEquals("   c", prop.getProperty("space"))
    assertEquals("\\", prop.getProperty("dblbackslash"))
    // added for new implementation
    assertEquals("foo,   ", prop.getProperty("trailing"))
    assertEquals("", prop.getProperty("bar"))
    assertEquals("""baz \  """, prop.getProperty("notrailing"))
  }

  @Test def load_InputStream_with_file_input(): Unit = {
    val file =
      new File("unit-tests/src/test/resources/properties-load-test.properties")
    val is: InputStream = new FileInputStream(file)
    val prop            = new Properties()
    prop.load(is)
    is.close()
    checkLoadFromFile(prop)
  }

  @Test def load_Reader_with_null_input(): Unit = {
    val prop = new java.util.Properties()
    assertThrows(classOf[NullPointerException], prop.load(null: Reader))
  }

  @Test def load_Reader(): Unit = {
    val is: InputStream = new ByteArrayInputStream(dummyProps)
    val prop            = new Properties()
    prop.load(new InputStreamReader(is))
    is.close()

    assertEquals("value1", prop.getProperty("key1"))
    assertNull(prop.getProperty("commented.key"))
    assertEquals("default_value",
                 prop.getProperty("commented.key", "default_value"))
  }

  @Test def load_Reader_handle_special_chars(): Unit = {
    var prop = new java.util.Properties()
    prop.load(
      new InputStreamReader(
        new ByteArrayInputStream(
          "#\u008d\u00d2\na=\u008d\u00d3".getBytes("UTF-8"))))
    assertEquals("\u008d\u00d3", prop.get("a"))

    prop = new java.util.Properties()
    prop.load(
      new InputStreamReader(
        new ByteArrayInputStream(
          "#properties file\r\nfred=1\r\n#last comment".getBytes("UTF-8"))))
    assertEquals("1", prop.get("fred"))
  }

  @Test def load_Reader_with_file_input(): Unit = {
    val file =
      new File("unit-tests/src/test/resources/properties-load-test.properties")
    val is: InputStream = new FileInputStream(file)
    val prop            = new Properties()
    prop.load(new InputStreamReader(is))
    is.close()

    checkLoadFromFile(prop)
  }

  @Test def store_OutputStream_comments_with_null_input(): Unit = {
    val prop = new java.util.Properties()
    assertThrows(classOf[NullPointerException],
                 prop.store(null: OutputStream, ""))
  }

  // used for next two tests, \b prints as \u0008
  val prop1 = new Properties()
  prop1.put("Property A", " aye\\\f\t\n\r\b")
  prop1.put("Property B", "b ee#!=:")
  prop1.put("Property C", "see")
  val header1 =
    "A Header\rLine2\nLine3\r\nLine4\n!AfterExclaim\r\n#AfterPound\nWow!"
  val out1 = new ByteArrayOutputStream()
  prop1.store(out1, header1)
  out1.close() // noop

  @Test def store_OutputStream_comments_load_InputStream_roundtrip(): Unit = {
    val prop2 = new Properties()

    val out1 = new ByteArrayOutputStream()
    prop1.store(out1, header1)

    val in = new ByteArrayInputStream(out1.toByteArray)
    prop2.load(in)
    in.close()

    val e = prop1.propertyNames()
    while (e.hasMoreElements) {
      val nextKey = e.nextElement().asInstanceOf[String]
      assertEquals(prop2.getProperty(nextKey), prop1.getProperty(nextKey))
    }
  }
  @Test def check_comment_formatted_correctly(): Unit = {
    // Avoid variable Date output which is last line in comment
    // Matches JVM output
    val commentsWithoutDate =
      """|#A Header
         |#Line2
         |#Line3
         |#Line4
         |!AfterExclaim
         |#AfterPound
         |#Wow!""".stripMargin

    assertTrue(out1.toString().startsWith(commentsWithoutDate))
  }

  @Test def check_properties_formatted_correctly(): Unit = {
    // for better or worse JVM outputs \b as \u0008 and you
    // can't just add \u0008 to the end of the last property
    val props = new StringBuilder("""|Property\ C=see
         |Property\ B=b ee\#\!\=\:
         |Property\ A=\ aye\\\f\t\n\r""")
      .appendAll(Array('\\', 'u', '0', '0', '0', '8'))
      .append(System.lineSeparator)
      .append('|')

    val res = props.toString.stripMargin
    // uncomment for debug
    //println(out1.toString())
    //println(res)
    assertTrue(out1.toString().endsWith(res))
  }

  @Test def store_Writer_comments_with_null_input(): Unit = {
    val prop = new java.util.Properties()
    assertThrows(classOf[NullPointerException], prop.store(null: Writer, ""))
  }

  @Test def store_Writer_comments(): Unit = {
    val prop1 = new Properties()
    val prop2 = new Properties()

    prop1.put("Property A", " aye\\\f\t\n\r\b")
    prop1.put("Property B", "b ee#!=:")
    prop1.put("Property C", "see")

    val out = new ByteArrayOutputStream()
    prop1.store(new OutputStreamWriter(out), "A Header")
    out.close()

    val in = new ByteArrayInputStream(out.toByteArray)
    prop2.load(in)
    in.close()

    val e = prop1.propertyNames()
    while (e.hasMoreElements) {
      val nextKey = e.nextElement().asInstanceOf[String]
      assertEquals(prop2.getProperty(nextKey), prop1.getProperty(nextKey))
    }
  }
}
