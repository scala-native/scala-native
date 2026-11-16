package org.scalanative.testsuite.javalib.lang

import org.junit.Assert._
import org.junit.Test

/* Since JDK 16 the methods comparing strings ignoring case work on code points
 * instead of chars.
 */
class StringTestOnJDK16 {
  // Deseret: U+10400 DESERET CAPITAL LETTER LONG I, U+10428 its lowercase
  final val DeseretUpperI = "𐐀"
  final val DeseretLowerI = "𐐨"
  final val DeseretUpperE = "𐐁"
  final val DeseretLowerE = "𐐩"

  // Warang Citi: "𑢹𑣗𑣁𑣜𑣊" with various case changes
  final val WarangCiti = "𑢹𑣗𑣁𑣜𑣊"
  final val WarangCitiFirstLower =
    "𑣙𑣗𑣁𑣜𑣊"
  final val WarangCitiRestUpper =
    "𑢹𑢷𑢡𑢼𑢪"
  final val WarangCitiOtherLetter =
    "𑣙𑣗𑣁𑣏𑣊"

  @Test def equalsIgnoreCaseSupplementary(): Unit = {
    assertTrue(DeseretUpperI.equalsIgnoreCase(DeseretLowerI))
    assertTrue(DeseretLowerI.equalsIgnoreCase(DeseretUpperI))
    assertFalse(DeseretUpperI.equalsIgnoreCase(DeseretLowerE))
    assertTrue(
      ("a" + DeseretUpperI + "B" + DeseretLowerE)
        .equalsIgnoreCase("A" + DeseretLowerI + "b" + DeseretUpperE)
    )

    assertTrue(WarangCiti.equalsIgnoreCase(WarangCitiFirstLower))
    assertTrue(WarangCiti.equalsIgnoreCase(WarangCitiRestUpper))
    assertFalse(WarangCiti.equalsIgnoreCase(WarangCitiOtherLetter))

    // substrings sharing the backing array
    val shared = "xx" + DeseretUpperI + "yy"
    assertTrue(shared.substring(2, 4).equalsIgnoreCase(DeseretLowerI))
    assertTrue(DeseretLowerI.equalsIgnoreCase(shared.substring(2, 4)))

    // lone surrogates are compared as they are
    assertTrue("\ud801".equalsIgnoreCase("\ud801"))
    assertFalse("\udc00".equalsIgnoreCase("\udc28"))
    assertFalse("\ud801x".equalsIgnoreCase(DeseretUpperI))
  }

  @Test def compareToIgnoreCaseSupplementary(): Unit = {
    assertEquals(0, DeseretUpperI.compareToIgnoreCase(DeseretLowerI))
    assertEquals(-1, DeseretUpperI.compareToIgnoreCase(DeseretUpperE))
    assertEquals(1, DeseretLowerE.compareToIgnoreCase(DeseretUpperI))
    assertEquals(
      -1,
      ("a" + DeseretUpperI + "b").compareToIgnoreCase("a" + DeseretLowerI + "c")
    )

    assertEquals(0, WarangCiti.compareToIgnoreCase(WarangCitiFirstLower))
    assertEquals(0, WarangCiti.compareToIgnoreCase(WarangCitiRestUpper))
    assertEquals(13, WarangCiti.compareToIgnoreCase(WarangCitiOtherLetter))
    assertEquals(
      -13,
      "𑣙𑣗𑣁𑢯𑣊"
        .compareToIgnoreCase(WarangCiti)
    )

    // pairs are not formed across the end of the shorter string
    assertEquals(1, DeseretUpperI.compareToIgnoreCase("\ud801"))
    assertEquals(-1, "\ud801".compareToIgnoreCase(DeseretUpperI))
    assertEquals(1, ("a" + DeseretUpperI).compareToIgnoreCase("a\ud801"))

    // a pair in one string compared to a lone surrogate in the other
    assertEquals(
      0x10428 - 'x',
      ("a" + DeseretUpperI).compareToIgnoreCase("a\ud801x")
    )
    assertEquals(-40, "\udc00".compareToIgnoreCase("\udc28"))
  }

  @Test def caseInsensitiveOrderSupplementary(): Unit = {
    val cmp = String.CASE_INSENSITIVE_ORDER
    assertEquals(0, cmp.compare(DeseretUpperI, DeseretLowerI))
    assertEquals(0, cmp.compare(WarangCitiRestUpper, WarangCitiFirstLower))
    assertTrue(cmp.compare(DeseretLowerI, DeseretUpperE) < 0)

    // pairs are formed up to the end of each string, keeping it transitive
    val a = DeseretUpperI
    val b = "ﬀ"
    val c = "ﬀa"
    assertEquals(0x10428 - 0xfb00, cmp.compare(a, b))
    assertEquals(-1, cmp.compare(b, c))
    assertEquals(0x10428 - 0xfb00, cmp.compare(a, c))
    assertEquals(0xfb00 - 0x10428, cmp.compare(b, a))

    // a pair starting in one string matched with a pair ending in the other
    val d = "\ud801" + DeseretUpperI
    val e = DeseretLowerI + "x"
    assertEquals(0, cmp.compare(d, e))
    assertEquals(0, cmp.compare(e, d))
    assertTrue(d.equalsIgnoreCase(e))
    assertTrue(e.equalsIgnoreCase(d))
    assertTrue(d.regionMatches(true, 0, e, 0, 3))
    assertTrue(e.regionMatches(true, 0, d, 0, 3))
  }

  @Test def regionMatchesIgnoreCaseSupplementary(): Unit = {
    assertTrue(DeseretUpperI.regionMatches(true, 0, DeseretLowerI, 0, 2))
    assertFalse(DeseretUpperI.regionMatches(false, 0, DeseretLowerI, 0, 2))

    // a region ending or starting in the middle of a pair
    assertTrue(DeseretUpperI.regionMatches(true, 0, DeseretLowerI, 0, 1))
    assertFalse(DeseretUpperI.regionMatches(true, 1, DeseretLowerI, 1, 1))
    assertFalse(
      ("x" + DeseretUpperI).regionMatches(true, 2, "y" + DeseretLowerI, 2, 1)
    )

    // indices are in chars, chars [2, 6) are code points [1, 3)
    assertFalse(WarangCiti.regionMatches(false, 0, WarangCitiFirstLower, 0, 4))
    assertTrue(WarangCiti.regionMatches(false, 2, WarangCitiFirstLower, 2, 4))
    assertTrue(WarangCiti.regionMatches(true, 0, WarangCitiFirstLower, 0, 4))
    assertTrue(WarangCiti.regionMatches(true, 2, WarangCitiRestUpper, 2, 6))
    assertFalse(WarangCiti.regionMatches(true, 4, WarangCitiOtherLetter, 4, 4))

    val text = "Prefix " + DeseretUpperI + "Body" + DeseretLowerE
    assertTrue(
      text.regionMatches(
        true,
        7,
        "--" + DeseretLowerI + "bODY" + DeseretUpperE,
        2,
        8
      )
    )
  }

  @Test def ignoreCaseBMP(): Unit = {
    assertTrue("åløb".equalsIgnoreCase("ÅLØB"))
    assertEquals(0, "ıiIİ".compareToIgnoreCase("İIiı"))
    assertEquals(64154, "Eﬀet".compareToIgnoreCase("effEt"))
    assertTrue("Hello World".regionMatches(true, 6, "WORLD", 0, 5))
    assertTrue("ΣΊΣΥΦΟΣ".equalsIgnoreCase("σίσυφος"))
  }
}
