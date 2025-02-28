package org.scalanative.testsuite.javalib.lang

import org.junit.Test
import org.junit.Assert._

import java.{lang => jl}

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class StringTestOnJDK21 {

  @Test def IndexOf_Int_BeginIndexEndIndex_CheckArgs(): Unit = {

    // No Exception in this special case of zero range, strarting at index 0
    assertEquals("length 0, start & end 0", -1, "".indexOf('a', 0, 0))

    assertThrows(
      classOf[jl.StringIndexOutOfBoundsException],
      assertEquals("negative startIndex", -999, "".indexOf('a', -1, 0))
    )

    assertThrows(
      classOf[jl.StringIndexOutOfBoundsException],
      assertEquals("endIndex > length", -999, "".indexOf('a', 0, 1))
    )
    assertThrows(
      classOf[jl.StringIndexOutOfBoundsException],
      assertEquals("beginIndex > endIndex", -999, "".indexOf('a', 1, 0))
    )
  }

  @Test def IndexOf_Int_BeginIndexEndIndex(): Unit = {
    val afoobar = "afoobar"
    assertEquals("a1_1", -1, afoobar.indexOf('a', 0, 0))
    assertEquals("a1_2", 0, afoobar.indexOf('a', 0, 7))
    assertEquals("a1_3", -1, afoobar.indexOf('a', 1, 5))
    assertEquals("a1_4", 5, afoobar.indexOf('a', 1, 6))

    val umlautAFoobar = "fubår" // check non-ANSI
    assertEquals("a2_1", -1, umlautAFoobar.indexOf('a', 0, 0))
    assertEquals("a2_2", -1, umlautAFoobar.indexOf('a', 0, 5))
    assertEquals("a2_3", -1, umlautAFoobar.indexOf('a', 1, 4))
    assertEquals("a2_44", -1, umlautAFoobar.indexOf('a', 1, 5))

    assertEquals("a3_1", -1, umlautAFoobar.indexOf('å', 0, 0))
    assertEquals("a3_2", 3, umlautAFoobar.indexOf('å', 0, 5))
    assertEquals("a3_3", 3, umlautAFoobar.indexOf('å', 1, 4))
    assertEquals("a3_4", -1, umlautAFoobar.indexOf('å', 4, 5))
  }

  @Test def IndexOf_String_BeginIndexEndIndex_CheckArgs(): Unit = {
    val needle = "needle"

    // No Exception in this special case of zero range, strarting at index 0
    assertEquals("length 0, start & end 0", 0, "".indexOf("", 0, 0))

    assertThrows(
      classOf[jl.StringIndexOutOfBoundsException],
      assertEquals("negative startIndex", -999, "".indexOf(needle, -1, 0))
    )

    assertThrows(
      classOf[jl.StringIndexOutOfBoundsException],
      assertEquals("endIndex > length", -999, "".indexOf(needle, 0, 1))
    )
    assertThrows(
      classOf[jl.StringIndexOutOfBoundsException],
      assertEquals("beginIndex > endIndex", -999, "".indexOf(needle, 1, 0))
    )
  }

  @Test def IndexOf_String_BeginIndexEndIndex(): Unit = {

    val needle_1 = "needle"
    val haystack_1 = "hay-needle-more_hay-needle-straw"
    assertEquals("a1_1", -1, haystack_1.indexOf(needle_1, 0, 0))
    assertEquals("a1_2", -1, haystack_1.indexOf(needle_1, 0, 7))

    assertEquals("a1_3", 4, haystack_1.indexOf(needle_1, 0, 10))
    assertEquals("a1_4", -1, haystack_1.indexOf(needle_1, 5, 10))
    assertEquals("a1_5", 20, haystack_1.indexOf(needle_1, 5, 26))

    // Use non-ANSI character, change length to exercise different indices.
    val needle_2 = "ne€dle"
    val haystack_2 = "hay-hay-ne€dle-more_hay-ne€dle-straw"
    assertEquals("a2_1", -1, haystack_2.indexOf(needle_2, 0, 0))
    assertEquals("a2_2", -1, haystack_2.indexOf(needle_2, 0, 7))

    assertEquals("a2_3", 8, haystack_2.indexOf(needle_2, 0, 14))
    assertEquals("a2_4", -1, haystack_2.indexOf(needle_2, 9, 14))
    assertEquals("a2_5", 24, haystack_2.indexOf(needle_2, 9, 34))
  }
}
