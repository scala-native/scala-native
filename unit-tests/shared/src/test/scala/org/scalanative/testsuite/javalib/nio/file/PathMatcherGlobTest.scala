package org.scalanative.testsuite
package javalib.nio.file

import java.nio.file._
import java.util.regex.PatternSyntaxException

import org.junit.Assert._
import org.junit.Assume._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform.isWindows

class PathMatcherGlobTest {

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

  @Test def correctMatcherOfClass(): Unit = {
    pass("ab[c-c].ext", "abc.ext")
    fail("ab[c-c].ext", "abd.ext")
    pass("ab[c-d].ext", "abd.ext")
    fail("ab[c-d].ext", "abb.ext")
    pass("ab[c-e].ext", "abe.ext")
    fail("ab[c-e].ext", "abf.ext")

    pass("[a-c].ext", "b.ext")
    fail("[a-c].ext", ".ext")

    pass("prefix[a-c]", "prefixb")
    fail("prefix[a-c]", "prefix")
    fail("prefix[a-c]", "prefixd")

    // mixed
    pass("[abf-i]", "a")
    pass("[abf-i]", "b")
    pass("[abf-i]", "f")
    pass("[abf-i]", "h")
    pass("[abf-i]", "i")
    pass("[abf-i]", "a")

    // special characters
    pass("a[-]b", "a-b")
    fail("a[-]b", "ab")

    pass("a[-a]b", "a-b")
    pass("a[a-]b", "a-b")

    pass("a[\\]b", "a\\b", onWindows = false)
    fail("a[\\]b", "ab", onWindows = false)

    pass("a[?]b", "a?b", onWindows = false)
    fail("a[?]b", "ab")

    pass("a[*]b", "a*b", onWindows = false)
    fail("a[*]b", "ab")
  }

  @Test def correctMatcherOfExclusionClass(): Unit = {
    pass("ab[!d].ext", "abe.ext")
    fail("ab[!d].ext", "abd.ext")
  }

  @Test def correctMatcherOfGroups(): Unit = {
    // prefix group
    pass("{ba,na}na", "bana")
    pass("{ba,na}na", "nana")
    fail("{ba,na}na", "na")
    fail("{ba,na}na", "ba")

    // infix, differing length
    pass("b{ana,n}a", "banaa")
    pass("b{ana,n}a", "bna")
    fail("b{ana,n}a", "banana")

    // infix, with empty
    pass("ba{na,}na", "bana")
    pass("ba{na,}na", "banana")
    fail("ba{na,}na", "ba")
    fail("ba{na,}na", "na")

    // suffix
    pass("b{an,ana,nana}", "ban")
    pass("b{an,ana,nana}", "bana")
    pass("b{an,ana,nana}", "bnana")
    fail("b{an,ana,nana}", "ba")
    fail("b{an,ana,nana}", "nan")
  }

  @Test def incorrectPattern(): Unit = {
    throws("{ba,{na{na}},{na}}", "nested groups")

    throws("[c-a]", "incorrect class definition")

    throws("a[b", "class does not end")

    throws("a[!b", "exclusion class does not end")

    throws("a{b,c", "group does not end")
  }

  @Test def correctMatchingOfQuestionMark(): Unit = {
    pass("?.ext", "a.ext")
    fail("?.ext", "/.ext")
    fail("?.ext", ".ext")

    pass("??.ext", "ba.ext")
    fail("??.ext", ".ext")

    pass("*.?", "any.c")
    pass("*.?", ".c")
    fail("*.?", "any.")
  }

  @Test def correctMatchingOfAsterisk(): Unit = {
    pass("*.ext", "banana.ext")
    pass("*.ext", ".ext")
    fail("*.ext", "/banana.ext")
    fail("*.ext", "/.ext")

    pass("*/*", "bana/na")
    pass("*/*", "/banana")
    fail("*/*", "banana/")
    fail("*/*", "ba/na/na")
    fail("*/*", "banana/")
  }

  @Test def correctMatchingOfDoubleAsterisk(): Unit = {
    pass("**/*", "/a")
    fail("**/*", "a/")
    pass("**/*", "a/b")
    pass("**/*", "a/b/c")
    fail("**/*", "ab")
    fail("**/*", "")

    pass("**??/*", "a/aa/a")
    pass("**??/*", "aa/a")
    pass("**??/*", "a/aa/a")
    pass("**??/*", "aa/a")
    fail("**??/*", "aa/")
    fail("**??/*", "a/a")
    fail("**??/*", "a/a/a")
  }

  @Test def allConstructs(): Unit = {
    pass("**/*/*.ext", "/file/file.ext")
    pass("**/????*/*.ext", "/file/file.ext")
    fail("**/????*/*.ext", "/dir/file.ext")
    fail("**/????*/*.ext", "/dir/file.ext")

    fail("**/{*ab*,*ba*}/", "dir/0ab0/")
    fail("**/{*ab*,*ba*}/", "dir/ba/")
    fail("**/{*ab*,*ba*}/", "dir/0a0b0/")

    // Standard examples
    pass("**/*/*{.zip,.gz}", "dir/dir2/file.zip")
    pass("**/*/*{.zip,.gz}", "dir/dir2/file.gz")
    fail("**/*/*{.zip,.gz}", "dir2/file.gz")
    fail("**/*/*{.zip,.gz}", "dir/dir2/file.ext")
  }

  @Test def emptyGlob(): Unit = {
    pass("", "")
    fail("", "a")
    fail("a", "")
    pass("*", "")
  }

  /* Issue #2937
   * Glob itself should not match glob "*.sbt" with "./local.sbt".
   * Files.getNewDirectoryStream() must normalize candidate path before
   * handing it off to glob.
   */
  @Test def correctMatchingOfInitialDotSlash(): Unit = {
    pass("*.sbt", "local.sbt") // establish baseline
    pass("./*.sbt", "./local.sbt")
    fail("*.sbt", "./local.sbt") // glob "*" will not cross "/", so no match
  }
}
