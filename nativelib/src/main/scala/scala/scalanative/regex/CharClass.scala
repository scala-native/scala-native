// Copyright 2010 The Go Authors. All rights reserved.
// Use of this source code is governed by a BSD-style
// license that can be found in the LICENSE file.

// Original Go source here:
// http://code.google.com/p/go/source/browse/src/pkg/regexp/syntax/parse.go

package scala.scalanative
package regex

// A "builder"-style helper class for manipulating character classes
// represented as an array of pairs of runes [lo, hi], each denoting an
// inclusive interval.
//
// All methods mutate the internal state and return {@code this}, allowing
// operations to be chained.
class CharClass private (unit: Unit) {
  import CharClass._

  // inclusive ranges, pairs of [lo,hi].  r.length is even.
  private var r: Array[Int] = _

  // prefix of |r| that is defined.  Even.
  private var len: Int = _

  // Constructs a CharClass with initial ranges |r|.
  // The right to mutate |r| is passed to the callee.
  def this(r: Array[Int]) {
    this(())
    this.r = r
    this.len = r.length
  }

  // Size the initial allocaton to reduce the number of doublings & copies
  // yet still be provident with memory.
  // 16 bytes is a best guess. See commit mesage for details on its derivation.

  // Constructs an empty CharClass.
  def this() {
    this(())
    val initialCapacity = 16
    this.r = new Array[Int](initialCapacity)
    this.len = 0
  }

  // After a call to ensureCapacity(), |r.length| is at least |newLen|.
  private def ensureCapacity(_newLen: Int): Unit = {
    var newLen = _newLen
    if (r.length < newLen) {
      // Expand by at least doubling, except when len == 0.
      if (newLen < len * 2) {
        newLen = len * 2
      }
      val r2 = new Array[Int](newLen)
      System.arraycopy(r, 0, r2, 0, len)
      r = r2
    }
  }

  // Returns the character class as an int array.  Subsequent CharClass
  // operations may mutate this array, so typically this is the last operation
  // performed on a given CharClass instance.
  def toArray(): Array[Int] = {
    if (this.len == r.length) {
      r
    } else {
      val r2 = new Array[Int](len)
      System.arraycopy(r, 0, r2, 0, len)
      r2
    }
  }

  // cleanClass() sorts the ranges (pairs of elements) of this CharClass,
  // merges them, and eliminates duplicates.
  def cleanClass(): CharClass = {

    if (len >= 4) { // < 4 is clean by definition.
      // Sort by lo increasing, hi decreasing to break ties.
      qsortIntPair(r, 0, len - 2)

      // Merge abutting, overlapping.
      var w = 2 // write index
      var i = 2
      while (i < len) {
        val lo = r(i)
        val hi = r(i + 1)
        if (lo <= r(w - 1) + 1) {
          // merge with previous range
          if (hi > r(w - 1)) {
            r(w - 1) = hi
          }
        } else {
          // new disjoint range
          r(w) = lo
          r(w + 1) = hi
          w += 2
        }
        i += 2
      }
      len = w
    }

    this
  }

  // appendLiteral() appends the literal |x| to this CharClass.
  def appendLiteral(x: Int, flags: Int): CharClass = {
    if ((flags & RE2.FOLD_CASE) != 0) {
      appendFoldedRange(x, x)
    } else {
      appendRange(x, x)
    }
  }

  // appendRange() appends the range [lo-hi] (inclusive) to this CharClass.
  def appendRange(lo: Int, hi: Int): CharClass = {
    // Expand last range or next to last range if it overlaps or abuts.
    // Checking two ranges helps when appending case-folded
    // alphabets, so that one range can be expanding A-Z and the
    // other expanding a-z.

    var coalesced = false

    if (len > 0) {
      var i   = 2
      val end = 4
      while (i <= end) {
        if (len >= i) {
          val rlo = r(len - i)
          val rhi = r(len - i + 1)
          if (lo <= rhi + 1 && rlo <= hi + 1) {
            if (lo < rlo) {
              r(len - i) = lo
            }
            if (hi > rhi) {
              r(len - i + 1) = hi
            }
            coalesced = true
            i = end // loop done
          }
        }
        i += 2
      }
    }

    // Can't coalesce append.	Expand capacity by doubling as needed.
    if (!coalesced) {
      ensureCapacity(len + 2)
      r(len) = lo
      len += 1
      r(len) = hi
      len += 1
    }
    this
  }

