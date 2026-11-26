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
// (org.apache.harmony.luni.internal.net.www.protocol.jar.Handler)

package scala.scalanative.javalib.net.www.protocol.jar

import java.io.IOException
import java.net.{
  MalformedURLException, Proxy, URL, URLConnection, URLStreamHandler,
  UnknownServiceException
}

/** Parses `jar:<url>!/<entry>` URLs; connections are not supported. */
final class Handler extends URLStreamHandler {

  @throws[IOException]
  override def openConnection(u: URL): URLConnection =
    throw new UnknownServiceException("protocol not supported: jar")

  @throws[IOException]
  override def openConnection(u: URL, p: Proxy): URLConnection =
    openConnection(u)

  /** An absolute spec must contain `!/`; a spec starting with `/` is relative
   *  to the root of the JAR, any other relative to the directory of the
   *  context's entry.
   */
  override def parseURL(u: URL, spec0: String, start: Int, limit: Int): Unit = {
    val spec = if (limit > start) spec0.substring(start, limit) else ""
    val context = if (u.getFile() == null) "" else u.getFile()
    val bang = context.indexOf("!/")
    val file =
      if (spec.indexOf("!/") >= 0 || bang < 0) spec
      else if (spec.isEmpty) context
      else if (spec.startsWith("/")) context.substring(0, bang + 1) + spec
      else {
        val entry =
          context.substring(bang + 1, context.lastIndexOf('/') + 1) + spec
        context.substring(0, bang + 1) + Handler.canonicalize(entry)
      }
    val sep = file.indexOf("!/")
    if (sep < 0) throw new MalformedURLException("no !/ in spec")
    try new URL(file.substring(0, sep))
    catch {
      case e: MalformedURLException =>
        throw new MalformedURLException(
          "invalid url: " + spec + " (" + e + ")"
        )
    }
    val q = file.indexOf('?')
    val path = if (q >= 0) file.substring(0, q) else file
    val query = if (q >= 0) file.substring(q + 1) else null
    setURL(u, "jar", "", -1, null, null, path, query, u.getRef())
  }

  override def toExternalForm(u: URL): String = {
    val ref = u.getRef()
    "jar:" + u.getFile() + (if (ref == null) "" else "#" + ref)
  }

  override def equals(u1: URL, u2: URL): Boolean =
    sameFile(u1, u2) && java.util.Objects.equals(u1.getRef(), u2.getRef())

  /** Equal JAR file URLs and entries. */
  override def sameFile(u1: URL, u2: URL): Boolean =
    u2.getProtocol() == "jar" && {
      val f1 = Handler.jarFile(u1)
      val f2 = Handler.jarFile(u2)
      Handler.entry(u1) == Handler.entry(u2) &&
        (if (f1 == null || f2 == null) f1 == f2 else f1.sameFile(f2))
    }

  /** `"jar".hashCode` plus the hash codes of the JAR file URL and the entry. */
  override def hashCode(u: URL): Int = {
    val f = Handler.jarFile(u)
    u.getProtocol().hashCode() +
      (if (f == null) u.getFile().hashCode() else f.hashCode()) +
      Handler.entry(u).hashCode()
  }
}

private object Handler {

  /** The JAR file URL, or `null` if it does not parse. */
  private def jarFile(u: URL): URL = {
    val file = u.getFile()
    val sep = file.indexOf("!/")
    if (sep < 0) null
    else
      try new URL(file.substring(0, sep))
      catch { case _: MalformedURLException => null }
  }

  private def entry(u: URL): String = {
    val file = u.getFile()
    val sep = file.indexOf("!/")
    if (sep < 0) file else file.substring(sep + 2)
  }

  /** Removes `.` segments and `..` segments with the segment before them,
   *  within the entry.
   */
  private def canonicalize(entry: String): String = {
    val segments = new java.util.ArrayList[String]()
    var from = 0
    var last = false
    while (!last) {
      var to = entry.indexOf('/', from)
      last = to < 0
      if (last) to = entry.length
      val p = entry.substring(from, to)
      if (p == "..") {
        if (segments.size() > 1) segments.remove(segments.size() - 1)
      } else if (p != "." || last) segments.add(if (p == ".") "" else p)
      from = to + 1
    }
    val sb = new java.lang.StringBuilder()
    var i = 0
    while (i < segments.size()) {
      if (i > 0) sb.append('/')
      sb.append(segments.get(i))
      i += 1
    }
    sb.toString
  }
}
