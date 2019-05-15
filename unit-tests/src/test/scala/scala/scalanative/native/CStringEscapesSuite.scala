package scala.scalanative
package native

object CStringEscapesSuite extends tests.Suite {
  test("""c"..." literals with various escapes""") {
    // note: `fromCString` is needed to trigger compilation errors against malformed literals
    fromCString(c"")
    fromCString(c"no escapes")
    fromCString(c"\'"); fromCString(c"\\'")
    fromCString(c"\?"); fromCString(c"\\?")
    fromCString(c"\\")
    fromCString(c"\a"); fromCString(c"\\a")
    fromCString(c"\b"); fromCString(c"\\b")
    fromCString(c"\f"); fromCString(c"\\f")
    fromCString(c"\n"); fromCString(c"\\n")
    fromCString(c"\r"); fromCString(c"\\r")
    fromCString(c"\t"); fromCString(c"\\t")
    fromCString(c"\v"); fromCString(c"\\v")
    fromCString(c"\012\x6a")
    fromCString(c"%s \\t %.2f %s/s\00")

    // uncomment the following to trigger compilation errors for testing
    // fromCString(c"\")     // error at NIR
    // fromCString(c"\x2ae") // error at NIR
    // fromCString(c"\"")    // error at Scala compiler
  }

  test("""the value of c"..." literals""") {
    assertEquals("\t", fromCString(c"\t"))
    assertEquals("\\t", fromCString(c"\\t"))
    assertEquals("\u0020\u0020\u0061\u0062", fromCString(c"\040\40\141\x62"))
  }
}
