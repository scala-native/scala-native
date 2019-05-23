package scala.scalanative
package regex

import regex.Utils._

object CharClassSuite extends tests.Suite {
  private def cc(arr: Array[Int]): CharClass = new CharClass(arr)

  private def s(str: String): Array[Int] = stringToRunes(str)

  private def assertClass(cc: CharClass, expected: Array[Int]): Unit = {
    val actual = cc.toArray
    if (actual.deep != expected.deep)
      throw new AssertionError(
        "Incorrect CharClass value:\n" + "Expected: " + expected
          .mkString(", ") + "\n" + "Actual:   " + actual.mkString(", "))
  }

  test("CleanClass") {
    assertClass(cc(Array.emptyIntArray).cleanClass, Array.emptyIntArray)
    assertClass(cc(Array(10, 20, 10, 20, 10, 20)).cleanClass, Array(10, 20))
    assertClass(cc(Array(10, 20)).cleanClass, Array(10, 20))
    assertClass(cc(Array(10, 20, 20, 30)).cleanClass, Array(10, 30))
    assertClass(cc(Array(10, 20, 30, 40, 20, 30)).cleanClass, Array(10, 40))
    assertClass(cc(Array(0, 50, 20, 30)).cleanClass, Array(0, 50))
    assertClass(cc(Array(10, 11, 13, 14, 16, 17, 19, 20, 22, 23)).cleanClass,
                Array(10, 11, 13, 14, 16, 17, 19, 20, 22, 23))
    assertClass(cc(Array(13, 14, 10, 11, 22, 23, 19, 20, 16, 17)).cleanClass,
                Array(10, 11, 13, 14, 16, 17, 19, 20, 22, 23))
    assertClass(cc(Array(13, 14, 10, 11, 22, 23, 19, 20, 16, 17)).cleanClass,
                Array(10, 11, 13, 14, 16, 17, 19, 20, 22, 23))
    assertClass(
      cc(Array(13, 14, 10, 11, 22, 23, 19, 20, 16, 17, 5, 25)).cleanClass,
      Array(5, 25))
    assertClass(
      cc(Array(13, 14, 10, 11, 22, 23, 19, 20, 16, 17, 12, 21)).cleanClass,
      Array(10, 23))
    assertClass(cc(Array(0, Unicode.MAX_RUNE)).cleanClass,
                Array(0, Unicode.MAX_RUNE))
    assertClass(cc(Array(0, 50)).cleanClass, Array(0, 50))
    assertClass(cc(Array(50, Unicode.MAX_RUNE)).cleanClass,
                Array(50, Unicode.MAX_RUNE))
  }

  test("AppendLiteral") {
    assertClass(cc(Array.emptyIntArray).appendLiteral('a', 0), Array('a', 'a'))
    assertClass(cc(Array('a', 'f')).appendLiteral('a', 0), Array('a', 'f'))
    assertClass(cc(Array('b', 'f')).appendLiteral('a', 0), Array('a', 'f'))
    assertClass(cc(Array('a', 'f')).appendLiteral('g', 0), Array('a', 'g'))
    assertClass(cc(Array('a', 'f')).appendLiteral('A', 0),
                Array('a', 'f', 'A', 'A'))
    assertClass(cc(Array.emptyIntArray).appendLiteral('A', RE2.FOLD_CASE),
                Array('A', 'A', 'a', 'a'))
    assertClass(cc(Array('a', 'f')).appendLiteral('a', RE2.FOLD_CASE),
                Array('a', 'f', 'A', 'A'))
    assertClass(cc(Array('b', 'f')).appendLiteral('a', RE2.FOLD_CASE),
                Array('a', 'f', 'A', 'A'))
    assertClass(cc(Array('a', 'f')).appendLiteral('g', RE2.FOLD_CASE),
                Array('a', 'g', 'G', 'G'))
    assertClass(cc(Array('a', 'f')).appendLiteral('A', RE2.FOLD_CASE),
                Array('a', 'f', 'A', 'A'))
    // ' ' is beneath the MIN-MAX_FOLD range.
    assertClass(cc(Array('a', 'f')).appendLiteral(' ', 0),
                Array('a', 'f', ' ', ' '))
    assertClass(cc(Array('a', 'f')).appendLiteral(' ', RE2.FOLD_CASE),
                Array('a', 'f', ' ', ' '))
  }

  test("AppendFoldedRange") {
    // These cases are derived directly from the program logic:

    // Range is full: folding can't add more.
    assertClass(cc(Array.emptyIntArray).appendFoldedRange(10, 0x10ff0),
                Array(10, 0x10ff0))
    // Range is outside folding possibilities.
    assertClass(cc(Array.emptyIntArray).appendFoldedRange(' ', '&'),
                Array(' ', '&'))
    // [lo, MIN_FOLD - 1] needs no folding.  Only [...abc] suffix is folded.
    assertClass(cc(Array.emptyIntArray).appendFoldedRange(' ', 'C'),
                Array(' ', 'C', 'a', 'c'))
    // [MAX_FOLD...] needs no folding
    assertClass(cc(Array.emptyIntArray).appendFoldedRange(0x10400, 0x104f0),
                Array(0x10450, 0x104f0, 0x10400, 0x10426, // lowercase Deseret
                  0x10426, 0x1044f)) // uppercase Deseret, abutting.
  }