  // appendFoldedRange() appends the range [lo-hi] and its case
  // folding-equivalent runes to this CharClass.
  def appendFoldedRange(_lo: Int, _hi: Int): CharClass = {
    var lo = _lo
    var hi = _hi

    // Optimizations.
    if (lo <= Unicode.MIN_FOLD && hi >= Unicode.MAX_FOLD) {
      // Range is full: folding can't add more.
      appendRange(lo, hi)
    } else if (hi < Unicode.MIN_FOLD || lo > Unicode.MAX_FOLD) {
      // Range is outside folding possibilities.
      appendRange(lo, hi)
    } else {

      if (lo < Unicode.MIN_FOLD) {
        // [lo, minFold-1] needs no folding.
        appendRange(lo, Unicode.MIN_FOLD - 1)
        lo = Unicode.MIN_FOLD
      }

      if (hi > Unicode.MAX_FOLD) {
        // [maxFold+1, hi] needs no folding.
        appendRange(Unicode.MAX_FOLD + 1, hi)
        hi = Unicode.MAX_FOLD
      }

      // Brute force.  Depend on appendRange to coalesce ranges on the fly.
      var c = lo
      while (c <= hi) {
        appendRange(c, c)
        var f = Unicode.simpleFold(c)
        while (f != c) {
          appendRange(f, f)
          f = Unicode.simpleFold(f)
        }
        c += 1
      }
    }

    this
  }

  // appendClass() appends the class |x| to this CharClass.
  // It assumes |x| is clean.  Does not mutate |x|.
  def appendClass(x: Array[Int]): CharClass = {
    var i = 0
    while (i < x.length) {
      appendRange(x(i), x(i + 1))
      i += 2
    }

    this
  }

  // appendFoldedClass() appends the case folding of the class |x| to this
  // CharClass.	 Does not mutate |x|.
  def appendFoldedClass(x: Array[Int]): CharClass = {
    var i = 0
    while (i < x.length) {
      appendFoldedRange(x(i), x(i + 1))
      i += 2
    }

    this
  }

  // appendNegatedClass() append the negation of the class |x| to this
  // CharClass.	 It assumes |x| is clean.  Does not mutate |x|.
  def appendNegatedClass(x: Array[Int]): CharClass = {
    var nextLo = 0
    var i      = 0
    while (i < x.length) {
      val lo = x(i)
      val hi = x(i + 1)
      if (nextLo <= lo - 1) {
        appendRange(nextLo, lo - 1)
      }
      nextLo = hi + 1
      i += 2
    }
    if (nextLo <= Unicode.MAX_RUNE) {
      appendRange(nextLo, Unicode.MAX_RUNE)
    }

    this
  }

  // appendTable() appends the Unicode range table |table| to this CharClass.
  // Does not mutate |table|.
  def appendTable(table: Array[Int]): CharClass = {

    val indexStep = 3 // Number of column in a logical row.

    var i = 0

    while (i < table.length) {
      val lo     = table(i + 0)
      val hi     = table(i + 1)
      val stride = table(i + 2)

      if (stride == 1) {
        appendRange(lo, hi)
      } else {
        var c = lo
        while (c <= hi) {
          appendRange(c, c)
          c += stride
        }
      }
      i += indexStep
    }

    this
  }

  // appendNegatedTable() returns the result of appending the negation of range
  // table |table| to this CharClass.  Does not mutate |table|.
  def appendNegatedTable(table: Array[Int]): CharClass = {

    val indexStep = 3 // Number of column in a logical row.

    var mark = 0 // character space
    var i    = 0 // array row

    while (i < table.length) {
      val lo     = table(i + 0)
      val hi     = table(i + 1)
      val stride = table(i + 2)

      if (mark < lo) {
        val newMark = lo - 1
        appendRange(mark, newMark)
        mark = newMark
      }

      if (stride > 1) {
        val candidates = List.range(lo, hi).filterNot(_ % stride == 0)

        // Keep this code simple (but slooow! Ok because it is not
        // frequently executed). Correctness is more important than
        // execution speed here

        // rely on appendRange coalescing abutting ranges.
        for (c <- candidates) {
          appendRange(c, c)
        }
      }

      mark = hi + 1

      i += indexStep
    }

    if (mark < Unicode.MAX_RUNE) {
      appendRange(mark, Unicode.MAX_RUNE)
    }

    this
  }

