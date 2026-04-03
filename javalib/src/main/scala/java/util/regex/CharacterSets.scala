// format: off
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

import java.util.{ArrayList, Arrays, Collections, Comparator, HashSet}
import java.util.ScalaOps._

import CaseFolding._

private[regex] object CharacterSets {

  /** Mathematical set of `CharSetElement`s.
   *
   *  Since `UnicodeSets` is always false for us (it is the 'v' flag, which
   *  we do not use), a `CharSetElement` is always a single code point.
   *  Therefore, this is a set of code points.
   *
   *  We represent such a set as a flat array of inclusive ranges. The ranges
   *  are non-overlapping and non-adjacent (the end of a range cannot be one
   *  less than the start of another range). They are sorted in increasing
   *  order of their start element, which means the entire array is sorted.
   *  Two elements of the flat array *can* be the same code point, when there
   *  is a single code point in a range.
   *
   *  @see [[https://262.ecma-international.org/#pattern-charset]]
   */
  class CharSet(private val ranges: Array[Int]) {
    import CharSet._

    def contains(cp: Int): Boolean = {
      var low = 0
      var high = ranges.length

      while (low < high) {
        val mid = (low + high) >>> 1
        val elem = ranges(mid)
        if (cp < elem)
          high = mid
        else if (cp == elem)
          return true
        else
          low = mid + 1
      }

      (low & 1) != 0 // insertion point is odd: inside a range end.
    }

    def union(that: CharSet): CharSet = {
      // The algorithm is like the Merge step of a MergeSort, but in addition we merge ranges

      val xs = this.ranges
      val ys = that.ranges
      val xslen = xs.length
      val yslen = ys.length

      val builder = new InOrderBuilder(maxLength = xslen + yslen)

      /* Invariant, excluding the "trivial" case of the first iteration, where
       * `builder.isEmpty`:
       *
       * The `start` of the last range in `builder` is less than or equal to
       * the `start` of both ranges being "looked at" in `xs` and `ys`
       * (i.e., `xs(xi)` and `ys(yi)`).
       *
       * In formulas: `builder.lastStart <= xs(xi)` and `builder.lastStart <= ys(yi)`.
       */

      var xi = 0
      var yi = 0
      while (xi != xslen && yi != yslen) {
        // By
        if (xs(xi) <= ys(yi)) {
          // Pre-condition fullfilled by the invariant
          builder.appendRange(xs(xi), xs(xi + 1))
          // Post-condition ensures that builder.lastStart <= xs(xi) <= ys(yi)
          xi += 2 // makes xs(xi) bigger, so preserves the invariant
        } else {
          // Similar comments apply here
          builder.appendRange(ys(yi), ys(yi + 1))
          yi += 2
        }
      }

      while (xi != xslen) {
        builder.appendRange(xs(xi), xs(xi + 1))
        xi += 2
      }
      while (yi != yslen) {
        builder.appendRange(ys(yi), ys(yi + 1))
        yi += 2
      }

      builder.result()
    }

    def addOneRange(start: Int, end: Int): CharSet = {
      // TODO? Optimize this method
      this.union(CharSet.fromSingleRange(start, end))
    }

    def addOneCodePoint(cp: Int): CharSet = {
      // TODO? Optimize this method
      this.union(CharSet.fromSingleCodePoint(cp))
    }

    def complement(): CharSet = {
      val ranges = this.ranges // local copy
      val len = ranges.length

      // Complement with an extended range, possibly creating empty ranges at start and/or end
      val output = new Array[Int](len + 2)
      output(0) = 0
      output(len + 1) = Character.MAX_CODE_POINT
      for (i <- 1 to len) {
        if ((i & 1) != 0)
          output(i) = ranges(i - 1) - 1 // output end <- input start - 1
        else
          output(i) = ranges(i - 1) + 1 // output start <- input end - 1
      }

      // Trim invalid ranges
      val validStart = if (output(1) < 0) 2 else 0
      val validEnd = if (output(len) > Character.MAX_CODE_POINT) len else len + 2

      /* Assert: validStart <= validEnd
       * - if len >= 2, true because `validStart <= 2`
       * - if len == 0, output(1) was not modified from its original value
       *   of `MAX_CODE_POINT`, and therefore `validStart == 0`.
       */

      val realOutput =
        if (validStart == 0 && validEnd == len + 2) output
        else Arrays.copyOfRange(output, validStart, validEnd)

      new CharSet(realOutput)
    }

    /** Map the elements of this `CharSet` by the simple case folding function. */
    def mapBySimpleCaseFolding(): CharSet = {
      val outputRanges = new ArrayList[Range]()

      def processOneRange(start: Int, end: Int): Unit = {
        val startInsertionPoint = findInsertionPointInCaseFoldingFromRanges(start)
        val endInsertionPoint = findInsertionPointInCaseFoldingFromRanges(end)

        def makeAndAddCanonRange(start: Int, end: Int, commonInsertionPoint: Int): Unit = {
          outputRanges.add(new Range(
              caseFoldCodePointBasedOnInsertionPoint(start, commonInsertionPoint),
              caseFoldCodePointBasedOnInsertionPoint(end, commonInsertionPoint)))
        }

        if (startInsertionPoint == endInsertionPoint) {
          makeAndAddCanonRange(start, end, startInsertionPoint)
        } else {
          val canonRanges = new ArrayList[CharSet]()

          // From `start` until the end of its range
          makeAndAddCanonRange(
              start, endOfCaseFoldingRange(startInsertionPoint), startInsertionPoint)

          // Entire ranges between `start` and `end`
          for (insertionPoint <- (startInsertionPoint + 1) until endInsertionPoint) {
            makeAndAddCanonRange(startOfCaseFoldingRange(insertionPoint),
                endOfCaseFoldingRange(insertionPoint), insertionPoint)
          }

          // From the start of the range of `end` until `end`
          makeAndAddCanonRange(
              startOfCaseFoldingRange(endInsertionPoint), end, endInsertionPoint)
        }
      }

      val ranges = this.ranges // local copy
      for (i <- 0 until ranges.length / 2)
        processOneRange(ranges(2 * i), ranges(2 * i + 1))

      Collections.sort(outputRanges, new Comparator[Range] {
        def compare(a: Range, b: Range): Int =
          if (a.start != b.start) Integer.compare(a.start, b.start)
          else Integer.compare(a.end, b.end)
      })
      val builder = new InOrderBuilder(maxLength = 2 * outputRanges.size())
      for (range <- outputRanges.scalaOps)
        builder.appendRange(range.start, range.end)
      builder.result()
    }
  }

  object CharSet {
    val Empty: CharSet = new CharSet(new Array(0))

    def fromSingleCodePoint(cp: Int): CharSet =
      new CharSet(Array(cp, cp))

    def fromSingleRange(start: Int, end: Int): CharSet =
      new CharSet(Array(start, end))

    def fromSortedRangesAcquireArray(ranges: Array[Int]): CharSet =
      new CharSet(ranges)

    // Note: this class has an ordering that is inconsistent with equality
    private final class Range(val start: Int, val end: Int) extends Comparable[Range] {
      def compareTo(that: Range): Int =
        if (this.start != that.start) this.start - that.start
        else this.end - that.end
    }

    /** A builder for a CharSet, where ranges are accumulated by monotonically
     *  increasing `start` value.
     */
    private final class InOrderBuilder(maxLength: Int) { // twice the number of ranges
      private val output = new Array[Int](maxLength)
      private var outputIndex = 0

      /** Appends a range to this builder.
       *
       *  Pre: `isEmpty || (start >= lastStart)`
       *
       *  Post: `!isEmpty && (start >= lastStart)`
       *
       *  where `lastStart` is the `start` of the last range in the builder
       *  before calling this method.
       */
      def appendRange(start: Int, end: Int): Unit = {
        if (outputIndex > 0 && start <= output(outputIndex - 1) + 1) {
          // The start is at most the end of the previous range, plus 1 -> merge
          if (end > output(outputIndex - 1))
            output(outputIndex - 1) = end
        } else {
          // Actually append a new range
          output(outputIndex) = start
          output(outputIndex + 1) = end
          outputIndex += 2
        }
      }

      def result(): CharSet = {
        val truncatedOutput =
          if (outputIndex == output.length) output
          else Arrays.copyOf(output, outputIndex)
        new CharSet(truncatedOutput)
      }
    }
  }
}
