package org.scalanative.testsuite.javalib.net

import java.net.*

/* Written against the documentation of URLEncoder
 *
 * https://docs.oracle.com/javase/8/docs/api/index.html?java/net/URLEncoder.html
 *
 * See also specification of HTML 4.01 and RFC 1738
 *
 * https://www.w3.org/TR/html401/interact/forms.html#h-17.13.4.1
 * http://www.ietf.org/rfc/rfc1738.txt
 */

import org.junit.Test
import org.junit.Assert.*

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class URLEncoderTest {
  @Test def nullInputString(): Unit = {
    assertThrows(
      classOf[NullPointerException],
      URLEncoder.encode(null: String, "ignored")
    )
  }

  @Test def nullEncodingName(): Unit = {
    assertThrows(
      classOf[NullPointerException],
      URLEncoder.encode("any", null: String)
    )
  }

  @Test def earlyThrowOfUnsupportedEncodingException(): Unit = {
    assertThrows(
      classOf[java.io.UnsupportedEncodingException],
      URLEncoder.encode("any", "invalid encoding")
    )
  }

  private def assertIsoEncoded(original: String): Unit = {
    // "The recommended encoding scheme to use is UTF-8."
    val encoded = URLEncoder.encode(original, "UTF-8")
    assertTrue(original == encoded)
  }

  @Test def charactersLowerCaseAlphaThroughZuluEncodeTheSame(): Unit = {
    Range('a', 'z') foreach { v => assertIsoEncoded(v.toChar.toString) }
  }

  @Test def charactersUpperCaseAlphaThroughZuluEncodeTheSame(): Unit = {
    Range('A', 'Z') foreach { v => assertIsoEncoded(v.toChar.toString) }
  }

  @Test def characters0Through9EncodeTheSame(): Unit = {
    Range('0', '9') foreach { v => assertIsoEncoded(v.toChar.toString) }
  }

  @Test def specialCharactersDotDashAsteriskUnderscoreEncodeTheSame(): Unit = {
    Seq('.', '-', '*', '_') foreach { v => assertIsoEncoded(v.toChar.toString) }
  }

  @Test def spaceCharacterIsConvertedToThePlusCharacter(): Unit = {
    val inStr = " "
    val outStr = URLEncoder.encode(inStr, "UTF-8")
    assertTrue(outStr == "+")
  }

  @Test def allOtherCharactersAreEncodedUsingPercentEscapes(): Unit = {
    val ex0 = "The string ü@foo-bar"
    val expectedEx0 = "The+string+%C3%BC%40foo-bar"
    val actualEx0 = URLEncoder.encode(ex0, "UTF-8")
    assertTrue(actualEx0 == expectedEx0)
  }
}
