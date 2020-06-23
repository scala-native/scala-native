/**
 * Ported from Harmony
 */
package java.util

import java.io._

object PropertiesSuite extends tests.Suite {
  test("put on null key or null value") {
    val properties = new Properties
    assertThrows[NullPointerException](properties.put(null, "any"))
    assertThrows[NullPointerException](properties.put("any", null))
  }

  test("non-string values") {
    val properties = new Properties

    properties.put("age", Int.box(18))
    assertNull(properties.getProperty("age"))
    assertThrows[ClassCastException] {
      properties.list(new PrintWriter(new ByteArrayOutputStream))
    }
  }

  test("list") {
    val properties = new Properties

    def assertResult(result: String): Unit = {
      val buffer4stream = new ByteArrayOutputStream
      val stream        = new PrintStream(buffer4stream)
      properties.list(stream)
      assertEquals(buffer4stream.toString.trim, result.trim)
      stream.flush()
    }

    val result0 = "-- listing properties --\n"
    assertResult(result0)

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

  test("load(InputStream) with null input") {
    val prop = new java.util.Properties()
    assertThrows[NullPointerException] {
      prop.load(null: java.io.InputStream)
    }
  }

  test("load(InputStream)") {
    val is: InputStream = new ByteArrayInputStream(dummyProps)
    val prop            = new Properties()
    prop.load(is)
    is.close()

    assertEquals("value1", prop.getProperty("key1"))
    assertNull(prop.getProperty("commented.key"))
    assertEquals("default_value",
                 prop.getProperty("commented.key", "default_value"))
  }

  test("load(InputStream) for empty keys") {
    var prop = new java.util.Properties()
    prop.load(new ByteArrayInputStream("=".getBytes()))
    assertEquals("", prop.get(""))

    prop = new java.util.Properties()
    prop.load(new ByteArrayInputStream(" = ".getBytes()))
    assertEquals("", prop.get(""))
  }

  test("load(InputStream) handle whitespace") {
    var prop = new java.util.Properties()
    prop.load(new ByteArrayInputStream(" a= b".getBytes()))
    assertEquals("b", prop.get("a"))

    prop = new java.util.Properties()
    prop.load(new ByteArrayInputStream(" a b".getBytes()))
    assertEquals("b", prop.get("a"))
  }

  test("load(InputStream) handle special chars") {
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

  test("load(InputStream) with file input") {
    val file =
      new File("unit-tests/src/test/resources/properties-load-test.properties")
    val is: InputStream = new FileInputStream(file)
    val prop            = new Properties()
    prop.load(is)
    is.close()

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
  }

  test("load(Reader) with null input") {
    val prop = new java.util.Properties()
    assertThrows[NullPointerException] {
      prop.load(null: Reader)
    }
  }

  test("load(Reader)") {
    val is: InputStream = new ByteArrayInputStream(dummyProps)
    val prop            = new Properties()
    prop.load(new InputStreamReader(is))
    is.close()

    assertEquals("value1", prop.getProperty("key1"))
    assertNull(prop.getProperty("commented.key"))
    assertEquals("default_value",
                 prop.getProperty("commented.key", "default_value"))
  }

  test("load(Reader) handle special chars") {
    var prop = new java.util.Properties()
    prop.load(
      new InputStreamReader(new ByteArrayInputStream(
        "#\u008d\u00d2\na=\u008d\u00d3".getBytes("UTF-8"))))
    assertEquals("\u008d\u00d3", prop.get("a"))

    prop = new java.util.Properties()
    prop.load(
      new InputStreamReader(new ByteArrayInputStream(
        "#properties file\r\nfred=1\r\n#last comment".getBytes("UTF-8"))))
    assertEquals("1", prop.get("fred"))
  }

  test("load(Reader) with file input") {
    val file =
      new File("unit-tests/src/test/resources/properties-load-test.properties")
    val is: InputStream = new FileInputStream(file)
    val prop            = new Properties()
    prop.load(new InputStreamReader(is))
    is.close()

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
  }

  test("store(OutputStream, comments) with null input") {
    val prop = new java.util.Properties()
    assertThrows[NullPointerException] {
      prop.store(null: OutputStream, "")
    }
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

  test("store(OutputStream, comments), load(InputStream) roundtrip") {
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
  test("check comment formatted correctly") {
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

  test("check properties formatted correctly") {
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

  test("store(Writer, comments) with null input") {
    val prop = new java.util.Properties()
    assertThrows[NullPointerException] {
      prop.store(null: Writer, "")
    }
  }

  test("store(Writer, comments)") {
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
  // memory version of properties file
  lazy val filestr =
    """|
       |
       |
       |
       |   \ \r \n \t \f
       |
       |
       |! dshfjklahfjkldashgjl;as
       |     #jdfagdfjagkdjfghksdajfd
       |
       |!!properties
       |
       |a=a
       |b bb as,dn   
       |c\r\ \t\nu =:: cu
       |bu= b\
       |		u
       |d=d\r\ne=e
       |f   :f\
       |f\
       |			f
       |g		g
       |h\ h
       |\   i=i
       |j=\   j
       |space=\   c
       |
       |dblbackslash=\\
       |
       |
       |
    """.stripMargin
}
