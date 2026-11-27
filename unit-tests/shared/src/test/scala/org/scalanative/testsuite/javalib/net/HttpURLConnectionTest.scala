package org.scalanative.testsuite.javalib.net

import java.io._
import java.net._
import java.nio.charset.StandardCharsets.ISO_8859_1
import java.util.{ArrayList, Arrays, Collections, HashMap, List, Map}

import org.junit.Assert._
import org.junit.Assume.{assumeFalse, assumeTrue}
import org.junit.{Before, Test}

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform

class HttpURLConnectionTest {
  import HttpURLConnectionTest._

  @Before def requiresThreads(): Unit =
    assumeTrue("requires multithreading", Platform.isMultithreadingEnabled)

  private def open(u: String): HttpURLConnection =
    new URL(u).openConnection().asInstanceOf[HttpURLConnection]

  private def body(in: InputStream): String =
    new String(in.readAllBytes(), ISO_8859_1)

  private def body(c: URLConnection): String = body(c.getInputStream())

  @Test def get(): Unit = withServer(_ => ok("hello")) { s =>
    val c = open(s.base + "/a?b=1#frag")
    assertEquals("GET", c.getRequestMethod())
    c.setRequestProperty("X-A", "1")
    c.addRequestProperty("x-a", "2")
    assertEquals(200, c.getResponseCode())
    assertEquals("OK", c.getResponseMessage())
    assertEquals("hello", body(c))
    assertEquals(5, c.getContentLength())
    val r = s.last
    assertEquals("GET /a?b=1 HTTP/1.1", r.line)
    assertEquals(Arrays.asList("1", "2"), r.all("x-a"))
    assertEquals("127.0.0.1:" + s.port, r.header("Host"))
    assertFalse(c.usingProxy())
    assertTrue(c.toString().endsWith(":" + s.base + "/a?b=1#frag"))
  }

  @Test def emptyPathSendsSlash(): Unit = withServer(_ => ok("")) { s =>
    open(s.base).getResponseCode()
    assertEquals("GET / HTTP/1.1", s.last.line)
    open(s.base + "?a=b").getResponseCode()
    assertEquals("GET /?a=b HTTP/1.1", s.last.line)
  }

  @Test def responseHeaders(): Unit = withServer { _ =>
    "HTTP/1.1 200 OK\r\nX-D: 1\r\nx-d: 2\r\nX-D: 3\r\nContent-Length: 0\r\n" +
      "Connection: close\r\n\r\n"
  } { s =>
    val c = open(s.base + "/")
    assertEquals("HTTP/1.1 200 OK", c.getHeaderField(0))
    assertNull(c.getHeaderFieldKey(0))
    assertEquals("X-D", c.getHeaderFieldKey(1))
    assertEquals("x-d", c.getHeaderFieldKey(2))
    assertEquals("3", c.getHeaderField(3))
    assertNull(c.getHeaderFieldKey(99))
    assertNull(c.getHeaderField(99))
    assertEquals("3", c.getHeaderField("x-d"))
    assertEquals("0", c.getHeaderField("content-length"))
    assertNull(c.getContentType())
    val fields = c.getHeaderFields()
    assertEquals(Arrays.asList("HTTP/1.1 200 OK"), fields.get(null))
    assertEquals(Arrays.asList("1", "3"), fields.get("X-D"))
    assertEquals(Arrays.asList("2"), fields.get("x-d"))
    assertThrows(
      classOf[UnsupportedOperationException],
      fields.put("a", Collections.emptyList[String]())
    )
  }

  @Test def statusLines(): Unit = withServer { r =>
    if (r.line.contains("/nomsg"))
      "HTTP/1.1 200\r\nContent-Length: 0\r\nConnection: close\r\n\r\n"
    else if (r.line.contains("/http10"))
      "HTTP/1.0 201 Created\r\nContent-Length: 0\r\n\r\n"
    else if (r.line.contains("/continue"))
      "HTTP/1.1 100 Continue\r\n\r\n" + ok("ok")
    else "garbage\r\n\r\n"
  } { s =>
    val noMessage = open(s.base + "/nomsg")
    assertEquals(200, noMessage.getResponseCode())
    assertNull(noMessage.getResponseMessage())
    val http10 = open(s.base + "/http10")
    assertEquals(201, http10.getResponseCode())
    assertEquals("Created", http10.getResponseMessage())
    val continued = open(s.base + "/continue")
    assertEquals(200, continued.getResponseCode())
    assertEquals("ok", body(continued))
    assertEquals(-1, open(s.base + "/bogus").getResponseCode())
  }

  @Test def requestMethods(): Unit = {
    val c = open("http://127.0.0.1:1/")
    val valid =
      Array("GET", "POST", "HEAD", "OPTIONS", "PUT", "DELETE", "TRACE")
    var i = 0
    while (i < valid.length) {
      c.setRequestMethod(valid(i))
      assertEquals(valid(i), c.getRequestMethod())
      i += 1
    }
    val invalid = Array("get", "PATCH", "CONNECT", "GET\r\n", null)
    i = 0
    while (i < invalid.length) {
      val e =
        assertThrows(classOf[ProtocolException], c.setRequestMethod(invalid(i)))
      assertEquals("Invalid HTTP method: " + invalid(i), e.getMessage())
      i += 1
    }
  }

