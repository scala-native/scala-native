/*
 * Scala.js (https://www.scala-js.org/)
 *
 * Copyright EPFL.
 *
 * Licensed under Apache License 2.0
 * (https://www.apache.org/licenses/LICENSE-2.0).
 *
 * See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 */

package java.util.regex

import scala.annotation.{switch, tailrec}

import java.util.{Arrays, ArrayList, HashMap, HashSet}
import java.util.ScalaOps._
import java.util.function.Supplier

import CaseFolding._
import CharacterSets._
import UnicodeProperties._

/** An implementation of `Engine` that only uses standalone Scala code.
 *
 *  It natively supports atomic groups and possessive quantifiers, which are
 *  not part of the spec of `js.RegExp`. They can be understood as if the
 *  proposal [[https://github.com/tc39/proposal-regexp-atomic-operators]] had
 *  been added to the spec.
 */
private[regex] object WasmEngine extends Engine {
  type Dictionary[V] = HashMap[String, V]

  type RegExp = WasmRegExp
  type ExecResult = WasmExecResult
  type IndicesArray = Array[Int] // flattened pairs at (2*i, 2*i + 1)

  @inline
  def dictEmpty[V](): Dictionary[V] =
    new HashMap[String, V]()

  @inline
  def dictSet[V](dict: Dictionary[V], key: String, value: V): Unit =
    dict.put(key, value)

  @inline
  def dictContains[V](dict: Dictionary[V], key: String): Boolean =
    dict.containsKey(key)

  @inline
  def dictRawApply[V](dict: Dictionary[V], key: String): V =
    dict.get(key)

  @inline
  def dictGetOrElse[V](dict: Dictionary[V], key: String)(default: Supplier[V]): V = {
    val result = dict.get(key)
    if (result != null) result
    else default.get()
  }

  @inline
  def dictGetOrElseUpdate[V](dict: Dictionary[V], key: String)(default: Supplier[V]): V =
    dict.computeIfAbsent(key, _ => default.get())

  @inline
  def featureTest(flags: String): Boolean = true

  def compile(pattern: String, flags: String): RegExp = {
    @inline def hasFlag(flag: Char): Boolean = flags.indexOf(flag) >= 0

    if (!hasFlag('u') || !hasFlag('s'))
      throw new AssertionError(s"Illegal flags: $flags")

    val unicodeIgnoreCase = hasFlag('i')
    val global = hasFlag('g')
    val sticky = hasFlag('y')

    val parser = new Parser(pattern, unicodeIgnoreCase)
    val root = parser.parseTopLevel()
    val groupNodeMap = parser.groupNodeMap.toArray(new Array[Matcher](parser.groupNodeMap.size()))
    new WasmRegExp(root, groupNodeMap, global, sticky)
  }

  @inline
  def validateScriptName(scriptName: String): Boolean =
    UnicodeProperties.validateScriptName(scriptName)

  @inline
  def getLastIndex(regexp: RegExp): Int =
    regexp.lastIndex

  @inline
  def setLastIndex(regexp: RegExp, newLastIndex: Int): Unit =
    regexp.lastIndex = newLastIndex

  @inline
  def exec(regexp: RegExp, string: String): ExecResult =
    regexp.exec(string)

  @inline
  def getIndex(result: ExecResult): Int =
    result.index

  @inline
  def getInput(result: ExecResult): String =
    result.input

  @inline
  def getIndices(result: ExecResult): IndicesArray =
    result.indices

  @inline
  def setIndices(result: ExecResult, indices: IndicesArray): Unit =
    ()

  @inline
  def getGroup(result: ExecResult, group: Int): String =
    result.getGroup(group)

  @inline
  def getStart(indices: IndicesArray, group: Int): Int =
    indices(2 * group)

  @inline
  def getEnd(indices: IndicesArray, group: Int): Int =
    indices(2 * group + 1)

  /** Corresponds to a `js.RegExp.ExecResult`. */
  final class WasmExecResult(val input: String, val indices: IndicesArray) {
    val index = indices(0)

    def getGroup(group: Int): String = {
      val start = indices(group * 2)
      if (start < 0) null
      else input.substring(start, indices(group * 2 + 1))
    }
  }

  /** Immutable list of `CaptureRange` pairs.
   *
   *  The pairs are flattened in a single array of ints.
   *
   *  @see [[https://262.ecma-international.org/#pattern-capturerange]]
   */
  private[regex] final class Captures(ranges: Array[Int]) {
    def this(n: Int) = {
      this(new Array[Int](n * 2))
      Arrays.fill(ranges, -1)
    }

    def toIndicesArray: IndicesArray = ranges.clone()

    @inline
    def get(group: Int): (Int, Int) =
      (ranges(group * 2), ranges(group * 2 + 1))

    /** Returns a copy of this `Captures` list with a new value for the given range. */
    def set(group: Int, start: Int, end: Int): Captures = {
      val newRanges = ranges.clone()
      newRanges(group * 2) = start
      newRanges(group * 2 + 1) = end
      new Captures(newRanges)
    }

    /** Returns a copy of this `Captures` list where a range of ranges was erased.
     *
     *  This method erases the ranges at indices `parenIndex + 1` to
     *  `parenIndex + parenCount` (inclusive).
     */
    def erase(parenIndex: Int, parenCount: Int): Captures = {
      val newRanges = ranges.clone()
      for (group <- (parenIndex + 1) to (parenIndex + parenCount)) {
        newRanges(group * 2) = -1
        newRanges(group * 2 + 1) = -1
      }
      new Captures(newRanges)
    }
  }

  /** Corresponds to a `js.RegExp`. */
  final class WasmRegExp private[WasmEngine] (root: Matcher, groupNodeMap: Array[Matcher],
      global: Boolean, sticky: Boolean) {

    val capturingGroupsCount = groupNodeMap.length - 1

    var lastIndex: Int = 0

    def exec(input: String): WasmExecResult = {
      // https://tc39.es/ecma262/multipage/text-processing.html#sec-regexpbuiltinexec

      // Step 1
      val length = input.length()

      // Step 2
      var lastIndex = this.lastIndex

      // Step 7: If global is false and sticky is false, set lastIndex to 0.
      if (!global && !sticky)
        lastIndex = 0

      // Step 13
      @inline @tailrec
      def loop(): MatchState = {
        if (lastIndex > length) {
          if (global || sticky)
            lastIndex = 0
          null
        } else {
          val initState = new MatchState(input, lastIndex, new Captures(capturingGroupsCount + 1))
          val r = root(initState, y => y)

          r match {
            case Failure =>
              if (sticky) {
                lastIndex = 0
                null
              } else {
                lastIndex =
                  if (lastIndex == input.length()) lastIndex + 1
                  else input.offsetByCodePoints(lastIndex, 1)
                loop()
              }

            case r: MatchState =>
              r
          }
        }
      }

      val r = loop()

      if (r == null) {
        null
      } else {
        // Step 14
        val e = r.endIndex

        // Step 16
        if (global || sticky)
          this.lastIndex = e

        val groups = new Array[String](groupNodeMap.length)
        val captures = r.captures.set(0, lastIndex, e)
        new WasmExecResult(input, captures.toIndicesArray)
      }
    }
  }

  /** A `MatchResult` is either a [[MatchState]] or the special token
   *  [[Failure]] that indicates that the match failed.
   *
   *  @see [[https://262.ecma-international.org/15.0/index.html#pattern-matchresult]]
   */
  private[regex] sealed abstract class MatchResult

  /** Partial match state in the regular expression algorithms.
   *
   *  - `input` is the String being matched.
   *  - `endIndex` is a non-negative integer, corresponding to a `Char` offset
   *    into `input`, which is after the last input code point matched so far
   *    by the pattern.
   *  - `captures` holds the result of capturing parentheses.
   *
   *  The nth element of `captures` is either a CaptureRange representing the
   *  range of characters captured by the nth set of capturing parentheses, or
   *  undefined if the nth set of capturing parentheses hasn't been reached yet.
   *  In our implementation, `undefined` is represented as the pair `(-1, -1)`.
   *
   *  Due to backtracking, many `MatchState`s may be in use at any time during
   *  the matching process.
   *
   *  @see [[https://262.ecma-international.org/15.0/index.html#pattern-matchstate]]
   */
  private[regex] final class MatchState(val input: String, val endIndex: Int, val captures: Captures)
      extends MatchResult

  private[regex] object MatchState {
    def apply(input: String, endIndex: Int, captures: Captures): MatchState =
      new MatchState(input, endIndex, captures)
  }

  private[regex] object Failure extends MatchResult

  /** A `MatcherContinuation`` is an *Abstract Closure* that takes one
   *  [[MatchState]] argument and returns a [[MatchResult]] result.
   *
   *  The `MatcherContinuation` attempts to match the remaining portion
   *  (specified by the closure's captured values) of the pattern against
   *  `x.input`, starting at the intermediate state given by its `MatchState`
   *  argument.
   *
   *  If the match succeeds, the `MatcherContinuation` returns the final
   *  `MatchState` that it reached; if the match fails, it returns `Failure`.
   *
   *  This interface is intended to be used as a SAM type.
   *
   *  @see [[https://262.ecma-international.org/15.0/index.html#pattern-matchercontinuation]]
   */
  @FunctionalInterface
  private[regex] trait MatchContinuation {
    def apply(x: MatchState): MatchResult
  }

  /** A regular expression node, which embeds the matching logic.
   *
   *  A `Matcher` is an *Abstract Closure* that takes a [[MatchState]] and a
   *  [[MatcherContinuation]] and returns a [[MatchResult]] result.
   *
   *  A `Matcher` attempts to match a middle subpattern (specified by the
   *  closure's captured values) of the pattern against the `MatchState`'s
   *  `input`, starting at the intermediate state given by its `MatchState`
   *  argument. The `MatcherContinuation` argument should be a closure that
   *  matches the rest of the pattern. After matching the subpattern of a
   *  pattern to obtain a new `MatchState`, the `Matcher` then calls
   *  `MatcherContinuation` on that new `MatchState` to test if the rest of the
   *  pattern can match as well. If it can, the `Matcher` returns the
   *  `MatchState` returned by `MatcherContinuation`; if not, the `Matcher` may
   *  try different choices at its choice points, repeatedly calling
   *  `MatcherContinuation` until it either succeeds or all possibilities have
   *  been exhausted.
   *
   *  @see [[https://262.ecma-international.org/15.0/index.html#pattern-matcher]]
   */
  private[regex] abstract class Matcher {
    def apply(x: MatchState, c: MatchContinuation): MatchResult
  }

  /** Repeat matcher.
   *
   *  @see [[https://262.ecma-international.org/15.0/index.html#sec-runtime-semantics-repeatmatcher-abstract-operation]]
   */
  private[regex] final class RepeatMatcher(m: Matcher, min: Int, max: Int,
      greedy: Boolean, parenIndex: Int, parenCount: Int)
      extends Matcher {

    import RepeatMatcher._

    def apply(x: MatchState, c: MatchContinuation): MatchResult =
      loop(min, max, x, c)

    private def loop(min: Int, max: Int, x: MatchState, c: MatchContinuation): MatchResult = {
      if (max == 0) {
        c(x)
      } else {
        val d: MatchContinuation = { y =>
          if (min == 0 && y.endIndex == x.endIndex) {
            Failure
          } else {
            val min2 = if (min == 0) 0 else min - 1
            val max2 = if (max == MaxInfinity) MaxInfinity else max - 1
            loop(min2, max2, y, c)
          }
        }
        val cap = x.captures.erase(parenIndex, parenCount)
        val xr = MatchState(x.input, x.endIndex, cap)

        if (min != 0) {
          m(xr, d)
        } else if (!greedy) {
          c(x) match {
            case z: MatchState => z
            case Failure       => m(xr, d)
          }
        } else {
          m(xr, d) match {
            case z: MatchState => z
            case Failure       => c(x)
          }
        }
      }
    }
  }

  private[regex] object RepeatMatcher {

    /** Used for the value of `RepeatMatcher.max` signaling that there is no maximum. */
    final val MaxInfinity = -1
  }

  /** Matcher for an atomic group.
   *
   *  This is generated around greedy quantifier to implement the semantics of
   *  possessive quantifiers.
   */
  private[regex] final class AtomicMatcher(m: Matcher) extends Matcher {
    def apply(x: MatchState, c: MatchContinuation): MatchResult = {
      /* Inspired by the logic for (?=...) lookaheads.
       * The terminal continuation `y => y` prevents backtracking within `m`.
       */
      m(x, y => y) match {
        case r: MatchState => c(r)
        case Failure       => Failure
      }
    }
  }

  private def makeRepeatMatcher(m: Matcher, min: Int, max: Int,
      greedy: Boolean, possessive: Boolean, parenIndex: Int, parenCount: Int): Matcher = {
    val repeatMatcher = new RepeatMatcher(m, min, max, greedy, parenIndex, parenCount)
    if (possessive)
      new AtomicMatcher(repeatMatcher)
    else
      repeatMatcher
  }

  /** A forward literal matcher. */
  private[regex] object EmptyMatcher extends Matcher {
    def apply(x: MatchState, c: MatchContinuation): MatchResult = c(x)
  }

  /** An alternatives node such as `ab|cd`.
   *
   *  Generalized to `n >= 0` alternatives from `MatchTwoAlternatives`.
   *  When `n == 0`, this matcher always fails.
   *
   *  @see [[https://262.ecma-international.org/15.0/index.html#sec-matchtwoalternatives]]
   */
  private[regex] final class MatchAlternatives(ms: Array[Matcher]) extends Matcher {
    def apply(x: MatchState, c: MatchContinuation): MatchResult = {
      // scalastyle:off return

      val ms = this.ms // local copy
      val len = ms.length
      var i = 0
      while (i != len) {
        ms(i)(x, c) match {
          case r: MatchState =>
            return r
          case Failure =>
            () // continue
        }
        i += 1
      }
      Failure

      // scalastyle:on return
    }
  }

  private def makeAlternativesMatcher(alternatives: ArrayList[Matcher]): Matcher =
    new MatchAlternatives(alternatives.toArray(new Array[Matcher](alternatives.size())))

  /** A sequence of consecutive nodes.
   *
   *  @see [[https://262.ecma-international.org/15.0/index.html#sec-matchsequence]]
   */
  private[regex] final class MatchSequence(m1: Matcher, m2: Matcher) extends Matcher {
    def apply(x: MatchState, c: MatchContinuation): MatchResult = {
      m1(x,
          { y =>
            m2(y, c)
          })
    }
  }

  private def makeMatchSequence(sequence: ArrayList[Matcher], forward: Boolean): Matcher = {
    sequence.size() match {
      case 0 =>
        EmptyMatcher
      case 1 =>
        sequence.get(0)
      case size if forward =>
        val iter = sequence.listIterator(size)
        var result = iter.previous()
        while (iter.hasPrevious())
          result = new MatchSequence(iter.previous(), result)
        result
      case size =>
        val iter = sequence.listIterator()
        var result = iter.next()
        while (iter.hasNext())
          result = new MatchSequence(iter.next(), result)
        result
    }
  }

  /** `^` assertion.
   *
   *  @see [[https://262.ecma-international.org/15.0/index.html#sec-compileassertion]]
   */
  private[regex] object CaretAssertion extends Matcher {
    def apply(x: MatchState, c: MatchContinuation): MatchResult = {
      if (x.endIndex == 0)
        c(x)
      else
        Failure
    }
  }

  /** `$` assertion.
   *
   *  @see [[https://262.ecma-international.org/15.0/index.html#sec-compileassertion]]
   */
  private[regex] object DollarAssertion extends Matcher {
    def apply(x: MatchState, c: MatchContinuation): MatchResult = {
      if (x.endIndex == x.input.length())
        c(x)
      else
        Failure
    }
  }

  /** `\b` or `\B` assertion, assuming the 'i' flag was not used.
   *
   *  @see [[https://262.ecma-international.org/15.0/index.html#sec-compileassertion]]
   */
  private[regex] final class WordBoundaryAssertion(negated: Boolean) extends Matcher {
    def apply(x: MatchState, c: MatchContinuation): MatchResult = {
      val a = isWordChar(x.input, x.endIndex - 1)
      val b = isWordChar(x.input, x.endIndex)
      val isBoundary = a != b
      if (isBoundary != negated)
        c(x)
      else
        Failure
    }

    private def isWordChar(input: String, e: Int): Boolean = {
      if (e < 0 || e >= input.length()) {
        false
      } else {
        // https://262.ecma-international.org/15.0/index.html#ASCII-word-characters
        val c = input.codePointAt(e)
        (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || (c == '_')
      }
    }
  }

  /** A positive look-around group of the form `(?= )` or `(?<= )`.
   *
   *  @see [[https://262.ecma-international.org/15.0/index.html#sec-compileassertion]]
   */
  private[regex] final class PositiveLookAroundMatcher(m: Matcher) extends Matcher {

    def apply(x: MatchState, c: MatchContinuation): MatchResult = {
      m(x, y => y) match {
        case Failure =>
          Failure
        case r: MatchState =>
          c(MatchState(x.input, x.endIndex, r.captures))
      }
    }
  }

  /** A negative look-around group of the form `(?! )` or `(?<! )`.
   *
   *  @see [[https://262.ecma-international.org/15.0/index.html#sec-compileassertion]]
   */
  private[regex] final class NegativeLookAroundMatcher(m: Matcher) extends Matcher {

    def apply(x: MatchState, c: MatchContinuation): MatchResult = {
      m(x, y => y) match {
        case _: MatchState =>
          Failure
        case Failure =>
          c(x)
      }
    }
  }

  private[regex] abstract class AnyLiteralMatcher extends Matcher

  /** A forward literal matcher. */
  private[regex] final class LiteralMatcher(val literal: String) extends AnyLiteralMatcher {
    def apply(x: MatchState, c: MatchContinuation): MatchResult = {
      import x._

      val newEndIndex = endIndex + literal.length()
      if (input.startsWith(literal, endIndex) && isCodePointBoundary(input, newEndIndex))
        c(MatchState(input, newEndIndex, captures))
      else
        Failure
    }
  }

  /** A backward literal matcher. */
  private[regex] final class BackwardLiteralMatcher(val literal: String) extends AnyLiteralMatcher {
    def apply(x: MatchState, c: MatchContinuation): MatchResult = {
      import x._

      val startIndex = endIndex - literal.length()
      if (startIndex >= 0 && input.startsWith(literal, startIndex) && isCodePointBoundary(
              input, startIndex)) {
        c(MatchState(input, startIndex, captures))
      } else {
        Failure
      }
    }
  }

  /** The `.` matcher, which matches any single code point.
   *
   *  @see [[https://262.ecma-international.org/15.0/index.html#sec-compileatom]]
   */
  private[regex] object ForwardDotMatcher extends Matcher {
    def apply(x: MatchState, c: MatchContinuation): MatchResult = {
      import x._
      if (endIndex < input.length())
        c(MatchState(input, input.offsetByCodePoints(endIndex, +1), captures))
      else
        Failure
    }
  }

  /** Backward `.` matcher, which matches any single code point.
   *
   *  @see [[https://262.ecma-international.org/15.0/index.html#sec-compileatom]]
   */
  private[regex] object BackwardDotMatcher extends Matcher {
    def apply(x: MatchState, c: MatchContinuation): MatchResult = {
      import x._
      if (endIndex > 0)
        c(MatchState(input, input.offsetByCodePoints(endIndex, -1), captures))
      else
        Failure
    }
  }

  /** Forward Matcher for a `CharSet`.
   *
   *  @see [[https://262.ecma-international.org/15.0/index.html#sec-compileatom]]
   */
  private[regex] final class ForwardCharacterSetMatcher(
      charSet: CharSet, invert: Boolean, unicodeIgnoreCase: Boolean)
      extends Matcher {

    def apply(x: MatchState, c: MatchContinuation): MatchResult = {
      val input = x.input
      val e = x.endIndex

      if (e >= input.length()) {
        Failure
      } else {
        val ch = input.codePointAt(e)
        val cc = if (unicodeIgnoreCase) caseFold(ch) else ch
        val found = charSet.contains(cc)
        if (found == invert)
          Failure
        else
          c(MatchState(input, e + Character.charCount(ch), x.captures))
      }
    }
  }

  /** Backward Matcher for a `CharSet`.
   *
   *  @see [[https://262.ecma-international.org/15.0/index.html#sec-compileatom]]
   */
  private[regex] final class BackwardCharacterSetMatcher(
      charSet: CharSet, invert: Boolean, unicodeIgnoreCase: Boolean)
      extends Matcher {

    def apply(x: MatchState, c: MatchContinuation): MatchResult = {
      val input = x.input
      val e = x.endIndex

      if (e <= 0) {
        Failure
      } else {
        val ch = input.codePointBefore(e)
        val cc = if (unicodeIgnoreCase) caseFold(ch) else ch
        val found = charSet.contains(cc)
        if (found == invert)
          Failure
        else
          c(MatchState(input, e - Character.charCount(ch), x.captures))
      }
    }
  }

  private def makeCharacterSetMatcher(charSet: CharSet, invert: Boolean,
      unicodeIgnoreCase: Boolean, forward: Boolean): Matcher = {
    if (forward)
      new ForwardCharacterSetMatcher(charSet, invert, unicodeIgnoreCase)
    else
      new BackwardCharacterSetMatcher(charSet, invert, unicodeIgnoreCase)
  }

  /** A capturing group.
   *
   *  @see [[https://262.ecma-international.org/15.0/index.html#sec-compileatom]]
   */
  private[regex] final class CapturingGroupMatcher(number: Int, m: Matcher, forward: Boolean)
      extends Matcher {
    def apply(x: MatchState, c: MatchContinuation): MatchResult = {
      m(x,
          { y =>
            val xe = x.endIndex
            val ye = y.endIndex
            val cap =
              if (forward) y.captures.set(number, xe, ye)
              else y.captures.set(number, ye, xe)
            c(MatchState(y.input, ye, cap))
          })
    }
  }

  /** An explicit non-capturing group meant to prevent unintended fusion. */
  private[regex] final class NonCapturingGroupMatcher(m: Matcher) extends Matcher {
    def apply(x: MatchState, c: MatchContinuation): MatchResult = m(x, c)
  }

  /** A back reference.
   *
   *  @see [[https://262.ecma-international.org/15.0/index.html#sec-backreference-matcher]]
   */
  private[regex] final class BackReferenceMatcher(groupNumber: Int, forward: Boolean) extends Matcher {
    def apply(x: MatchState, c: MatchContinuation): MatchResult = {
      val input = x.input
      val cap = x.captures
      val r = cap.get(groupNumber)

      if (r._1 < 0) {
        // That group was not matched; match an empty string
        c(x)
      } else {
        val e = x.endIndex
        val rs = r._1
        val re = r._2
        val len = re - rs

        val captured = input.substring(rs, re)

        if (forward) {
          val f = e + len
          if (input.startsWith(captured, e) && isCodePointBoundary(input, f))
            c(MatchState(input, f, cap))
          else
            Failure
        } else {
          val f = e - len
          if (f >= 0 && input.startsWith(captured, f) && isCodePointBoundary(input, f))
            c(MatchState(input, f, cap))
          else
            Failure
        }
      }
    }
  }

  private def isCodePointBoundary(input: String, index: Int): Boolean = {
    index == 0 ||
    index == input.length() ||
    !Character.isSurrogatePair(input.charAt(index - 1), input.charAt(index))
  }

  // !!! Lots of copy-paste with IndicesBuilder.Parser

  private[regex] final class Parser(pattern0: String, unicodeIgnoreCase: Boolean) {
    /* Use an additional ')' at the end of the string so that we don't have to
     * check `pIndex < pattern.length` all the time.
     */
    private val pattern: String = pattern0 + ')'

    private var pIndex: Int = 0
    private var forward: Boolean = true

    val groupNodeMap = new ArrayList[Matcher]()
    groupNodeMap.add(null) // index 0 is not used

    def parsedGroupCount: Int = groupNodeMap.size() - 1

    def parseTopLevel(): Matcher =
      parseInsideParensAndClosingParen()

    private def parseInsideParensAndClosingParen(): Matcher = {
      // scalastyle:off return
      val alternatives = new ArrayList[Matcher]() // completed alternatives
      var sequence = new ArrayList[Matcher]() // current sequence

      // Explicitly take the sequence, otherwise we capture a `var`
      def completeSequence(sequence: ArrayList[Matcher]): Matcher =
        makeMatchSequence(sequence, forward)

      while (true) {
        val groupCountBeforeBaseNode = parsedGroupCount
        val dispatchCP = pattern.codePointAt(pIndex)

        val baseNode = (dispatchCP: @switch) match {
          case '|' =>
            // Complete one alternative
            alternatives.add(completeSequence(sequence))
            sequence = new ArrayList[Matcher]()
            pIndex += 1
            null

          case ')' =>
            // Complete the last alternative
            pIndex += 1 // go past the closing paren
            val lastAlternative = completeSequence(sequence)
            if (alternatives.size() == 0) {
              return lastAlternative
            } else {
              alternatives.add(lastAlternative)
              return makeAlternativesMatcher(alternatives)
            }

          case '(' =>
            val indicator = pattern.substring(pIndex + 1, pIndex + 3)
            if (indicator == "?=" || indicator == "?!") {
              // Look-ahead group
              pIndex += 3
              val savedForward = forward
              forward = true
              val inner = parseInsideParensAndClosingParen()
              forward = savedForward
              if (indicator == "?=")
                new PositiveLookAroundMatcher(inner)
              else
                new NegativeLookAroundMatcher(inner)
            } else if (indicator == "?<") {
              // Look-behind group, which must be ?<= or ?<!
              val fullIndicator = pattern.substring(pIndex + 1, pIndex + 4)
              pIndex += 4
              val savedForward = forward
              forward = false
              val inner = parseInsideParensAndClosingParen()
              forward = savedForward
              if (fullIndicator == "?<=")
                new PositiveLookAroundMatcher(inner)
              else
                new NegativeLookAroundMatcher(inner)
            } else if (indicator == "?:") {
              // Non-capturing group
              pIndex += 3
              val inner = parseInsideParensAndClosingParen()
              // Wrap literal matchers so that they do not merge with their neighbors
              if (inner.isInstanceOf[AnyLiteralMatcher])
                new NonCapturingGroupMatcher(inner)
              else
                inner
            } else if (indicator == "?>") {
              // Atomic group (not supported by js.RegExp)
              pIndex += 3
              val inner = parseInsideParensAndClosingParen()
              new AtomicMatcher(inner)
            } else {
              // Capturing group
              pIndex += 1
              val groupIndex = groupNodeMap.size()
              groupNodeMap.add(null) // reserve slot before parsing inner
              val inner = parseInsideParensAndClosingParen()
              val groupNode = new CapturingGroupMatcher(groupIndex, inner, forward)
              groupNodeMap.set(groupIndex, groupNode)
              groupNode
            }

          case '.' =>
            pIndex += 1
            if (forward)
              ForwardDotMatcher
            else
              BackwardDotMatcher

          case '^' =>
            pIndex += 1
            CaretAssertion

          case '$' =>
            pIndex += 1
            DollarAssertion

          case '\\' =>
            pIndex += 1
            val c = pattern.charAt(pIndex)

            if (isDigit(c)) {
              // it is a back reference; parse all following digits
              new BackReferenceMatcher(parseInt(), forward)
            } else {
              pIndex += 1 // c

              (c: @switch) match {
                case 'b' | 'B' =>
                  if (unicodeIgnoreCase) {
                    throw new AssertionError(
                        s"PatternCompiler was not supposed to generate \\$c with the 'i' flag")
                  }
                  new WordBoundaryAssertion(negated = c == 'B')
                case 'p' | 'P' =>
                  makeCharacterSetMatcher(parseUnicodePropertyWithBraces(),
                      invert = c == 'P', unicodeIgnoreCase, forward)
                case _ =>
                  makeSingleCodePointMatcher(c, forward)
              }
            }

          case '[' =>
            pIndex += 1
            val invert = pattern.charAt(pIndex) == '^'
            if (invert)
              pIndex += 1
            val charSet = parseCharacterClass()
            pIndex += 1 // ']'
            makeCharacterSetMatcher(charSet, invert, unicodeIgnoreCase, forward)

          case _ =>
            val start = pIndex
            pIndex += Character.charCount(dispatchCP)
            makeSingleCodePointMatcher(dispatchCP, forward)
        }

        if (baseNode ne null) { // null if we just completed an alternative
          val groupCountAfterBaseNode = parsedGroupCount

          def finishRepeater(min: Int, max: Int): Unit = {
            var greedy = true
            var possessive = false
            if (pattern.charAt(pIndex) == '?') {
              greedy = false
              pIndex += 1
            } else if (pattern.charAt(pIndex) == '+') {
              possessive = true
              pIndex += 1
            }

            val repeatMatcher = makeRepeatMatcher(baseNode, min, max, greedy, possessive,
                parenIndex = groupCountBeforeBaseNode,
                parenCount = groupCountAfterBaseNode - groupCountBeforeBaseNode)
            sequence.add(repeatMatcher)
          }

          (pattern.charAt(pIndex): @switch) match {
            case '+' =>
              pIndex += 1
              finishRepeater(min = 1, max = RepeatMatcher.MaxInfinity)

            case '*' =>
              pIndex += 1
              finishRepeater(min = 0, max = RepeatMatcher.MaxInfinity)

            case '?' =>
              pIndex += 1
              finishRepeater(min = 0, max = 1)

            case '{' =>
              pIndex += 1
              val min = parseInt()
              val max = if (pattern.charAt(pIndex) != ',') {
                min
              } else {
                pIndex += 1
                if (pattern.charAt(pIndex) != '}')
                  parseInt()
                else
                  RepeatMatcher.MaxInfinity
              }
              pIndex += 1 // '}'
              finishRepeater(min, max)

            case _ =>
              val sequenceLen = sequence.size()
              if (sequenceLen != 0) {
                // Merge consecutive literal matchers
                (sequence.get(sequenceLen - 1), baseNode) match {
                  case (prev: LiteralMatcher, baseNode: LiteralMatcher) =>
                    val fused = new LiteralMatcher(prev.literal + baseNode.literal)
                    sequence.set(sequenceLen - 1, fused)
                  case (prev: BackwardLiteralMatcher, baseNode: BackwardLiteralMatcher) =>
                    val fused = new BackwardLiteralMatcher(prev.literal + baseNode.literal)
                    sequence.set(sequenceLen - 1, fused)
                  case _ =>
                    sequence.add(baseNode)
                }
              } else {
                sequence.add(baseNode)
              }
          }
        }
      }

      throw null // unreachable
      // scalastyle:on return
    }

    @inline
    private def isDigit(c: Char): Boolean = c >= '0' && c <= '9'

    private def parseInt(): Int = {
      val pattern = this.pattern // local copy

      val startIndex = pIndex
      while (isDigit(pattern.charAt(pIndex)))
        pIndex += 1
      Integer.parseInt(pattern.substring(startIndex, pIndex))
    }

    private def makeSingleCodePointMatcher(cp: Int, forward: Boolean): Matcher = {
      if (unicodeIgnoreCase) {
        makeCharacterSetMatcher(
            CharSet.fromSingleCodePoint(caseFold(cp)),
            invert = false,
            unicodeIgnoreCase = true,
            forward)
      } else {
        val literal = Character.toString(cp)
        if (forward)
          new LiteralMatcher(literal)
        else
          new BackwardLiteralMatcher(literal)
      }
    }

    private def parseCharacterClass(): CharSet = {
      val pattern = this.pattern // local copy

      var result = CharSet.Empty

      while (pattern.charAt(pIndex) != ']') {
        val cp1 = pattern.codePointAt(pIndex)
        pIndex += Character.charCount(cp1)

        var wasPCharacterClass = false

        val cpStart: Int = if (cp1 == '\\') {
          val esc = pattern.charAt(pIndex)
          pIndex += 1
          if (esc == 'p' || esc == 'P') {
            val charSet = parseUnicodePropertyWithBraces()
            if (esc == 'p')
              result = result.union(charSet)
            else
              result = result.union(charSet.complement())
            wasPCharacterClass = true
          }
          esc.toInt
        } else {
          cp1
        }

        if (!wasPCharacterClass) {
          if (pattern.charAt(pIndex) == '-') {
            pIndex += 1
            val cp2 = pattern.codePointAt(pIndex)
            pIndex += Character.charCount(cp2)

            val cpEnd: Int = if (cp2 == '\\') {
              pIndex += 1
              pattern.charAt(pIndex - 1).toInt
            } else {
              cp2
            }

            result = result.addOneRange(cpStart, cpEnd)
          } else {
            result = result.addOneCodePoint(cpStart)
          }
        }
      }

      if (unicodeIgnoreCase)
        result.mapBySimpleCaseFolding()
      else
        result
    }

    private def parseUnicodePropertyWithBraces(): CharSet = {
      pIndex += 1 // '{'
      val startIndex = pIndex
      while (pattern.charAt(pIndex) != '}')
        pIndex += 1
      pIndex += 1 // '}'
      charSetForUnicodeProperty(pattern.substring(startIndex, pIndex - 1))
    }
  }
}
