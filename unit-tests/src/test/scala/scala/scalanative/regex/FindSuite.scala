package scala.scalanative
package regex

import java.util

import ScalaTestCompat.fail

object FindSuite extends tests.Suite {
  import FindSuiteHelper._

  // First the simple cases.

  test("FindUTF8") {
    for (test <- FIND_TESTS) {
      val re: RE2 = RE2.compile(test.pat)
      if (!(re.toString == test.pat)) {
        fail(
          "RE2.toString() = \"%s\"; should be \"%s\"".format(re.toString,
                                                             test.pat))
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
              test))
        }
      }
    }
  }

  test("Find") {
    for (test <- FIND_TESTS) {
      val result: String = RE2.compile(test.pat).find(test.text)
      if (test.matches.length == 0 && result.isEmpty) {} else if (test.matches.length == 0 && !(result.isEmpty)) {
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

  private def testFindIndexCommon(testName: String,
                                  test: Test,
                                  _result: Array[Int],
                                  resultIndicesAreUTF8: Boolean): Unit = {
    var result = _result
    if (test.matches.length == 0 && GoTestUtils.len(result) == 0) {} else if (test.matches.length == 0 && result != null) {
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
          "%s: expected %s got %s: %s".format(testName,
                                              util.Arrays.toString(expect),
                                              util.Arrays.toString(result),
                                              test))
      }
    }
  }

  test("FindUTF8Index") {
    for (test <- FIND_TESTS) {
      testFindIndexCommon("testFindUTF8Index",
                          test,
                          RE2.compile(test.pat).findUTF8Index(test.textUTF8),
                          true)
    }
  }

  test("FindIndex") {
    for (test <- FIND_TESTS) {
      val result: Array[Int] = RE2.compile(test.pat).findIndex(test.text)
      testFindIndexCommon("testFindIndex", test, result, false)
    }
  }

  // Now come the simple All cases.

  test("FindAllUTF8") {
    for (test <- FIND_TESTS) {
      val result = RE2.compile(test.pat).findAllUTF8(test.textUTF8, -1)
      if (test.matches.length == 0 && result == null) {} else if (test.matches.length == 0 && result != null) {
        fail("findAllUTF8: expected no match; got one: %s".format(test))
      } else if (test.matches.length > 0 && result == null) {
        throw new AssertionError(
          "findAllUTF8: expected match; got none: " + test)
      } else if (test.matches.length != result.size) {
        fail(
          "findAllUTF8: expected %d matches; got %d: %s"
            .format(test.matches.length, result.size, test))
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
              test))
        }
        i += 1
      }
    }
  }

  test("FindAll") {
    for (test <- FIND_TESTS) {
      val result = RE2.compile(test.pat).findAll(test.text, -1)
      if (test.matches.length == 0 && result == null) {} else if (test.matches.length == 0 && result != null) {
        fail("findAll: expected no match; got one: %s".format(test))
      } else if (test.matches.length > 0 && result == null) {
        fail("findAll: expected match; got none: %s".format(test))
      } else if (test.matches.length != result.size) {
        fail(
          "findAll: expected %d matches; got %d: %s"
            .format(test.matches.length, result.size, test))
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

  private def testFindAllIndexCommon(testName: String,
                                     test: Test,
                                     result: util.List[Array[Int]],
                                     resultIndicesAreUTF8: Boolean): Unit = {
    if (test.matches.length == 0 && result == null) {} else if (test.matches.length == 0 && result != null) {
      fail("%s: expected no match; got one: %s".format(testName, test))
    } else if (test.matches.length > 0 && result == null) {
      fail("%s: expected match; got none: %s".format(testName, test))
    } else if (test.matches.length != result.size) {
      fail(
        "%s: expected %d matches; got %d: %s"
          .format(testName, test.matches.length, result.size, test))
    }
    var k: Int = 0
    while (k < test.matches.length) {
      val e: Array[Int]   = test.matches(k)
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
            test))
      }
      k += 1
    }
  }

  test("FindAllUTF8Index") {
    for (test <- FIND_TESTS) {
      testFindAllIndexCommon(
        "testFindAllUTF8Index",
        test,
        RE2.compile(test.pat).findAllUTF8Index(test.textUTF8, -(1)),
        true)
    }
  }

  test("FindAllIndex") {
    for (test <- FIND_TESTS) {
      testFindAllIndexCommon(
        "testFindAllIndex",
        test,
        RE2.compile(test.pat).findAllIndex(test.text, -(1)),
        false)
    }
  }

  // Now come the Submatch cases.

  def testSubmatchBytes(testName: String,
                        test: Test,
                        n: Int,
                        result: Array[Array[Byte]]): Unit = {
    val submatches: Array[Int] = test.matches(n)
    if (submatches.length != GoTestUtils.len(result) * 2) {
      fail(
        "%s %d: expected %d submatches; got %d: %s".format(
          testName,
          n,
          submatches.length / 2,
          GoTestUtils.len(result),
          test))
    }
    var k: Int = 0
    while (k < GoTestUtils.len(result)) {
      var continue = false
      if (submatches(k * 2) == -(1)) {
        if (result(k) != null) {
          fail(
            "%s %d: expected null got %s: %s".format(testName, n, result, test))
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
              test))
        }
      }
      k += 1
    }
  }

  test("FindUTF8Submatch") {
    for (test <- FIND_TESTS) {
      val result: Array[Array[Byte]] =
        RE2.compile(test.pat).findUTF8Submatch(test.textUTF8)
      if (test.matches.length == 0 && result == null) {} else if (test.matches.length == 0 && result != null) {
        fail("expected no match; got one: %s".format(test))
      } else if (test.matches.length > 0 && result == null) {
        fail("expected match; got none: %s".format(test))
      } else {
        testSubmatchBytes("testFindUTF8Submatch", test, 0, result)
      }
    }
  }

  // (Go: testSubmatchString)
  private def testSubmatch(testName: String,
                           test: Test,
                           n: Int,
                           result: Array[String]): Unit = {
    val submatches: Array[Int] = test.matches(n)
    if (submatches.length != GoTestUtils.len(result) * 2) {
      fail(
        "%s %d: expected %d submatches; got %d: %s".format(
          testName,
          n,
          submatches.length / 2,
          GoTestUtils.len(result),
          test))
    }
    var k: Int = 0
    while (k < submatches.length) {
      var continue = false
      if (submatches(k) == -(1)) {
        if (result(k / 2) != null && !(result(k / 2).isEmpty)) {
          fail(
            "%s %d: expected null got %s: %s"
              .format(testName, n, result.mkString(", "), test))
        }
        continue = true
      }
      if (!continue) {
//        System.err.println(testName + "  " + test + " " + n + " " + k + " ")
        val expect: String = test.submatchString(n, k / 2)
        if (!(expect == result(k / 2))) {
          fail(
            "%s %d: expected %s got %s: %s"
              .format(testName, n, expect, result, test))
        }
      }
      k += 2
    }
  }

  // (Go: TestFindStringSubmatch)
  test("FindSubmatch") {
    for (test <- FIND_TESTS) {
      val result: Array[String] = RE2.compile(test.pat).findSubmatch(test.text)
      if (test.matches.length == 0 && result == null) {} else if (test.matches.length == 0 && result != null) {
        fail("expected no match; got one: %s".format(test))
      } else if (test.matches.length > 0 && result == null) {
        fail("expected match; got none: %s".format(test))
      } else {
        testSubmatch("testFindSubmatch", test, 0, result)
      }
    }
  }

  private def testSubmatchIndices(testName: String,
                                  test: Test,
                                  n: Int,
                                  _result: Array[Int],
                                  resultIndicesAreUTF8: Boolean): Unit = {
    var result             = _result
    val expect: Array[Int] = test.matches(n)
    if (expect.length != GoTestUtils.len(result)) {
      fail(
        "%s %d: expected %d matches; got %d: %s".format(
          testName,
          n,
          expect.length / 2,
          GoTestUtils.len(result) / 2,
          test))
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
            test))
      }
      k += 1
    }
  }

  private def testFindSubmatchIndexCommon(
      testName: String,
      test: Test,
      result: Array[Int],
      resultIndicesAreUTF8: Boolean): Unit = {
    if (test.matches.length == 0 && result == null) {} else if (test.matches.length == 0 && result != null) {
      fail("%s: expected no match; got one: %s".format(testName, test))
    } else if (test.matches.length > 0 && result == null) {
      fail("%s: expected match; got none: %s".format(testName, test))
    } else {
      testSubmatchIndices(testName, test, 0, result, resultIndicesAreUTF8)
    }
  }

  test("FindUTF8SubmatchIndex") {
    for (test <- FIND_TESTS) {
      testFindSubmatchIndexCommon(
        "testFindSubmatchIndex",
        test,
        RE2.compile(test.pat).findUTF8SubmatchIndex(test.textUTF8),
        true)
    }
  }

  // (Go: TestFindStringSubmatchIndex)
  test("FindSubmatchIndex") {
    for (test <- FIND_TESTS) {
      testFindSubmatchIndexCommon(
        "testFindStringSubmatchIndex",
        test,
        RE2.compile(test.pat).findSubmatchIndex(test.text),
        false)
    }
  }

  // Now come the monster AllSubmatch cases.

  // (Go: TestFindAllSubmatch)
  test("FindAllUTF8Submatch") {
    for (test <- FIND_TESTS) {
      val result =
        RE2.compile(test.pat).findAllUTF8Submatch(test.textUTF8, -1)
      if (test.matches.length == 0 && result == null) {} else {
        if (test.matches.length == 0 && result != null) {
          fail("expected no match; got one: %s".format(test))
        } else if (test.matches.length > 0 && result == null) {
          fail("expected match; got none: %s".format(test))
        } else if (test.matches.length != result.size) {
          fail(
            "expected %d matches; got %d: %s"
              .format(test.matches.length, result.size, test))
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
  test("FindAllSubmatch") {
    for (test <- FIND_TESTS) {
      val result =
        RE2.compile(test.pat).findAllSubmatch(test.text, -(1))
      if (test.matches.length == 0 && result == null) {} else {
        if (test.matches.length == 0 && result != null) {
          fail("expected no match; got one: %s".format(test))
        } else if (test.matches.length > 0 && result == null) {
          fail("expected match; got none: %s".format(test))
        } else if (test.matches.length != result.size) {
          fail(
            "expected %d matches; got %d: %s"
              .format(test.matches.length, result.size, test))
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
      test: Test,
      result: util.List[Array[Int]],
      resultIndicesAreUTF8: Boolean): Unit = {
    if (test.matches.length == 0 && result == null) {} else {
      if (test.matches.length == 0 && result != null) {
        fail("%s: expected no match; got one: %s".format(testName, test))
      } else if (test.matches.length > 0 && result == null) {
        fail("%s: expected match; got none: %s".format(testName, test))
      } else if (test.matches.length != result.size) {
        fail(
          "%s: expected %d matches; got %d: %s"
            .format(testName, test.matches.length, result.size, test))
      } else {
        var k: Int = 0
        while (k < test.matches.length) {
          testSubmatchIndices(testName,
                              test,
                              k,
                              result.get(k),
                              resultIndicesAreUTF8)
          k += 1
        }
      }
    }
  }

  // (Go: TestFindAllSubmatchIndex)
  test("FindAllUTF8SubmatchIndex") {
    for (test <- FIND_TESTS) {
      testFindAllSubmatchIndexCommon(
        "testFindAllUTF8SubmatchIndex",
        test,
        RE2.compile(test.pat).findAllUTF8SubmatchIndex(test.textUTF8, -(1)),
        true)
    }
  }

  // (Go: TestFindAllStringSubmatchIndex)
  test("FindAllSubmatchIndex") {
    for (test <- FIND_TESTS) {
      testFindAllSubmatchIndexCommon(
        "testFindAllSubmatchIndex",
        test,
        RE2.compile(test.pat).findAllSubmatchIndex(test.text, -(1)),
        false)
    }
  }

  // The find_test.go benchmarks are ported to Benchmarks.java.
}

object FindSuiteHelper {
  // For each pattern/text pair, what is the expected output of each
  // function?  We can derive the textual results from the indexed
  // results, the non-submatch results from the submatched results, the
  // single results from the 'all' results, and the String results from
  // the UTF-8 results. Therefore the table includes only the
  // findAllUTF8SubmatchIndex result.
  case class Test(pat: String, text: String, n: Int, _x: Int*) {
    val x: Array[Int] = _x.toArray
    // The n and x parameters construct a [][]int by extracting n
    // sequences from x.  This represents n matches with len(x)/n
    // submatches each.

    val textUTF8: Array[Byte] = GoTestUtils.utf8(text)
    // Each element is an even-length array of indices into textUTF8.  Not null.
    val matches = new Array[Array[Int]](n)
    if (n > 0) {
      val runLength = x.length / n
      var j         = 0
      var i         = 0
      while (i < n) {
        matches(i) = new Array[Int](runLength)
        System.arraycopy(x, j, matches(i), 0, runLength)
        j += runLength
        if (j > x.length) assert(false, "invalid build entry")
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
  val FIND_TESTS: Array[Test] = Array(
    Test("", "", 1, 0, 0),
    Test("^abcdefg", "abcdefg", 1, 0, 7),
    Test("a+", "baaab", 1, 1, 4),
    Test("abcd..", "abcdef", 1, 0, 6),
    Test("a", "a", 1, 0, 1),
    Test("x", "y", 0),
    Test("b", "abc", 1, 1, 2),
    Test(".", "a", 1, 0, 1),
    Test(".*", "abcdef", 1, 0, 6),
    Test("^", "abcde", 1, 0, 0),
    Test("$", "abcde", 1, 5, 5),
    Test("^abcd$", "abcd", 1, 0, 4),
    Test("^bcd'", "abcdef", 0),
    Test("^abcd$", "abcde", 0),
    Test("a+", "baaab", 1, 1, 4),
    Test("a*", "baaab", 3, 0, 0, 1, 4, 5, 5),
    Test("[a-z]+", "abcd", 1, 0, 4),
    Test("[^a-z]+", "ab1234cd", 1, 2, 6),
    Test("[a\\-\\]z]+", "az]-bcz", 2, 0, 4, 6, 7),
    Test("[^\\n]+", "abcd\n", 1, 0, 4),
    Test("[日本語]+", "日本語日本語", 1, 0, 18),
    Test("日本語+", "日本語", 1, 0, 9),
    Test("日本語+", "日本語語語語", 1, 0, 18),
    Test("()", "", 1, 0, 0, 0, 0),
    Test("(a)", "a", 1, 0, 1, 0, 1),
    Test("(.)(.)", "日a", 1, 0, 4, 0, 3, 3, 4),
    Test("(.*)", "", 1, 0, 0, 0, 0),
    Test("(.*)", "abcd", 1, 0, 4, 0, 4),
    Test("(..)(..)", "abcd", 1, 0, 4, 0, 2, 2, 4),
    Test("(([^xyz]*)(d))", "abcd", 1, 0, 4, 0, 4, 0, 3, 3, 4),
    Test("((a|b|c)*(d))", "abcd", 1, 0, 4, 0, 4, 2, 3, 3, 4),
    Test("(((a|b|c)*)(d))", "abcd", 1, 0, 4, 0, 4, 0, 3, 2, 3, 3, 4),
    Test("\\a\\f\\n\\r\\t\\v", "\u0007\f\n\r\t\u000b", 1, 0, 6),
    Test("[\\a\\f\\n\\r\\t\\v]+", "\u0007\f\n\r\t\u000b", 1, 0, 6),
    Test("a*(|(b))c*", "aacc", 1, 0, 4, 2, 2, -(1), -(1)),
    Test("(.*).*", "ab", 1, 0, 2, 0, 2),
    Test("[.]", ".", 1, 0, 1),
    Test("/$", "/abc/", 1, 4, 5),
    Test("/$", "/abc", 0), // multiple matches
    Test(".", "abc", 3, 0, 1, 1, 2, 2, 3),
    Test("(.)", "abc", 3, 0, 1, 0, 1, 1, 2, 1, 2, 2, 3, 2, 3),
    Test(".(.)", "abcd", 2, 0, 2, 1, 2, 2, 4, 3, 4),
    Test("ab*", "abbaab", 3, 0, 3, 3, 4, 4, 6),
    Test("a(b*)", "abbaab", 3, 0, 3, 1, 3, 3, 4, 4, 4, 4, 6, 5, 6), // fixed bugs
    Test("ab$", "cab", 1, 1, 3),
    Test("axxb$", "axxcb", 0),
    Test("data", "daXY data", 1, 5, 9),
    Test("da(.)a$", "daXY data", 1, 5, 9, 7, 8),
    Test("zx+", "zzx", 1, 1, 3),
    Test("ab$", "abcab", 1, 3, 5),
    Test("(aa)*$", "a", 1, 1, 1, -(1), -(1)),
    Test("(?:.|(?:.a))", "", 0),
    Test("(?:A(?:A|a))", "Aa", 1, 0, 2),
    Test("(?:A|(?:A|a))", "a", 1, 0, 1),
    Test("(a){0}", "", 1, 0, 0, -(1), -(1)),
    Test("(?-s)(?:(?:^).)", "\n", 0),
    Test("(?s)(?:(?:^).)", "\n", 1, 0, 1),
    Test("(?:(?:^).)", "\n", 0),
    Test("\\b", "x", 2, 0, 0, 1, 1),
    Test("\\b", "xx", 2, 0, 0, 2, 2),
    Test("\\b", "x y", 4, 0, 0, 1, 1, 2, 2, 3, 3),
    Test("\\b", "xx yy", 4, 0, 0, 2, 2, 3, 3, 5, 5),
    Test("\\B", "x", 0),
    Test("\\B", "xx", 1, 1, 1),
    Test("\\B", "x y", 0),
    Test("\\B", "xx yy", 2, 1, 1, 4, 4), // RE2 tests
    Test("[^\\S\\s]", "abcd", 0),
    Test("[^\\S[:space:]]", "abcd", 0),
    Test("[^\\D\\d]", "abcd", 0),
    Test("[^\\D[:digit:]]", "abcd", 0),
    Test("(?i)\\W", "x", 0),
    Test("(?i)\\W", "k", 0),
    Test("(?i)\\W", "s", 0), // can backslash-escape any punctuation
    Test(
      "\\!\\\"\\#\\$\\%\\&\\'\\(\\)\\*\\+\\,\\-\\.\\/\\:\\;\\<\\=\\>\\?\\@\\[\\\\\\]\\^\\_\\{\\|\\}\\~",
      "!\"#$%&'()*+,-./:;<=>?@[\\]^_{|}~",
      1,
      0,
      31),
    Test(
      "[\\!\\\"\\#\\$\\%\\&\\'\\(\\)\\*\\+\\,\\-\\.\\/\\:\\;\\<\\=\\>\\?\\@\\[\\\\\\]\\^\\_\\{\\|\\}\\~]+",
      "!\"#$%&'()*+,-./:;<=>?@[\\]^_{|}~",
      1,
      0,
      31),
    Test("\\`", "`", 1, 0, 1),
    Test("[\\`]+", "`", 1, 0, 1), // long set of matches
    Test(
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
