package org.scalanative.testsuite.javalib.net

import java.net._

import scala.annotation.nowarn

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform
import org.scalanative.testsuite.utils.Platform.executingInJVMOnLowerThanJDK

/* Hosts compared by equals/hashCode are IP literals, `localhost` or names
 * under `.invalid`, so that their DNS lookups stay local.
 */
@nowarn("cat=deprecation")
class URLTest {

  private def describe(u: URL): String =
    s"[$u] proto=${u.getProtocol()} auth=${u.getAuthority()} user=${u.getUserInfo()} host=${u.getHost()} " +
      s"port=${u.getPort()} dport=${u.getDefaultPort()} file=${u.getFile()} path=${u.getPath()} " +
      s"query=${u.getQuery()} ref=${u.getRef()}"

  private def assertParses(expected: String, spec: String): Unit =
    assertEquals(spec, expected, describe(new URL(spec)))

  private def assertMalformed(message: String, spec: String): Unit = {
    val e = assertThrows(spec, classOf[MalformedURLException], new URL(spec))
    assertEquals(spec, message, e.getMessage())
  }

  private def assertMalformedURL(message: String, url: => URL): Unit = {
    val e = assertThrows(message, classOf[MalformedURLException], url)
    assertEquals(message, e.getMessage())
  }

  @Test def absoluteSpecs(): Unit = {
    assertParses(
      "[http://www.example.com/a/b.html?q=1#frag] proto=http auth=www.example.com user=null host=www.example.com port=-1 dport=80 file=/a/b.html?q=1 path=/a/b.html query=q=1 ref=frag",
      "http://www.example.com/a/b.html?q=1#frag"
    )
    assertParses(
      "[http://WWW.Example.COM:8080/A] proto=http auth=WWW.Example.COM:8080 user=null host=WWW.Example.COM port=8080 dport=80 file=/A path=/A query=null ref=null",
      "HTTP://WWW.Example.COM:8080/A"
    )
    assertParses(
      "[http://user:pw@host:81/p?x#y] proto=http auth=user:pw@host:81 user=user:pw host=host port=81 dport=80 file=/p?x path=/p query=x ref=y",
      "http://user:pw@host:81/p?x#y"
    )
    assertParses(
      "[http://host] proto=http auth=host user=null host=host port=-1 dport=80 file= path= query=null ref=null",
      "http://host"
    )
    assertParses(
      "[http://host:/p] proto=http auth=host: user=null host=host port=-1 dport=80 file=/p path=/p query=null ref=null",
      "http://host:/p"
    )
    assertParses(
      "[http://host:99999/] proto=http auth=host:99999 user=null host=host port=99999 dport=80 file=/ path=/ query=null ref=null",
      "http://host:99999/"
    )
    assertParses(
      "[http://host?q] proto=http auth=host user=null host=host port=-1 dport=80 file=?q path= query=q ref=null",
      "http://host?q"
    )
    assertParses(
      "[http://host#r] proto=http auth=host user=null host=host port=-1 dport=80 file= path= query=null ref=r",
      "http://host#r"
    )
    assertParses(
      "[http://host/p?q?r#s#t] proto=http auth=host user=null host=host port=-1 dport=80 file=/p?q?r path=/p query=q?r ref=s#t",
      "http://host/p?q?r#s#t"
    )
    assertParses(
      "[http://h/] proto=http auth=h user=null host=h port=-1 dport=80 file=/ path=/ query=null ref=null",
      "  http://h/  "
    )
    assertParses(
      "[http://h/a b] proto=http auth=h user=null host=h port=-1 dport=80 file=/a b path=/a b query=null ref=null",
      "http://h/a b"
    )
    assertParses(
      "[http://h/a/../b] proto=http auth=h user=null host=h port=-1 dport=80 file=/a/../b path=/a/../b query=null ref=null",
      "http://h/a/../b"
    )
    assertParses(
      "[https://h/] proto=https auth=h user=null host=h port=-1 dport=443 file=/ path=/ query=null ref=null",
      "https://h/"
    )
    assertParses(
      "[ftp://h/f] proto=ftp auth=h user=null host=h port=-1 dport=21 file=/f path=/f query=null ref=null",
      "ftp://h/f"
    )
    assertParses(
      "[mailto:a@b.com] proto=mailto auth=null user=null host= port=-1 dport=-1 file=a@b.com path=a@b.com query=null ref=null",
      "mailto:a@b.com"
    )
    assertEquals("http://h/x", new URL("url:http://h/x").toString())
    assertEquals("http://h/x", new URL("URL:http://h/x").toString())
    assertEquals("a_b", new URL("http://a_b/").getHost())
    assertEquals("a%20b", new URL("http://a%20b/").getHost())
  }

