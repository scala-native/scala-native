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

package java.net

import java.io.IOException

/** The protocol-specific part of [[URL]]: parsing, comparison, formatting and
 *  opening connections.
 */
abstract class URLStreamHandler {

  @throws[IOException]
  protected[net] def openConnection(u: URL): URLConnection

  @throws[IOException]
  protected[net] def openConnection(u: URL, p: Proxy): URLConnection =
    throw new UnsupportedOperationException("Method not implemented.")

  /** Parses `spec(start until limit)` into `u`, starting from the fields `u`
   *  already has (those of the context URL): an authority after `//`, then the
   *  file (relative files are resolved against the context's directory and
   *  `.`/`..` segments removed), `?query` and `#ref`.
   */
  protected[net] def parseURL(
      u: URL,
      spec: String,
      start: Int,
      limit: Int
  ): Unit = {
    if (limit < start || limit < 0) {
      if ((limit <= Integer.MIN_VALUE + 1 && (start >= spec.length || start < 0)) ||
          (spec.startsWith("//", start) && spec.indexOf('/', start + 2) == -1))
        throw new StringIndexOutOfBoundsException(limit)
      if (this ne u.handler)
        throw new SecurityException(
          "handler for url different from this handler"
        )
      return
    }
    val s = spec.substring(start, limit)
    val end = limit - start
    var fileIdx = 0

    // Start from the context's fields
    var host = u.getHost()
    var port = u.getPort()
    var ref = u.getRef()
    var file = u.getPath()
    var query = u.getQuery()
    var authority = u.getAuthority()
    var userInfo = u.getUserInfo()

    val refIdx = s.indexOf('#', 0)
    if (s.startsWith("//") && !s.startsWith("////")) {
      var hostIdx = 2
      port = -1
      fileIdx = s.indexOf('/', hostIdx)
      val questionMark = s.indexOf('?', hostIdx)
      if (questionMark != -1 && (fileIdx == -1 || fileIdx > questionMark))
        fileIdx = questionMark
      if (fileIdx == -1) {
        fileIdx = end
        file = ""
      }
      var hostEnd = fileIdx
      if (refIdx != -1 && refIdx < fileIdx) hostEnd = refIdx
      val userIdx = s.lastIndexOf('@', hostEnd)
      authority = s.substring(hostIdx, hostEnd)
      userInfo = null
      if (userIdx > -1) {
        userInfo = s.substring(hostIdx, userIdx)
        hostIdx = userIdx + 1
      }
      var portIdx = s.indexOf(':', if (userIdx == -1) hostIdx else userIdx)
      val endOfIPv6 = s.indexOf(']')
      if (endOfIPv6 != -1)
        portIdx =
          if (s.length > endOfIPv6 + 1 && s.charAt(endOfIPv6 + 1) == ':')
            endOfIPv6 + 1
          else -1
      if (portIdx == -1 || portIdx > hostEnd)
        host = s.substring(hostIdx, hostEnd)
      else {
        host = s.substring(hostIdx, portIdx)
        port = URLStreamHandler.parsePort(s.substring(portIdx + 1, hostEnd))
      }
      URL.checkHost(host, allowAt = true)
    }

    if (refIdx > -1) ref = s.substring(refIdx + 1, end)
    var fileEnd = if (refIdx == -1) end else refIdx

    val queryIdx = {
      val q = s.indexOf('?', fileIdx max 0)
      if (q >= 0 && q < fileEnd) q else -1
    }
    var canonicalize = false
    if (queryIdx > -1) {
      query = s.substring(queryIdx + 1, fileEnd)
      if (queryIdx == 0 && file != null) {
        if (file.isEmpty) file = "/"
        else if (file.startsWith("/")) canonicalize = true
        file = file.substring(0, file.lastIndexOf('/') + 1)
      }
      fileEnd = queryIdx
    } else if (refIdx != 0 && end > 0) {
      // Keep the context's query only when just the reference changes or the
      // spec is empty
      query = null
    }

    if (fileIdx > -1) {
      if (fileIdx < end && s.charAt(fileIdx) == '/')
        file = s.substring(fileIdx, fileEnd)
      else if (fileEnd > fileIdx) {
        if (file == null) file = ""
        else if (file.isEmpty) file = "/"
        else if (file.startsWith("/")) canonicalize = true
        val last = file.lastIndexOf('/') + 1
        file =
          if (last == 0) s.substring(fileIdx, fileEnd)
          else file.substring(0, last) + s.substring(fileIdx, fileEnd)
      }
    }
    if (file == null) file = ""
    if (host == null) host = ""
    if (canonicalize) file = URLStreamHandler.canonicalizePath(file)
    setURL(
      u,
      u.getProtocol(),
      host,
      port,
      authority,
      userInfo,
      file,
      query,
      ref
    )
  }

  @deprecated(
    "Use setURL(URL, String, String, int, String, String, String, String)",
    "1.2"
  )
  protected def setURL(
      u: URL,
      protocol: String,
      host: String,
      port: Int,
      file: String,
      ref: String
  ): Unit = {
    if (this ne u.handler)
      throw new SecurityException("handler for url different from this handler")
    u.set(protocol, host, port, file, ref)
  }

  protected def setURL(
      u: URL,
      protocol: String,
      host: String,
      port: Int,
      authority: String,
      userInfo: String,
      path: String,
      query: String,
      ref: String
  ): Unit = {
    if (this ne u.handler)
      throw new SecurityException("handler for url different from this handler")
    u.set(protocol, host, port, authority, userInfo, path, query, ref)
  }

