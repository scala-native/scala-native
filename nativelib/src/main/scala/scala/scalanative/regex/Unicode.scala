/// This code has been extensively modified for Scala Native.
///
/// This is the original copyright notice and must be maintained.
//
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
//	   Highly modified by Lee Tibbert for Scala Native.

import scala.collection.immutable.HashSet

object Unicode {

  // The highest valid rune value.
  final val MAX_RUNE = 0x10FFFF

  // The highest valid Latin-1 value.
  private final val MAX_LATIN1 = 0xFF

  // Minimum and maximum runes involved in folding.
  // Checked during test.
  private[regex] final val MIN_FOLD = 0x0041
  private[regex] final val MAX_FOLD = 0x1044f

  // Format: off
   private final val printableTypes = HashSet(
       // Letters, L*
       Character.UPPERCASE_LETTER,
       Character.LOWERCASE_LETTER,
       Character.TITLECASE_LETTER,
       Character.MODIFIER_LETTER,
       Character.OTHER_LETTER,

       // Numbers, N*
       Character.DECIMAL_DIGIT_NUMBER,
       Character.LETTER_NUMBER,
       Character.OTHER_NUMBER,

       // Punctuation, P*
       Character.CONNECTOR_PUNCTUATION,
       Character.DASH_PUNCTUATION,
       Character.FINAL_QUOTE_PUNCTUATION,
       Character.END_PUNCTUATION,
       Character.INITIAL_QUOTE_PUNCTUATION,
       Character.OTHER_PUNCTUATION,
       Character.START_PUNCTUATION,

       // Symbols. S*
       Character.CURRENCY_SYMBOL,
       Character.MATH_SYMBOL,
       Character.MODIFIER_SYMBOL,
       Character.OTHER_SYMBOL,

       // Marks, M*
       Character.COMBINING_SPACING_MARK,
       Character.ENCLOSING_MARK,
       Character.NON_SPACING_MARK
     )
     // Format: on

  /// Make visible to Utils.scala
  // isPrint reports whether the rune is printable (Unicode L/M/N/P/S or ' ').
  //
  // Java has no isPrint() method, probably because the concept quickly
  // becomes entangled with Locale. This implements the RE2 concept.
  private[regex] def isPrint(r: Int): Boolean = {
    if (r <= MAX_LATIN1) {
      (r >= 0x20 && r < 0x7F) || (r >= 0xA1 && r != 0xAD)
    } else {
      val t = Character.getType(r)
      printableTypes(t.toByte)
    }
  }

  /// For Scala Native the original concept below is maintained but the
  /// implementation is simplified by the use Scala idioms
  //
  // simpleFold iterates over Unicode code points equivalent under
  // the Unicode-defined simple case folding.  Among the code points
  // equivalent to rune (including rune itself), SimpleFold returns the
  // smallest r >= rune if one exists, or else the smallest r >= 0.
  //
  // For example:
  //	  SimpleFold('A') = 'a'
  //	  SimpleFold('a') = 'A'
  //
  //	  SimpleFold('K') = 'k'
  //	  SimpleFold('k') = '\u212A' (Kelvin symbol, K)
  //	  SimpleFold('\u212A') = 'K'
  //
  //	  SimpleFold('1') = '1'
  //
  // Derived from Go's unicode.SimpleFold.

  def simpleFold(r: Int): Int = {

    // Consult caseOrbit table for special cases.

    val result = UnicodeTables.CASE_ORBIT.get(r) match {
      case Some(cp) => cp
      case None     =>
        // No folding specified.  This is a one- or two-element
        // equivalence class containing rune and toLower(rune)
        // and toUpper(rune) if they are different from rune.

        Character.getType(r) match {
          case Character.LOWERCASE_LETTER => Character.toUpperCase(r)
          case Character.UPPERCASE_LETTER => Character.toLowerCase(r)
          case Character.TITLECASE_LETTER => Character.toLowerCase(r)
          case _                          => r
        }
    }

    result
  }

}
