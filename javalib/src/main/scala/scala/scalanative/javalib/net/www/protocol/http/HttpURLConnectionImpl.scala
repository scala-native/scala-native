/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Ported from Apache Harmony
// (org.apache.harmony.luni.internal.net.www.protocol.http.HttpURLConnectionImpl),
// simplified to one connection per request.

package scala.scalanative.javalib.net.www.protocol.http

import java.io.{
  BufferedInputStream, BufferedOutputStream, ByteArrayInputStream,
  ByteArrayOutputStream, FileNotFoundException, IOException, InputStream,
  OutputStream
}
import java.net._
import java.nio.charset.StandardCharsets
import java.util.{ArrayList, Base64, Collections, LinkedHashMap, List, Map}

import scala.scalanative.javalib.net.{
  DefaultProxySelector, HttpDates, HttpStatusLine
}

/** An HTTP/1.1 client connection over a plain `Socket`, directly or through an
 *  HTTP proxy. Each request uses a new connection, closed at the end of the
 *  response body.
 *
 *    - Redirects within `http` are followed for 300-303 and 307, at most 20;
 *      `POST` becomes `GET` except for 307. Credentials are dropped when a
 *      redirect leaves the host.
 *    - Basic authentication after a 401, or a 407 from the proxy.
 *    - `CookieHandler.getDefault()` and, for `GET`,
 *      `ResponseCache.getDefault()` are consulted.
 */