  @Test def headerInjection(): Unit = {
    val c = open("http://127.0.0.1:1/")
    val badValues = Array("a\r\nY: b", "a\nb", "a\r\n")
    var i = 0
    while (i < badValues.length) {
      val e = assertThrows(
        classOf[IllegalArgumentException],
        c.setRequestProperty("X", badValues(i))
      )
      assertEquals(
        "Illegal character(s) in message header value: " + badValues(i),
        e.getMessage()
      )
      assertThrows(
        classOf[IllegalArgumentException],
        c.addRequestProperty("X", badValues(i))
      )
      i += 1
    }
    val badKeys = Array("X\r\nY", "X:Y", "X\nY")
    i = 0
    while (i < badKeys.length) {
      val e = assertThrows(
        classOf[IllegalArgumentException],
        c.setRequestProperty(badKeys(i), "a")
      )
      assertEquals(
        "Illegal character(s) in message header field: " + badKeys(i),
        e.getMessage()
      )
      i += 1
    }
    c.setRequestProperty("X", "a\r\n b")
    c.setRequestProperty("Y", "a\n\tb")
    c.setRequestProperty("Z", null)
    assertNull(c.getRequestProperty("Z"))
  }

  @Test def urlWithLineBreak(): Unit = withServer(_ => ok("")) { s =>
    val u = new URL(s.base + "/a\r\nX-Injected: 1")
    assertThrows(
      classOf[MalformedURLException],
      u.openConnection().asInstanceOf[HttpURLConnection].getResponseCode()
    )
    assertTrue(s.requests.isEmpty())
  }

  @Test def restrictedHeaders(): Unit = withServer(_ => ok("")) { s =>
    val c = open(s.base + "/")
    c.setRequestProperty("Host", "evil.test")
    c.setRequestProperty("Content-Length", "5")
    assertNull(c.getRequestProperty("Host"))
    assertNull(c.getRequestProperty("Content-Length"))
    c.setRequestProperty("User-Agent", "me")
    c.setRequestProperty("Accept", "a/b")
    assertEquals(200, c.getResponseCode())
    val r = s.last
    assertEquals(Arrays.asList("127.0.0.1:" + s.port), r.all("Host"))
    assertNull(r.header("Content-Length"))
    assertEquals(Arrays.asList("me"), r.all("User-Agent"))
    assertEquals(Arrays.asList("a/b"), r.all("Accept"))
  }

  @Test def afterConnect(): Unit = withServer(_ => ok("")) { s =>
    val c = open(s.base + "/")
    c.setRequestProperty("A", "1")
    c.connect()
    c.connect()
    assertEquals("1", c.getRequestProperty("A"))
    assertNull(c.getRequestProperty("B"))
    assertThrows(classOf[IllegalStateException], c.getRequestProperties())
    assertThrows(classOf[IllegalStateException], c.setRequestProperty("a", "b"))
    assertEquals(
      "connect in progress",
      assertThrows(classOf[IllegalStateException], c.setRequestMethod("POST"))
        .getMessage()
    )
    assertEquals(
      "Already connected",
      assertThrows(
        classOf[IllegalStateException],
        c.setFixedLengthStreamingMode(1)
      ).getMessage()
    )
    assertEquals(
      "Can't set streaming mode: already connected",
      assertThrows(classOf[IllegalStateException], c.setChunkedStreamingMode(1))
        .getMessage()
    )
    assertEquals(200, c.getResponseCode())
  }

  @Test def streamingModes(): Unit = {
    val c = open("http://127.0.0.1:1/")
    assertEquals(
      "invalid content length",
      assertThrows(
        classOf[IllegalArgumentException],
        c.setFixedLengthStreamingMode(-1)
      ).getMessage()
    )
    c.setFixedLengthStreamingMode(3)
    assertEquals(
      "Fixed length streaming mode set",
      assertThrows(classOf[IllegalStateException], c.setChunkedStreamingMode(1))
        .getMessage()
    )
    val d = open("http://127.0.0.1:1/")
    d.setChunkedStreamingMode(1)
    assertEquals(
      "Chunked encoding streaming mode set",
      assertThrows(
        classOf[IllegalStateException],
        d.setFixedLengthStreamingMode(1L)
      ).getMessage()
    )
  }

  @Test def postBuffered(): Unit = withServer(r => ok(r.body)) { s =>
    val c = open(s.base + "/x")
    c.setDoOutput(true)
    val out = c.getOutputStream()
    assertSame(out, c.getOutputStream())
    assertEquals("POST", c.getRequestMethod())
    out.write("hi".getBytes(ISO_8859_1))
    assertEquals("hi", body(c))
    val r = s.last
    assertEquals("POST /x HTTP/1.1", r.line)
    assertEquals("2", r.header("Content-Length"))
    assertEquals("application/x-www-form-urlencoded", r.header("Content-Type"))
  }

  @Test def putWithBody(): Unit = withServer(r => ok(r.body)) { s =>
    val c = open(s.base + "/x")
    c.setRequestMethod("PUT")
    c.setDoOutput(true)
    c.getOutputStream().write('z')
    assertEquals("z", body(c))
    assertEquals("PUT /x HTTP/1.1", s.last.line)
    assertNull(s.last.header("Content-Type"))
    val empty = open(s.base + "/y")
    empty.setRequestMethod("PUT")
    empty.setDoOutput(true)
    empty.getOutputStream()
    assertEquals(200, empty.getResponseCode())
    assertEquals("0", s.last.header("Content-Length"))
  }

