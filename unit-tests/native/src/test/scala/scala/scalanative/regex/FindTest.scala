package scala.scalanative
package regex

import java.util

import org.junit.Test
import org.junit.Assert.*

class FindTest {
  import FindTest.*

  // First the simple cases.

  @Test def findUTF8(): Unit = {
    for (test <- FIND_TESTS) {
      val re: RE2 = RE2.compile(test.pat)
      if (!(re.toString == test.pat)) {
        fail(
          "RE2.toString() = \"%s\"; should be \"%s\"".format(
            re.toString,
            test.pat
          )
        )
      }
      val result: Array[Byte] = re.findUTF8(test.textUTF8)
      if (test.matches.length == 0 && GoTestUtils.len(result) == 0) {
        // ok
      } else if (test.matches.length == 0 && result != null) {
        fail("findUTF8: expected no match; got one: %s".format(test))
      } else if (test.matches.length > 0 && result == null) {
        fail("findUTF8: expected match; got none: %s".format(test))
      } else {
        val expect: Array[Byte] = test.submatchBytes(0, 0)
        if (!util.Arrays.equals(expect, result)) {
          fail(
            "findUTF8: expected %s; got %s: %s".format(
              GoTestUtils.fromUTF8(expect),
              GoTestUtils.fromUTF8(result),
              test
            )
          )
        }
      }
    }
  }

  @Test def find(): Unit = {
    for (test <- FIND_TESTS) {
      val result: String = RE2.compile(test.pat).find(test.text)
      if (test.matches.length == 0 && result.isEmpty) {
        // no op
      } else if (test.matches.length == 0 && !(result.isEmpty)) {
        fail("find: expected no match; got one: %s".format(test))
      } else if (test.matches.length > 0 && result.isEmpty) {
        // Tricky because an empty result has two meanings:
        // no match or empty match.
        val `match`: Array[Int] = test.matches(0)
        if (`match`(0) != `match`(1)) {
          fail("find: expected match; got none: %s".format(test))
        }
      } else {
        val expect: String = test.submatchString(0, 0)
        if (!(expect == result)) {
          fail("find: expected %s got %s: %s".format(expect, result, test))
        }
      }
    }
  }

  private def testFindIndexCommon(
      testName: String,
      test: RETest,
      _result: Array[Int],
      resultIndicesAreUTF8: Boolean
  ): Unit = {
    var result = _result
    if (test.matches.length == 0 && GoTestUtils.len(result) == 0) {
      // no op
    } else if (test.matches.length == 0 && result != null) {
      fail("%s: expected no match; got one: %s".format(testName, test))
    } else if (test.matches.length > 0 && result == null) {
      fail("%s: expected match; got none: %s".format(testName, test))
    } else {
      if (!(resultIndicesAreUTF8)) {
        result = GoTestUtils.utf16IndicesToUtf8(result, test.text)
      }
      val expect: Array[Int] = test.matches(0) // UTF-8 indices
      if (expect(0) != result(0) || expect(1) != result(1)) {
        fail(
          "%s: expected %s got %s: %s".format(
            testName,
            util.Arrays.toString(expect),
            util.Arrays.toString(result),
            test
          )
        )
      }
    }
  }

  @Test def findUTF8Index(): Unit = {
    for (test <- FIND_TESTS) {
      testFindIndexCommon(
        "testFindUTF8Index",
        test,
        RE2.compile(test.pat).findUTF8Index(test.textUTF8),
        true
      )
    }
  }

  @Test def findIndex(): Unit = {
    for (test <- FIND_TESTS) {
      val result: Array[Int] = RE2.compile(test.pat).findIndex(test.text)
      testFindIndexCommon("testFindIndex", test, result, false)
    }
  }

  // Now come the simple All cases.

  @Test def findAllUTF8(): Unit = {
    for (test <- FIND_TESTS) {
      val result = RE2.compile(test.pat).findAllUTF8(test.textUTF8, -1)
      if (test.matches.length == 0 && result == null) {
        // no op
      } else if (test.matches.length == 0 && result != null) {
        fail("findAllUTF8: expected no match; got one: %s".format(test))
      } else if (test.matches.length > 0 && result == null) {
        throw new AssertionError(
          "findAllUTF8: expected match; got none: " + test
        )
      } else if (test.matches.length != result.size) {
        fail(
          "findAllUTF8: expected %d matches; got %d: %s"
            .format(test.matches.length, result.size, test)
        )
      }
      var i: Int = 0
      while (i < test.matches.length) {
        val expect: Array[Byte] = test.submatchBytes(i, 0)
        if (!util.Arrays.equals(expect, result.get(i))) {
          fail(
            "findAllUTF8: match %d: expected %s; got %s: %s".format(
              i / 2,
              GoTestUtils.fromUTF8(expect),
              GoTestUtils.fromUTF8(result.get(i)),
              test
            )
          )
        }
        i += 1
      }
    }
  }

