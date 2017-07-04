// Copyright 2010 The Go Authors. All rights reserved.
// Use of this source code is governed by a BSD-style
// license that can be found in the LICENSE file.

// Original Go source here:
// http://code.google.com/p/go/source/browse/src/pkg/regexp/syntax/regexp.go

package java.util.regex

import java.util.Arrays

/**
 * Regular expression abstract syntax tree.
 * Produced by parser, used by compiler.
 * NB, this corresponds to {@code syntax.regexp} in the Go implementation
 * Go's {@code regexp} is called {@code RE2} in Java.
 */
class Regexp private {
  import Regexp._

  var op: Op              = _ // operator
  var flags: Int          = _ // bitmap of parse flags
  var subs: Array[Regexp] = _ // subexpressions, if any.  Never null.
  // subs[0] is used as the freelist.
  var runes: Array[Int] = _ // matched runes, for LITERAL, CHAR_CLASS
  var min, max: Int     = _ // min, max for REPEAT
  var cap: Int          = _ // capturing index, for CAPTURE
  var name: String      = _ // capturing name, for CAPTURE

  def this(op: Regexp.Op) = {
    this()
    this.op = op
  }

  // Shallow copy constructor.
  def this(that: Regexp) = {
    this()
    this.op = that.op
    this.flags = that.flags
    this.subs = that.subs
    this.runes = that.runes
    this.min = that.min
    this.max = that.max
    this.cap = that.cap
    this.name = that.name
  }

  def reinit(): Unit = {
    this.flags = 0
    subs = EMPTY_SUBS
    runes = null
    min = 0
    max = 0
    cap = 0
    name = null
  }

  override def toString = {
    val out = new java.lang.StringBuilder
    appendTo(out)
    out.toString
  }

  // appendTo() appends the Perl syntax for |this| regular expression to |out|.
  private def appendTo(out: java.lang.StringBuilder): Unit = {
    (op: @scala.annotation.switch) match {
      case Op.NO_MATCH =>
        out.append("[^\\x00-\\x{10FFFF}]")
      case Op.EMPTY_MATCH =>
        out.append("(?:)")
      case Op.STAR | Op.PLUS | Op.QUEST | Op.REPEAT =>
        val sub = subs(0)
        if (sub.op > Op.CAPTURE ||
            sub.op == Op.LITERAL && sub.runes.length > 1) {
          out.append("(?:")
          sub.appendTo(out)
          out.append(')')
        } else {
          sub.appendTo(out)
        }
        (op: @scala.annotation.switch) match {
          case Op.STAR =>
            out.append('*')
          case Op.PLUS =>
            out.append('+')
          case Op.QUEST =>
            out.append('?')
          case Op.REPEAT =>
            out.append('{').append(min)
            if (min != max) {
              out.append(',')
              if (max >= 0) {
                out.append(max)
              }
            }
            out.append('}')
        }
        if ((flags & RE2.NON_GREEDY) != 0) {
          out.append('?')
        }
      case Op.CONCAT =>
        var i = 0
        while (i <= subs.length) {
          val sub = subs(i)
          if (sub.op == Op.ALTERNATE) {
            out.append("(?:")
            sub.appendTo(out)
            out.append(')')
          } else {
            sub.appendTo(out)
          }
          i += 1
        }
      case Op.ALTERNATE =>
        var sep = ""
        var i   = 0
        while (i <= subs.length) {
          val sub = subs(i)
          out.append(sep)
          sep = "|"
          sub.appendTo(out)
          i += 1
        }
      case Op.LITERAL =>
        if ((flags & RE2.FOLD_CASE) != 0) {
          out.append("(?i:")
        }
        var i = 0
        while (i < runes.length) {
          val rune = runes(i)
          Utils.escapeRune(out, rune)
          i += 1
        }
        if ((flags & RE2.FOLD_CASE) != 0) {
          out.append(')')
        }
      case Op.ANY_CHAR_NOT_NL =>
        out.append("(?-s:.)")
      case Op.ANY_CHAR =>
        out.append("(?s:.)")
      case Op.CAPTURE =>
        if (name == null || name.isEmpty()) {
          out.append('(')
        } else {
          out.append("(?P<")
          out.append(name)
          out.append(">")
        }
        if (subs(0).op != Op.EMPTY_MATCH) {
          subs(0).appendTo(out)
        }
        out.append(')')
      case Op.BEGIN_TEXT =>
        out.append("\\A")
      case Op.END_TEXT =>
        if ((flags & RE2.WAS_DOLLAR) != 0) {
          out.append("(?-m:$)")
        } else {
          out.append("\\z")
        }
      case Op.BEGIN_LINE =>
        out.append('^')
      case Op.END_LINE =>
        out.append('$')
      case Op.WORD_BOUNDARY =>
        out.append("\\b")
      case Op.NO_WORD_BOUNDARY =>
        out.append("\\B")
      case Op.CHAR_CLASS =>
        if (runes.length % 2 != 0) {
          out.append("[invalid char class]")
        } else {
          out.append('[')
          if (runes.length == 0) {
            out.append("^\\x00-\\x{10FFFF}")
          } else if (runes(0) == 0 &&
                     runes(runes.length - 1) == Unicode.MAX_RUNE) {
            // Contains 0 and MAX_RUNE.  Probably a negated class.
            // Print the gaps.
            out.append('^')
            var i = 1
            while (i < runes.length - 1) {
              val lo = runes(i) + 1
              val hi = runes(i + 1) - 1
              quoteIfHyphen(out, lo)
              Utils.escapeRune(out, lo)
              if (lo != hi) {
                out.append('-')
                quoteIfHyphen(out, hi)
                Utils.escapeRune(out, hi)
              }
              i += 2
            }
          } else {
            var i = 0
            while (i < runes.length) {
              val lo = runes(i)
              val hi = runes(i + 1)
              quoteIfHyphen(out, lo)
              Utils.escapeRune(out, lo)
              if (lo != hi) {
                out.append('-')
                quoteIfHyphen(out, hi)
                Utils.escapeRune(out, hi)
              }
              i += 2
            }
          }
          out.append(']')
        }
      case _ => // incl. pseudos
        out.append(op)
    }
  }

