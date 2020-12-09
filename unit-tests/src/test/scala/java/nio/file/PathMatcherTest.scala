package java.nio.file

import org.junit.Test
import org.junit.Assert._

import scalanative.junit.utils.AssertThrows._

class PathMatcherTest {

  @Test def supportsRegexSyntax(): Unit = {
    val matcher = getMatcher("regex:fo*")
    assertTrue(matcher.matches(Paths.get("foo")))
  }

  @Test def throwsUnsupportedOperationExceptionIfUnknownSyntaxIsUsed(): Unit = {
    assertThrows(classOf[UnsupportedOperationException],
                 getMatcher("foobar:blabla"))
  }

  @Test def throwsIllegalArgumentExceptionIfParamIsNotSyntaxPattern(): Unit = {
    assertThrows(classOf[IllegalArgumentException], getMatcher("helloworld"))
  }

  private def getMatcher(syntaxAndPattern: String): PathMatcher =
    FileSystems.getDefault().getPathMatcher(syntaxAndPattern)
}
