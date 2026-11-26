package org.scalanative.testsuite.javalib.net

import java.net._

import scala.annotation.nowarn

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

@nowarn("cat=deprecation")
class URLStreamHandlerTest {

  private class Handler extends URLStreamHandler {
    var fail: RuntimeException = null
    var opened: URL = null
    override protected def openConnection(u: URL): URLConnection = {
      opened = u
      null
    }
    override protected def parseURL(
        u: URL,
        spec: String,
        start: Int,
        limit: Int
    ): Unit = {
      if (fail != null) throw fail
      super.parseURL(u, spec, start, limit)
    }
    def steal(u: URL): URL = {
      setURL(u, "x", "h", 1, "a", "u", "/p", "q", "r")
      u
    }
    def defaultPort: Int = getDefaultPort()
  }

  private class PortHandler(port: Int) extends Handler {
    override protected def getDefaultPort(): Int = port
    def legacy(u: URL, host: String, port: Int, file: String): URL = {
      setURL(u, "demo", host, port, file, null)
      u
    }
  }

  @Test def legacySetURL(): Unit = {
    val h = new PortHandler(-1)
    val u = h.legacy(new URL(null, "demo://old/p", h), "", -1, "/new")
    assertEquals("demo:/new", u.toString())
    assertNull(u.getAuthority())
    val v = h.legacy(new URL(null, "demo://old/p", h), "nh", 5, "/x?a?b")
    assertEquals(
      Seq("nh:5", "/x?a", "b"),
      Seq(v.getAuthority(), v.getPath(), v.getQuery())
    )
  }

  @Test def omittedPortUsesEachURLsDefault(): Unit = {
    val a = new URL(null, "demo://h.invalid/p", new PortHandler(80))
    val b = new URL(null, "demo://h.invalid:80/p", new PortHandler(81))
    assertEquals(a, b)
    assertEquals(b, a)
  }

  @Test def parseURLGetsTheSpecWithoutReference(): Unit = {
    var seen: (String, String) = null
    val h = new Handler {
      override protected def parseURL(
          u: URL,
          spec: String,
          start: Int,
          limit: Int
      ): Unit = {
        seen = (spec.substring(start, limit), u.getRef())
        super.parseURL(u, spec, start, limit)
      }
    }
    val u = new URL(null, "demo:entry#section", h)
    assertEquals(("entry", "section"), seen)
    assertEquals("demo:entry#section", u.toString())
  }

  @Test def customHandler(): Unit = {
    val h = new Handler
    val u = new URL(null, "foo://h:5/p?q#r", h)
    assertEquals("foo://h:5/p?q#r", u.toString())
    assertEquals(-1, u.getDefaultPort())
    assertEquals(-1, h.defaultPort)
    assertEquals("foo://h:5/p", new URL("foo", "h", 5, "/p", h).toString())
    assertEquals(
      new URI("foo://u@h:5/p?q#r"),
      new URL(null, "foo://u@h:5/p?q#r", h).toURI()
    )
    assertEquals(
      "foo".hashCode + "h.invalid".hashCode + "/p".hashCode - 1,
      new URL(null, "foo://h.invalid/p", h).hashCode()
    )
  }

  @Test def parseErrorsBecomeMalformed(): Unit = {
    val h = new Handler
    Seq(
      new IllegalArgumentException("boom"),
      new NullPointerException("npe"),
      new SecurityException("sec")
    ).foreach { ex =>
      h.fail = ex
      val e = assertThrows(
        classOf[MalformedURLException],
        new URL(null, "foo://h/", h)
      )
      assertEquals(ex.getMessage(), e.getMessage())
    }
  }

  @Test def setURLChecksTheHandler(): Unit = {
    val h = new Handler
    val other = new Handler
    val e = assertThrows(
      classOf[SecurityException],
      other.steal(new URL(null, "foo://h/", h))
    )
    assertEquals("handler for url different from this handler", e.getMessage())
    val u = h.steal(new URL(null, "foo://h/", h))
    assertEquals("foo://a/p?q#r", u.toString())
    assertEquals(
      Seq("foo", "a", "u", "h", "/p?q", "q", "r"),
      Seq(
        u.getProtocol(),
        u.getAuthority(),
        u.getUserInfo(),
        u.getHost(),
        u.getFile(),
        u.getQuery(),
        u.getRef()
      )
    )
  }

  @Test def relativeURLsKeepTheHandler(): Unit = {
    val h = new Handler
    val ctx = new URL(null, "foo://h/a/b", h)
    val rel = new URL(ctx, "c")
    assertEquals("foo://h/a/c", rel.toString())
    assertNull(rel.openConnection())
    assertSame(rel, h.opened)
    val abs = new URL(ctx, "foo://z/")
    abs.openConnection()
    assertSame(abs, h.opened)
    // Equality goes through the handler, not its class
    assertEquals(
      new URL(null, "foo://h.invalid/", h),
      new URL(null, "foo://h.invalid/", new Handler)
    )
  }

  @Test def proxyNotSupportedByDefault(): Unit = {
    val h = new Handler
    val u = new URL(null, "foo://h/", h)
    assertThrows(
      classOf[UnsupportedOperationException],
      u.openConnection(Proxy.NO_PROXY)
    )
  }
}