final class HttpURLConnectionImpl(u: URL, requestedProxy: Proxy)
    extends HttpURLConnection(u) {
  import HttpURLConnectionImpl._

  private var authenticator: Authenticator = null
  private var socket: Socket = null
  private var socketIn: SocketInput = null
  private var socketOut: OutputStream = null
  private var proxy: Proxy = Proxy.NO_PROXY
  private var requestProps: ArrayList[Array[String]] = null
  private var headSent = false
  private var requestBody: ByteArrayOutputStream = null
  private var streamingBody: OutputStream = null
  private var responseRead = false
  private var failure: IOException = null
  private var retryFailure: HttpRetryException = null
  private var statusLine: String = null
  private var headers = new ArrayList[Array[String]]()
  private var headerFields: Map[String, List[String]] = null
  private var headerFieldsFor: ArrayList[Array[String]] = null
  private var cachedUri: URI = null
  private var cachedUriFor: URL = null
  private var body: InputStream = null
  private var bodyEmpty = false
  private var authorization: String = null
  private var proxyAuthorization: String = null
  private var proxyAuthorizationFor: Proxy = null
  private var triedAuth = false
  private var triedProxyAuthFor: Proxy = null
  private var redirects = 0

  override def setAuthenticator(auth: Authenticator): Unit = {
    if (auth == null) throw new NullPointerException()
    if (connected)
      throw new IllegalStateException(
        "Authenticator must be set before connecting"
      )
    authenticator = auth
  }

  override def setRequestProperty(key: String, value: String): Unit = {
    checkHeader(key, value)
    if (key == null || !isRestricted(key) || connected)
      super.setRequestProperty(key, value)
  }

  override def addRequestProperty(key: String, value: String): Unit = {
    checkHeader(key, value)
    if (key == null || !isRestricted(key) || connected)
      super.addRequestProperty(key, value)
  }

  override def getRequestProperty(key: String): String =
    if (!connected) super.getRequestProperty(key)
    else if (key == null) null
    else {
      val i = lastIndexOf(requestProps, key)
      if (i < 0) null else requestProps.get(i)(1)
    }

  @throws[IOException]
  override def connect(): Unit = if (!connected) {
    checkURL(url)
    requestProps = snapshotRequestProperties()
    if (!loadFromCache()) openSocket()
    connected = true
  }

  private def snapshotRequestProperties(): ArrayList[Array[String]] = {
    val out = new ArrayList[Array[String]]()
    val it = getRequestProperties().entrySet().iterator()
    while (it.hasNext()) {
      val e = it.next()
      val vs = e.getValue().iterator()
      while (vs.hasNext()) out.add(Array(e.getKey(), vs.next()))
    }
    out
  }

  private def host: String = {
    val h = url.getHost()
    if (h.startsWith("[") && h.endsWith("]")) h.substring(1, h.length - 1)
    else h
  }

  private def port: Int =
    if (url.getPort() == -1) url.getDefaultPort() else url.getPort()

  private def uri: URI = {
    if (cachedUriFor ne url) {
      cachedUri = toURI(url)
      cachedUriFor = url
    }
    cachedUri
  }

  private def toURI(url: URL): URI =
    try url.toURI()
    catch {
      case _: URISyntaxException =>
        try
          new URI(
            url.getProtocol(),
            url.getUserInfo(),
            host,
            url.getPort(),
            url.getPath(),
            url.getQuery(),
            url.getRef()
          )
        catch { case _: URISyntaxException => null }
    }

  @throws[IOException]
  private def loadFromCache(): Boolean = {
    val cache = ResponseCache.getDefault()
    if (cache == null || !useCaches || method != "GET") return false
    val u = uri
    if (u == null) return false
    val cached = cache.get(u, method, groupHeaders(requestProps))
    if (cached == null) return false
    val cachedHeaders =
      try cached.getHeaders()
      catch { case _: IOException => return false }
    if (cachedHeaders == null) return false
    val status = cachedHeaders.get(null)
    if (status == null || status.isEmpty()) return false
    val cachedBody =
      try cached.getBody()
      catch { case _: IOException => return false }
    val it = cachedHeaders.entrySet().iterator()
    while (it.hasNext()) {
      val e = it.next()
      if (e.getKey() != null && e.getValue() != null) {
        val vs = e.getValue().iterator()
        while (vs.hasNext()) headers.add(Array(e.getKey(), vs.next()))
      }
    }
    statusLine = status.get(status.size() - 1)
    parseStatus()
    body = cachedBody
    if (body == null) body = new ByteArrayInputStream(new Array[Byte](0))
    responseRead = true
    true
  }

  /** `scheme://host:port/` for a URL with no URI form, so that proxy selection
   *  still sees the host.
   */
  @throws[IOException]
  private def selectorURI(): URI = {
    val authority =
      if (url.getPort() == -1) url.getHost()
      else url.getHost() + ":" + url.getPort()
    try new URI(url.getProtocol() + "://" + authority + "/")
    catch {
      case _: URISyntaxException =>
        throw new IOException("Cannot select a proxy for " + url)
    }
  }

  @throws[IOException]
  private def openSocket(): Unit = {
    if (host.isEmpty) throw new UnknownHostException("no host in " + url)
    val selector = ProxySelector.getDefault()
    val target = if (uri != null) uri else selectorURI()
    val candidates =
      if (requestedProxy != null) Collections.singletonList(requestedProxy)
      else if (selector == null) DefaultProxySelector.Direct
      else {
        val l = selector.select(target)
        if (l == null || l.isEmpty()) DefaultProxySelector.Direct else l
      }
    var last: IOException = null
    val it = candidates.iterator()
    while (socket == null && it.hasNext()) {
      val p = it.next()
      try connectVia(p)
      catch {
        case e: IOException =>
          last = e
          if (requestedProxy == null && selector != null &&
              p.`type`() != Proxy.Type.DIRECT)
            selector.connectFailed(target, p.address(), e)
      }
    }
    if (socket == null) throw last
  }

  @throws[IOException]
  private def connectVia(p: Proxy): Unit = {
    val address = p.`type`() match {
      case Proxy.Type.DIRECT => new InetSocketAddress(host, port)
      case Proxy.Type.HTTP   =>
        p.address() match {
          case a: InetSocketAddress if a.isUnresolved =>
            new InetSocketAddress(a.getHostString, a.getPort)
          case a: InetSocketAddress => a
          case _ => throw new IOException("Invalid proxy address: " + p)
        }
      case _ => throw new IOException("SOCKS proxies are not supported")
    }
    if (address.isUnresolved)
      throw new UnknownHostException(address.getHostString)
    val s = new Socket()
    try {
      s.connect(address, getConnectTimeout())
      s.setSoTimeout(getReadTimeout())
    } catch {
      case e: IOException =>
        s.close()
        throw e
    }
    socket = s
    proxy = p
    socketIn = new SocketInput(new BufferedInputStream(s.getInputStream))
    socketOut = new BufferedOutputStream(s.getOutputStream)
  }

  private def closeSocket(): Unit = {
    val s = socket
    socket = null
    if (s != null) {
      socketIn.close()
      try s.close()
      catch { case _: IOException => () }
    }
  }

  override def disconnect(): Unit = {
    val hadSocket = socket != null
    closeSocket()
    body match {
      case c: CachingInputStream        => c.abort()
      case b if b != null && !hadSocket =>
        try b.close()
        catch { case _: IOException => () }
      case _ => ()
    }
  }

  override def usingProxy(): Boolean = {
    val p = if (requestedProxy != null) requestedProxy else proxy
    p.`type`() == Proxy.Type.HTTP
  }

  private def fixedLength: Long =
    if (fixedContentLengthLong >= 0) fixedContentLengthLong
    else fixedContentLength

  private def streaming: Boolean = chunkLength > 0 || fixedLength >= 0

  @throws[IOException]
  override def getOutputStream(): OutputStream = {
    if (!doOutput)
      throw new ProtocolException(
        "cannot write to a URLConnection if doOutput=false - call setDoOutput(true)"
      )
    if (method == "TRACE")
      throw new ProtocolException("HTTP method TRACE doesn't support output")
    if (responseRead)
      throw new ProtocolException("Cannot write output after reading input.")
    if (method == "GET") method = "POST"
    if (streaming) {
      if (streamingBody == null) {
        connect()
        sendHead(hasBody = true, if (chunkLength > 0) -1L else fixedLength)
        streamingBody =
          if (chunkLength > 0) new ChunkedOutputStream(socketOut, chunkLength)
          else new FixedLengthOutputStream(socketOut, fixedLength)
      }
      streamingBody
    } else {
      connect()
      if (requestBody == null) requestBody = new ByteArrayOutputStream()
      requestBody
    }
  }

  private def checkDoInput(): Unit =
    if (!doInput)
      throw new ProtocolException(
        "Cannot read from URLConnection if doInput=false (call setDoInput(true))"
      )

  @throws[IOException]
  override def getInputStream(): InputStream = {
    checkDoInput()
    readResponse()
    if (retryFailure != null) throw retryFailure
    if (responseCode >= 400) {
      if (responseCode == 404 || responseCode == 410)
        throw new FileNotFoundException(url.toString)
      throw new IOException(
        "Server returned HTTP response code: " + responseCode + " for URL: " +
          url
      )
    }
    body
  }

  override def getErrorStream(): InputStream =
    if (responseRead && failure == null && responseCode >= 400 && !bodyEmpty)
      body
    else null

  @throws[IOException]
  override def getResponseCode(): Int = {
    checkDoInput()
    readResponse()
    responseCode
  }

  private def readQuietly(): Unit =
    try readResponse()
    catch { case _: IOException => () }

  override def getHeaderField(name: String): String = {
    readQuietly()
    if (statusLine == null) null
    else if (name == null) statusLine
    else {
      val i = lastIndexOf(headers, name)
      if (i < 0) null else headers.get(i)(1)
    }
  }

  override def getHeaderFieldKey(n: Int): String = {
    readQuietly()
    if (statusLine == null || n <= 0 || n > headers.size()) null
    else headers.get(n - 1)(0)
  }

  override def getHeaderField(n: Int): String = {
    readQuietly()
    if (statusLine == null || n < 0 || n > headers.size()) null
    else if (n == 0) statusLine
    else headers.get(n - 1)(1)
  }

  override def getHeaderFields(): Map[String, List[String]] = {
    readQuietly()
    if (statusLine == null) Collections.emptyMap()
    else responseHeaderFields()
  }

  private def responseHeaderFields(): Map[String, List[String]] = {
    if (headerFieldsFor ne headers) {
      headerFields = groupHeaders(withStatusLine())
      headerFieldsFor = headers
    }
    headerFields
  }

  private def withStatusLine(): ArrayList[Array[String]] = {
    val all = new ArrayList[Array[String]](headers.size() + 1)
    all.add(Array(null, statusLine))
    all.addAll(headers)
    all
  }

  /** Sends the request and reads the response, following redirects and
   *  authentication.
   */
  @throws[IOException]
  private def readResponse(): Unit = {
    if (failure != null) throw failure
    if (responseRead) return
    try {
      connect()
      if (!responseRead) {
        var done = false
        while (!done) {
          if (!headSent)
            sendHead(
              requestBody != null,
              if (requestBody != null) requestBody.size().toLong else -1L
            )
          if (requestBody != null) requestBody.writeTo(socketOut)
          if (streamingBody != null) streamingBody.close()
          socketOut.flush()
          responseRead = true
          receive()
          storeCookies()
          done = !(followRedirect() || retryWithAuthentication())
        }
        requestBody = null
        offerToCache()
      }
    } catch {
      case e: IOException =>
        failure = e
        closeSocket()
        throw e
    }
  }

  /** Reconnects for another request on the same connection object. */
  @throws[IOException]
  private def restart(): Unit = {
    closeSocket()
    headSent = false
    statusLine = null
    headers = new ArrayList[Array[String]]()
    body = null
    openSocket()
  }

  @throws[IOException]
  private def followRedirect(): Boolean = {
    if (!instanceFollowRedirects || !(responseCode >= 300 &&
          responseCode <= 303 || responseCode == 307)) return false
    val location = getHeaderField("Location")
    if (location == null) return false
    val target =
      try new URL(url, location)
      catch { case _: MalformedURLException => return false }
    if (target.getProtocol() != url.getProtocol()) return false
    if (streamingBody != null) {
      retryFailure = new HttpRetryException(
        "cannot retry due to redirection, in streaming mode",
        responseCode,
        location
      )
      return false
    }
    redirects += 1
    if (redirects >= MaxRedirects)
      throw new ProtocolException(
        "Server redirected too many times (" + MaxRedirects + ")"
      )
    checkURL(target)
    if (responseCode != 307 && method == "POST") {
      method = "GET"
      requestBody = null
    }
    if (!target.getHost().equalsIgnoreCase(url.getHost()) ||
        target.getPort() != url.getPort()) {
      authorization = null
      triedAuth = false
      removeRequestProps(CrossHostHeaders)
    }
    url = target
    val previousProxy = proxy
    restart()
    if (proxy != previousProxy) removeRequestProps(ProxyHeaders)
    true
  }

  private def removeRequestProps(names: Array[String]): Unit = {
    var i = requestProps.size() - 1
    while (i >= 0) {
      val k = requestProps.get(i)(0)
      var j = 0
      while (j < names.length && !names(j).equalsIgnoreCase(k)) j += 1
      if (j < names.length) requestProps.remove(i)
      i -= 1
    }
  }

  @throws[IOException]
  private def retryWithAuthentication(): Boolean = {
    val forProxy = responseCode == 407 && usingProxy()
    if (!(responseCode == 401 && !triedAuth || forProxy && triedProxyAuthFor != proxy))
      return false
    val challenge =
      basicChallenge(if (forProxy) "Proxy-Authenticate" else "WWW-Authenticate")
    if (challenge == null) return false
    if (streamingBody != null) {
      retryFailure = new HttpRetryException(
        "cannot retry due to " + (if (forProxy) "proxy" else "server") +
          " authentication, in streaming mode",
        responseCode
      )
      return false
    }
    if (forProxy) triedProxyAuthFor = proxy else triedAuth = true
    val auth =
      if (authenticator != null) authenticator else Authenticator.getDefault()
    if (auth == null) return false
    val (authHost, authPort, site) =
      if (forProxy) {
        val a = proxy.address().asInstanceOf[InetSocketAddress]
        (a.getHostString, a.getPort, a.getAddress)
      } else {
        val s =
          try InetAddress.getByName(host)
          catch { case _: IOException => null }
        (url.getHost(), port, s)
      }
    val pa = Authenticator.requestPasswordAuthentication(
      auth,
      authHost,
      site,
      authPort,
      url.getProtocol(),
      realm(challenge),
      "basic",
      url,
      if (forProxy) Authenticator.RequestorType.PROXY
      else Authenticator.RequestorType.SERVER
    )
    if (pa == null) return false
    val credentials = pa.getUserName() + ":" + new String(pa.getPassword())
    val value = "Basic " + Base64
      .getEncoder()
      .encodeToString(credentials.getBytes(StandardCharsets.ISO_8859_1))
    if (forProxy) {
      proxyAuthorization = value
      proxyAuthorizationFor = proxy
    } else authorization = value
    restart()
    true
  }

  /** The first Basic challenge in the `name` headers, or `null`. */
  private def basicChallenge(name: String): String = {
    var i = 0
    while (i < headers.size()) {
      val kv = headers.get(i)
      val v = kv(1)
      if (kv(0).equalsIgnoreCase(name) && (v.equalsIgnoreCase("Basic") ||
          v.regionMatches(true, 0, "Basic ", 0, 6)))
        return v
      i += 1
    }
    null
  }

  @throws[IOException]
  private def sendHead(hasBody: Boolean, contentLength: Long): Unit = {
    val viaProxy = usingProxy()
    val target =
      if (viaProxy) {
        val ref = url.getRef()
        val s = url.toExternalForm()
        if (ref == null) s else s.substring(0, s.length - ref.length - 1)
      } else {
        val f = url.getFile()
        if (f == null || f.isEmpty) "/"
        else if (f.charAt(0) != '/') "/" + f
        else f
      }
    val sent = new ArrayList[Array[String]]()
    def add(k: String, v: String): Unit = sent.add(Array(k, v))
    def has(k: String): Boolean = lastIndexOf(sent, k) >= 0
    if (!useCaches) {
      add("Cache-Control", "no-cache")
      add("Pragma", "no-cache")
    }
    val props = requestProps.iterator()
    while (props.hasNext()) {
      val kv = props.next()
      if (viaProxy || !"Proxy-Authorization".equalsIgnoreCase(kv(0)))
        sent.add(kv)
    }
    if (!has("User-Agent")) add("User-Agent", userAgent)
    add(
      "Host",
      if (url.getPort() == -1 || url.getPort() == url.getDefaultPort())
        url.getHost()
      else url.getHost() + ":" + url.getPort()
    )
    if (!has("Accept")) add("Accept", "*/*")
    if (!has("Connection") && !has("Proxy-Connection"))
      add(if (viaProxy) "Proxy-Connection" else "Connection", "keep-alive")
    if (ifModifiedSince > 0 && !has("If-Modified-Since"))
      add("If-Modified-Since", HttpDates.format(ifModifiedSince))
    if (authorization != null && !has("Authorization"))
      add("Authorization", authorization)
    if (proxyAuthorization != null && viaProxy &&
        proxy == proxyAuthorizationFor)
      add("Proxy-Authorization", proxyAuthorization)
    addCookies(sent)
    if (hasBody) {
      if (method == "POST" && !has("Content-Type"))
        add("Content-type", "application/x-www-form-urlencoded")
      if (chunkLength > 0) add("Transfer-Encoding", "chunked")
      else if (contentLength >= 0)
        add("Content-Length", java.lang.Long.toString(contentLength))
    }
    val sb = new java.lang.StringBuilder()
    sb.append(method).append(' ').append(target).append(" HTTP/1.1\r\n")
    val it = sent.iterator()
    while (it.hasNext()) {
      val kv = it.next()
      sb.append(kv(0)).append(": ")
      if (kv(1) != null) sb.append(kv(1))
      sb.append("\r\n")
    }
    sb.append("\r\n")
    socketOut.write(sb.toString.getBytes(StandardCharsets.ISO_8859_1))
    headSent = true
  }

  /** Adds the `Cookie` and `Cookie2` values of the cookie handler, dropping any
   *  that would break the request.
   */
  @throws[IOException]
  private def addCookies(sent: ArrayList[Array[String]]): Unit = {
    val handler = CookieHandler.getDefault()
    val u = uri
    if (handler == null || u == null) return
    val cookies = handler.get(u, groupHeaders(sent))
    if (cookies == null) return
    val it = cookies.entrySet().iterator()
    while (it.hasNext()) {
      val e = it.next()
      val k = e.getKey()
      if (("Cookie".equalsIgnoreCase(k) || "Cookie2".equalsIgnoreCase(k)) &&
          e.getValue() != null) {
        val sb = new java.lang.StringBuilder()
        val vs = e.getValue().iterator()
        while (vs.hasNext()) {
          val v = vs.next()
          if (v != null && isValidValue(v) && v.indexOf('\n') < 0) {
            if (sb.length() > 0) sb.append("; ")
            sb.append(v)
          }
        }
        if (sb.length() > 0) sent.add(Array(k, sb.toString))
      }
    }
  }

  /** Reads the status line and the headers, and sets up the body stream. */
  @throws[IOException]
  private def receive(): Unit = {
    var interim = true
    while (interim) {
      statusLine = HttpStreams.readLine(socketIn)
      if (statusLine == null)
        throw new IOException("Unexpected end of file from server")
      headers = new ArrayList[Array[String]]()
      var line = HttpStreams.readLine(socketIn)
      while (line != null && !line.isEmpty) {
        if (headers.size() >= MaxHeaders)
          throw new IOException("Too many HTTP headers")
        val colon = line.indexOf(':')
        if (colon > 0)
          headers.add(
            Array(
              line.substring(0, colon).trim(),
              line.substring(colon + 1).trim()
            )
          )
        line = HttpStreams.readLine(socketIn)
      }
      parseStatus()
      interim = responseCode >= 100 && responseCode < 200 && responseCode != 101
    }
    val onEnd = () => closeSocket()
    val te = getHeaderField("Transfer-Encoding")
    val chunked = te != null && te.trim().equalsIgnoreCase("chunked")
    val length =
      if (te != null) -1L else getHeaderFieldLong("Content-Length", -1L)
    bodyEmpty = method == "HEAD" || responseCode == 204 ||
      responseCode == 304 || responseCode >= 100 && responseCode < 200 ||
      !chunked && length == 0
    body =
      if (bodyEmpty) {
        closeSocket()
        new ByteArrayInputStream(new Array[Byte](0))
      } else if (chunked) new ChunkedInputStream(socketIn, onEnd)
      else if (length > 0) new FixedLengthInputStream(socketIn, length, onEnd)
      else new UntilCloseInputStream(socketIn, onEnd)
  }

  private def parseStatus(): Unit = {
    responseCode = HttpStatusLine.code(statusLine)
    responseMessage = HttpStatusLine.message(statusLine)
  }

  @throws[IOException]
  private def storeCookies(): Unit = {
    val handler = CookieHandler.getDefault()
    val u = uri
    if (handler != null && u != null)
      handler.put(u, responseHeaderFields())
  }

  @throws[IOException]
  private def offerToCache(): Unit = {
    val cache = ResponseCache.getDefault()
    val u = uri
    if (cache == null || !useCaches || method != "GET" || u == null) return
    val request = cache.put(u, this)
    if (request == null) return
    val out = request.getBody()
    if (out == null) request.abort()
    else body = new CachingInputStream(body, request, out)
  }
}