  @Test def findAll(): Unit = {
    for (test <- FIND_TESTS) {
      val result = RE2.compile(test.pat).findAll(test.text, -1)
      if (test.matches.length == 0 && result == null) {
        // no op
      } else if (test.matches.length == 0 && result != null) {
        fail("findAll: expected no match; got one: %s".format(test))
      } else if (test.matches.length > 0 && result == null) {
        fail("findAll: expected match; got none: %s".format(test))
      } else if (test.matches.length != result.size) {
        fail(
          "findAll: expected %d matches; got %d: %s"
            .format(test.matches.length, result.size, test)
        )
      }
      var i: Int = 0
      while (i < test.matches.length) {
        val expect: String = test.submatchString(i, 0)
        if (!(expect == result.get(i))) {
          fail("findAll: expected %s; got %s: %s".format(expect, result, test))
        }
        i += 1
      }
    }
  }

  private def testFindAllIndexCommon(
      testName: String,
      test: RETest,
      result: util.List[Array[Int]],
      resultIndicesAreUTF8: Boolean
  ): Unit = {
    if (test.matches.length == 0 && result == null) {
      // no op
    } else if (test.matches.length == 0 && result != null) {
      fail("%s: expected no match; got one: %s".format(testName, test))
    } else if (test.matches.length > 0 && result == null) {
      fail("%s: expected match; got none: %s".format(testName, test))
    } else if (test.matches.length != result.size) {
      fail(
        "%s: expected %d matches; got %d: %s"
          .format(testName, test.matches.length, result.size, test)
      )
    }
    var k: Int = 0
    while (k < test.matches.length) {
      val e: Array[Int] = test.matches(k)
      var res: Array[Int] = result.get(k)
      if (!resultIndicesAreUTF8) {
        res = GoTestUtils.utf16IndicesToUtf8(res, test.text)
      }
      if (e(0) != res(0) || e(1) != res(1)) {
        fail(
          "%s: match %d: expected %s; got %s: %s".format(
            testName,
            k,
            util.Arrays.toString(e), // (only 1st two elements matter here)
            util.Arrays.toString(res),
            test
          )
        )
      }
      k += 1
    }
  }

  @Test def findAllUTF8Index(): Unit = {
    for (test <- FIND_TESTS) {
      testFindAllIndexCommon(
        "testFindAllUTF8Index",
        test,
        RE2.compile(test.pat).findAllUTF8Index(test.textUTF8, -(1)),
        true
      )
    }
  }

  @Test def findAllIndex(): Unit = {
    for (test <- FIND_TESTS) {
      testFindAllIndexCommon(
        "testFindAllIndex",
        test,
        RE2.compile(test.pat).findAllIndex(test.text, -(1)),
        false
      )
    }
  }

  // Now come the Submatch cases.

  def testSubmatchBytes(
      testName: String,
      test: RETest,
      n: Int,
      result: Array[Array[Byte]]
  ): Unit = {
    val submatches: Array[Int] = test.matches(n)
    if (submatches.length != GoTestUtils.len(result) * 2) {
      fail(
        "%s %d: expected %d submatches; got %d: %s".format(
          testName,
          n,
          submatches.length / 2,
          GoTestUtils.len(result),
          test
        )
      )
    }
    var k: Int = 0
    while (k < GoTestUtils.len(result)) {
      var continue = false
      if (submatches(k * 2) == -(1)) {
        if (result(k) != null) {
          fail(
            "%s %d: expected null got %s: %s".format(testName, n, result, test)
          )
        }
        continue = true
      }
      if (!continue) {
        val expect: Array[Byte] = test.submatchBytes(n, k)
        if (!util.Arrays.equals(expect, result(k))) {
          fail(
            "%s %d: expected %s; got %s: %s".format(
              testName,
              n,
              GoTestUtils.fromUTF8(expect),
              GoTestUtils.fromUTF8(result(k)),
              test
            )
          )
        }
      }
      k += 1
    }
  }

