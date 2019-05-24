// Copyright 2010 The Go Authors. All rights reserved.
// Use of this source code is governed by a BSD-style
// license that can be found in the LICENSE file.

// Many of these were derived from the corresponding Go functions in
// http://code.google.com/p/go/source/browse/src/pkg/unicode/letter.go

package scala.scalanative
package regex

// Utilities for dealing with Unicode better than Java does.
//
// @author adonovan@google.com (Alan Donovan)
//         Highly modified by Lee Tibbert for Scala Native.
object Unicode {

  // The highest legal rune value.
  final val MAX_RUNE = 0x10FFFF

  // The highest legal ASCII value.
  final val MAX_ASCII = 0x7f

  // The highest legal Latin-1 value.
  final val MAX_LATIN1 = 0xFF

  private final val MAX_CASE = 3

  // Represents invalid code points.
  private final val REPLACEMENT_CHAR = 0xFFFD

  // Minimum and maximum runes involved in folding.
  // Checked during test.
  final val MIN_FOLD = 0x0041
  final val MAX_FOLD = 0x1044f

  // midpoint() returns the half way point between the lo and hi indices
  // adjusted to be an integral number of steps above low and below
  // hi. lo & hi do not need alignment beyond Byte.
  //
  // The algorithm is slightly biased towards lo; truncation rather than
  // rounding. The code which calls this method uses a semi-open range
  // with hi excluded. So the bias towards lo is not material and of
  // interest only to those interested in the analysis of algorithms,
  // particularly search algorithms.

  @inline
  private def midpoint(lo: Int, hi: Int, step: Int): Int = {
    (((lo + hi) / 2) / step) * step
  }

  // is32 uses binary search to test whether rune is in the specified
  // slice of 32-bit ranges.

  private def is32(ranges: Array[Int], r: Int): Boolean = {
    // Use when the rune is expected to be outside the first few ranges.

    val indexStep = 3 // Number of column in a logical row.

    var lo = 0
    var hi = ranges.length

    var found = false

    while (lo < hi) {
      val m           = midpoint(lo, hi, indexStep)
      val rangeLow    = ranges(m)
      val rangeHigh   = ranges(m + 1)
      val rangeStride = ranges(m + 2)

      if (rangeLow <= r && r <= rangeHigh) {
        found = ((r - rangeLow) % rangeStride) == 0
        lo = hi // Done! Exit loop.
      } else if (r < rangeLow) {
        hi = m // Search lower half, no indexStep decrement, range is [)
      } else {
        lo = m + indexStep // Search upper half
      }
    }

    found
  }

  // is tests whether rune is in the specified table of ranges.

  private def is(ranges: Array[Int], r: Int): Boolean = {
    // All the code which calls this private method filter
    // out all characters <= MAX_LATIN1, so always use binary search.

    if (r <= MAX_LATIN1) {
      assert(false, s"Bad MAX_LATIN1 guess Lee!")
    }

    (ranges.length > 0) && (r >= ranges(0)) && is32(ranges, r)
  }

  /// isLower, isTitle, and isUpper are used by the regex test Suite so make
  /// them visible to package. Java Character.isLowerCase(codepoint),
  /// Character.isTitleCase(codepoint), and isUpperCase() are possible
  //  replacements. Keep the existing code until regex Unicode & Java
  //  Character are sorted out and/or unified. Testing code should be
  //  as close to 'known good' as feasible.

  // isLower reports whether the rune is a lower case letter.
  private[regex] def isLower(r: Int): Boolean = {
    if (r <= MAX_LATIN1) {
      Character.isLowerCase(r.toChar)
    } else {
      is(UnicodeTables.Lower, r)
    }
  }

  // isUpper reports whether the rune is an upper case letter.
  private[regex] def isUpper(r: Int): Boolean = {
    if (r <= MAX_LATIN1) {
      Character.isUpperCase(r.toChar)
    } else {
      is(UnicodeTables.Upper, r)
    }
  }

  // isTitle reports whether the rune is a title case letter.
  private[regex] def isTitle(r: Int): Boolean = {
    if (r <= MAX_LATIN1) {
      false
    } else {
      is(UnicodeTables.Title, r)
    }
  }