object HttpURLConnectionImpl {

  private final val MaxRedirects = 20
  private final val MaxHeaders = 1000

  private val ProxyHeaders = Array("Proxy-Authorization")
  private val CrossHostHeaders =
    Array("Authorization", "Proxy-Authorization", "Cookie", "Cookie2")

  private val restricted = Array(
    "Access-Control-Request-Headers",
    "Access-Control-Request-Method",
    "Content-Length",
    "Content-Transfer-Encoding",
    "Host",
    "Keep-Alive",
    "Origin",
    "Trailer",
    "Transfer-Encoding",
    "Upgrade",
    "Via"
  )

  private def isRestricted(key: String): Boolean = {
    var i = 0
    while (i < restricted.length && !restricted(i).equalsIgnoreCase(key))
      i += 1
    i < restricted.length
  }

  /** `Java/<java.version>`, prefixed by the `http.agent` property. */
  private lazy val userAgent: String = {
    val version = "Java/" + System.getProperty("java.version")
    val agent = System.getProperty("http.agent")
    if (agent == null) version else agent + " " + version
  }

  /** Rejects names with `:` or line breaks, and values with line breaks other
   *  than CRLF or LF followed by a space or tab (folding).
   */
  private def checkHeader(key: String, value: String): Unit = {
    if (key != null && (key.indexOf(':') >= 0 || key.indexOf('\r') >= 0 ||
        key.indexOf('\n') >= 0))
      throw new IllegalArgumentException(
        "Illegal character(s) in message header field: " + key
      )
    if (value != null && !isValidValue(value))
      throw new IllegalArgumentException(
        "Illegal character(s) in message header value: " + value
      )
  }

