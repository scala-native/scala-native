package scala.scalanative.misc

import regex._

object RegexSuite extends tests.Suite {

  val simpleText = "First second third"

  test("search words") {
    Scope { implicit in =>
      assert(Regex.search("""\S+""", simpleText))
    }
  }

  test("search many times") {
    Scope { implicit in =>
      val reusableRegex = Regex("""\w+""")

      assert(reusableRegex.search(simpleText))
      assert(reusableRegex.search("Quick brown fox"))
      assertNot(reusableRegex.search("  \t \n \r"))
      assert(reusableRegex.search("singleword"))
    }
  }

  test("search uft-8") {
    Scope { implicit in =>
      // UTF-8 support
      assert(Regex.search("рег.+ия", "регулярные выражения"))
      assert(Regex.search("게 개", "다르게 개발되었기"))
      assert(Regex.search("[一]", "文字列 の集合を一つの文字列で表現する方法の一つである。"))
      assert(Regex.search("个.*?个", "正则表达式使用单个字符串来描述、匹配一系列符合某个句法规则的 字符串 。"))
    }
  }

  test("search with result") {
    Scope { implicit in =>
      // reusable regex
      val reusableRegex = Regex("""\w+""")

      // .result is compound method which uses other methods under-the-hood hence it is slower than primitive methods,
      // but for test sake it is good enough
      val result1 = reusableRegex.searchWithResult(simpleText).result
      // instant regex
      val result2 = Regex.searchWithResult("""\w+""", simpleText).result

      assert(result1 == result2)
      assert(result1.prefix == "")
      assert(result1.suffix == " second third")
      assert(result1.groups(0).string == "First")
      assert(result1.groups(0).range.position == 0)
      assert(result1.groups(0).range.length == 5)
    }
  }

  test("matchAll") {
    Scope { implicit in =>
      assert(Regex.matchAll("""(\s*\w+\s*)+""", simpleText))
      assertNot(Regex.matchAll("""(\w+\s)+""", simpleText))
    }
  }

  test("matchAll with result") {
    Scope { implicit in =>
      // reusable regex
      val reusableRegex = Regex("""(\s*\w+\s*)+""")

      // .result is compound method which uses other methods under-the-hood hence it is slower than primitive methods,
      // but for test sake it is good enough
      val result1 = reusableRegex.matchAllWithResult(simpleText).result
      // instant regex
      val result2 =
        Regex.matchAllWithResult("""(\s*\w+\s*)+""", simpleText).result

      assert(result1 == result2)
      assert(result1.prefix == "")
      assert(result1.suffix == "")
      assert(result1.groups.length == 2)
      assert(result1.groups(0).string == simpleText) // zero group always shows the whole matched range
      assert(result1.groups(1).string == "third")
      assert(result1.groups(1).range.position == 13)
      assert(result1.groups(1).range.length == 5)
    }
  }

  test("iterator") {
    Scope { implicit in =>
      // reusable regex
      val reusableRegex = Regex("""\w+""")

      // .iterator calls internally searchWithResult each time on previous result (except first time),
      // it does calls in more optimal way at very low level,
      // but it's also compound method which uses `iteratorFirst` and `iteratorNext` under-the-hood
      val result1 = reusableRegex.iterator(simpleText)
      // instant regex
      val result2 = Regex.iterator("""\w+""", simpleText)

      assert(Match.compareArrays(result1, result2))

      assert(result1.length == 3)

      assert(result1(0).prefix == "")
      assert(result1(0).suffix == " second third") // next step will search in here
      assert(result1(0).groups.length == 1)
      assert(result1(0).groups(0).string == "First")
      assert(result1(0).groups(0).range.position == 0) // position is always relative to original text
      assert(result1(0).groups(0).range.length == 5)

      assert(result1(1).prefix == " ")
      assert(result1(1).suffix == " third") // next step will search in here
      assert(result1(1).groups.length == 1)
      assert(result1(1).groups(0).string == "second")
      assert(result1(1).groups(0).range.position == 6) // position is always relative to original text
      assert(result1(1).groups(0).range.length == 6)

      assert(result1(2).prefix == " ")
      assert(result1(2).suffix == "") // next step will search in here
      assert(result1(2).groups.length == 1)
      assert(result1(2).groups(0).string == "third")
      assert(result1(2).groups(0).range.position == 13) // position is always relative to original text
      assert(result1(2).groups(0).range.length == 5)

    }
  }

  test("token iterator") {
    Scope { implicit in =>
      // reusable regex
      val reusableRegex = Regex("""\w+""")

      // .tokenIterator calls internally searchWithResult each time on previous result (except first time),
      // it also does some kind of flatMap on `.iterator` to iterate on groups directly,
      // it does calls in more optimal way at very low level,
      // but it's also compound method which uses `tokenIteratorFirst` and `tokenIteratorNext` under-the-hood
      //
      // Array(0) here means iterate on zero group only (skip others),
      // You can specify multiple groups in array, for instance Array(-1, 0, 2, 99) will flatMap all these groups in one array
      // There is a trick like Array(-1) will iterate on prefix of every match, it's how `split` method works.
      val result1 = reusableRegex.tokenIterator(simpleText, Array(0))
      // instant regex
      val result2 = Regex.tokenIterator("""\w+""", simpleText, Array(0))

      assert(Match.compareArrays(result1, result2))

      assert(result1.length == 3)

      assert(result1(0).string == "First")
      assert(result1(0).range.position == 0)
      assert(result1(0).range.length == 5)

      assert(result1(1).string == "second")
      assert(result1(1).range.position == 6)
      assert(result1(1).range.length == 6)

      assert(result1(2).string == "third")
      assert(result1(2).range.position == 13)
      assert(result1(2).range.length == 5)
    }
  }

