package scala.scalanative.misc
package regex

import scala.scalanative.native._

sealed trait Regex {
  def search(text: String, flags: Match.Flags = Match.default): Boolean
  def searchWithResult(text: String, matchFlags: Match.Flags = Match.default)(
      implicit in: Scope): Match
  def matchAll(text: String, matchFlags: Match.Flags = Match.default): Boolean
  def matchAllWithResult(
      text: String,
      matchFlags: Match.Flags = Match.default)(implicit in: Scope): Match
  def iteratorFirst(text: String, matchFlags: Match.Flags = Match.default)(
      implicit in: Scope): Match
  def tokenIteratorFirst(
      text: String,
      tokens: Array[Int],
      matchFlags: Match.Flags = Match.default)(implicit in: Scope): Match
  def replaceAll(text: String,
                 format: String,
                 formatFlags: Match.FormatFlags = Match.format_default,
                 matchFlags: Match.Flags = Match.default): String
  def replaceFirst(text: String,
                   format: String,
                   formatFlags: Match.FormatFlags = Match.format_default,
                   matchFlags: Match.Flags = Match.default): String
  def split(text: String, matchFlags: Match.Flags = Match.default)(
      implicit in: Scope): Array[String]
  def iterator(text: String, matchFlags: Match.Flags = Match.default)(
      implicit in: Scope): Array[Match.MatchResult]
  def tokenIterator(text: String,
                    tokens: Array[Int],
                    matchFlags: Match.Flags = Match.default)(
      implicit in: Scope): Array[Match.Group]
}

object Regex {
  // Flags
  type Flags = CUnsignedInt

  // default
  val default: Flags = 0x0000.toUInt

  // "Ignore case": Character matching should be performed without regard to case.
  val icase: Flags = 0x0100.toUInt

  // "No substitutions": When performing matches, all marked sub-expressions (expr) are treated as non-marking sub-expressions (?:expr). No matches are stored in the supplied RegexMatch structure and markCount() is zero
  val nosubs: Flags = 0x0200.toUInt

  // "Optimize for multiple uses": Instructs the regular expression engine to make matching faster, with the potential cost of making construction slower. For example, this might mean converting a non-deterministic FSA to a deterministic FSA.
  val optimize: Flags = 0x0400.toUInt

  // "Locale sensetive": Character ranges of the form "[a-b]" will be locale sensitive.
  val collate: Flags = 0x0800.toUInt

  // "No unicode": Ignore UTF-8 (faster processing)
  val no_unicode: Flags = 0x8000.toUInt

  // Syntax
  sealed trait Syntax {
    def f: Flags = ???
  }

  // Similar to JavaScript (default)
  // (differences from JavaScript: http://en.cppreference.com/w/cpp/regex/ecmascript)
  final object ECMAScript extends Syntax {
    override val f: Flags = 0x01.toUInt
  }

  // Similar to POSIX BRE
  final object Basic extends Syntax { override val f: Flags = 0x02.toUInt }

  // Similar to POSIX ERE
  final object Extended extends Syntax { override val f: Flags = 0x04.toUInt }

  // Same as Extended, with the addition of supporting common escapes for non-printable characters.
  final object Awk extends Syntax { override val f: Flags = 0x08.toUInt }

  // Same as Basic, with the addition of treating line feeds as alternation operators.
  final object Grep extends Syntax { override val f: Flags = 0x10.toUInt }

  // Same as Extended, with the addition of treating line feeds as alternation operators.
  final object Egrep extends Syntax { override val f: Flags = 0x20.toUInt }

  // default locale
  val locDefault: String = "en-US"

  def apply(pattern: String,
            flags: Flags = default,
            syntax: Syntax = ECMAScript,
            locale: String = null)(implicit in: Scope): Regex = Zone {
    implicit z =>
      val cre: CRegex.CRegexStruct =
        createRegexInternal(pattern, flags, syntax, locale)
      val r = new RegexImpl(pattern, cre)
      in.acquire(r)
  }

  def search(pattern: String,
             text: String,
             flags: Flags = default,
             matchFlags: Match.Flags = Match.default): Boolean = Zone {
    implicit z =>
      val cre    = createRegexInternal(pattern, flags, ECMAScript, null)
      val result = CRegex.search(cre, toCString(text), matchFlags)
      CRegex.delete(cre)
      result
  }