  test("AppendClass") {
    assertClass(cc(Array.emptyIntArray).appendClass(Array('a', 'z')),
                Array('a', 'z'))
    assertClass(cc(Array('a', 'f')).appendClass(Array('c', 't')),
                Array('a', 't'))
    assertClass(cc(Array('c', 't')).appendClass(Array('a', 'f')),
                Array('a', 't'))
  }

  test("AppendNegatedClass") {
    assertClass(cc(Array('d', 'e')).appendNegatedClass(Array('b', 'f')),
                Array('d', 'e', 0, 'a', 'g', Unicode.MAX_RUNE))
  }

  test("AppendFoldedClass") {
    // NB, local variable names use Unicode.
    // 0x17F is an old English long s (looks like an f) and folds to s.
    // 0x212A is the Kelvin symbol and folds to k.
    val ſ = 0x17F
    val K = 0x212A

    assertClass(cc(Array.emptyIntArray).appendFoldedClass(Array('a', 'z')),
                s(s"akAKKKlsLSſſtzTZ"))
    assertClass(cc(Array('a', 'f')).appendFoldedClass(Array('c', 't')),
                s("akCKKKlsLSſſttTT"))
    assertClass(cc(Array('c', 't')).appendFoldedClass(Array('a', 'f')),
                Array('c', 't', 'a', 'f', 'A', 'F'))
  }

  test("NegateClass") {
    assertClass(cc(Array.emptyIntArray).negateClass,
                Array('\u0000', Unicode.MAX_RUNE))
    assertClass(cc(Array('A', 'Z')).negateClass,
                Array('\u0000', '@', '[', Unicode.MAX_RUNE))
    assertClass(cc(Array('A', 'Z', 'a', 'z')).negateClass,
                Array('\u0000', '@', '[', '`', '{', Unicode.MAX_RUNE))
  }

  test("AppendTable") {
    assertClass(cc(Array.emptyIntArray)
                  .appendTable(Array('a', 'z', 1, 'A', 'M', 4)),
                Array('a', 'z', 'A', 'A', 'E', 'E', 'I', 'I', 'M', 'M'))
    assertClass(cc(Array.emptyIntArray).appendTable(Array('Ā', 'Į', 2)),
                s("ĀĀĂĂĄĄĆĆĈĈĊĊČČĎĎĐĐĒĒĔĔĖĖĘĘĚĚĜĜĞĞĠĠĢĢĤĤĦĦĨĨĪĪĬĬĮĮ"))
    assertClass(cc(Array.emptyIntArray).appendTable(Array('Ā' + 1, 'Į' + 1, 2)),
                s("āāăăąąććĉĉċċččďďđđēēĕĕėėęęěěĝĝğğġġģģĥĥħħĩĩīīĭĭįį"))
    assertClass(cc(Array.emptyIntArray).appendNegatedTable(Array('b', 'f', 1)),
                Array(0, 'a', 'g', Unicode.MAX_RUNE))
  }

// format: off

  test("AppendNegatedTable") {
    // stride == 1
    assertClass(
      cc(Array.emptyIntArray).appendNegatedTable(Array('b', 'f', 1)),
      Array(0, 'a', 'g', Unicode.MAX_RUNE))

    // stride > 1

    // 0x0138  Char = ĸ Decimal 312 Latin small letter 'kra'
    // 0x0148  Char = ň Decimal 328 Latin small letter 'n' with caron

    assertClass(
      cc(Array.emptyIntArray).appendNegatedTable(Array(0x0138, 0x0148, 2)),
      Array(0x0000, 0x0137,
            0x0139, 0x0139,
            0x013b, 0x013b,
            0x013d, 0x013d,
            0x013f, 0x013f,
            0x0141, 0x0141,
            0x0143, 0x0143,
            0x0145, 0x0145,
            0x0147, 0x0147,
            0x0149, Unicode.MAX_RUNE))


    // stride > 2 appendRange should coalesce abutting ranges.
    
    assertClass(
      cc(Array.emptyIntArray).appendNegatedTable(Array(0x0138, 0x0148, 3)),
      Array(0x0000, 0x0137,
            0x0139, 0x013A,
            0x013c, 0x013d,
            0x013f, 0x0140,
            0x0142, 0x0143,
            0x0145, 0x0146,
            0x0149, Unicode.MAX_RUNE))
  }

// format: on

  test("AppendGroup") {
    assertClass(cc(Array.emptyIntArray)
                  .appendGroup(CharGroup.PERL_GROUPS("\\d"), false),
                Array('0', '9'))
    assertClass(cc(Array.emptyIntArray)
                  .appendGroup(CharGroup.PERL_GROUPS("\\D"), false),
                Array(0, '/', ':', Unicode.MAX_RUNE))
  }

  test("ToString") {
    assert("[0xa 0xc-0x14]" == cc(Array(10, 10, 12, 20)).toString)
  }

}
