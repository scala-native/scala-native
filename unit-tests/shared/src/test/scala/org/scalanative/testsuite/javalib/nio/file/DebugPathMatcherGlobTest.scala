package org.scalanative.testsuite.javalib.nio.file

import java.nio.file._

import org.junit.Test
import org.junit.Assert._
import org.junit.Assume._

import java.util.regex.PatternSyntaxException

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform.isWindows

class DebugPathMatcherGlobTest {

  @inline def pass(glob: String, path: String, onWindows: Boolean = true) = {
    assumeFalse(
      s"Skipping pass($glob, $path) on Windows",
      isWindows && !onWindows
    )
    val pattern = s"glob:$glob"
    val matched = Paths.get(path)
    val matcher = FileSystems.getDefault().getPathMatcher(pattern)
    assertTrue(
      s"glob: $glob, path: $path should be matched",
      matcher.matches(matched)
    )
  }

  @inline def fail(glob: String, path: String, onWindows: Boolean = true) = {
    assumeFalse(
      s"Skipping fail($glob, $path) on Windows",
      isWindows && !onWindows
    )
    val pattern = s"glob:$glob"
    val matched = Paths.get(path)
    val matcher = FileSystems.getDefault().getPathMatcher(pattern)
    assertFalse(
      s"glob: $glob, path: $path should not be matched",
      matcher.matches(matched)
    )
  }

  @inline def throws[T <: Throwable](glob: String, message: String) = {
    val pattern = s"glob:$glob"
    assertThrows(
      message,
      classOf[PatternSyntaxException],
      FileSystems.getDefault().getPathMatcher(pattern)
    )
  }

  /* Issue #2937
   * On both ScalaJVM & Scala Native the DirectoryStream method
   * produces a Stream of file of the form "./name" for files in the
   * current working directory.
   * It is poorly documented but well experienced that Unix glob()
   * allows a glob pattern of "name" to match ".name".
   */
  @Test def correctMatchingOfInitialDotSlash(): Unit = {
    pass("*.sbt", "Xlocal.sbt") // establish baseline
    pass("*.sbt", "./Xlocal.sbt") // Xlocal.sbt does not exist on AthaB
  }
}
