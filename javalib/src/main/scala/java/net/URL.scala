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

// Ported from Apache Harmony, without the permission checks.

package java.net

import java.io.{IOException, InputStream}
import java.util.concurrent.ConcurrentHashMap

import scala.scalanative.javalib.net.www.protocol.BuiltinHandlers

/** A Uniform Resource Locator: a protocol, an authority (user info, host,
 *  port), a file (path and query) and a reference. The [[URLStreamHandler]] of
 *  the protocol parses it, compares it and opens connections to it.
 *
 *  Only `file` URLs can be opened by the built-in handlers; `http`, `https`,
 *  `ftp`, `jar` and `mailto` URLs parse but cannot be opened.
 */
final class URL private () extends Serializable {

  private var protocol: String = null
  private var host: String = null
  private var port: Int = -1
  private var file: String = null
  private var query: String = null
  private var authority: String = null
  private var path: String = null
  private var userInfo: String = null
  private var ref: String = null
  @volatile private var hashCodeValue: Int = -1
  private[net] var handler: URLStreamHandler = null

  @throws[MalformedURLException]
  def this(spec: String) = {
    this()
    init(null, spec, null)
  }

  /** Parses `spec`, relative to `context` when it has no protocol or the same
   *  one.
   */
  @throws[MalformedURLException]
  def this(context: URL, spec: String) = {
    this()
    init(context, spec, null)
  }

  @throws[MalformedURLException]
  def this(context: URL, spec: String, handler: URLStreamHandler) = {
    this()
    init(context, spec, handler)
  }

  @throws[MalformedURLException]
  def this(protocol: String, host: String, file: String) = {
    this()
    init(protocol, host, -1, file, null)
  }

  @throws[MalformedURLException]
  def this(protocol: String, host: String, port: Int, file: String) = {
    this()
    init(protocol, host, port, file, null)
  }

  /** The first `#` in `file` starts the reference. An IPv6 `host` may be given
   *  without brackets.
   */
  @throws[MalformedURLException]
  def this(
      protocol: String,
      host: String,
      port: Int,
      file: String,
      handler: URLStreamHandler
  ) = {
    this()
    init(protocol, host, port, file, handler)
  }

  private def init(context: URL, spec0: String, h: URLStreamHandler): Unit = {
    if (spec0 == null) throw new MalformedURLException("spec is null")
    var spec = spec0.trim()
    if (spec.regionMatches(true, 0, "url:", 0, 4)) spec = spec.substring(4)
    if (h != null) handler = h

    var index = spec.indexOf(':')
    val bracket = spec.indexOf('[')
    if (index >= 0 && (bracket == -1 || index < bracket)) {
      val p = spec.substring(0, index)
      if (URL.isValidScheme(p)) protocol = p.toLowerCase()
      else index = -1
    } else index = -1

    if (protocol != null) {
      if (context != null && protocol == context.getProtocol()) {
        val cPath = context.getPath()
        if (cPath != null && cPath.startsWith("/"))
          set(
            protocol,
            context.getHost(),
            context.getPort(),
            context.getAuthority(),
            context.getUserInfo(),
            cPath,
            context.getQuery(),
            null
          )
        if (handler == null) handler = context.handler
      }
    } else {
      if (context == null)
        throw new MalformedURLException("no protocol: " + spec)
      set(
        context.getProtocol(),
        context.getHost(),
        context.getPort(),
        context.getAuthority(),
        context.getUserInfo(),
        context.getPath(),
        context.getQuery(),
        if (spec.isEmpty) context.getRef() else null
      )
      if (handler == null) handler = context.handler
    }

    if (handler == null) handler = URL.requireHandler(protocol)

    try handler.parseURL(this, spec, index + 1, spec.length)
    catch {
      case e: MalformedURLException => throw e
      case e: Exception             =>
        val msg = if (e.getMessage() != null) e.getMessage() else e.toString
        val m = new MalformedURLException(msg)
        m.initCause(e)
        throw m
    }
    URL.checkPort(port)
  }

  private def init(
      protocol0: String,
      host0: String,
      port0: Int,
      file0: String,
      h: URLStreamHandler
  ): Unit = {
    URL.checkPort(port0)
    if (protocol0 == null) throw new NullPointerException("protocol is null")
    var host = host0
    if (host != null && host.indexOf(':') >= 0 && !host.startsWith("[")) {
      if (!URL.isIPv6Literal(host))
        throw new MalformedURLException("Unrecognized IPv6 address format")
      host = "[" + host + "]"
    }
    if (host != null) URL.checkHost(host, allowAt = false)
    protocol = protocol0.toLowerCase()
    this.host = host
    port = if (host == null) -1 else port0
    val index = file0.indexOf('#')
    if (index >= 0) {
      file = file0.substring(0, index)
      ref = file0.substring(index + 1)
    } else file = file0
    fixURL(fixHost = false)
    handler = if (h != null) h else URL.requireHandler(protocol)
  }

  private def fixURL(fixHost: Boolean): Unit = {
    // From a spec, an empty host has no authority; from the parts, it has an
    // empty one
    if (host != null && (host.nonEmpty || !fixHost)) {
      authority = host
      if (port != -1) authority = authority + ":" + port
    }
    if (fixHost) {
      val at = if (host == null) -1 else host.lastIndexOf('@')
      if (at > -1) {
        userInfo = host.substring(0, at)
        host = host.substring(at + 1)
      } else userInfo = null
    }
    val q = if (file == null) -1 else file.indexOf('?')
    if (q > -1) {
      query = file.substring(q + 1)
      path = file.substring(0, q)
    } else {
      query = null
      path = file
    }
  }

