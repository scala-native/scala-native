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

// Ported from Apache Harmony (java.net.ProxySelectorImpl).

package scala.scalanative.javalib.net

import java.io.IOException
import java.net.{InetSocketAddress, Proxy, ProxySelector, SocketAddress, URI}
import java.util.{Collections, List}

/** The initial `ProxySelector.getDefault()`, reading the proxy system
 *  properties on every call:
 *
 *    - `http`, `https`, `ftp`: `<scheme>.proxyHost`/`<scheme>.proxyPort`
 *      (default port 80, 443 and 80), then the legacy `proxyHost`/`proxyPort`,
 *      then `socksProxyHost`. `http.nonProxyHosts` (also for `https`) and
 *      `ftp.nonProxyHosts` list the hosts reached directly.
 *    - `socket`: `socksProxyHost`/`socksProxyPort` (default 1080);
 *      `socksNonProxyHosts`.
 *
 *  A non-proxy list is `|`-separated host patterns, matched ignoring case; a
 *  pattern may start or end with `*`. Loopback hosts are reached directly
 *  unless the list is set to the empty string.
 */
final class DefaultProxySelector extends ProxySelector {
  import DefaultProxySelector._

  /** The host, also for registry authorities such as `build_host:80`. */
  private def hostOf(uri: URI): String = {
    val h = uri.getHost()
    val auth = uri.getAuthority()
    if (h != null || auth == null) h
    else {
      val start = auth.lastIndexOf('@') + 1
      val colon = auth.lastIndexOf(':')
      val end = if (colon > start && !auth.endsWith("]")) colon else auth.length
      if (start < end) auth.substring(start, end) else null
    }
  }

  override def select(uri: URI): List[Proxy] = {
    if (uri == null) throw new IllegalArgumentException("URI can't be null.")
    val scheme = uri.getScheme()
    val host = hostOf(uri)
    if (scheme == null || host == null)
      throw new IllegalArgumentException(
        "protocol = " + scheme + " host = " + host
      )
    val proxy = scheme.toLowerCase() match {
      case s @ ("http" | "https" | "ftp") =>
        choose(
          host,
          if (s == "ftp") "ftp.nonProxyHosts" else "http.nonProxyHosts",
          s + ".proxyHost",
          s + ".proxyPort",
          if (s == "https") 443 else 80
        )
      case "socket" =>
        if (bypasses(host, "socksNonProxyHosts")) Proxy.NO_PROXY else socks()
      case _ =>
        throw new IllegalArgumentException(
          "protocol = " + scheme + " host = " + host
        )
    }
    if (proxy eq Proxy.NO_PROXY) Direct else Collections.singletonList(proxy)
  }

  override def connectFailed(
      uri: URI,
      sa: SocketAddress,
      ioe: IOException
  ): Unit =
    if (uri == null || sa == null || ioe == null)
      throw new IllegalArgumentException("Arguments can't be null.")

  private def choose(
      host: String,
      nonProxyProperty: String,
      hostProperty: String,
      portProperty: String,
      defaultPort: Int
  ): Proxy =
    if (bypasses(host, nonProxyProperty)) Proxy.NO_PROXY
    else {
      val proxyHost = property(hostProperty)
      if (proxyHost != null)
        httpProxy(
          proxyHost,
          portOr(property(portProperty), property("proxyPort")),
          defaultPort
        )
      else {
        val legacyHost = property("proxyHost")
        if (legacyHost != null)
          httpProxy(
            legacyHost,
            portOr(property("proxyPort"), property(portProperty)),
            defaultPort
          )
        else socks()
      }
    }

  private def socks(): Proxy = {
    val host = property("socksProxyHost")
    if (host == null) Proxy.NO_PROXY
    else
      new Proxy(
        Proxy.Type.SOCKS,
        InetSocketAddress.createUnresolved(
          host,
          port(property("socksProxyPort"), 1080)
        )
      )
  }

  private def httpProxy(host: String, portValue: String, defaultPort: Int) =
    new Proxy(
      Proxy.Type.HTTP,
      InetSocketAddress.createUnresolved(host, port(portValue, defaultPort))
    )
}

object DefaultProxySelector {

  val Direct: List[Proxy] =
    Collections.singletonList(Proxy.NO_PROXY)

  private val loopback = Array("localhost", "127.*", "::1", "0.0.0.0", "::0")

  /** `null` when unset or empty. */
  private def property(name: String): String = {
    val v = System.getProperty(name)
    if (v == null || v.isEmpty) null else v
  }

  private def portOr(first: String, second: String): String =
    if (first != null) first else second

  /** `default` when missing or not a number. */
  private def port(value: String, default: Int): Int =
    if (value == null) default
    else
      try Integer.parseInt(value)
      catch { case _: NumberFormatException => default }

  private def stripBrackets(s: String): String =
    if (s.length >= 2 && s.charAt(0) == '[' && s.charAt(s.length - 1) == ']')
      s.substring(1, s.length - 1)
    else s

  /** Whether `host` is reached directly by the non-proxy list in `property`. */
  private def bypasses(host: String, property: String): Boolean = {
    val list = System.getProperty(property)
    if (list != null && list.isEmpty) return false
    val h = stripBrackets(host).toLowerCase()
    var i = 0
    while (i < loopback.length) {
      if (matches(h, loopback(i))) return true
      i += 1
    }
    if (list == null) return false
    val patterns = list.split("\\|")
    i = 0
    while (i < patterns.length) {
      val p = patterns(i)
      if (!p.isEmpty && matches(h, stripBrackets(p).toLowerCase())) return true
      i += 1
    }
    false
  }

  /** A lower-case host against a lower-case pattern with an optional leading
   *  and/or trailing `*`.
   */
  private def matches(host: String, pattern: String): Boolean = {
    if (pattern == "*") return true
    val lead = pattern.startsWith("*")
    val trail = pattern.length > 1 && pattern.endsWith("*")
    val core = pattern.substring(
      if (lead) 1 else 0,
      pattern.length - (if (trail) 1 else 0)
    )
    if (lead && trail) host.contains(core)
    else if (lead) host.endsWith(core)
    else if (trail) host.startsWith(core)
    else host == core
  }
}