  @Test def fixedLengthStreaming(): Unit = withServer(r => ok(r.body)) { s =>
    val c = open(s.base + "/x")
    c.setDoOutput(true)
    c.setFixedLengthStreamingMode(2L)
    c.getOutputStream().write("hi".getBytes(ISO_8859_1))
    assertEquals("hi", body(c))
    assertEquals("2", s.last.header("Content-Length"))

    val tooMany = open(s.base + "/x")
    tooMany.setDoOutput(true)
    tooMany.setFixedLengthStreamingMode(1)
    assertEquals(
      "too many bytes written",
      assertThrows(
        classOf[IOException],
        tooMany.getOutputStream().write("hi".getBytes(ISO_8859_1))
      ).getMessage()
    )

    val tooFew = open(s.base + "/x")
    tooFew.setDoOutput(true)
    tooFew.setFixedLengthStreamingMode(3)
    tooFew.getOutputStream().write("hi".getBytes(ISO_8859_1))
    assertEquals(
      "insufficient data written",
      assertThrows(classOf[IOException], tooFew.getResponseCode()).getMessage()
    )
  }

  @Test def chunkedStreaming(): Unit = withServer(r => ok(r.body)) { s =>
    val c = open(s.base + "/x")
    c.setDoOutput(true)
    c.setChunkedStreamingMode(3)
    c.getOutputStream().write("hello".getBytes(ISO_8859_1))
    assertEquals("hello", body(c))
    assertEquals("chunked", s.last.header("Transfer-Encoding"))
    assertNull(s.last.header("Content-Length"))
  }

  @Test def outputErrors(): Unit = withServer(_ => ok("")) { s =>
    assertEquals(
      "cannot write to a URLConnection if doOutput=false - call setDoOutput(true)",
      assertThrows(classOf[ProtocolException], open(s.base).getOutputStream())
        .getMessage()
    )
    val trace = open(s.base)
    trace.setRequestMethod("TRACE")
    trace.setDoOutput(true)
    assertEquals(
      "HTTP method TRACE doesn't support output",
      assertThrows(classOf[ProtocolException], trace.getOutputStream())
        .getMessage()
    )
    val read = open(s.base)
    read.setDoOutput(true)
    read.getOutputStream()
    read.getInputStream()
    assertEquals(
      "Cannot write output after reading input.",
      assertThrows(classOf[ProtocolException], read.getOutputStream())
        .getMessage()
    )
    val noInput = open(s.base)
    noInput.setDoInput(false)
    assertThrows(classOf[ProtocolException], noInput.getInputStream())
    assertThrows(classOf[ProtocolException], noInput.getResponseCode())
  }

  @Test def responseBodies(): Unit = withServer { r =>
    if (r.line.contains("/chunked"))
      "HTTP/1.1 200 OK\r\nTransfer-Encoding: chunked\r\nConnection: close\r\n" +
        "\r\n3\r\nabc\r\n2;x=y\r\nde\r\n0\r\nT: v\r\n\r\n"
    else if (r.line.contains("/close"))
      "HTTP/1.1 200 OK\r\nConnection: close\r\n\r\nuntil-close"
    else ok("0123456789")
  } { s =>
    val chunked = open(s.base + "/chunked")
    assertEquals("abcde", body(chunked))
    assertEquals(-1, chunked.getContentLength())
    assertEquals("until-close", body(open(s.base + "/close")))
    val head = open(s.base + "/head")
    head.setRequestMethod("HEAD")
    assertEquals(200, head.getResponseCode())
    assertEquals(10, head.getContentLength())
    assertEquals("", body(head))
    assertEquals("HEAD /head HTTP/1.1", s.last.line)
  }

  @Test def errorResponses(): Unit = withServer { r =>
    if (r.line.contains("/nf"))
      "HTTP/1.1 404 Not Found\r\nContent-Length: 4\r\nConnection: close\r\n\r\nnope"
    else if (r.line.contains("/gone"))
      "HTTP/1.1 410 Gone\r\nContent-Length: 0\r\nConnection: close\r\n\r\n"
    else if (r.line.contains("/empty"))
      "HTTP/1.1 500 Boom\r\nContent-Length: 0\r\nConnection: close\r\n\r\n"
    else
      "HTTP/1.1 500 Boom\r\nContent-Length: 4\r\nConnection: close\r\n\r\nboom"
  } { s =>
    val before = open(s.base + "/nf")
    assertNull(before.getErrorStream())
    val nf = open(s.base + "/nf")
    assertEquals(404, nf.getResponseCode())
    assertEquals("Not Found", nf.getResponseMessage())
    assertEquals(
      s.base + "/nf",
      assertThrows(classOf[FileNotFoundException], nf.getInputStream())
        .getMessage()
    )
    assertEquals("nope", body(nf.getErrorStream()))
    assertThrows(
      classOf[FileNotFoundException],
      open(s.base + "/gone").getInputStream()
    )

    val err = open(s.base + "/err")
    assertEquals(
      "Server returned HTTP response code: 500 for URL: " + s.base + "/err",
      assertThrows(classOf[IOException], err.getInputStream()).getMessage()
    )
    assertThrows(classOf[IOException], err.getInputStream())
    assertEquals("boom", body(err.getErrorStream()))
    assertEquals(500, err.getResponseCode())

    val empty = open(s.base + "/empty")
    assertEquals(500, empty.getResponseCode())
    assertNull(empty.getErrorStream())
  }