  protected[net] def toExternalForm(u: URL): String = {
    val sb = new java.lang.StringBuilder()
    sb.append(u.getProtocol()).append(':')
    val authority = u.getAuthority()
    if (authority != null && authority.nonEmpty)
      sb.append("//").append(authority)
    val file = u.getFile()
    if (file != null) sb.append(file)
    val ref = u.getRef()
    if (ref != null) sb.append('#').append(ref)
    sb.toString
  }

  protected[net] def equals(u1: URL, u2: URL): Boolean =
    sameFile(u1, u2) && java.util.Objects.equals(u1.getRef(), u2.getRef())

  protected[net] def getDefaultPort(): Int = -1

  /** The address of the host of `u`, or `null` if it has none or does not
   *  resolve.
   */
  protected def getHostAddress(u: URL): InetAddress = {
    val host = u.getHost()
    if (host == null || host.isEmpty) null
    else
      try InetAddress.getByName(host)
      catch {
        case _: UnknownHostException | _: SecurityException |
            _: IllegalArgumentException =>
          null
      }
  }

  /** Sum of the hash codes of the protocol, host address (or lower-case host
   *  name), file, port (or default port) and reference.
   */
  protected[net] def hashCode(u: URL): Int = {
    var h = 0
    val protocol = u.getProtocol()
    if (protocol != null) h += protocol.hashCode()
    val addr = getHostAddress(u)
    if (addr != null) h += URLStreamHandler.addressHash(addr)
    else {
      val host = u.getHost()
      if (host != null) h += host.toLowerCase().hashCode()
    }
    val file = u.getFile()
    if (file != null) h += file.hashCode()
    h += (if (u.getPort() == -1) getDefaultPort() else u.getPort())
    val ref = u.getRef()
    if (ref != null) h += ref.hashCode()
    h
  }

  /** Equal addresses when both resolve, otherwise equal names ignoring case (an
   *  empty `file:` host is `localhost`).
   */
  protected def hostsEqual(u1: URL, u2: URL): Boolean = {
    val a1 = getHostAddress(u1)
    val a2 = getHostAddress(u2)
    // InetAddress.equals also compares host names
    if (a1 != null && a2 != null)
      java.util.Arrays.equals(a1.getAddress(), a2.getAddress())
    else {
      val h1 = URLStreamHandler.hostOf(u1)
      val h2 = URLStreamHandler.hostOf(u2)
      if (h1 == null && h2 == null) true
      else h1 != null && h1.equalsIgnoreCase(h2)
    }
  }

  protected[net] def sameFile(u1: URL, u2: URL): Boolean =
    u1.getProtocol() == u2.getProtocol() &&
      java.util.Objects.equals(u1.getFile(), u2.getFile()) &&
      hostsEqual(u1, u2) && {
        val p1 = if (u1.getPort() == -1) getDefaultPort() else u1.getPort()
        val p2 = if (u2.getPort() == -1) getDefaultPort() else u2.getPort()
        p1 == p2
      }
}

private[net] object URLStreamHandler {

  /** The address as an `int` for IPv4, the sum of its four `int`s for IPv6.
   *  `InetAddress.hashCode` also hashes the host name.
   */
  private def addressHash(addr: InetAddress): Int = {
    val b = addr.getAddress()
    var h = 0
    var i = 0
    while (i + 3 < b.length) {
      h += ((b(i) & 0xff) << 24) | ((b(i + 1) & 0xff) << 16) |
        ((b(i + 2) & 0xff) << 8) | (b(i + 3) & 0xff)
      i += 4
    }
    h
  }

  private def hostOf(u: URL): String = {
    val host = u.getHost()
    if ("file" == u.getProtocol() && "" == host) "localhost" else host
  }

  /** An empty string is -1. */
  private def parsePort(s: String): Int =
    if (s.isEmpty) -1
    else
      try Integer.parseInt(s)
      catch {
        case _: NumberFormatException =>
          var i = 0
          while (i < s.length && Character.isDigit(s.charAt(i))) i += 1
          throw new IllegalArgumentException(
            "Error at index " + i + " in: \"" + s + "\""
          )
      }

  /** Removes `.` segments and `..` segments with the segment before them; `..`
   *  at the root stays.
   */
  def canonicalizePath(p: String): String = {
    var path = p
    var i = path.indexOf("/./")
    while (i >= 0) {
      path = path.substring(0, i + 1) + path.substring(i + 3)
      i = path.indexOf("/./")
    }
    if (path.endsWith("/.")) path = path.substring(0, path.length - 1)
    var from = 0
    i = path.indexOf("/../", from)
    while (i >= 0) {
      if (i == 0) from = 1
      else {
        val prev = path.lastIndexOf('/', i - 1)
        if (path.substring(prev + 1, i) == "..") from = i + 1
        else
          path =
            if (prev < 0) path.substring(i + 4)
            else path.substring(0, prev) + path.substring(i + 3)
      }
      i = path.indexOf("/../", from)
    }
    if (path.endsWith("/..") && path.length > 3) {
      val prev = path.lastIndexOf('/', path.length - 4)
      if (path.substring(prev + 1, path.length - 3) != "..")
        path = path.substring(0, prev + 1)
    }
    path
  }
}