  /** The protocol is set only once. */
  private[net] def set(
      protocol: String,
      host: String,
      port: Int,
      file: String,
      ref: String
  ): Unit = {
    if (this.protocol == null) this.protocol = protocol
    this.host = host
    this.file = file
    this.port = port
    this.ref = ref
    hashCodeValue = -1
    fixURL(fixHost = true)
  }

  private[net] def set(
      protocol: String,
      host: String,
      port: Int,
      authority: String,
      userInfo: String,
      path: String,
      query: String,
      ref: String
  ): Unit = {
    var filePart = path
    if (query != null)
      filePart = if (filePart != null) filePart + "?" + query else "?" + query
    set(protocol, host, port, filePart, ref)
    this.authority = authority
    this.userInfo = userInfo
    this.path = path
    this.query = query
  }

  def getQuery(): String = query

  def getPath(): String = path

  def getUserInfo(): String = userInfo

  def getAuthority(): String = authority

  def getPort(): Int = port

  def getDefaultPort(): Int = handler.getDefaultPort()

  def getProtocol(): String = protocol

  def getHost(): String = host

  def getFile(): String = file

  def getRef(): String = ref

  override def equals(obj: Any): Boolean = obj match {
    case u: URL => (u eq this) || handler.equals(this, u)
    case _      => false
  }

  override def hashCode(): Int = synchronized {
    if (hashCodeValue == -1) hashCodeValue = handler.hashCode(this)
    hashCodeValue
  }

  def sameFile(other: URL): Boolean = handler.sameFile(this, other)

  override def toString(): String = toExternalForm()

  def toExternalForm(): String = handler.toExternalForm(this)

  @throws[URISyntaxException]
  def toURI(): URI = new URI(toExternalForm())

  @throws[IOException]
  def openConnection(): URLConnection = handler.openConnection(this)

  @throws[IOException]
  def openConnection(proxy: Proxy): URLConnection = {
    if (proxy == null)
      throw new IllegalArgumentException("proxy can not be null")
    handler.openConnection(this, proxy)
  }

  @throws[IOException]
  final def openStream(): InputStream = openConnection().getInputStream()

  @throws[IOException]
  final def getContent(): AnyRef = openConnection().getContent()

  @throws[IOException]
  final def getContent(classes: Array[Class[_]]): AnyRef =
    openConnection().getContent(classes)
}

object URL {

  private val handlers = new ConcurrentHashMap[String, URLStreamHandler]()

  @volatile private var factory: URLStreamHandlerFactory = null

  /** @throws Error
   *    if a factory is already set
   */
  def setURLStreamHandlerFactory(fac: URLStreamHandlerFactory): Unit =
    synchronized {
      if (factory != null) throw new Error("factory already defined")
      handlers.clear()
      factory = fac
    }

  /** The handler of a lower-case `protocol`: a cached one, the factory's or a
   *  built-in one. `java.protocol.handler.pkgs` is not supported: it would need
   *  reflective instantiation.
   */
  private[net] def handlerFor(protocol: String): URLStreamHandler = {
    var h = handlers.get(protocol)
    if (h != null) return h
    val fac = factory
    if (fac != null) h = fac.createURLStreamHandler(protocol)
    if (h == null) h = BuiltinHandlers.forProtocol(protocol)
    if (h != null) {
      val previous = handlers.putIfAbsent(protocol, h)
      if (previous != null) h = previous
    }
    h
  }

  private def requireHandler(protocol: String): URLStreamHandler = {
    val h = handlerFor(protocol)
    if (h == null)
      throw new MalformedURLException("unknown protocol: " + protocol)
    h
  }

  private def checkPort(port: Int): Unit =
    if (port < -1)
      throw new MalformedURLException("Invalid port number :" + port)

  /** RFC 2396: `alpha *( alpha | digit | "+" | "-" | "." )`. */
  private def isValidScheme(s: String): Boolean = {
    if (s.isEmpty) return false
    var i = 0
    while (i < s.length) {
      val c = s.charAt(i)
      val ok = (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') ||
        (i > 0 && ((c >= '0' && c <= '9') || c == '+' || c == '-' || c == '.'))
      if (!ok) return false
      i += 1
    }
    true
  }

  /** A rough IPv6 literal check (without brackets): hex digits, colons and
   *  dots, at least two colons, optionally followed by a `%` zone.
   */
  private[net] def isIPv6Literal(s: String): Boolean = {
    val pct = s.indexOf('%')
    val addr = if (pct >= 0) s.substring(0, pct) else s
    var colons = 0
    var i = 0
    while (i < addr.length) {
      val c = addr.charAt(i)
      if (c == ':') colons += 1
      else if (!(Character.digit(c, 16) >= 0 || c == '.')) return false
      i += 1
    }
    colons >= 2
  }

  /** Rejects hosts with whitespace, control characters, `/`, `?`, `#` or (when
   *  not allowed) `@`.
   */
  private[net] def checkHost(host: String, allowAt: Boolean): Unit = {
    var i = 0
    while (i < host.length) {
      val c = host.charAt(i)
      if (c <= ' ' || c == 0x7f || c == '/' || c == '?' || c == '#' ||
          (!allowAt && c == '@'))
        throw new MalformedURLException(
          "Illegal character found in host: '" + c + "'"
        )
      i += 1
    }
  }
}