  private def redirectServer(r: Request): String = {
    val code = r.line.split(' ')(1).stripPrefix("/r")
    if (r.line.contains("/loop"))
      "HTTP/1.1 302 Found\r\nLocation: /loop\r\nContent-Length: 0\r\n" +
        "Connection: close\r\n\r\n"
    else if (r.line.contains("/toftp"))
      "HTTP/1.1 302 Found\r\nLocation: ftp://x/final\r\nContent-Length: 0\r\n" +
        "Connection: close\r\n\r\n"
    else if (r.line.contains("/r"))
      "HTTP/1.1 " + code + " Moved\r\nLocation: /final\r\nContent-Length: 0\r\n" +
        "Connection: close\r\n\r\n"
    else ok(r.line.split(' ')(0) + " " + r.body)
  }

  @Test def redirects(): Unit = withServer(redirectServer) { s =>
    def send(code: String, method: String): HttpURLConnection = {
      val c = open(s.base + "/r" + code)
      c.setRequestMethod(method)
      if (method != "GET") {
        c.setDoOutput(true)
        c.getOutputStream().write("BODY".getBytes(ISO_8859_1))
      }
      c
    }
    val codes = Array("300", "301", "302", "303")
    var i = 0
    while (i < codes.length) {
      val post = send(codes(i), "POST")
      assertEquals(200, post.getResponseCode())
      assertEquals(s.base + "/final", post.getURL().toString())
      assertEquals("GET", post.getRequestMethod())
      assertEquals("GET ", body(post))
      val put = send(codes(i), "PUT")
      assertEquals("PUT BODY", body(put))
      i += 1
    }
    val temp = send("307", "POST")
    assertEquals("POST BODY", body(temp))
    assertEquals(308, send("308", "GET").getResponseCode())

    val other = open(s.base + "/toftp")
    assertEquals(302, other.getResponseCode())
    assertEquals(s.base + "/toftp", other.getURL().toString())

    val manual = open(s.base + "/r302")
    manual.setInstanceFollowRedirects(false)
    assertEquals(302, manual.getResponseCode())
    assertEquals("/final", manual.getHeaderField("Location"))
  }

  @Test def followRedirectsDefault(): Unit = withServer(redirectServer) { s =>
    assertTrue(HttpURLConnection.getFollowRedirects())
    HttpURLConnection.setFollowRedirects(false)
    try {
      val c = open(s.base + "/r302")
      assertFalse(c.getInstanceFollowRedirects())
      assertEquals(302, c.getResponseCode())
    } finally HttpURLConnection.setFollowRedirects(true)
    assertTrue(open(s.base + "/r302").getInstanceFollowRedirects())
  }

  @Test def redirectLimit(): Unit = withServer(redirectServer) { s =>
    val c = open(s.base + "/loop")
    assertEquals(
      "Server redirected too many times (20)",
      assertThrows(classOf[ProtocolException], c.getResponseCode())
        .getMessage()
    )
    assertEquals(20, s.requests.size())
    assertThrows(classOf[ProtocolException], c.getInputStream())
  }

  @Test def redirectInStreamingMode(): Unit = withServer(redirectServer) { s =>
    val c = open(s.base + "/r302")
    c.setDoOutput(true)
    c.setChunkedStreamingMode(2)
    c.getOutputStream().write("BODY".getBytes(ISO_8859_1))
    assertEquals(302, c.getResponseCode())
    val e = assertThrows(classOf[HttpRetryException], c.getInputStream())
    assertEquals(302, e.responseCode())
    assertEquals("/final", e.getLocation())
    assertEquals(
      "cannot retry due to redirection, in streaming mode",
      e.getReason()
    )
  }

  @Test def redirectToOtherHostDropsAuthorization(): Unit = withServer { r =>
    if (r.line.contains("/away"))
      "HTTP/1.1 302 Found\r\nLocation: http://localhost:" +
        r.header("Host").split(':')(1) + "/final\r\nContent-Length: 0\r\n" +
        "Connection: close\r\n\r\n"
    else ok("")
  } { s =>
    val c = open(s.base + "/away")
    c.setRequestProperty("Authorization", "Bearer secret")
    c.setRequestProperty("X-C", "1")
    assertEquals(200, c.getResponseCode())
    assertEquals("localhost", c.getURL().getHost())
    assertEquals("Bearer secret", s.requests.get(0).header("Authorization"))
    assertNull(s.last.header("Authorization"))
    assertEquals("1", s.last.header("X-C"))
  }

  private def authServer(r: Request): String =
    if (r.header("Authorization") != null) ok(r.header("Authorization"))
    else
      "HTTP/1.1 401 Unauthorized\r\nWWW-Authenticate: Basic realm=\"test realm\"" +
        "\r\nContent-Length: 6\r\nConnection: close\r\n\r\ndenied"