  @Test def findUTF8Submatch(): Unit = {
    for (test <- FIND_TESTS) {
      val result: Array[Array[Byte]] =
        RE2.compile(test.pat).findUTF8Submatch(test.textUTF8)
      if (test.matches.length == 0 && result == null) {
        // no op
      } else if (test.matches.length == 0 && result != null) {
        fail("expected no match; got one: %s".format(test))
      } else if (test.matches.length > 0 && result == null) {
        fail("expected match; got none: %s".format(test))
      } else {
        testSubmatchBytes("testFindUTF8Submatch", test, 0, result)
      }
    }
  }

  // (Go: testSubmatchString)
  private def testSubmatch(
      testName: String,
      test: RETest,
      n: Int,
      result: Array[String]
  ): Unit = {
    val submatches: Array[Int] = test.matches(n)
    if (submatches.length != GoTestUtils.len(result) * 2) {
      fail(
        "%s %d: expected %d submatches; got %d: %s".format(
          testName,
          n,
          submatches.length / 2,
          GoTestUtils.len(result),
          test
        )
      )
    }
    var k: Int = 0
    while (k < submatches.length) {
      var continue = false
      if (submatches(k) == -(1)) {
        if (result(k / 2) != null && !(result(k / 2).isEmpty)) {
          fail(
            "%s %d: expected null got %s: %s"
              .format(testName, n, result.mkString(", "), test)
          )
        }
        continue = true
      }
      if (!continue) {
//        System.err.println(testName + "  " + test + " " + n + " " + k + " ")
        val expect: String = test.submatchString(n, k / 2)
        if (!(expect == result(k / 2))) {
          fail(
            "%s %d: expected %s got %s: %s"
              .format(testName, n, expect, result, test)
          )
        }
      }
      k += 2
    }
  }

  // (Go: TestFindStringSubmatch)
  @Test def findSubmatch(): Unit = {
    for (test <- FIND_TESTS) {
      val result: Array[String] = RE2.compile(test.pat).findSubmatch(test.text)
      if (test.matches.length == 0 && result == null) {
        // no op
      } else if (test.matches.length == 0 && result != null) {
        fail("expected no match; got one: %s".format(test))
      } else if (test.matches.length > 0 && result == null) {
        fail("expected match; got none: %s".format(test))
      } else {
        testSubmatch("testFindSubmatch", test, 0, result)
      }
    }
  }

  private def testSubmatchIndices(
      testName: String,
      test: RETest,
      n: Int,
      _result: Array[Int],
      resultIndicesAreUTF8: Boolean
  ): Unit = {
    var result = _result
    val expect: Array[Int] = test.matches(n)
    if (expect.length != GoTestUtils.len(result)) {
      fail(
        "%s %d: expected %d matches; got %d: %s".format(
          testName,
          n,
          expect.length / 2,
          GoTestUtils.len(result) / 2,
          test
        )
      )
      return
    }
    if (!resultIndicesAreUTF8) {
      result = GoTestUtils.utf16IndicesToUtf8(result, test.text)
    }
    var k: Int = 0
    while (k < expect.length) {
      if (expect(k) != result(k)) {
        fail(
          "%s %d: submatch error: expected %s got %s: %s".format(
            testName,
            n,
            util.Arrays.toString(expect),
            util.Arrays.toString(result),
            test
          )
        )
      }
      k += 1
    }
  }

  private def testFindSubmatchIndexCommon(
      testName: String,
      test: RETest,
      result: Array[Int],
      resultIndicesAreUTF8: Boolean
  ): Unit = {
    if (test.matches.length == 0 && result == null) {
      // no op
    } else if (test.matches.length == 0 && result != null) {
      fail("%s: expected no match; got one: %s".format(testName, test))
    } else if (test.matches.length > 0 && result == null) {
      fail("%s: expected match; got none: %s".format(testName, test))
    } else {
      testSubmatchIndices(testName, test, 0, result, resultIndicesAreUTF8)
    }
  }

  @Test def findUTF8SubmatchIndex(): Unit = {
    for (test <- FIND_TESTS) {
      testFindSubmatchIndexCommon(
        "testFindSubmatchIndex",
        test,
        RE2.compile(test.pat).findUTF8SubmatchIndex(test.textUTF8),
        true
      )
    }
  }