  @Test def ipv6Hosts(): Unit = {
    assertParses(
      "[http://[::1]:8080/p] proto=http auth=[::1]:8080 user=null host=[::1] port=8080 dport=80 file=/p path=/p query=null ref=null",
      "http://[::1]:8080/p"
    )
    assertParses(
      "[http://[::1]/p] proto=http auth=[::1] user=null host=[::1] port=-1 dport=80 file=/p path=/p query=null ref=null",
      "http://[::1]/p"
    )
    assertParses(
      "[http://[::1]] proto=http auth=[::1] user=null host=[::1] port=-1 dport=80 file= path= query=null ref=null",
      "http://[::1]"
    )
  }

  @Test def missingAuthority(): Unit = {
    assertParses(
      "[http:/p] proto=http auth=null user=null host= port=-1 dport=80 file=/p path=/p query=null ref=null",
      "http:/p"
    )
    assertParses(
      "[http:p] proto=http auth=null user=null host= port=-1 dport=80 file=p path=p query=null ref=null",
      "http:p"
    )
    assertParses(
      "[http:] proto=http auth=null user=null host= port=-1 dport=80 file= path= query=null ref=null",
      "http:"
    )
    assertParses(
      "[http:] proto=http auth= user=null host= port=-1 dport=80 file= path= query=null ref=null",
      "http://"
    )
    assertParses(
      "[http:/p] proto=http auth= user=null host= port=-1 dport=80 file=/p path=/p query=null ref=null",
      "http:///p"
    )
    assertParses(
      "[http:////p] proto=http auth=null user=null host= port=-1 dport=80 file=////p path=////p query=null ref=null",
      "http:////p"
    )
  }

  @Test def fileSpecs(): Unit = {
    assertParses(
      "[file:/tmp/x] proto=file auth=null user=null host= port=-1 dport=-1 file=/tmp/x path=/tmp/x query=null ref=null",
      "file:/tmp/x"
    )
    assertParses(
      "[file:/tmp/x] proto=file auth= user=null host= port=-1 dport=-1 file=/tmp/x path=/tmp/x query=null ref=null",
      "file:///tmp/x"
    )
    assertParses(
      "[file://host/tmp/x] proto=file auth=host user=null host=host port=-1 dport=-1 file=/tmp/x path=/tmp/x query=null ref=null",
      "file://host/tmp/x"
    )
    assertParses(
      "[file:tmp/x] proto=file auth=null user=null host= port=-1 dport=-1 file=tmp/x path=tmp/x query=null ref=null",
      "file:tmp/x"
    )
    assertParses(
      "[file:] proto=file auth=null user=null host= port=-1 dport=-1 file= path= query=null ref=null",
      "file:"
    )
  }

  @Test def errors(): Unit = {
    assertMalformed("Error at index 0 in: \"abc\"", "http://host:abc/")
    assertMalformed("Error at index 0 in: \":1\"", "http://::1/p")
    assertMalformed("Invalid port number :-5", "http://host:-5/")
    assertMalformed("unknown protocol: unknown", "unknown://h/")
    assertMalformed("unknown protocol: netdoc", "netdoc:/x")
    assertMalformed("no protocol: 1http://h/", "1http://h/")
    assertMalformed("no protocol: ht~tp://h/", "ht~tp://h/")
    assertMalformed("no protocol: /relative", "/relative")
    assertMalformed("no protocol: ", "")
    // Differs on JDK 17 and 21
    if (!executingInJVMOnLowerThanJDK(25))
      assertMalformed("Illegal character found in host: ' '", "http://a b/")
    assertThrows(classOf[MalformedURLException], new URL(null: String))
    val e =
      assertThrows(classOf[MalformedURLException], new URL(null: URL, "x"))
    assertEquals("no protocol: x", e.getMessage())
  }