  private class RecordingAuthenticator extends Authenticator {
    val calls = new ArrayList[String]()
    override protected def getPasswordAuthentication()
        : PasswordAuthentication = {
      calls.add(
        getRequestingHost() + "|" + getRequestingSite() + "|" +
          getRequestingPort() + "|" + getRequestingProtocol() + "|" +
          getRequestingPrompt() + "|" + getRequestingScheme() + "|" +
          getRequestingURL() + "|" + getRequestorType()
      )
      new PasswordAuthentication("user", "pw".toCharArray())
    }
  }

  @Test def basicAuthentication(): Unit = withServer(authServer) { s =>
    val auth = new RecordingAuthenticator
    val c = open(s.base + "/auth?q")
    c.setAuthenticator(auth)
    assertEquals(200, c.getResponseCode())
    assertEquals("Basic dXNlcjpwdw==", body(c))
    assertEquals(
      Arrays.asList(
        "127.0.0.1|/127.0.0.1|" + s.port + "|http|test realm|basic|" + s.base +
          "/auth?q|SERVER"
      ),
      auth.calls
    )
  }

  @Test def defaultAuthenticator(): Unit = withServer(authServer) { s =>
    val noAuth = open(s.base + "/a")
    assertEquals(401, noAuth.getResponseCode())
    assertEquals("denied", body(noAuth.getErrorStream()))
    assertEquals(
      "Basic realm=\"test realm\"",
      noAuth.getHeaderField("WWW-Authenticate")
    )
    val auth = new RecordingAuthenticator
    Authenticator.setDefault(auth)
    try {
      assertSame(auth, Authenticator.getDefault())
      assertEquals("Basic dXNlcjpwdw==", body(open(s.base + "/b")))
      assertEquals(1, auth.calls.size())
    } finally Authenticator.setDefault(null)
    assertNull(Authenticator.getDefault())
  }

  @Test def setAuthenticator(): Unit = withServer(_ => ok("")) { s =>
    val c = open(s.base)
    assertThrows(classOf[NullPointerException], c.setAuthenticator(null))
    c.setAuthenticator(new Authenticator {})
    c.setAuthenticator(new Authenticator {})
    c.connect()
    assertEquals(
      "Authenticator must be set before connecting",
      assertThrows(
        classOf[IllegalStateException],
        c.setAuthenticator(new Authenticator {})
      ).getMessage()
    )
  }

  @Test def explicitProxy(): Unit = withServer(r => ok(r.line)) { s =>
    val proxy =
      new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", s.port))
    val c = new URL("http://example.invalid:8080/p?q#f")
      .openConnection(proxy)
      .asInstanceOf[HttpURLConnection]
    assertTrue(c.usingProxy())
    assertEquals("GET http://example.invalid:8080/p?q HTTP/1.1", body(c))
    assertTrue(c.usingProxy())
    assertEquals("example.invalid:8080", s.last.header("Host"))
    val direct = new URL(s.base + "/")
      .openConnection(Proxy.NO_PROXY)
      .asInstanceOf[HttpURLConnection]
    assertEquals("GET / HTTP/1.1", body(direct))
    assertFalse(direct.usingProxy())
    assertThrows(
      classOf[IllegalArgumentException],
      new URL(s.base).openConnection(null)
    )
  }

