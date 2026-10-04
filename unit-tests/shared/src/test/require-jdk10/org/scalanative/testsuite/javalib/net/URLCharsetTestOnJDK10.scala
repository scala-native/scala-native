package org.scalanative.testsuite.javalib.net

import java.net.{URLDecoder, URLEncoder}
import java.nio.charset.StandardCharsets._
import java.nio.charset.{Charset, CharsetDecoder, CharsetEncoder}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class URLCharsetTestOnJDK10 {
  @Test def encodingUsesCharsetAndFormRules(): Unit = {
    assertEquals(
      "AZaz09.-*_+%2B%25%2F",
      URLEncoder.encode("AZaz09.-*_ +%/", UTF_8)
    )
    assertEquals("%C3%A9%F0%9F%98%80", URLEncoder.encode("é😀", UTF_8))
    assertEquals("%E9", URLEncoder.encode("é", ISO_8859_1))
    assertEquals("%00%E9", URLEncoder.encode("é", UTF_16BE))
  }

  @Test def decodingUsesCharsetAndFormRules(): Unit = {
    assertEquals("a +%/", URLDecoder.decode("a+%2b%25%2f", UTF_8))
    assertEquals("é😀", URLDecoder.decode("%C3%A9%F0%9F%98%80", UTF_8))
    assertEquals("é", URLDecoder.decode("%E9", ISO_8859_1))
    assertEquals("é", URLDecoder.decode("%00%E9", UTF_16BE))
  }

  @Test def nonAsciiRoundTrips(): Unit = {
    for (charset <- Array(UTF_8, UTF_16BE, UTF_16LE, ISO_8859_1)) {
      val original = "café + 50% / déjà"
      assertEquals(
        original,
        URLDecoder.decode(URLEncoder.encode(original, charset), charset)
      )
    }
  }

  @Test def suppliedCustomCharsetIsUsedWithoutNameLookup(): Unit = {
    val charset = new Charset("X-S10-Latin1", Array.empty[String]) {
      def contains(cs: Charset): Boolean = ISO_8859_1.contains(cs)
      def newDecoder(): CharsetDecoder = ISO_8859_1.newDecoder()
      def newEncoder(): CharsetEncoder = ISO_8859_1.newEncoder()
    }
    assertEquals("%E9", URLEncoder.encode("é", charset))
    assertEquals("é", URLDecoder.decode("%E9", charset))
  }

  @Test def malformedAndUnmappableInputUsesReplacement(): Unit = {
    assertEquals("%3F", URLEncoder.encode("é", US_ASCII))
    assertEquals("%3F", URLEncoder.encode("\uD800", UTF_8))
    assertEquals("\uFFFD(", URLDecoder.decode("%C3%28", UTF_8))
    assertEquals("\uFFFD", URLDecoder.decode("%FF", UTF_8))
    assertEquals("\uFFFD", URLDecoder.decode("%80", US_ASCII))
  }

  @Test def malformedPercentEscapesAreRejected(): Unit = {
    for (encoded <- Array("%", "%0", "%GG", "x%0G", "%20%"))
      assertThrows(
        classOf[IllegalArgumentException],
        URLDecoder.decode(encoded, UTF_8)
      )
  }

  @Test def nullArgumentsAreRejectedEagerly(): Unit = {
    for (input <- Array("", "abc", "+", "%")) {
      assertThrows(
        classOf[NullPointerException],
        URLEncoder.encode(input, null: Charset)
      )
      assertThrows(
        classOf[NullPointerException],
        URLDecoder.decode(input, null: Charset)
      )
    }
    assertThrows(classOf[NullPointerException], URLEncoder.encode(null, UTF_8))
    assertThrows(classOf[NullPointerException], URLDecoder.decode(null, UTF_8))
  }
}