  // (Go: TestFindStringSubmatchIndex)
  @Test def findSubmatchIndex(): Unit = {
    for (test <- FIND_TESTS) {
      testFindSubmatchIndexCommon(
        "testFindStringSubmatchIndex",
        test,
        RE2.compile(test.pat).findSubmatchIndex(test.text),
        false
      )
    }
  }

  // Now come the monster AllSubmatch cases.

  // (Go: TestFindAllSubmatch)
  @Test def findAllUTF8Submatch(): Unit = {
    for (test <- FIND_TESTS) {
      val result =
        RE2.compile(test.pat).findAllUTF8Submatch(test.textUTF8, -1)
      if (test.matches.length == 0 && result == null) {}
      else {
        if (test.matches.length == 0 && result != null) {
          fail("expected no match; got one: %s".format(test))
        } else if (test.matches.length > 0 && result == null) {
          fail("expected match; got none: %s".format(test))
        } else if (test.matches.length != result.size) {
          fail(
            "expected %d matches; got %d: %s"
              .format(test.matches.length, result.size, test)
          )
        } else {
          var k: Int = 0
          while (k < test.matches.length) {
            testSubmatchBytes("testFindAllSubmatch", test, k, result.get(k))
            k += 1
          }
        }
      }
    }
  }
  // (Go: TestFindAllStringSubmatch)
  @Test def findAllSubmatch(): Unit = {
    for (test <- FIND_TESTS) {
      val result =
        RE2.compile(test.pat).findAllSubmatch(test.text, -(1))
      if (test.matches.length == 0 && result == null) {}
      else {
        if (test.matches.length == 0 && result != null) {
          fail("expected no match; got one: %s".format(test))
        } else if (test.matches.length > 0 && result == null) {
          fail("expected match; got none: %s".format(test))
        } else if (test.matches.length != result.size) {
          fail(
            "expected %d matches; got %d: %s"
              .format(test.matches.length, result.size, test)
          )
        } else {
          var k: Int = 0
          while (k < test.matches.length) {
            testSubmatch("testFindAllStringSubmatch", test, k, result.get(k))
            k += 1
          }
        }
      }
    }
  }

  // (Go: testFindSubmatchIndex)
  private def testFindAllSubmatchIndexCommon(
      testName: String,
      test: RETest,
      result: util.List[Array[Int]],
      resultIndicesAreUTF8: Boolean
  ): Unit = {
    if (test.matches.length == 0 && result == null) {
      // no op
    } else {
      if (test.matches.length == 0 && result != null) {
        fail("%s: expected no match; got one: %s".format(testName, test))
      } else if (test.matches.length > 0 && result == null) {
        fail("%s: expected match; got none: %s".format(testName, test))
      } else if (test.matches.length != result.size) {
        fail(
          "%s: expected %d matches; got %d: %s"
            .format(testName, test.matches.length, result.size, test)
        )
      } else {
        var k: Int = 0
        while (k < test.matches.length) {
          testSubmatchIndices(
            testName,
            test,
            k,
            result.get(k),
            resultIndicesAreUTF8
          )
          k += 1
        }
      }
    }
  }

  // (Go: TestFindAllSubmatchIndex)
  @Test def findAllUTF8SubmatchIndex(): Unit = {
    for (test <- FIND_TESTS) {
      testFindAllSubmatchIndexCommon(
        "testFindAllUTF8SubmatchIndex",
        test,
        RE2.compile(test.pat).findAllUTF8SubmatchIndex(test.textUTF8, -(1)),
        true
      )
    }
  }

  // (Go: TestFindAllStringSubmatchIndex)
  @Test def findAllSubmatchIndex(): Unit = {
    for (test <- FIND_TESTS) {
      testFindAllSubmatchIndexCommon(
        "testFindAllSubmatchIndex",
        test,
        RE2.compile(test.pat).findAllSubmatchIndex(test.text, -(1)),
        false
      )
    }
  }

  // The find_test.go benchmarks are ported to Benchmarks.java.
}

