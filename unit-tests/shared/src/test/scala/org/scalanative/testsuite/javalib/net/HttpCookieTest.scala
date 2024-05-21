package org.scalanative.testsuite.javalib.net

import java.net._

import org.junit.Test
import org.junit.Assert._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class HttpCookieTest {

  @Test
  def basicParseTest(): Unit = {

    val out = HttpCookie.parse("set-cookie2:potato=tomato")
    assertEquals(out.size(), 1)
    val cookie = out.get(0)
    assertEquals("potato", cookie.getName())
    assertEquals("tomato", cookie.getValue())
    assertEquals(null, cookie.getComment())
    assertEquals(false, cookie.getDiscard())
    assertEquals(null, cookie.getCommentURL())
    assertEquals(null, cookie.getDomain())
    assertEquals(-1, cookie.getMaxAge())
    assertEquals(null, cookie.getPath())
    assertEquals(null, cookie.getPortlist())
    assertEquals(false, cookie.getSecure())
    assertEquals(1, cookie.getVersion)

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

    assertEquals(out.size(), 1)
    val cookie = out.get(0)
    assertEquals("potato", cookie.getName())
    assertEquals("tomato", cookie.getValue())
    assertEquals("This is a comment", cookie.getComment())
    assertEquals(true, cookie.getDiscard())
    assertEquals(
      "https://definitely.real.website.cool/",
      cookie.getCommentURL()
    )
    assertEquals("definitely.real.webiste.cool", cookie.getDomain())
    assertEquals(123, cookie.getMaxAge())
    assertEquals("/potato/tomato", cookie.getPath())
    assertEquals("123 456 789", cookie.getPortlist())
    assertEquals(true, cookie.getSecure())
    assertEquals(1, cookie.getVersion)

  }

  @Test
  def domainMatchTest: Unit = {
    assert(HttpCookie.domainMatches("website.com", "website.com"))
    assert(HttpCookie.domainMatches("website.com", "www.website.com"))
    assert(!HttpCookie.domainMatches("www.website.com", "website.com"))
    assert(!HttpCookie.domainMatches("website.com", "awebsite.com"))
  }

}