  def searchWithResult(
      pattern: String,
      text: String,
      flags: Flags = default,
      matchFlags: Match.Flags = Match.default)(implicit in: Scope): Match =
    Zone { implicit z =>
      val cre = createRegexInternal(pattern, flags, ECMAScript, null)
      val result =
        CRegex.searchWithResult(cre, toCString(text), matchFlags, null)
      val matchResult = Match(result)
      CRegex.delete(cre)
      matchResult
    }

  def matchAll(pattern: String,
               text: String,
               flags: Flags = default,
               matchFlags: Match.Flags = Match.default): Boolean = Zone {
    implicit z =>
      val cre    = createRegexInternal(pattern, flags, ECMAScript, null)
      val result = CRegex.matchAll(cre, toCString(text), matchFlags)
      CRegex.delete(cre)
      result
  }

  def matchAllWithResult(
      pattern: String,
      text: String,
      flags: Flags = default,
      matchFlags: Match.Flags = Match.default)(implicit in: Scope): Match =
    Zone { implicit z =>
      val cre = createRegexInternal(pattern, flags, ECMAScript, null)
      val result =
        CRegex.matchAllWithResult(cre, toCString(text), matchFlags, null)
      val matchResult = Match(result)
      CRegex.delete(cre)
      matchResult
    }

  def iteratorFirst(
      pattern: String,
      text: String,
      flags: Flags = default,
      matchFlags: Match.Flags = Match.default)(implicit in: Scope): Match =
    Zone { implicit z =>
      val cre = createRegexInternal(pattern, flags, ECMAScript, null)
      val result =
        CRegex.matchIteratorFirst(cre, toCString(text), matchFlags, null)
      val matchResult = Match(result)
      CRegex.delete(cre)
      matchResult
    }

  def tokenIteratorFirst(
      pattern: String,
      text: String,
      tokens: Array[Int],
      flags: Flags = default,
      matchFlags: Match.Flags = Match.default)(implicit in: Scope): Match =
    Zone { implicit z =>
      val ctokens = stackalloc[CInt](100)
      var idx     = tokens.length
      (0 until idx).foreach(i => {
        ctokens(i) = tokens(i)
      })
      val cre = createRegexInternal(pattern, flags, ECMAScript, null)
      val result = CRegex.matchTokenIteratorFirst(cre,
                                                  toCString(text),
                                                  idx,
                                                  ctokens,
                                                  matchFlags,
                                                  null)
      val matchResult = Match(result)
      CRegex.delete(cre)
      matchResult
    }

  def replaceAll(pattern: String,
                 text: String,
                 format: String,
                 formatFlags: Match.FormatFlags = Match.format_default,
                 flags: Flags = default,
                 matchFlags: Match.Flags = Match.default): String = Zone {
    implicit z =>
      val result      = stackalloc[CString]
      val result_size = stackalloc[CInt]
      val cre         = createRegexInternal(pattern, flags, ECMAScript, null)
      val success = CRegex.replaceAll(cre,
                                      toCString(text),
                                      toCString(format),
                                      matchFlags | formatFlags,
                                      result,
                                      result_size)
      val resultString =
        if (!result_size > 0 && success) fromCString(!result) else text
      CRegex.delete(cre)
      resultString
  }

  def replaceFirst(pattern: String,
                   text: String,
                   format: String,
                   formatFlags: Match.FormatFlags = Match.format_default,
                   flags: Flags = default,
                   matchFlags: Match.Flags = Match.default): String = {
    replaceAll(pattern,
               text,
               format,
               formatFlags | Match.format_first_only,
               flags,
               matchFlags)
  }

  def split(pattern: String,
            text: String,
            flags: Flags = default,
            matchFlags: Match.Flags = Match.default)(
      implicit in: Scope): Array[String] = Scope { implicit in =>
    import scala.annotation.tailrec

    @tailrec def innerLoop(matchResult: Match, result: Array[String] = Array.empty[String])
      : Array[String] = {
      if (matchResult.tokenIteratorNext) {
        innerLoop(matchResult, result :+ matchResult.tokenString)
      } else {
        result
      }
    }
    val matchResult =
      tokenIteratorFirst(pattern, text, Array(-1), flags, matchFlags)
    innerLoop(matchResult)
  }

