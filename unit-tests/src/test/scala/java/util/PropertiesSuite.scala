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
}