object FindTest {
  // For each pattern/text pair, what is the expected output of each
  // function?  We can derive the textual results from the indexed
  // results, the non-submatch results from the submatched results, the
  // single results from the 'all' results, and the String results from
  // the UTF-8 results. Therefore the table includes only the
  // findAllUTF8SubmatchIndex result.
  case class RETest(pat: String, text: String, n: Int, _x: Int*) {
    val x: Array[Int] = _x.toArray
    // The n and x parameters construct a [][]int by extracting n
    // sequences from x.  This represents n matches with len(x)/n
    // submatches each.

    val textUTF8: Array[Byte] = GoTestUtils.utf8(text)
    // Each element is an even-length array of indices into textUTF8.  Not null.
    val matches = new Array[Array[Int]](n)
    if (n > 0) {
      val runLength = x.length / n
      var j = 0
      var i = 0
      while (i < n) {
        matches(i) = new Array[Int](runLength)
        System.arraycopy(x, j, matches(i), 0, runLength)
        j += runLength
        if (j > x.length) assertTrue("invalid build entry", false)
        i += 1
      }
    }

    def submatchBytes(i: Int, j: Int) =
      Utils.subarray(textUTF8, matches(i)(2 * j), matches(i)(2 * j + 1))

    def submatchString(i: Int, j: Int) =
      GoTestUtils.fromUTF8(submatchBytes(i, j)) // yikes

    override def toString: String = "pat=%s text=%s".format(pat, text)
  }
  // Used by RE2Test also.
  val FIND_TESTS: Array[RETest] = Array(
    RETest("", "", 1, 0, 0),
    RETest("^abcdefg", "abcdefg", 1, 0, 7),
    RETest("a+", "baaab", 1, 1, 4),
    RETest("abcd..", "abcdef", 1, 0, 6),
    RETest("a", "a", 1, 0, 1),
    RETest("x", "y", 0),
    RETest("b", "abc", 1, 1, 2),
    RETest(".", "a", 1, 0, 1),
    RETest(".*", "abcdef", 1, 0, 6),
    RETest("^", "abcde", 1, 0, 0),
    RETest("$", "abcde", 1, 5, 5),
    RETest("^abcd$", "abcd", 1, 0, 4),
    RETest("^bcd'", "abcdef", 0),
    RETest("^abcd$", "abcde", 0),
    RETest("a+", "baaab", 1, 1, 4),
    RETest("a*", "baaab", 3, 0, 0, 1, 4, 5, 5),
    RETest("[a-z]+", "abcd", 1, 0, 4),
    RETest("[^a-z]+", "ab1234cd", 1, 2, 6),
    RETest("[a\\-\\]z]+", "az]-bcz", 2, 0, 4, 6, 7),
    RETest("[^\\n]+", "abcd\n", 1, 0, 4),
    RETest("[日本語]+", "日本語日本語", 1, 0, 18),
    RETest("日本語+", "日本語", 1, 0, 9),
    RETest("日本語+", "日本語語語語", 1, 0, 18),
    RETest("()", "", 1, 0, 0, 0, 0),
    RETest("(a)", "a", 1, 0, 1, 0, 1),
    RETest("(.)(.)", "日a", 1, 0, 4, 0, 3, 3, 4),
    RETest("(.*)", "", 1, 0, 0, 0, 0),
    RETest("(.*)", "abcd", 1, 0, 4, 0, 4),
    RETest("(..)(..)", "abcd", 1, 0, 4, 0, 2, 2, 4),
    RETest("(([^xyz]*)(d))", "abcd", 1, 0, 4, 0, 4, 0, 3, 3, 4),
    RETest("((a|b|c)*(d))", "abcd", 1, 0, 4, 0, 4, 2, 3, 3, 4),
    RETest("(((a|b|c)*)(d))", "abcd", 1, 0, 4, 0, 4, 0, 3, 2, 3, 3, 4),
    RETest("\\a\\f\\n\\r\\t\\v", "\u0007\f\n\r\t\u000b", 1, 0, 6),
    RETest("[\\a\\f\\n\\r\\t\\v]+", "\u0007\f\n\r\t\u000b", 1, 0, 6),
    RETest("a*(|(b))c*", "aacc", 1, 0, 4, 2, 2, -(1), -(1)),
    RETest("(.*).*", "ab", 1, 0, 2, 0, 2),
    RETest("[.]", ".", 1, 0, 1),
    RETest("/$", "/abc/", 1, 4, 5),
    RETest("/$", "/abc", 0), // multiple matches
    RETest(".", "abc", 3, 0, 1, 1, 2, 2, 3),
    RETest("(.)", "abc", 3, 0, 1, 0, 1, 1, 2, 1, 2, 2, 3, 2, 3),
    RETest(".(.)", "abcd", 2, 0, 2, 1, 2, 2, 4, 3, 4),
    RETest("ab*", "abbaab", 3, 0, 3, 3, 4, 4, 6),
    // fixed bugs
    RETest("a(b*)", "abbaab", 3, 0, 3, 1, 3, 3, 4, 4, 4, 4, 6, 5, 6),
    RETest("ab$", "cab", 1, 1, 3),
    RETest("axxb$", "axxcb", 0),
    RETest("data", "daXY data", 1, 5, 9),
    RETest("da(.)a$", "daXY data", 1, 5, 9, 7, 8),
    RETest("zx+", "zzx", 1, 1, 3),
    RETest("ab$", "abcab", 1, 3, 5),
    RETest("(aa)*$", "a", 1, 1, 1, -(1), -(1)),
    RETest("(?:.|(?:.a))", "", 0),
    RETest("(?:A(?:A|a))", "Aa", 1, 0, 2),
    RETest("(?:A|(?:A|a))", "a", 1, 0, 1),
    RETest("(a){0}", "", 1, 0, 0, -(1), -(1)),
    RETest("(?-s)(?:(?:^).)", "\n", 0),
    RETest("(?s)(?:(?:^).)", "\n", 1, 0, 1),
    RETest("(?:(?:^).)", "\n", 0),
    RETest("\\b", "x", 2, 0, 0, 1, 1),
    RETest("\\b", "xx", 2, 0, 0, 2, 2),
    RETest("\\b", "x y", 4, 0, 0, 1, 1, 2, 2, 3, 3),
    RETest("\\b", "xx yy", 4, 0, 0, 2, 2, 3, 3, 5, 5),
    RETest("\\B", "x", 0),
    RETest("\\B", "xx", 1, 1, 1),
    RETest("\\B", "x y", 0),
    RETest("\\B", "xx yy", 2, 1, 1, 4, 4), // RE2 tests
    RETest("[^\\S\\s]", "abcd", 0),
    RETest("[^\\S[:space:]]", "abcd", 0),
    RETest("[^\\D\\d]", "abcd", 0),
    RETest("[^\\D[:digit:]]", "abcd", 0),
    RETest("(?i)\\W", "x", 0),
    RETest("(?i)\\W", "k", 0),
    RETest("(?i)\\W", "s", 0), // can backslash-escape any punctuation
    RETest(
      "\\!\\\"\\#\\$\\%\\&\\'\\(\\)\\*\\+\\,\\-\\.\\/\\:\\;\\<\\=\\>\\?\\@\\[\\\\\\]\\^\\_\\{\\|\\}\\~",
      "!\"#$%&'()*+,-./:;<=>?@[\\]^_{|}~",
      1,
      0,
      31
    ),
    RETest(
      "[\\!\\\"\\#\\$\\%\\&\\'\\(\\)\\*\\+\\,\\-\\.\\/\\:\\;\\<\\=\\>\\?\\@\\[\\\\\\]\\^\\_\\{\\|\\}\\~]+",
      "!\"#$%&'()*+,-./:;<=>?@[\\]^_{|}~",
      1,
      0,
      31
    ),
    RETest("\\`", "`", 1, 0, 1),
    RETest("[\\`]+", "`", 1, 0, 1), // long set of matches
    RETest(
      ".",
      "qwertyuiopasdfghjklzxcvbnm1234567890",
      36,
      0,
      1,
      1,
      2,
      2,
      3,
      3,
      4,
      4,
      5,
      5,
      6,
      6,
      7,
      7,
      8,
      8,
      9,
      9,
      10,
      10,
      11,
      11,
      12,
      12,
      13,
      13,
      14,
      14,
      15,
      15,
      16,
      16,
      17,
      17,
      18,
      18,
      19,
      19,
      20,
      20,
      21,
      21,
      22,
      22,
      23,
      23,
      24,
      24,
      25,
      25,
      26,
      26,
      27,
      27,
      28,
      28,
      29,
      29,
      30,
      30,
      31,
      31,
      32,
      32,
      33,
      33,
      34,
      34,
      35,
      35,
      36
    )
  )
}
