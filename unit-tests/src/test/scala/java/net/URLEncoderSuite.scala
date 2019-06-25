package java.net

/* Written against the documentation of URLEncoder
 *
 * https://docs.oracle.com/javase/8/docs/api/index.html?java/net/URLEncoder.html
 *
 * See also specification of HTML 4.01 and RFC 1738
 *
 * https://www.w3.org/TR/html401/interact/forms.html#h-17.13.4.1
 * http://www.ietf.org/rfc/rfc1738.txt
 */

object URLEncoderSuite extends tests.Suite {
  test("null input string") {
    assertThrows[NullPointerException] {
      URLEncoder.encode(null: String, "ignored")
    }
  }

  test("null encoding name") {
    assertThrows[NullPointerException] {
      URLEncoder.encode("any", null: String)
    }
  }

  test("early throw of UnsupportedEncodingException") {
    assertThrows[java.io.UnsupportedEncodingException] {
      URLEncoder.encode("any", "invalid encoding")
    }
  }

  def assertIsoEncoded(original: String): Unit = {
    // "The recommended encoding scheme to use is UTF-8."
    val encoded = URLEncoder.encode(original, "UTF-8")
    assert(original == encoded)
  }

  test("characters 'a' through 'z' encode the same") {
    Range('a', 'z') foreach { v =>
      assertIsoEncoded(v.toChar.toString)
    }
  }

  test("characters 'A' through 'Z' encode the same") {
    Range('A', 'Z') foreach { v =>
      assertIsoEncoded(v.toChar.toString)
    }
  }

  test("characters '0' through '9' encode the same") {
    Range('0', '9') foreach { v =>
      assertIsoEncoded(v.toChar.toString)
    }
  }

  test("special characters '.', '-', '*', and '_' encode the same") {
    Seq('.', '-', '*', '_') foreach { v =>
      assertIsoEncoded(v.toChar.toString)
    }
  }

  test("space character is converted to a '+' character") {
    val inStr  = " "
    val outStr = URLEncoder.encode(inStr, "UTF-8")
    assert(outStr == "+")
  }

  test("all other characters are encoded using '%' escapes") {
    val ex0         = "The string ü@foo-bar"
    val expectedEx0 = "The+string+%C3%BC%40foo-bar"
    val actualEx0   = URLEncoder.encode(ex0, "UTF-8")
    assert(actualEx0 == expectedEx0)
  }
}