  @Test def componentConstructors(): Unit = {
    assertEquals(
      "[http://h:80/f?q#r] proto=http auth=h:80 user=null host=h port=80 dport=80 file=/f?q path=/f query=q ref=r",
      describe(new URL("http", "h", 80, "/f?q#r"))
    )
    assertEquals(
      "[http://h/f] proto=http auth=h user=null host=h port=-1 dport=80 file=/f path=/f query=null ref=null",
      describe(new URL("http", "h", "/f"))
    )
    assertEquals("http://hf", new URL("http", "h", -1, "f").toString())
    assertEquals(
      "[http://[::1]:80/f] proto=http auth=[::1]:80 user=null host=[::1] port=80 dport=80 file=/f path=/f query=null ref=null",
      describe(new URL("http", "::1", 80, "/f"))
    )
    assertEquals(
      "http://[::1]:80/f",
      new URL("http", "[::1]", 80, "/f").toString()
    )
    assertEquals(
      "[http://h:80/f] proto=http auth=h:80 user=null host=h port=80 dport=80 file=/f path=/f query=null ref=null",
      describe(new URL("HTTP", "h", 80, "/f"))
    )
    assertEquals(
      "[http:/f] proto=http auth=null user=null host=null port=-1 dport=80 file=/f path=/f query=null ref=null",
      describe(new URL("http", null, 80, "/f"))
    )
    assertEquals(
      "[http:/f] proto=http auth= user=null host= port=-1 dport=80 file=/f path=/f query=null ref=null",
      describe(new URL("http", "", -1, "/f"))
    )
    assertEquals(
      "[file:tmp] proto=file auth= user=null host= port=-1 dport=-1 file=tmp path=tmp query=null ref=null",
      describe(new URL("file", "", -1, "tmp"))
    )
    val u = new URL("http", "h", 80, "/p?a#b?c")
    assertEquals(
      Seq("/p", "a", "b?c"),
      Seq(u.getPath(), u.getQuery(), u.getRef())
    )
    val v = new URL("http", "h", 80, "/a#b/c")
    assertEquals(Seq("/a", "b/c"), Seq(v.getFile(), v.getRef()))
    val w = new URL("http", "h", 80, "a#b")
    assertEquals(Seq("a", "b"), Seq(w.getFile(), w.getRef()))

    assertMalformedURL(
      "Invalid port number :-2",
      new URL("http", "h", -2, "/f")
    )
    assertMalformedURL("unknown protocol: foo", new URL("foo", "h", 80, "/f"))
    // Differs on JDK 17 and 21
    if (!executingInJVMOnLowerThanJDK(25)) {
      assertMalformedURL(
        "Illegal character found in host: '@'",
        new URL("http", "u@h", 80, "/f")
      )
      assertMalformedURL(
        "Illegal character found in host: ' '",
        new URL("http", "a b", 80, "/")
      )
      assertMalformedURL(
        "Illegal character found in host: '/'",
        new URL("http", "a/b", 80, "/")
      )
      assertMalformedURL(
        "Unrecognized IPv6 address format",
        new URL("http", "a:b", 80, "/")
      )
    }
    assertThrows(classOf[NullPointerException], new URL(null, "h", 80, "/f"))
    assertThrows(classOf[NullPointerException], new URL("http", "h", 80, null))
  }

  @Test def relativeResolution(): Unit = {
    val base = new URL("http://h:81/a/b/c.html?q#r")
    val cases = Seq(
      "d.html" -> "http://h:81/a/b/d.html",
      "./d" -> "http://h:81/a/b/d",
      "../d" -> "http://h:81/a/d",
      "../../d" -> "http://h:81/d",
      "../../../d" -> "http://h:81/../d",
      "/x/../y" -> "http://h:81/x/../y",
      "//other/z" -> "http://other/z",
      "//other" -> "http://other",
      "?nq" -> "http://h:81/a/b/?nq",
      "#nr" -> "http://h:81/a/b/c.html?q#nr",
      "" -> "http://h:81/a/b/c.html?q#r",
      "   " -> "http://h:81/a/b/c.html?q#r",
      "d?x#y" -> "http://h:81/a/b/d?x#y",
      "http:rel" -> "http://h:81/a/b/rel",
      "http:/abs" -> "http://h:81/abs",
      "https://s/x" -> "https://s/x",
      "file:/f" -> "file:/f",
      "d/../e" -> "http://h:81/a/b/e",
      "." -> "http://h:81/a/b/",
      ".." -> "http://h:81/a/",
      "./" -> "http://h:81/a/b/",
      "a/./b/../c" -> "http://h:81/a/b/a/c",
      "#" -> "http://h:81/a/b/c.html?q#",
      "?" -> "http://h:81/a/b/?",
      ";p" -> "http://h:81/a/b/;p",
      "d#x?y" -> "http://h:81/a/b/d#x?y",
      "  d  " -> "http://h:81/a/b/d",
      "url:c" -> "http://h:81/a/b/c"
    )
    cases.foreach {
      case (spec, expected) =>
        assertEquals(spec, expected, new URL(base, spec).toString())
    }
    assertEquals(
      "http://h/a/x",
      new URL(new URL("http://h/a/"), "x").toString()
    )
    val noPath = new URL("http://h")
    assertEquals("http://h/x", new URL(noPath, "x").toString())
    assertEquals("http://h/?q", new URL(noPath, "?q").toString())
    assertEquals("http://h/../x", new URL(noPath, "../x").toString())
    val fileBase = new URL("file:/tmp/dir/f.txt")
    assertEquals("file:/tmp/dir/g", new URL(fileBase, "g").toString())
    assertEquals("file:/tmp/g", new URL(fileBase, "../g").toString())
    assertEquals("file:/tmp/dir/g", new URL(fileBase, "file:g").toString())
    assertEquals("file:/a/c", new URL(new URL("file:/a/b"), "c").toString())
    assertEquals("file:tmp/y", new URL(new URL("file:tmp/x"), "y").toString())
    assertEquals("http://h/", new URL(null: URL, "http://h/").toString())
  }