  @Test def proxySelector(): Unit = withServer(r => ok(r.line)) { s =>
    val dead = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 1))
    val good =
      new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", s.port))
    val seen = new ArrayList[String]()
    val old = ProxySelector.getDefault()
    ProxySelector.setDefault(new ProxySelector {
      def select(uri: URI): List[Proxy] = {
        seen.add("select " + uri)
        Arrays.asList(dead, good)
      }
      def connectFailed(uri: URI, sa: SocketAddress, e: IOException): Unit =
        seen.add("failed " + uri + " " + sa)
    })
    try {
      val c = open("http://example.invalid/sel?q=1")
      assertEquals("GET http://example.invalid/sel?q=1 HTTP/1.1", body(c))
      assertTrue(c.usingProxy())
      assertEquals(
        Arrays.asList(
          "select http://example.invalid/sel?q=1",
          "failed http://example.invalid/sel?q=1 " + dead.address()
        ),
        seen
      )
    } finally ProxySelector.setDefault(old)
    ProxySelector.setDefault(null)
    try assertEquals("GET / HTTP/1.1", body(open(s.base + "/")))
    finally ProxySelector.setDefault(old)
  }

  @Test def proxyAuthentication(): Unit = {
    assumeFalse("JDK 25 does not answer this 407", Platform.executingInJVM)
    withServer { r =>
      if (r.header("Proxy-Authorization") != null)
        ok(r.header("Proxy-Authorization") + "|" + r.header("Authorization"))
      else
        "HTTP/1.1 407 Proxy Auth\r\nProxy-Authenticate: Basic realm=\"px\"\r\n" +
          "Content-Length: 0\r\nConnection: close\r\n\r\n"
    } { s =>
      val auth = new RecordingAuthenticator
      val proxy =
        new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", s.port))
      val c = new URL("http://example.invalid/p")
        .openConnection(proxy)
        .asInstanceOf[HttpURLConnection]
      c.setAuthenticator(auth)
      assertEquals("Basic dXNlcjpwdw==|null", body(c))
      assertEquals(
        Arrays.asList(
          "127.0.0.1|/127.0.0.1|" + s.port +
            "|http|px|basic|http://example.invalid/p|PROXY"
        ),
        auth.calls
      )
    }
  }

  @Test def cookieHandler(): Unit = withServer { _ =>
    "HTTP/1.1 200 OK\r\nSet-Cookie: a=1\r\nSet-Cookie: b=2\r\n" +
      "Content-Length: 0\r\nConnection: close\r\n\r\n"
  } { s =>
    val seen = new ArrayList[String]()
    CookieHandler.setDefault(new CookieHandler {
      def get(
          uri: URI,
          h: Map[String, List[String]]
      ): Map[String, List[String]] = {
        seen.add("get " + uri + " " + h.get("X-R"))
        val m = new HashMap[String, List[String]]()
        m.put("Cookie", Arrays.asList("x=1", "y=2"))
        m.put("X-Other", Arrays.asList("no"))
        m
      }
      def put(uri: URI, h: Map[String, List[String]]): Unit =
        seen.add("put " + uri + " " + h.get("Set-Cookie"))
    })
    try {
      val c = open(s.base + "/c?z#f")
      c.setRequestProperty("X-R", "1")
      assertEquals(200, c.getResponseCode())
    } finally CookieHandler.setDefault(null)
    assertEquals(
      Arrays.asList(
        "get " + s.base + "/c?z#f [1]",
        "put " + s.base + "/c?z#f [a=1, b=2]"
      ),
      seen
    )
    assertEquals("x=1; y=2", s.last.header("Cookie"))
    assertNull(s.last.header("X-Other"))
  }

  @Test def cookieValuesCannotInjectHeaders(): Unit = {
    assumeFalse("JDK 25 sends the line break", Platform.executingInJVM)
    withServer(_ => ok("")) { s =>
      CookieHandler.setDefault(new CookieHandler {
        def get(
            u: URI,
            h: Map[String, List[String]]
        ): Map[String, List[String]] =
          Collections.singletonMap(
            "Cookie",
            Arrays.asList("x=1\r\nEvil: 1", "y=2")
          )
        def put(u: URI, h: Map[String, List[String]]): Unit = ()
      })
      try assertEquals(200, open(s.base).getResponseCode())
      finally CookieHandler.setDefault(null)
      assertEquals("y=2", s.last.header("Cookie"))
      assertNull(s.last.header("Evil"))
    }
  }

  @Test def responseCacheMiss(): Unit = withServer(_ => ok("fresh")) { s =>
    val seen = new ArrayList[String]()
    val stored = new ByteArrayOutputStream()
    ResponseCache.setDefault(new ResponseCache {
      def get(
          uri: URI,
          m: String,
          h: Map[String, List[String]]
      ): CacheResponse = {
        seen.add("get " + uri + " " + m + " " + h.get("X-R"))
        null
      }
      def put(uri: URI, c: URLConnection): CacheRequest = {
        seen.add("put " + uri + " " + c.getHeaderField("Content-Length"))
        new CacheRequest {
          def getBody(): OutputStream = stored
          def abort(): Unit = seen.add("abort")
        }
      }
    })
    try {
      val c = open(s.base + "/m?a#f")
      c.setRequestProperty("X-R", "1")
      assertEquals("fresh", body(c))
      assertEquals(
        Arrays.asList(
          "get " + s.base + "/m?a#f GET [1]",
          "put " + s.base + "/m?a#f 5"
        ),
        seen
      )
      assertEquals("fresh", stored.toString("ISO-8859-1"))
      seen.clear()
      val uncached = open(s.base + "/n")
      uncached.setUseCaches(false)
      assertEquals(200, uncached.getResponseCode())
      assertTrue(seen.isEmpty())
      assertEquals("no-cache", s.last.header("Cache-Control"))
      assertEquals("no-cache", s.last.header("Pragma"))
    } finally ResponseCache.setDefault(null)
  }

  @Test def responseCacheHit(): Unit = withServer(_ => ok("fresh")) { s =>
    ResponseCache.setDefault(new ResponseCache {
      def get(
          uri: URI,
          m: String,
          h: Map[String, List[String]]
      ): CacheResponse =
        new CacheResponse {
          def getHeaders(): Map[String, List[String]] = {
            val m = new HashMap[String, List[String]]()
            m.put(null, Arrays.asList("HTTP/1.1 203 FromCache"))
            m.put("X-Cached", Arrays.asList("yes"))
            m
          }
          def getBody(): InputStream =
            new ByteArrayInputStream("cached".getBytes(ISO_8859_1))
        }
      def put(uri: URI, c: URLConnection): CacheRequest = null
    })
    try {
      val c = open(s.base + "/h")
      assertEquals(203, c.getResponseCode())
      assertEquals("FromCache", c.getResponseMessage())
      assertEquals("yes", c.getHeaderField("X-Cached"))
      assertEquals("cached", body(c))
      assertTrue(s.requests.isEmpty())
    } finally ResponseCache.setDefault(null)
  }

  @Test def ifModifiedSince(): Unit = withServer(_ => ok("")) { s =>
    val c = open(s.base)
    c.setIfModifiedSince(1000L)
    c.getResponseCode()
    assertEquals(
      "Thu, 01 Jan 1970 00:00:01 GMT",
      s.last.header("If-Modified-Since")
    )
  }

  @Test def readTimeout(): Unit = withServer { _ =>
    Thread.sleep(2000)
    ok("late")
  } { s =>
    val c = open(s.base)
    c.setReadTimeout(200)
    assertThrows(classOf[SocketTimeoutException], c.getResponseCode())
  }

  @Test def connectionFailures(): Unit = {
    val free = {
      val ss = new ServerSocket(0, 1, loopback); val p = ss.getLocalPort();
      ss.close(); p
    }
    val c = open("http://127.0.0.1:" + free + "/")
    assertThrows(classOf[ConnectException], c.getResponseCode())
    assertThrows(classOf[ConnectException], c.getInputStream())
    assertNull(c.getHeaderField(0))
    assertTrue(c.getHeaderFields().isEmpty())
    assertEquals(-1, c.getContentLength())
    assertThrows(
      classOf[UnknownHostException],
      open("http://nonexistent.invalid/").getResponseCode()
    )
  }

  @Test def disconnect(): Unit = withServer(_ => ok("hello")) { s =>
    val before = open(s.base)
    before.disconnect()
    assertEquals(200, before.getResponseCode())
    val c = open(s.base)
    val in = c.getInputStream()
    c.disconnect()
    assertEquals(200, c.getResponseCode())
    assertEquals("HTTP/1.1 200 OK", c.getHeaderField(0))
    assertThrows(classOf[IOException], in.read())
  }

  @Test def defaultProxySelector(): Unit = {
    val sel = ProxySelector.getDefault()
    val props = Array("http.proxyHost", "http.proxyPort", "http.nonProxyHosts")
    val saved = props.map(System.getProperty(_))
    try {
      props.foreach(System.clearProperty(_))
      assertEquals(
        Arrays.asList(Proxy.NO_PROXY),
        sel.select(URI.create("http://example.com/"))
      )
      System.setProperty("http.proxyHost", "proxy.test")
      System.setProperty("http.proxyPort", "3128")
      val p = sel.select(URI.create("http://example.com/")).get(0)
      assertEquals(Proxy.Type.HTTP, p.`type`())
      val a = p.address().asInstanceOf[InetSocketAddress]
      assertTrue(a.isUnresolved())
      assertEquals("proxy.test", a.getHostString())
      assertEquals(3128, a.getPort())
      assertEquals(
        Arrays.asList(Proxy.NO_PROXY),
        sel.select(URI.create("http://127.0.0.1/"))
      )
      assertEquals(
        Arrays.asList(Proxy.NO_PROXY),
        sel.select(URI.create("https://example.com/"))
      )
      System.setProperty("http.nonProxyHosts", "*.example.com|foo")
      assertEquals(
        Arrays.asList(Proxy.NO_PROXY),
        sel.select(URI.create("http://a.example.com/"))
      )
      assertEquals(
        Arrays.asList(Proxy.NO_PROXY),
        sel.select(URI.create("http://foo/"))
      )
      assertEquals(p, sel.select(URI.create("http://bar/")).get(0))
      assertThrows(
        classOf[IllegalArgumentException],
        sel.select(URI.create("mailto:a@b"))
      )
      assertThrows(classOf[IllegalArgumentException], sel.select(null))
    } finally {
      var i = 0
      while (i < props.length) {
        if (saved(i) == null) System.clearProperty(props(i))
        else System.setProperty(props(i), saved(i))
        i += 1
      }
    }
  }

  @Test def staticProxySelector(): Unit = {
    val addr = new InetSocketAddress("127.0.0.1", 5)
    val sel = ProxySelector.of(addr)
    val http = new Proxy(Proxy.Type.HTTP, addr)
    assertEquals(Arrays.asList(http), sel.select(URI.create("http://a/")))
    assertEquals(Arrays.asList(http), sel.select(URI.create("https://a/")))
    assertEquals(
      Arrays.asList(Proxy.NO_PROXY),
      sel.select(URI.create("ftp://a/"))
    )
    assertEquals(
      Arrays.asList(Proxy.NO_PROXY),
      ProxySelector.of(null).select(URI.create("http://a/"))
    )
    assertEquals(
      "URI can't be null",
      assertThrows(classOf[IllegalArgumentException], sel.select(null))
        .getMessage()
    )
    assertEquals(
      "Arguments can't be null.",
      assertThrows(
        classOf[IllegalArgumentException],
        sel.connectFailed(null, null, null)
      ).getMessage()
    )
  }

  @Test def httpsIsParseOnly(): Unit = {
    assumeFalse("the JDK supports https", Platform.executingInJVM)
    val u = new URL("https://127.0.0.1:1/x")
    assertEquals(443, u.getDefaultPort())
    assertThrows(classOf[IOException], u.openConnection())
  }

  @Test def supportingClasses(): Unit = {
    val e = new HttpRetryException("why", 307, "/there")
    assertEquals("why", e.getReason())
    assertEquals("why", e.getMessage())
    assertEquals(307, e.responseCode())
    assertEquals("/there", e.getLocation())
    assertNull(new HttpRetryException("why", 401).getLocation())

    val pw = Array('a', 'b')
    val pa = new PasswordAuthentication("u", pw)
    pw(0) = 'x'
    assertEquals("u", pa.getUserName())
    assertEquals("ab", new String(pa.getPassword()))

    assertNull(
      Authenticator.requestPasswordAuthentication(
        null,
        null,
        80,
        "http",
        "p",
        "basic"
      )
    )
    val auth = new RecordingAuthenticator
    assertEquals(
      "user",
      Authenticator
        .requestPasswordAuthentication(
          auth,
          "h",
          null,
          1,
          "http",
          "p",
          "basic",
          null,
          Authenticator.RequestorType.PROXY
        )
        .getUserName()
    )
    assertEquals(Arrays.asList("h|null|1|http|p|basic|null|PROXY"), auth.calls)
    assertEquals(
      Arrays.asList(
        Authenticator.RequestorType.PROXY,
        Authenticator.RequestorType.SERVER
      ),
      Arrays.asList(Authenticator.RequestorType.values(): _*)
    )
    assertEquals(
      Authenticator.RequestorType.SERVER,
      Authenticator.RequestorType.valueOf("SERVER")
    )
    assertNull(ResponseCache.getDefault())
    assertNull(CookieHandler.getDefault())
    assertEquals(HttpURLConnection.HTTP_OK, 200)
    assertEquals(HttpURLConnection.HTTP_VERSION, 505)
  }
}

