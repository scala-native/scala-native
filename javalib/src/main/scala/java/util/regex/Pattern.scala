// format: off
/*
 * Derived from Scala.js / scala-wasm (https://www.scala-js.org/)
 *
 * Copyright EPFL.
 *
 * Licensed under Apache License 2.0
 * (https://www.apache.org/licenses/LICENSE-2.0).
 */

package java.util.regex

import scala.annotation.tailrec

import java.util.Arrays
import java.util.function.Predicate
import java.util.stream.Stream

import PatternCompiler.Support._

final class Pattern private[regex] (
    _pattern: String,
    _flags: Int,
    jsPattern: String,
    jsFlags: String,
    sticky: Boolean,
    private[regex] val groupCount: Int,
    groupNumberMap: Array[Int],
    namedGroups: Engine.engine.Dictionary[Int]
) extends Serializable {

  import Pattern._
  import Engine.engine

  @inline private def jsFlagsForFind: String =
    jsFlags + (if (sticky && supportsSticky) "gy" else "g")

  /** The RegExp that is used for `Matcher.find()`. */
  private val jsRegExpForFind: engine.RegExp =
    engine.compile(jsPattern, jsFlagsForFind)

  /** RegExp used by `Matcher.matches()`. */
  private val jsRegExpForMatches: engine.RegExp =
    engine.compile(wrapJSPatternForMatches(jsPattern), jsFlags)

  private[regex] def execMatches(input: String): engine.ExecResult =
    engine.exec(jsRegExpForMatches, input)

  @inline // to stack-allocate the tuple
  private[regex] def execFind(input: String, start: Int): (engine.ExecResult, Int) = {
    val mtch = execFindInternal(input, start)
    val end = engine.getLastIndex(jsRegExpForFind)
    (mtch, end)
  }

  private def execFindInternal(input: String, start: Int): engine.ExecResult = {
    val regexp = jsRegExpForFind

    if (!supportsSticky && sticky) {
      engine.setLastIndex(regexp, start)
      val mtch = engine.exec(regexp, input)
      if (mtch == null || engine.getIndex(mtch) > start)
        null
      else
        mtch
    } else if (supportsUnicode) {
      engine.setLastIndex(regexp, start)
      engine.exec(regexp, input)
    } else {
      @tailrec
      def loop(start: Int): engine.ExecResult = {
        engine.setLastIndex(regexp, start)
        val mtch = engine.exec(regexp, input)
        if (mtch == null) {
          null
        } else {
          val index = engine.getIndex(mtch)
          if (index > start && index < input.length() &&
              Character.isLowSurrogate(input.charAt(index)) &&
              Character.isHighSurrogate(input.charAt(index - 1))) {
            loop(index + 1)
          } else {
            mtch
          }
        }
      }
      loop(start)
    }
  }

  private[regex] def numberedGroup(group: Int): Int = {
    if (group < 0 || group > groupCount)
      throw new IndexOutOfBoundsException(group.toString())
    groupNumberMap(group)
  }

  private[regex] def namedGroup(name: String): Int = {
    groupNumberMap(engine.dictGetOrElse(namedGroups, name) { () =>
      throw new IllegalArgumentException(s"No group with name <$name>")
    })
  }

  private[regex] def getIndices(lastMatch: engine.ExecResult,
      _forMatches: Boolean): engine.IndicesArray = {
    val indices = engine.getIndices(lastMatch)
    if (indices == null)
      throw new AssertionError("Unreachable; WasmEngine always supports and produces indices")
    indices
  }

  def pattern(): String = _pattern
  def flags(): Int = _flags

  override def toString(): String = pattern()

  @inline
  def matcher(input: CharSequence): Matcher =
    new Matcher(this, input.toString())

  def asPredicate(): Predicate[String] =
    new Predicate[String] {
      def test(t: String): Boolean = matcher(t).matches()
    }

  @inline
  def split(input: CharSequence): Array[String] =
    split(input, 0)

  @inline
  def split(input: CharSequence, limit: Int): Array[String] =
    split(input.toString(), limit)

  def splitAsStream(input: CharSequence): Stream[String] =
    Arrays.stream(split(input)).asInstanceOf[Stream[String]]

  private def split(inputStr: String, limit: Int): Array[String] = {
    if (inputStr == "") {
      Array("")
    } else {
      val lim = if (limit > 0) limit else Int.MaxValue
      val matcher = this.matcher(inputStr)
      val result = new SplitBuilder(if (limit > 0) limit else 16)
      var prevEnd = 0
      while ((result.length < lim - 1) && matcher.find()) {
        if (matcher.end() == 0) {
          ()
        } else {
          result.push(inputStr.substring(prevEnd, matcher.start()))
        }
        prevEnd = matcher.end()
      }
      result.push(inputStr.substring(prevEnd))

      var actualLength = result.length
      if (limit == 0) {
        while (actualLength != 0 && result(actualLength - 1) == "")
          actualLength -= 1
      }

      result.build(actualLength)
    }
  }
}

object Pattern {
  final val UNIX_LINES = 0x01
  final val CASE_INSENSITIVE = 0x02
  final val COMMENTS = 0x04
  final val MULTILINE = 0x08
  final val LITERAL = 0x10
  final val DOTALL = 0x20
  final val UNICODE_CASE = 0x40
  final val CANON_EQ = 0x80
  final val UNICODE_CHARACTER_CLASS = 0x100

  private def validateCompileFlags(flags: Int): Unit = {
    val known = UNIX_LINES | CASE_INSENSITIVE | COMMENTS | MULTILINE | LITERAL | DOTALL |
      UNICODE_CASE | CANON_EQ | UNICODE_CHARACTER_CLASS
    if ((flags & ~known) != 0)
      throw new IllegalArgumentException(s"Unknown flag $flags")
  }

  def compile(regex: String, flags: Int): Pattern = {
    validateCompileFlags(flags)
    PatternCompiler.compile(regex, flags)
  }

  def compile(regex: String): Pattern =
    compile(regex, 0)

  @inline
  def matches(regex: String, input: CharSequence): Boolean =
    matches(regex, input.toString())

  private def matches(regex: String, input: String): Boolean =
    compile(regex).matcher(input).matches()

  def quote(s: String): String = {
    var result = "\\Q"
    var start = 0
    var end = s.indexOf("\\E", start)
    while (end >= 0) {
      result += s.substring(start, end) + "\\E\\\\E\\Q"
      start = end + 2
      end = s.indexOf("\\E", start)
    }
    result + s.substring(start) + "\\E"
  }

  @inline
  private[regex] def wrapJSPatternForMatches(jsPattern: String): String =
    "^(?:" + jsPattern + ")$"

  @inline
  private class SplitBuilder(initialCapacity: Int) {
    private var array: Array[String] = new Array[String](initialCapacity)
    private var _length = 0

    def length: Int = _length

    def apply(index: Int): String = array(index)

    def push(x: String): Unit = {
      if (_length == array.length)
        array = Arrays.copyOf(array, _length * 2)
      array(_length) = x
      _length += 1
    }

    def build(actualLength: Int): Array[String] =
      if (actualLength == array.length) array
      else Arrays.copyOf(array, actualLength)
  }
}