  def iterator(pattern: String,
               text: String,
               flags: Flags = default,
               matchFlags: Match.Flags = Match.default)(
      implicit in: Scope): Array[Match.MatchResult] = Scope { implicit in =>
    import scala.annotation.tailrec

    def fill(matchResult: Match): Match.MatchResult = {
      @tailrec def makeGroups(matchResult: Match, i: Int = 0, result: Array[Match.Group] = Array.empty[Match.Group])
        : Array[Match.Group] = {
        if (i < matchResult.groupCount) {
          makeGroups(matchResult,
                     i + 1,
                     result :+ new Match.Group(matchResult.groupString(i),
                                               matchResult.groupRange(i)))
        } else {
          result
        }
      }
      new Match.MatchResult(matchResult.prefix,
                            makeGroups(matchResult),
                            matchResult.suffix)
    }

    @tailrec
    def innerLoop(matchResult: Match,
                  result: Array[Match.MatchResult] = Array
                    .empty[Match.MatchResult]): Array[Match.MatchResult] = {
      if (matchResult.iteratorNext) {
        innerLoop(matchResult, result :+ fill(matchResult))
      } else {
        result
      }
    }
    val matchResult = iteratorFirst(pattern, text, flags, matchFlags)
    innerLoop(matchResult)
  }

  def tokenIterator(pattern: String,
                    text: String,
                    tokens: Array[Int],
                    flags: Flags = default,
                    matchFlags: Match.Flags = Match.default)(
      implicit in: Scope): Array[Match.Group] = Scope { implicit in =>
    import scala.annotation.tailrec

    @tailrec def innerLoop(matchResult: Match, result: Array[Match.Group] = Array.empty[Match.Group])
      : Array[Match.Group] = {
      if (matchResult.tokenIteratorNext) {
        innerLoop(matchResult,
                  result :+ new Match.Group(matchResult.tokenString,
                                            matchResult.tokenRange))
      } else {
        result
      }
    }
    val matchResult =
      tokenIteratorFirst(pattern, text, tokens, flags, matchFlags)
    innerLoop(matchResult)
  }

  // private section