  @Test def equalsAndSameFile(): Unit = {
    val u1 = new URL("http://127.0.0.1/p?q#r")
    assertEquals(u1, new URL("http://127.0.0.1/p?q#r"))
    assertNotEquals(u1, new URL("http://127.0.0.1/p?q#s"))
    assertTrue(u1.sameFile(new URL("http://127.0.0.1/p?q#s")))
    assertFalse(u1.sameFile(new URL("http://127.0.0.1/p?x#r")))
    assertEquals(new URL("http://127.0.0.1/"), new URL("http://127.0.0.1:80/"))
    assertTrue(
      new URL("http://127.0.0.1/").sameFile(new URL("http://127.0.0.1:80/"))
    )
    assertNotEquals(
      new URL("http://127.0.0.1/"),
      new URL("http://127.0.0.1:81/")
    )
    assertEquals(new URL("HTTP://127.0.0.1/"), new URL("http://127.0.0.1/"))
    assertNotEquals(
      new URL("http://127.0.0.1/A"),
      new URL("http://127.0.0.1/a")
    )
    assertEquals(new URL("http://u@127.0.0.1/"), new URL("http://127.0.0.1/"))
    assertNotEquals(new URL("http://127.0.0.1/"), new URL("https://127.0.0.1/"))
    assertEquals(new URL("http://localhost/"), new URL("http://127.0.0.1/"))
    assertEquals(new URL("file:/x"), new URL("file://localhost/x"))
    assertEquals(new URL("http://a.invalid/"), new URL("http://A.INVALID/"))
    assertNotEquals(u1, null)
    assertNotEquals(u1, "http://127.0.0.1/p?q#r")
    assertThrows(classOf[NullPointerException], u1.sameFile(null))
  }

  @Test def hashCodes(): Unit = {
    // protocol + host address (or lower-case name) + file + port (or default port) + ref
    val addr = 0x7f000001 // the JDK's hash of 127.0.0.1
    assertEquals(
      "http".hashCode + addr + "/p".hashCode + 80,
      new URL("http://127.0.0.1/p").hashCode()
    )
    assertEquals(
      new URL("http://127.0.0.1/p").hashCode(),
      new URL("http://127.0.0.1:80/p").hashCode()
    )
    assertEquals(
      "http".hashCode + addr + "/p".hashCode + 80 + "r".hashCode,
      new URL("http://127.0.0.1/p#r").hashCode()
    )
    assertEquals(
      "file".hashCode + "/x".hashCode - 1,
      new URL("file:/x").hashCode()
    )
    assertEquals(
      "http".hashCode + "h.invalid".hashCode + "/p".hashCode + 80,
      new URL("http://H.INVALID/p").hashCode()
    )
  }

  @Test def externalFormAndURI(): Unit = {
    assertEquals(
      "http://u@h:5/p?q#r",
      new URL("http://u@h:5/p?q#r").toExternalForm()
    )
    assertEquals(new URI("http://h/a?b#c"), new URL("http://h/a?b#c").toURI())
    assertEquals(new URI("file:/tmp/x"), new URL("file:/tmp/x").toURI())
    assertThrows(classOf[URISyntaxException], new URL("http://h/a b").toURI())
  }

  @Test def fromURIAndFile(): Unit = {
    val u = new URI("http://h/a?b#c").toURL()
    assertEquals(
      "[http://h/a?b#c] proto=http auth=h user=null host=h port=-1 dport=80 file=/a?b path=/a query=b ref=c",
      describe(u)
    )
    assertThrows(classOf[IllegalArgumentException], new URI("a/b").toURL())
    assertThrows(classOf[MalformedURLException], new URI("foo://h/").toURL())
    if (!Platform.isWindows)
      assertEquals(
        "file:/tmp/x%20y",
        new java.io.File("/tmp/x y").toURI().toURL().toString()
      )
  }

  @Test def openConnectionArguments(): Unit = {
    val u = new URL("file:/tmp")
    val e =
      assertThrows(classOf[IllegalArgumentException], u.openConnection(null))
    assertEquals("proxy can not be null", e.getMessage())
    assertNotNull(u.openConnection(Proxy.NO_PROXY))
  }
}
