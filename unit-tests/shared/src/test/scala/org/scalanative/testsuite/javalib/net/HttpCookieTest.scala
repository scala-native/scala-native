package org.scalanative.testsuite.javalib.net

import java.net._

import org.junit.Test
import org.junit.Assert._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class HttpCookieTest {

  // label parameter gets added to the clue field on assertions
  def assertCookie(
      testSubject: HttpCookie,
      label: String,
      name: String,
      value: String,
      comment: String = null,
      discard: Boolean = false,
      commentUrl: String = null,
      domain: String = null,
      maxAge: Int = -1,
      path: String = null,
      portlist: String = null,
      secure: Boolean = false,
      version: Int = 1
  ): Unit = {

    assertEquals(s"$label: name", name, testSubject.getName())
    assertEquals(s"$label: value", value, testSubject.getValue())
    assertEquals(s"$label: comment", comment, testSubject.getComment())
    assertEquals(s"$label: discard", discard, testSubject.getDiscard())
    assertEquals(s"$label: commentURL", commentUrl, testSubject.getCommentURL())
    assertEquals(s"$label: domain", domain, testSubject.getDomain())
    assertEquals(s"$label: maxAge", maxAge, testSubject.getMaxAge())
    assertEquals(s"$label: path", path, testSubject.getPath())
    assertEquals(s"$label: portlist", portlist, testSubject.getPortlist())
    assertEquals(s"$label: secure", secure, testSubject.getSecure())
    assertEquals(s"$label: version", version, testSubject.getVersion())
  }

  @Test
  def basicParseTest(): Unit = {
    val out = HttpCookie.parse("set-cookie2:potato=tomato")
    assertEquals("number of cookies", out.size(), 1)
    val cookie = out.get(0)
    assertCookie(cookie, "cookie", name = "potato", value = "tomato")
  }

  @Test
  def badAttrParsingTest(): Unit = {
    val out = HttpCookie.parse("set-cookie2:potato=tomato;version=asdf")
    assertEquals("number of cookies", out.size(), 1)
    val cookie = out.get(0)
    assertCookie(
      cookie,
      "cookie",
      name = "potato",
      value = "tomato",
      version = 1
    )
  }

  @Test
  def stringEscapingTest(): Unit = {
    // I named this test "string escaping", but really it's more about how
    // the jvm does no such thing
    val out =
      HttpCookie.parse(
        "set-cookie2:potato=\"toma\\\"t,o=asdf\",second=second;comment=\"something;discard;portlist=123\""
      )
    assertEquals("number of cookies", 2, out.size())
    val cookieOne = out.get(0)
    assertCookie(
      cookieOne,
      "cookie one",
      name = "potato",
      value = "\"toma\\\"t"
    )
    // despite the fact that the first comma is in a quoted string - the JVM
    // still splits it into a separate cookie at that point. Hence we must do so as well
    //
    // Similarly for semicolons - the JVM doesn't seem to distinguish between semicolons
    // in strings and those outside, and hence we do the same
    val cookieTwo = out.get(1)
    assertCookie(
      cookieTwo,
      "cookie two",
      name = "o",
      value = "asdf\",second=second",
      comment = "\"something",
      discard = true
    )

  }

  @Test
  def attributeTest(): Unit = {
    val out = HttpCookie.parse("""set-cookie2: 
                               |potato = tomato ; 
                               |comment = "This is a comment"; 
                               |discard;
                               |commenturl= https://definitely.real.website.cool/;
                               |domain=definitely.real.webiste.cool;
                               |max-age=123;
                               |path=/potato/tomato;
                               |port="123 456 789";
                               |secure;
                               |version=1""".stripMargin.replaceAll("\n", ""))

    assertEquals("number of cookies", out.size(), 1)
    val cookie = out.get(0)

    assertCookie(
      cookie,
      "Cookie",
      name = "potato",
      value = "tomato",
      comment = "This is a comment",
      discard = true,
      commentUrl = "https://definitely.real.website.cool/",
      domain = "definitely.real.webiste.cool",
      maxAge = 123,
      path = "/potato/tomato",
      portlist = "123 456 789",
      secure = true,
      version = 1
    )

  }

  @Test
  def multipleCookieTest(): Unit = {
    val out = HttpCookie.parse(
      """set-cookie2: 
                               |potato = tomato ; 
                               |comment = "This is a comment"; 
                               |discard,
                               |second="second";
                               |secure,
                               |third=third;
                               |comment = "This is the third cookie" """.stripMargin
        .replaceAll("\n", "")
    )

    assertEquals("number of cookies", out.size(), 3)
    assertCookie(
      out.get(0),
      "Cookie 0",
      name = "potato",
      value = "tomato",
      discard = true,
      comment = "This is a comment"
    )

    assertCookie(
      out.get(1),
      "Cookie 1",
      name = "second",
      value = "second",
      secure = true
    )

    assertCookie(
      out.get(2),
      "Cookie 2",
      name = "third",
      value = "third",
      comment = "This is the third cookie"
    )

  }

  @Test
  def illegalCookieNameTest: Unit = {
    assertThrows(
      "Names containing CTL characters should not be allowed",
      classOf[IllegalArgumentException],
      new HttpCookie(s"potato${5: Char}potato", "tomato")
    )

    assertThrows(
      "Names containing spaces or tabs should not be allowed",
      classOf[IllegalArgumentException],
      new HttpCookie(s"potato potato", "tomato")
    )

    assertThrows(
      "Names are not allowed to start with a '$' character'",
      classOf[IllegalArgumentException],
      new HttpCookie("$$$$", "")
    )
    assertThrows(
      "Empty names should not be allowed",
      classOf[IllegalArgumentException],
      new HttpCookie("", "")
    )
  }

  @Test
  def illegalAttributeTest: Unit = {
    assertThrows(
      "Max-age must be a number",
      classOf[IllegalArgumentException],
      HttpCookie.parse("set-cookie2:potato=tomato;max-age=bad")
    )

    assertThrows(
      "Version must be 0 or 1",
      classOf[IllegalArgumentException],
      HttpCookie.parse("set-cookie2:potato=tomato;version=123")
    )
  }

  @Test
  def testExpiration: Unit = {
    assertFalse(
      "Cookies without max-age set should be never expired",
      new HttpCookie("potato", "tomato").hasExpired()
    )

    val expiredCookie = new HttpCookie("potato", "tomato")
    expiredCookie.setMaxAge(0)
    assertTrue(
      "Cookies with max-age set should be compared with the time they were created at",
      expiredCookie.hasExpired()
    )
  }

  @Test
  def domainMatchTest: Unit = {
    assertTrue(
      "equal domains should match",
      HttpCookie.domainMatches("website.com", "website.com")
    )
    assertFalse(
      "if domain is subdomain of host - should not match",
      HttpCookie.domainMatches("www.website.com", "website.com")
    )
    // I have no words really
    assertTrue(HttpCookie.domainMatches("website.com", "awebsite.com"))
  }

}