  // appendTableWithSign() calls append{,Negated}Table depending on sign.
  // Does not mutate |table|.
  def appendTableWithSign(table: Array[Int], sign: Int): CharClass = {
    if (sign < 0) {
      appendNegatedTable(table)
    } else {
      appendTable(table)
    }
  }

  // negateClass() negates this CharClass, which must already be clean.
  def negateClass(): CharClass = {
    var nextLo = 0 // lo end of next class to add
    var w      = 0 // write index
    var i      = 0
    while (i < len) {
      val lo = r(i)
      val hi = r(i + 1)
      if (nextLo <= lo - 1) {
        r(w) = nextLo
        r(w + 1) = lo - 1
        w += 2
      }
      nextLo = hi + 1
      i += 2
    }
    len = w

    if (nextLo <= Unicode.MAX_RUNE) {
      // It's possible for the negation to have one more
      // range - this one - than the original class, so use append.
      ensureCapacity(len + 2)
      r(len) = nextLo
      len += 1
      r(len) = Unicode.MAX_RUNE
      len += 1
    }

    this
  }

  // appendClassWithSign() calls appendClass() if sign is +1 or
  // appendNegatedClass if sign is -1.	Does not mutate |x|.
  def appendClassWithSign(x: Array[Int], sign: Int): CharClass = {
    if (sign < 0) {
      appendNegatedClass(x)
    } else {
      appendClass(x)
    }
  }

  // appendGroup() appends CharGroup |g| to this CharClass, folding iff
  // |foldCase|.  Does not mutate |g|.
  def appendGroup(g: CharGroup, foldCase: Boolean): CharClass = {
    var cls = g.cls
    if (foldCase) {
      cls = new CharClass().appendFoldedClass(cls).cleanClass().toArray()
    }

    appendClassWithSign(cls, g.sign)
  }

  override def toString =
    charClassToString(r, len)
}

object CharClass {

  // cmp() returns the ordering of the pair (a(i), a(i+1)) relative to
  // (pivotFrom, pivotTo), where the first component of the pair (lo) is
  // ordered naturally and the second component (hi) is in reverse order.
  @inline private def cmp(array: Array[Int],
                          i: Int,
                          pivotFrom: Int,
                          pivotTo: Int): Int = {
    val cmp = array(i) - pivotFrom

    if (cmp != 0) cmp else pivotTo - array(i + 1)
  }

  // qsortIntPair() quicksorts pairs of ints in |array| according to lt().
  // Precondition: |left|, |right|, |this.len| must all be even |this.len > 1|.
  private def qsortIntPair(array: Array[Int], left: Int, right: Int): Unit = {
    val pivotIndex = ((left + right) / 2) & ~1
    val pivotFrom  = array(pivotIndex)
    val pivotTo    = array(pivotIndex + 1)
    var i          = left
    var j          = right

    while (i <= j) {
      while (i < right && cmp(array, i, pivotFrom, pivotTo) < 0) {
        i += 2
      }
      while (j > left && cmp(array, j, pivotFrom, pivotTo) > 0) {
        j -= 2
      }
      if (i <= j) {
        if (i != j) {
          var temp = array(i)
          array(i) = array(j)
          array(j) = temp
          temp = array(i + 1)
          array(i + 1) = array(j + 1)
          array(j + 1) = temp
        }
        i += 2
        j -= 2
      }
    }
    if (left < j) {
      qsortIntPair(array, left, j)
    }
    if (i < right) {
      qsortIntPair(array, i, right)
    }
  }

  // Exposed, since useful for debugging CharGroups too.
  def charClassToString(r: Array[Int], len: Int): String = {
    val b = new StringBuilder()
    b.append('[')
    var i = 0
    while (i < len) {
      if (i > 0) {
        b.append(' ')
      }
      val lo = r(i)
      val hi = r(i + 1)
      // Avoid String.format (not available on GWT).
      // Cf. https://code.google.com/p/google-web-toolkit/issues/detail?id=3945
      if (lo == hi) {
        b.append("0x")
        b.append(Integer.toHexString(lo))
      } else {
        b.append("0x")
        b.append(Integer.toHexString(lo))
        b.append("-0x")
        b.append(Integer.toHexString(hi))
      }
      i += 2
    }
    b.append(']')
    b.toString()
  }
}
