package java.net

// Tests ported from Scala.js project

object URISuite extends tests.Suite {

  def expectURI(uri: URI, isAbsolute: Boolean, isOpaque: Boolean)(
      authority: String = null,
      fragment: String = null,
      host: String = null,
      path: String = null,
      port: Int = -1,
      query: String = null,
      scheme: String = null,
      userInfo: String = null,
      schemeSpecificPart: String = null)(rawAuthority: String = authority,
                                         rawFragment: String = fragment,
                                         rawPath: String = path,
                                         rawQuery: String = query,
                                         rawUserInfo: String = userInfo,
                                         rawSchemeSpecificPart: String =
                                           schemeSpecificPart): Unit = {

    assertEquals(authority, uri.getAuthority())
    assertEquals(fragment, uri.getFragment())
    assertEquals(host, uri.getHost())
    assertEquals(path, uri.getPath())
    assertEquals(port, uri.getPort())
    assertEquals(query, uri.getQuery())
    assertEquals(rawAuthority, uri.getRawAuthority())
    assertEquals(rawFragment, uri.getRawFragment())
    assertEquals(rawPath, uri.getRawPath())
    assertEquals(rawQuery, uri.getRawQuery())
    assertEquals(rawSchemeSpecificPart, uri.getRawSchemeSpecificPart())
    assertEquals(rawUserInfo, uri.getRawUserInfo())
    assertEquals(scheme, uri.getScheme())
    assertEquals(schemeSpecificPart, uri.getSchemeSpecificPart())
    assertEquals(userInfo, uri.getUserInfo())
    assertEquals(isAbsolute, uri.isAbsolute())
    assertEquals(isOpaque, uri.isOpaque())
  }

  test("should parse vanilla absolute URIs") {
    expectURI(new URI("http://java.sun.com/j2se/1.3/"), true, false)(
      scheme = "http",
      host = "java.sun.com",
      path = "/j2se/1.3/",
      authority = "java.sun.com",
      schemeSpecificPart = "//java.sun.com/j2se/1.3/")()
  }

  test("should parse absolute URIs with empty path") {
    expectURI(new URI("http://foo:bar"), true, false)(authority = "foo:bar",
                                                      path = "",
                                                      scheme = "http",
                                                      schemeSpecificPart =
                                                        "//foo:bar")()
  }

  test("should parse absolute URIs with IPv6") {
    val uri = new URI("http://hans@[ffff::0:128.4.5.3]:345/~hans/")
    expectURI(uri, true, false)(
      scheme = "http",
      host = "[ffff::0:128.4.5.3]",
      userInfo = "hans",
      port = 345,
      path = "/~hans/",
      authority = "hans@[ffff::0:128.4.5.3]:345",
      schemeSpecificPart = "//hans@[ffff::0:128.4.5.3]:345/~hans/"
    )()
  }

  test("should parse absolute URIs without authority") {
    expectURI(new URI("file:/~/calendar"), true, false)(scheme = "file",
                                                        path = "/~/calendar",
                                                        schemeSpecificPart =
                                                          "/~/calendar")()
  }

  test("should parse absolute URIs with empty authority") {
    expectURI(new URI("file:///~/calendar"), true, false)(scheme = "file",
                                                          path = "/~/calendar",
                                                          schemeSpecificPart =
                                                            "///~/calendar")()
  }

  test("should parse opaque URIs") {
    expectURI(new URI("mailto:java-net@java.sun.com"), true, true)(
      scheme = "mailto",
      schemeSpecificPart = "java-net@java.sun.com")()

    expectURI(new URI("news:comp.lang.java"), true, true)(scheme = "news",
                                                          schemeSpecificPart =
                                                            "comp.lang.java")()

    expectURI(new URI("urn:isbn:096139210x"), true, true)(
      scheme = "urn",
      schemeSpecificPart = "isbn:096139210x")()
  }

  test("should parse relative URIs") {
    expectURI(new URI("docs/guide/collections/designfaq.html#28"),
              false,
              false)(path = "docs/guide/collections/designfaq.html",
                     fragment = "28",
                     schemeSpecificPart =
                       "docs/guide/collections/designfaq.html")()
    expectURI(new URI("../../../demo/jfc/SwingSet2/src/SwingSet2.java"),
              false,
              false)(path = "../../../demo/jfc/SwingSet2/src/SwingSet2.java",
                     schemeSpecificPart =
                       "../../../demo/jfc/SwingSet2/src/SwingSet2.java")()
  }

  test("should fail on bad URIs") {
    val badURIs = Array(
      "http:///a path#frag", // space char in path, not in escaped
      // octet form, with no host
      "http://host/a[path#frag", // an illegal char, not in escaped
      // octet form, should throw an
      // exception
      "http://host/a%path#frag", // invalid escape sequence in path
      "http://host/a%#frag", // incomplete escape sequence in path
      "http://host#a frag",  // space char in fragment, not in
      // escaped octet form, no path
      "http://host/a#fr#ag", // illegal char in fragment
      "http:///path#fr%ag",  // invalid escape sequence in fragment,
      // with no host
      "http://host/path#frag%", // incomplete escape sequence in
      // fragment
      "http://host/path?a query#frag", // space char in query, not
      // in escaped octet form
      "http://host?query%ag", // invalid escape sequence in query, no
      // path
      "http:///path?query%", // incomplete escape sequence in query,
      // with no host
      "mailto:user^name@fklkf.com", // invalid char in scheme specific part
      // authority validation
      "http://user@[3ffe:2x00:100:7031::1]:80/test", // malformed
      // IPv6 authority
      "http://[ipv6address]/apath#frag", // malformed ipv6 address
      "http://[ipv6address/apath#frag", // malformed ipv6 address
      "http://ipv6address]/apath#frag", // illegal char in host name
      "http://ipv6[address/apath#frag",
      "http://ipv6addr]ess/apath#frag",
      "http://ipv6address[]/apath#frag",
      // illegal char in username...
      "http://us[]er@host/path?query#frag",
      "http://host name/path", // illegal
      // char in authority
      "http://host^name#fragment", // illegal char in authority
      "telnet://us er@hostname/",  // illegal char in authority
      // missing components
      "//", // Authority expected
      "ascheme://", // Authority expected
      "ascheme:",   // Scheme-specific part expected
      // scheme validation
      "a scheme://reg/", // illegal char
      "1scheme://reg/", // non alpha char as 1st char
      "asche\u00dfme:ssp", // unicode char , not USASCII
      "asc%20heme:ssp"
    )

    for (uri <- badURIs) {
      assertThrows[URISyntaxException] { new URI(uri) }
    }
  }

}