object HttpURLConnectionTest {

  val loopback: InetAddress = InetAddress.getByName("127.0.0.1")

  def ok(body: String): String =
    "HTTP/1.1 200 OK\r\nContent-Length: " + body.length +
      "\r\nConnection: close\r\n\r\n" + body

  final class Request(
      val line: String,
      headers: List[Array[String]],
      val body: String,
      val chunks: List[String]
  ) {
    def all(name: String): List[String] = {
      val out = new ArrayList[String]()
      val it = headers.iterator()
      while (it.hasNext()) {
        val kv = it.next()
        if (kv(0).equalsIgnoreCase(name)) out.add(kv(1))
      }
      out
    }
    def header(name: String): String = {
      val vs = all(name)
      if (vs.isEmpty()) null else vs.get(vs.size() - 1)
    }
  }

  private def readLine(in: InputStream): String = {
    val sb = new java.lang.StringBuilder()
    var c = in.read()
    while (c != -1 && c != '\n') {
      if (c != '\r') sb.append(c.toChar)
      c = in.read()
    }
    if (c == -1 && sb.length() == 0) null else sb.toString
  }

  private def readN(in: InputStream, n: Int): String = {
    val b = new Array[Byte](n)
    var off = 0
    while (off < n) {
      val k = in.read(b, off, n - off)
      if (k < 0) throw new EOFException()
      off += k
    }
    new String(b, ISO_8859_1)
  }