  // maxCap() walks the regexp to find the maximum capture index.
  def maxCap(): Int = {
    var m = 0
    if (op == Op.CAPTURE) {
      m = cap
    }
    if (subs != null) {
      var i = 0
      while (i < subs.length) {
        val sub = subs(i)
        val n   = sub.maxCap()
        if (m < n) {
          m = n
        }
        i += 1
      }
    }
    m
  }

  // equals() returns true if this and that have identical structure.
  override def equals(that: Any): Boolean = {
    if (!that.isInstanceOf[Regexp]) {
      return false
    }
    val x = this
    val y = that.asInstanceOf[Regexp]
    if (x.op != y.op) {
      return false
    }
    (x.op: @scala.annotation.switch) match {
      case Op.END_TEXT =>
        // The parse flags remember whether this is \z or \Z.
        if ((x.flags & RE2.WAS_DOLLAR) != (y.flags & RE2.WAS_DOLLAR)) {
          return false
        }
      case Op.LITERAL | Op.CHAR_CLASS =>
        if (!Arrays.equals(x.runes, y.runes)) {
          return false
        }
      case Op.ALTERNATE | Op.CONCAT =>
        if (x.subs.length != y.subs.length) {
          return false
        }
        var i = 0
        while (i < x.subs.length) {
          if (!x.subs(i).equals(y.subs(i))) {
            return false
          }
          i += 1
        }
      case Op.STAR | Op.PLUS | Op.QUEST =>
        if ((x.flags & RE2.NON_GREEDY) != (y.flags & RE2.NON_GREEDY) ||
            !x.subs(0).equals(y.subs(0))) {
          return false
        }
      case Op.REPEAT =>
        if ((x.flags & RE2.NON_GREEDY) != (y.flags & RE2.NON_GREEDY) ||
            x.min != y.min || x.max != y.max || !x.subs(0).equals(y.subs(0))) {
          return false
        }
      case Op.CAPTURE =>
        if (x.cap != y.cap || x.name != y.name ||
            !x.subs(0).equals(y.subs(0))) {
          return false
        }
    }
    return true
  }

}

object Regexp {
  type Op = Int
  object Op {
    final val NO_MATCH         = 0  // Matches no strings.
    final val EMPTY_MATCH      = 1  // Matches empty string.
    final val LITERAL          = 2  // Matches runes[] sequence
    final val CHAR_CLASS       = 3  // Matches Runes interpreted as range pair list
    final val ANY_CHAR_NOT_NL  = 4  // Matches any character except '\n'
    final val ANY_CHAR         = 5  // Matches any character
    final val BEGIN_LINE       = 6  // Matches empty string at end of line
    final val END_LINE         = 7  // Matches empty string at end of line
    final val BEGIN_TEXT       = 8  // Matches empty string at beginning of text
    final val END_TEXT         = 9  // Matches empty string at end of text
    final val WORD_BOUNDARY    = 10 // Matches word boundary `\b`
    final val NO_WORD_BOUNDARY = 11 // Matches word non-boundary `\B`
    final val CAPTURE          = 12 // Capturing subexpr with index cap, optional name name
    final val STAR             = 13 // Matches subs[0] zero or more times.
    final val PLUS             = 14 // Matches subs[0] one or more times.
    final val QUEST            = 15 // Matches subs[0] zero or one times.
    final val REPEAT           = 16 // Matches subs[0] [min, max] times max=-1 => no limit.
    final val CONCAT           = 17 // Matches concatenation of subs[]
    final val ALTERNATE        = 18 // Matches union of subs[]

    // Pseudo ops, used internally by Parser for parsing stack:
    final val LEFT_PAREN   = 19
    final val VERTICAL_BAR = 20

    def isPseudo(op: Op): Boolean = op >= LEFT_PAREN
  }

  final val EMPTY_SUBS = new Array[Regexp](0)

  private def quoteIfHyphen(out: java.lang.StringBuilder, rune: Int): Unit = {
    if (rune == '-') {
      out.append('\\')
    }
  }
}