  private[regex] class RegexImpl(
      val pattern: String,
      var cre: CRegex.CRegexStruct)(implicit in: Scope)
      extends Regex
      with Resource {

    def search(text: String,
               matchFlags: Match.Flags = Match.default): Boolean = Zone {
      implicit z =>
        CRegex.search(cre, toCString(text), matchFlags)
    }

    def searchWithResult(
        text: String,
        matchFlags: Match.Flags = Match.default)(implicit in: Scope): Match =
      Zone { implicit z =>
        val result =
          CRegex.searchWithResult(cre, toCString(text), matchFlags, null)
        val matchResult = Match(result)
        matchResult
      }

    def matchAll(text: String,
                 matchFlags: Match.Flags = Match.default): Boolean = Zone {
      implicit z =>
        val result = CRegex.matchAll(cre, toCString(text), matchFlags)
        result
    }

    def matchAllWithResult(
        text: String,
        matchFlags: Match.Flags = Match.default)(implicit in: Scope): Match =
      Zone { implicit z =>
        val result =
          CRegex.matchAllWithResult(cre, toCString(text), matchFlags, null)
        val matchResult = Match(result)
        matchResult
      }

    def iteratorFirst(text: String, matchFlags: Match.Flags = Match.default)(
        implicit in: Scope): Match = Zone { implicit z =>
      val result =
        CRegex.matchIteratorFirst(cre, toCString(text), matchFlags, null)
      val matchResult = Match(result)
      matchResult
    }

    def tokenIteratorFirst(
        text: String,
        tokens: Array[Int],
        matchFlags: Match.Flags = Match.default)(implicit in: Scope): Match =
      Zone { implicit z =>
        val ctokens = stackalloc[CInt](100)
        var idx     = tokens.length
        (0 until idx).foreach(i => {
          ctokens(i) = tokens(i)
        })
        val result = CRegex.matchTokenIteratorFirst(cre,
                                                    toCString(text),
                                                    idx,
                                                    ctokens,
                                                    matchFlags,
                                                    null)
        val matchResult = Match(result)
        matchResult
      }

    def replaceAll(text: String,
                   format: String,
                   formatFlags: Match.FormatFlags = Match.format_default,
                   matchFlags: Match.Flags = Match.default): String = Zone {
      implicit z =>
        val result      = stackalloc[CString]
        val result_size = stackalloc[CInt]
        val success = CRegex.replaceAll(cre,
                                        toCString(text),
                                        toCString(format),
                                        matchFlags,
                                        result,
                                        result_size)
        val resultString =
          if (!result_size > 0 && success) fromCString(!result) else text
        resultString
    }

    def replaceFirst(text: String,
                     format: String,
                     formatFlags: Match.FormatFlags = Match.format_default,
                     matchFlags: Match.Flags = Match.default): String = {
      replaceAll(text,
                 format,
                 formatFlags | Match.format_first_only,
                 matchFlags)
    }

    def split(text: String, matchFlags: Match.Flags = Match.default)(
        implicit in: Scope): Array[String] = Scope { implicit in =>
      import scala.annotation.tailrec

      @tailrec def innerLoop(matchResult: Match, result: Array[String] = Array.empty[String])
        : Array[String] = {
        if (matchResult.tokenIteratorNext) {
          innerLoop(matchResult, result :+ matchResult.tokenString)
        } else {
          result
        }
      }
      val matchResult = tokenIteratorFirst(text, Array(-1), matchFlags)
      innerLoop(matchResult)
    }

    def iterator(text: String, matchFlags: Match.Flags = Match.default)(
        implicit in: Scope): Array[Match.MatchResult] = Scope { implicit in =>
      import scala.annotation.tailrec

      def fill(matchResult: Match): Match.MatchResult = {
        @tailrec def makeGroups(matchResult: Match, i: Int = 0, result: Array[Match.Group] = Array.empty[Match.Group])
          : Array[Match.Group] = {
          if (i < matchResult.groupCount) {
            makeGroups(matchResult,
                       i + 1,
                       result :+ new Match.Group(matchResult.groupString(i),
                                                 matchResult.groupRange(i)))
          } else {
            result
          }
        }
        new Match.MatchResult(matchResult.prefix,
                              makeGroups(matchResult),
                              matchResult.suffix)
      }

      @tailrec
      def innerLoop(matchResult: Match,
                    result: Array[Match.MatchResult] = Array
                      .empty[Match.MatchResult]): Array[Match.MatchResult] = {
        if (matchResult.iteratorNext) {
          innerLoop(matchResult, result :+ fill(matchResult))
        } else {
          result
        }
      }
      val matchResult = iteratorFirst(text, matchFlags)
      innerLoop(matchResult)
    }

    def tokenIterator(text: String,
                      tokens: Array[Int],
                      matchFlags: Match.Flags = Match.default)(
        implicit in: Scope): Array[Match.Group] = Scope { implicit in =>
      import scala.annotation.tailrec

      @tailrec def innerLoop(matchResult: Match, result: Array[Match.Group] = Array.empty[Match.Group])
        : Array[Match.Group] = {
        if (matchResult.tokenIteratorNext) {
          innerLoop(matchResult,
                    result :+ new Match.Group(matchResult.tokenString,
                                              matchResult.tokenRange))
        } else {
          result
        }
      }
      val matchResult = tokenIteratorFirst(text, tokens, matchFlags)
      innerLoop(matchResult)
    }

    def close(): Unit = {
      CRegex.delete(cre)
      cre = null
    }
  }

  private[regex] def createRegexInternal(
      pattern: String,
      flags: Flags,
      syntax: Syntax,
      locale: String)(implicit z: Zone): CRegex.CRegexStruct = {
    val error = stackalloc[CChar](1024).cast[CString]
    val cre = CRegex.create(toCString(pattern),
                            flags | syntax.f,
                            if (locale != null) toCString(locale) else null,
                            error,
                            1024)
    if (error(0) != 0) {
      throw new RegexException(fromCString(error))
    }
    cre
  }
}