  private def readRequest(in: InputStream): Request = {
    val line = readLine(in)
    if (line == null) return null
    val headers = new ArrayList[Array[String]]()
    var h = readLine(in)
    while (h != null && !h.isEmpty) {
      val colon = h.indexOf(':')
      headers.add(Array(h.substring(0, colon), h.substring(colon + 1).trim()))
      h = readLine(in)
    }
    val req0 = new Request(line, headers, "", Collections.emptyList())
    val chunks = new ArrayList[String]()
    val body =
      if (req0.header("Content-Length") != null)
        readN(in, Integer.parseInt(req0.header("Content-Length")))
      else if ("chunked" == req0.header("Transfer-Encoding")) {
        val sb = new java.lang.StringBuilder()
        var size = Integer.parseInt(readLine(in).trim(), 16)
        while (size > 0) {
          val chunk = readN(in, size)
          chunks.add(chunk)
          sb.append(chunk)
          readLine(in)
          size = Integer.parseInt(readLine(in).trim(), 16)
        }
        readLine(in)
        sb.toString
      } else ""
    new Request(line, headers, body, chunks)
  }

  /** Answers each connection with `handler` and closes it. */
  final class Server(handler: Request => String) {
    private val ss = new ServerSocket(0, 50, loopback)
    ss.setSoTimeout(100)
    @volatile private var running = true
    val requests: List[Request] =
      Collections.synchronizedList(new ArrayList[Request]())
    val port: Int = ss.getLocalPort()
    val base: String = "http://127.0.0.1:" + port

    def last: Request = requests.get(requests.size() - 1)

    private val acceptor = new Thread(() => {
      while (running) {
        try {
          val s = ss.accept()
          val t = new Thread(() => serve(s))
          t.setDaemon(true)
          t.start()
        } catch { case _: IOException => () }
      }
    })
    acceptor.setDaemon(true)
    acceptor.start()

    private def serve(s: Socket): Unit =
      try {
        s.setSoTimeout(5000)
        val in = new BufferedInputStream(s.getInputStream())
        val r = readRequest(in)
        if (r != null) {
          requests.add(r)
          val out = s.getOutputStream()
          out.write(handler(r).getBytes(ISO_8859_1))
          out.flush()
        }
      } catch {
        case _: IOException => ()
      } finally s.close()

    def close(): Unit = {
      running = false
      acceptor.join(2000)
      ss.close()
    }
  }

  def withServer(handler: Request => String)(test: Server => Unit): Unit = {
    val s = new Server(handler)
    try test(s)
    finally s.close()
  }
}
