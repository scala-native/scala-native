package java.util.jar

// Ported from Apache Harmony

object AttributesNameSuite extends tests.Suite {

  test("Constructor()") {
    assertThrows[IllegalArgumentException] {
      new Attributes.Name(
        "01234567890123456789012345678901234567890123456789012345678901234567890");
    }
  }
}