  /// Make visible to Utils.scala
  // isPrint reports whether the rune is printable (Unicode L/M/N/P/S or ' ').
  private[regex] def isPrint(r: Int): Boolean = {
    if (r <= MAX_LATIN1) {
      r >= 0x20 && r < 0x7F ||
      r >= 0xA1 && r != 0xAD
    } else {
      is(UnicodeTables.L, r) ||
      is(UnicodeTables.M, r) ||
      is(UnicodeTables.N, r) ||
      is(UnicodeTables.P, r) ||
      is(UnicodeTables.S, r)
    }
  }

  private def to(kase: Int, r: Int, caseRange: Array[Int]): Int = {
    if (kase < 0 || MAX_CASE <= kase) {
      REPLACEMENT_CHAR // as reasonable an error as any
    } else {
      // binary search over ranges

      val indexStep = 5 // Number of column in a logical row.

      var lo = 0
      var hi = caseRange.length

      var found = -1

      while (lo < hi) {
        val m    = midpoint(lo, hi, indexStep)
        val crlo = caseRange(m)
        val crhi = caseRange(m + 1)

        if (crlo <= r && r <= crhi) {
          lo = hi // Done! Exit loop.

          val delta = caseRange(m + 2 + kase)
          if (delta <= Unicode.MAX_RUNE) {
            found = r + delta
          } else {
            // In an Upper-Lower sequence, which always starts with
            // an UpperCase letter, the real deltas always look like:
            //	  {0, 1, 0}    UpperCase (Lower is next)
            //	  {-1, 0, -1}  LowerCase (Upper, Title are previous)
            // The characters at even offsets from the beginning of the
            // sequence are upper case the ones at odd offsets are lower.
            // The correct mapping can be done by clearing or setting the low
            // bit in the sequence offset.
            // The constants UpperCase and TitleCase are even while LowerCase
            // is odd so we take the low bit from kase.

            found = crlo + (((r - crlo) & ~1) | (kase & 1))
          }
        } else if (r < crlo) {
          hi = m // Search lower half, no indexStep decrement, range is [)
        } else {
          lo = m + indexStep // Search upper half
        }
      }

      if (found >= 0) found else r
    }
  }

  // to maps the rune to specified case: UpperCase, LowerCase, or TitleCase.

  private def to(kase: Int, r: Int): Int =
    to(kase, r, UnicodeTables.CASE_RANGES)

  // toUpper maps the rune to upper case.
  private def toUpper(r: Int): Int = {
    if (r <= MAX_ASCII) {
      var res = r
      if ('a' <= r && r <= 'z') {
        res -= 'a' - 'A'
      }
      res
    } else {
      to(UnicodeTables.UpperCase, r)
    }
  }

  // toLower maps the rune to lower case.
  private def toLower(r: Int): Int = {
    if (r <= MAX_ASCII) {
      var res = r
      if ('A' <= r && r <= 'Z') {
        res += 'a' - 'A'
      }
      res
    } else {
      to(UnicodeTables.LowerCase, r)
    }
  }

  // simpleFold iterates over Unicode code points equivalent under
  // the Unicode-defined simple case folding.  Among the code points
  // equivalent to rune (including rune itself), SimpleFold returns the
  // smallest r >= rune if one exists, or else the smallest r >= 0.
  //
  // For example:
  //      SimpleFold('A') = 'a'
  //      SimpleFold('a') = 'A'
  //
  //      SimpleFold('K') = 'k'
  //      SimpleFold('k') = '\u212A' (Kelvin symbol, K)
  //      SimpleFold('\u212A') = 'K'
  //
  //      SimpleFold('1') = '1'
  //
  // Derived from Go's unicode.SimpleFold.

  def simpleFold(r: Int): Int = {

    // Consult caseOrbit table for special cases.

    val indexStep = 2 // Number of column in a logical row.

    var lo = 0
    var hi = UnicodeTables.CASE_ORBIT.length

    var found = -1

    while (lo < hi) {
      val m = midpoint(lo, hi, indexStep)

      r.compare(UnicodeTables.CASE_ORBIT(m)) match {

        case -1 =>
          hi = m // Search lower half, no indexStep decrement, range is [)

        case 0 =>
          found = m
          lo = hi // Done! Exit loop.

        case 1 =>
          lo = m + indexStep // Search upper half
      }
    }

    val result = if (found >= 0) {
      UnicodeTables.CASE_ORBIT(found + 1)
    } else {
      // No folding specified.  This is a one- or two-element
      // equivalence class containing rune and toLower(rune)
      // and toUpper(rune) if they are different from rune.
      val l = toLower(r)
      if (l != r) l else toUpper(r)
    }

    result
  }

}
