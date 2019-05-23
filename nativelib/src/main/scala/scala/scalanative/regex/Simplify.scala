// Copyright 2011 The Go Authors.  All rights reserved.
// Use of this source code is governed by a BSD-style
// license that can be found in the LICENSE file.

// Original Go source here:
// http://code.google.com/p/go/source/browse/src/pkg/regexp/syntax/simplify.go

package scala.scalanative
package regex

import java.util.ArrayList
import Regexp.{Op => ROP}

object Simplify {

  // Simplify returns a regexp equivalent to re but without counted
  // repetitions and with various other simplifications, such as
  // rewriting /(?:a+)+/ to /a+/.  The resulting regexp will execute
  // correctly but its string representation will not produce the same
  // parse tree, because capturing parentheses may have been duplicated
  // or removed.  For example, the simplified form for /(x){1,2}/ is
  // /(x)(x)?/ but both parentheses capture as $1.  The returned regexp
  // may share structure with or be the original.
  def simplify(re: Regexp): Regexp = {
    if (re == null) {
      return null
    }
    (re.op: @scala.annotation.switch) match {
      case ROP.CAPTURE | ROP.CONCAT | ROP.ALTERNATE =>
        // Simplify children, building new Regexp if children change.
        var nre = re
        var i   = 0
        while (i < re.subs.length) {
          val sub  = re.subs(i)
          val nsub = simplify(sub)
          if (nre == re && nsub != sub) {
            // Start a copy.
            nre = new Regexp(re) // shallow copy
            nre.runes = null
            nre.subs = Parser.subarray(re.subs, 0, re.subs.length) // clone
          }
          if (nre != re) {
            nre.subs(i) = nsub
          }
          i += 1
        }
        nre
      case ROP.STAR | ROP.PLUS | ROP.QUEST =>
        val sub = simplify(re.subs(0))
        simplify1(re.op, re.flags, sub, re)
      case ROP.REPEAT =>
        // Special special case: x{0} matches the empty string
        // and doesn't even need to consider x.
        if (re.min == 0 && re.max == 0) {
          return new Regexp(ROP.EMPTY_MATCH)
        }

        // The fun begins.
        val sub = simplify(re.subs(0))

        // x{n,} means at least n matches of x.
        if (re.max == -1) {
          // Special case: x{0,} is x*.
          if (re.min == 0) {
            return simplify1(ROP.STAR, re.flags, sub, null)
          }

          // Special case: x{1,} is x+.
          if (re.min == 1) {
            return simplify1(ROP.PLUS, re.flags, sub, null)
          }

          // General case: x{4,} is xxxx+.
          val nre  = new Regexp(ROP.CONCAT)
          val subs = new ArrayList[Regexp]()
          var i    = 0
          while (i < re.min - 1) {
            subs.add(sub)
            i += 1
          }
          subs.add(simplify1(ROP.PLUS, re.flags, sub, null))
          nre.subs = subs.toArray(new Array[Regexp](subs.size()))
          return nre
        }

        // Special case x{0} handled above.

        // Special case: x{1} is just x.
        if (re.min == 1 && re.max == 1) {
          return sub
        }

        // General case: x{n,m} means n copies of x and m copies of x?
        // The machine will do less work if we nest the final m copies,
        // so that x{2,5} = xx(x(x(x)?)?)?

        // Build leading prefix: xx.
        var prefixSubs: ArrayList[Regexp] = null
        if (re.min > 0) {
          prefixSubs = new ArrayList[Regexp]()
          var i = 0
          while (i < re.min) {
            prefixSubs.add(sub)
            i += 1
          }
        }

        // Build and attach suffix: (x(x(x)?)?)?
        if (re.max > re.min) {
          var suffix = simplify1(ROP.QUEST, re.flags, sub, null)
          var i      = re.min + 1
          while (i < re.max) {
            val nre2 = new Regexp(ROP.CONCAT)
            nre2.subs = Array[Regexp](sub, suffix)
            suffix = simplify1(ROP.QUEST, re.flags, nre2, null)
            i += 1
          }
          if (prefixSubs == null) {
            return suffix
          }
          prefixSubs.add(suffix)
        }
        if (prefixSubs != null) {
          val prefix = new Regexp(ROP.CONCAT)
          prefix.subs = prefixSubs.toArray(new Array[Regexp](prefixSubs.size()))
          return prefix
        }

        // Some degenerate case like min > max or min < max < 0.
        // Handle as impossible match.
        new Regexp(ROP.NO_MATCH)
      case _ =>
        re
    }
  }

  // simplify1 implements Simplify for the unary OpStar,
  // OpPlus, and OpQuest operators.  It returns the simple regexp
  // equivalent to
  //
  //      Regexp{Op: op, Flags: flags, Sub: {sub}}
  //
  // under the assumption that sub is already simple, and
  // without first allocating that structure.  If the regexp
  // to be returned turns out to be equivalent to re, simplify1
  // returns re instead.
  //
  // simplify1 is factored out of Simplify because the implementation
  // for other operators generates these unary expressions.
  // Letting them call simplify1 makes sure the expressions they
  // generate are simple.
  private def simplify1(op: ROP,
                        flags: Int,
                        sub: Regexp,
                        _re: Regexp): Regexp = {
    var re = _re

    // Special case: repeat the empty string as much as
    // you want, but it's still the empty string.
    if (sub.op == ROP.EMPTY_MATCH) {
      return sub
    }
    // The operators are idempotent if the flags match.
    if (op == sub.op &&
        (flags & RE2.NON_GREEDY) == (sub.flags & RE2.NON_GREEDY)) {
      return sub
    }
    if (re != null && re.op == op &&
        (re.flags & RE2.NON_GREEDY) == (flags & RE2.NON_GREEDY) &&
        sub == re.subs(0)) {
      return re
    }

    re = new Regexp(op)
    re.flags = flags
    re.subs = Array[Regexp](sub)
    re
  }
}