  private def isValidValue(value: String): Boolean = {
    val n = value.length
    var i = 0
    while (i < n) {
      value.charAt(i) match {
        case '\r' =>
          if (i + 1 >= n || value.charAt(i + 1) != '\n') return false
        case '\n' =>
          if (i + 1 >= n) return false
          val next = value.charAt(i + 1)
          if (next != ' ' && next != '\t') return false
        case _ => ()
      }
      i += 1
    }
    true
  }

  /** Rejects URLs that would break the request line or the `Host` header. */
  @throws[MalformedURLException]
  private def checkURL(u: URL): Unit = {
    val s = u.toExternalForm()
    var i = 0
    while (i < s.length) {
      val c = s.charAt(i)
      if (c == '\r' || c == '\n' || c == '\u0000')
        throw new MalformedURLException("Illegal character in URL")
      i += 1
    }
  }

  private def lastIndexOf(pairs: List[Array[String]], key: String): Int = {
    var i = pairs.size() - 1
    while (i >= 0 && !key.equalsIgnoreCase(pairs.get(i)(0))) i -= 1
    i
  }

  /** The `realm` parameter of a challenge, or `null`. */
  private def realm(challenge: String): String = {
    val i = challenge.toLowerCase().indexOf("realm=")
    if (i < 0) null
    else {
      var v = challenge.substring(i + 6).trim()
      if (v.startsWith("\"")) {
        val end = v.indexOf('"', 1)
        v = if (end > 0) v.substring(1, end) else v.substring(1)
      } else {
        val end = v.indexOf(',')
        if (end >= 0) v = v.substring(0, end).trim()
      }
      v
    }
  }

  /** An unmodifiable map of (key, value) pairs; keys keep their spelling. */
  private def groupHeaders(
      pairs: List[Array[String]]
  ): Map[String, List[String]] = {
    val lists = new LinkedHashMap[String, ArrayList[String]]()
    val it = pairs.iterator()
    while (it.hasNext()) {
      val kv = it.next()
      var values = lists.get(kv(0))
      if (values == null) {
        values = new ArrayList[String]()
        lists.put(kv(0), values)
      }
      values.add(kv(1))
    }
    val out = new LinkedHashMap[String, List[String]]()
    val es = lists.entrySet().iterator()
    while (es.hasNext()) {
      val e = es.next()
      out.put(e.getKey(), Collections.unmodifiableList(e.getValue()))
    }
    Collections.unmodifiableMap(out)
  }
}
