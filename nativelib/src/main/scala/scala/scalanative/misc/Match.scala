package scala.scalanative.misc
package regex

import scala.scalanative.native._
import scalanative.runtime.struct

sealed trait Match {
  def groupCount: Int
  def format(fmt: String, flags: Match.FormatFlags = Match.default): String
  def groupRange(index: Int): Match.Range
  def groupString(index: Int): String
  def tokenRange: Match.Range
  def tokenString: String
  def prefix: String
  def suffix: String
  def iteratorNext: Boolean
  def tokenIteratorNext: Boolean
  // compound methods
  def group(index: Int): Match.Group
  def token: Match.Group
  def result: Match.MatchResult
}

object Match {
  // Flags
  type Flags       = CUnsignedInt
  type FormatFlags = CUnsignedInt

  // Note: [first, last) refers to the character sequence being matched.
  val default: Flags = 0x0000.toUInt
  // The first character in [first,last) will be treated as if it is not at the beginning of a line (i.e. ^ will not match [first,first)
  val not_bol: Flags = 0x0001.toUInt
  // The last character in [first,last) will be treated as if it is not at the end of a line (i.e. $ will not match [last,last)
  val not_eol: Flags = 0x0002.toUInt
  // "\b" will not match [first,first)
  val not_bow: Flags = 0x0004.toUInt
  // "\b" will not match [last,last)
  val not_eow: Flags = 0x0008.toUInt
  // If more than one match is possible, then any match is an acceptable result
  val any: Flags = 0x0010.toUInt
  // Do not match empty sequences
  val not_null: Flags = 0x0020.toUInt
  // Only match a sub-sequence that begins at first
  val continuous: Flags = 0x0040.toUInt
  // --first is a valid iterator position. When set, causes match_not_bol and match_not_bow to be ignored
  val prev_avail: Flags = 0x0100.toUInt

  // Don't keep copy of input data where possible, user garantees non-moving memory (faster processing)
  val dont_keep_text_copy: Flags = 0x8000.toUInt

  // Use ECMAScript rules to construct strings in replaceAll (http://ecma-international.org/ecma-262/5.1/#sec-15.5.4.11)
  val format_default: FormatFlags = 0x0000.toUInt
  // Use POSIX sed utility rules in replaceAll (http://pubs.opengroup.org/onlinepubs/9699919799/utilities/sed.html#tag_20_116_13_03)
  val format_sed: FormatFlags = 0x0400.toUInt
  // Do not copy un-matched strings to the output in replaceAll
  val format_no_copy: FormatFlags = 0x0800.toUInt
  // Only replace the first match in replaceAll
  val format_first_only: FormatFlags = 0x1000.toUInt

  private[regex] def apply(matchResult: CRegex.CRegexMatchStruct)(
      implicit in: Scope): Match = {
    val r = new MatchImpl(matchResult)
    in.acquire(r)
  }

  def compareArrays(a: Array[MatchResult], b: Array[MatchResult]): Boolean = {
    if (a.length == b.length) {
      (0 until a.length).find(i => a(i) != b(i)).isEmpty
    } else false
  }

  def compareArrays(a: Array[Group], b: Array[Group]): Boolean = {
    if (a.length == b.length) {
      (0 until a.length).find(i => a(i) != b(i)).isEmpty
    } else false
  }

  final case class Range(val position: Int, val length: Int) {
    override def toString: String = s"(pos: $position, len: $length)"

    def ==(other: Range): Boolean = {
      position == other.position && length == other.length
    }

    def !=(other: Range): Boolean = {
      !(this == other)
    }
  }

  final case class Group(string: String, range: Range) {
    override def toString: String = s"('$string', $range)"

    def ==(other: Group): Boolean = {
      string == other.string && range == other.range
    }

    def !=(other: Group): Boolean = {
      !(this == other)
    }
  }
  final case class MatchResult(prefix: String,
                               groups: Array[Group],
                               suffix: String) {
    override def toString: String = {
      val g = groups.mkString("[", ", ", "]")
      s"{'$prefix', $g, '$suffix'}"
    }
    def ==(other: MatchResult): Boolean = {
      prefix == other.prefix && suffix == other.suffix && compareArrays(
        groups,
        other.groups)
    }

    def !=(other: MatchResult): Boolean = {
      !(this == other)
    }
  }

  // Private section below

  private[regex] class MatchImpl(var matchResult: CRegex.CRegexMatchStruct)(
      implicit in: Scope)
      extends Match
      with Resource {

    def groupCount: Int = {
      CRegex.getMatchSubmatchCount(matchResult)
    }

    def groupRange(index: Int): Range = {
      if (0 <= index && index < groupCount) {
        val result = stackalloc[CInt](2)
        CRegex.getMatchSubmatchRange(matchResult, index, result)
        new Range(result(0), result(1))
      } else throw new RegexException("Group index out of bound")
    }

    def groupString(index: Int): String = Zone { implicit z =>
      if (0 <= index && index < groupCount) {
        val result      = stackalloc[CString]
        val result_size = stackalloc[CInt]
        CRegex.getMatchSubmatchString(matchResult, index, result, result_size)
        if (!result_size > 0) fromCString(!result) else ""
      } else throw new RegexException("Group index out of bound")
    }

    def prefix: String = Zone { implicit z =>
      val result      = stackalloc[CString]
      val result_size = stackalloc[CInt]
      CRegex.getMatchSubmatchString(matchResult, -1, result, result_size)
      if (!result_size > 0) fromCString(!result) else ""
    }

    def suffix: String = Zone { implicit z =>
      val result      = stackalloc[CString]
      val result_size = stackalloc[CInt]
      CRegex.getMatchSubmatchString(matchResult,
                                    groupCount,
                                    result,
                                    result_size)
      if (!result_size > 0) fromCString(!result) else ""
    }

    def tokenRange: Range = {
      val result = stackalloc[CInt](2)
      CRegex.getMatchTokenRange(matchResult, result)
      new Range(result(0), result(1))
    }

    def tokenString: String = Zone { implicit z =>
      val result      = stackalloc[CString]
      val result_size = stackalloc[CInt]
      CRegex.getMatchTokenString(matchResult, result, result_size)
      if (!result_size > 0) fromCString(!result) else ""
    }

    def iteratorNext: Boolean = {
      matchResult = CRegex.matchIteratorNext(matchResult)
      if (matchResult != null) true else false
    }
    def tokenIteratorNext: Boolean = {
      matchResult = CRegex.matchTokenIteratorNext(matchResult)
      if (matchResult != null) true else false
    }

    def format(fmt: String,
               flags: Match.FormatFlags = Match.format_default): String =
      Zone { implicit z =>
        val result      = stackalloc[CString]
        val result_size = stackalloc[CInt]
        CRegex.matchFormat(matchResult,
                           toCString(fmt),
                           flags,
                           result,
                           result_size)
        if (!result_size > 0) fromCString(!result) else ""
      }

    def group(index: Int): Match.Group = {
      new Group(groupString(index), groupRange(index))
    }
    def token: Match.Group = {
      new Group(tokenString, tokenRange)
    }
    def result: Match.MatchResult = {
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
      fill(this)
    }

    def close(): Unit = {
      CRegex.deleteMatchResult(matchResult)
      matchResult = null
    }
  }
}