  test("replaceAll") {
    Scope { implicit in =>
      // $` means prefix
      // $& means match
      // $' means suffix
      // $0..9 means groups (also \0..\99)
      assert(
        Regex.replaceAll("""\w+""", simpleText, "[$&]") == "[First] [second] [third]")
      assert(
        Regex.replaceAll("""i|e|o""", simpleText, "*") == "F*rst s*c*nd th*rd")
      assert(
        Regex.replaceAll("""(\w+) & (\w+)""", "cpp & scala", "$2 & $1") == "scala & cpp")
    }
  }

  test("replaceFirst") {
    Scope { implicit in =>
      // $` means prefix
      // $& means match
      // $' means suffix
      // $0..9 means groups (also \0..\99)
      assert(
        Regex.replaceFirst("""sec\w+""", simpleText, "$`[$&]$'") == "First First [second] third third")
    }
  }

  test("split") {
    Scope { implicit in =>
      val result = Regex.split("""\s+""", simpleText)
      assert(result.length == 3)
      assert(result(0) == "First")
      assert(result(1) == "second")
      assert(result(2) == "third")
      assert(result.mkString("(", " :: ", ")") == "(First :: second :: third)")
    }
  }

  test("ignore case") {
    Scope { implicit in =>
      assertNot(Regex.search("""SECOND""", simpleText))
      assert(Regex.search("""SECOND""", simpleText, Regex.icase))
    }
  }

  test("format and back tracing") {
    Scope { implicit in =>
      // we are trying to parse html syntax, but we want only parse closed tags by using back tracing ability of regex,
      // here we use "\1" group inside regex which was already parsed and we compare if it's the same tag we're looking for,
      // we also use "Ignore case" because each tag could use different case.
      val result =
        Regex.searchWithResult("""<([a-zA-Z][a-zA-Z0-9]*)\b[^>]*>(.*?)</\1>""",
                               "<hTmL><b>Head</b><br></Html>",
                               Regex.icase);
      // here is one of useful methods of result is `format` which you can use to format result
      val info = result.format("tag: $1, body: $2").toLowerCase()
      assert(info == "tag: html, body: <b>head</b><br>")
    }
  }

  test("simple http url parser") {
    Scope { implicit in =>
      // group(0) whole URL
      // group(1) protocol HTTP/HTTPS
      // group(2) host
      // group(3) path on server and parameters
      val url_re = Regex(
        """(https?):\/\/((?:wwww\.)?[-a-zA-Z0-9@:%._\+~#=]{2,256}\.[a-z]{2,4}\b)([-a-zA-Z0-9@:%_\+.~#?&//=]*)""",
        Regex.optimize | // pre-compile and optimize expresion (slower creation but faster parsing)
          Regex.icase |  // ignore case
          Regex.no_unicode // ignore UTF-8 characters (dramatically speed-ups parsing)
      )
      assert(
        url_re
          .searchWithResult("""http://www.scala-native.org/en/latest/""")
          .result
          .groups(2)
          .string == "www.scala-native.org")
      assert(
        url_re
          .searchWithResult("""https://github.com/scala-native/scala-native""")
          .result
          .groups(2)
          .string == "github.com")
      val search_url =
        """https://www.google.com/search?q=scala+native&oq=scala+native"""
      // make sure we captured a whole url
      assert(
        url_re
          .searchWithResult(search_url)
          .result
          .groups(0)
          .string == search_url)
    }
  }

  test("simple ip address validator") {
    Scope { implicit in =>
      val url_re = Regex(
        """\b(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\b""",
        Regex.optimize | // pre-compile and optimize expresion (slower creation but faster parsing)
          Regex.icase |  // ignore case
          Regex.no_unicode // ignore UTF-8 characters (dramatically speed-ups parsing)
      )
      assertNot(url_re.search("""000.0000.00.00"""))
      assert(url_re.search("""192.168.1.1"""))
      assertNot(url_re.search("""912.456.123.123"""))
    }
  }

  test("an issue until we will start to use C++17") {
  import scala.scalanative.runtime.Platform
    Scope { implicit in =>
      val reusableRegex1 = Regex("""\w+""")
      val reusableRegex2 = Regex("""\S+""")
      val text           = "正则表达式"
      // on windows """\S+""" doesn't work with complex unicodes, but """\w+""" works fine
      if (Platform.isWindows)
      {
        assert(reusableRegex1.search(text))
        assertNot(reusableRegex2.search(text))
      }
      else // on posix that other way around
      {
        assertNot(reusableRegex1.search(text))
        assert(reusableRegex2.search(text))
      }
    }
  }

  test("setting up locale doesn't really work until c++17'") {
    Scope { implicit in =>
      val text =
        "symbol 'é' the regex [[:lower:]] imbued with a French locale should match the character"
      val re_str = "'[[:lower:]]'"

      // we will fail without locale settings (C means no locale, clean locale),
      // by default we use 'en-US' locale, for Grep syntax some one would prefer `POSIX` locale
      val re_c = Regex(re_str, Regex.no_unicode, Regex.ECMAScript, "C")
      assertNot(re_c.search(text))

      // we will even fail with locale settings which are by the way the last parameter of `apply` method,
      // because Scala-native represents all strings in utf8 by default,
      // but locale will be useful when you process raw data/stream etc
      val re = Regex(re_str, Regex.no_unicode, Regex.ECMAScript, "fr-FR")
      assertNot(re.search(text))
    }
  }
}
