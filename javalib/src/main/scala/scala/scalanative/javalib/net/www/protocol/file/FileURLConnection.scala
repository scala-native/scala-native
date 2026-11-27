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
// (org.apache.harmony.luni.internal.net.www.protocol.file.FileURLConnection)

package scala.scalanative.javalib.net.www.protocol.file

import java.io.{
  BufferedInputStream, ByteArrayInputStream, File, FileInputStream,
  FileNotFoundException, IOException, InputStream
}
import java.net.{URL, URLConnection}
import java.nio.charset.StandardCharsets
import java.util.{ArrayList, List, Map}

import scala.scalanative.javalib.net.HttpDates

/** A directory reads as its entry names, one per line. */
final class FileURLConnection(u: URL) extends URLConnection(u) {

  private val file = FileURLConnection.toFile(u)
  // A NUL would truncate the path passed to the OS
  private val invalidPath = file.getPath().indexOf('\u0000') >= 0
  private var is: InputStream = null
  private var headers: List[Array[String]] = null
  private var contentType: String = null
  private var length = 0L
  private var lastModified = 0L

  @throws[IOException]
  override def connect(): Unit = if (!connected) {
    if (invalidPath)
      throw new FileNotFoundException("Invalid file path")
    is =
      if (file.isDirectory()) {
        val sb = new java.lang.StringBuilder()
        val names = file.list()
        if (names == null)
          throw new FileNotFoundException(
            file.getPath() + " exists, but is not accessible"
          )
        // The JDK sorts with a Collator; equal for ASCII names
        java.util.Arrays.sort(names, String.CASE_INSENSITIVE_ORDER)
        var i = 0
        while (i < names.length) {
          sb.append(names(i)).append('\n')
          i += 1
        }
        new ByteArrayInputStream(sb.toString.getBytes(StandardCharsets.UTF_8))
      } else new BufferedInputStream(new FileInputStream(file))
    connected = true
  }

  private def initHeaders(): List[Array[String]] = {
    if (headers == null) {
      val h = new ArrayList[Array[String]]()
      if (!invalidPath) {
        length = file.length()
        lastModified = file.lastModified()
      }
      try {
        connect()
        h.add(Array("content-type", getContentType()))
        if (!file.isDirectory()) {
          h.add(Array("content-length", java.lang.Long.toString(length)))
          h.add(Array("last-modified", HttpDates.format(lastModified)))
        }
      } catch { case _: IOException => () }
      headers = h
    }
    headers
  }

  override def getHeaderField(name: String): String = {
    val it = initHeaders().iterator()
    while (it.hasNext()) {
      val kv = it.next()
      if (kv(0).equalsIgnoreCase(name)) return kv(1)
    }
    null
  }

  override def getHeaderFieldKey(n: Int): String = {
    val h = initHeaders()
    if (n >= 0 && n < h.size()) h.get(n)(0) else null
  }

  override def getHeaderField(n: Int): String = {
    val h = initHeaders()
    if (n >= 0 && n < h.size()) h.get(n)(1) else null
  }

  override def getHeaderFields(): Map[String, List[String]] =
    FileURLConnection.group(initHeaders())

  /** `text/plain` for a directory, else guessed from the name (even if the file
   *  cannot be read), then from the content; `content/unknown` if neither
   *  works.
   */
  override def getContentType(): String = {
    if (contentType == null) {
      val opened =
        try { connect(); true }
        catch { case _: IOException => false }
      contentType =
        if (!invalidPath && file.isDirectory()) "text/plain"
        else {
          var t = URLConnection.guessContentTypeFromName(file.getPath())
          if (t == null && opened)
            t =
              try URLConnection.guessContentTypeFromStream(is)
              catch { case _: IOException => null }
          if (t == null) "content/unknown" else t
        }
    }
    contentType
  }

  override def getContentLength(): Int = {
    val n = getContentLengthLong()
    if (n > Int.MaxValue) -1 else n.toInt
  }

  override def getContentLengthLong(): Long = {
    initHeaders()
    length
  }

  override def getLastModified(): Long = {
    initHeaders()
    lastModified
  }

  @throws[IOException]
  override def getInputStream(): InputStream = {
    connect()
    is
  }
}

object FileURLConnection {

  private def group(pairs: List[Array[String]]): Map[String, List[String]] = {
    val m = new java.util.LinkedHashMap[String, List[String]]()
    val it = pairs.iterator()
    while (it.hasNext()) {
      val kv = it.next()
      m.put(kv(0), java.util.Collections.singletonList(kv(1)))
    }
    java.util.Collections.unmodifiableMap(m)
  }

  /** Whether `u` has no host other than `localhost`. */
  def isLocal(u: URL): Boolean = {
    val host = u.getHost()
    host == null || host.isEmpty || host.equalsIgnoreCase("localhost")
  }

  /** The file named by the decoded path of `u`. */
  def toFile(u: URL): File =
    new File(decode(if (u.getPath() == null) "" else u.getPath()))

  /** Decodes `%XX` escapes as UTF-8; other characters are kept. */
  def decode(s: String): String = {
    if (s.indexOf('%') < 0) return s
    val out = new java.io.ByteArrayOutputStream()
    val sb = new java.lang.StringBuilder()
    def flush(): Unit = if (out.size() > 0) {
      sb.append(new String(out.toByteArray(), StandardCharsets.UTF_8))
      out.reset()
    }
    var i = 0
    while (i < s.length) {
      val c = s.charAt(i)
      val hi =
        if (c == '%' && i + 2 < s.length) Character.digit(s.charAt(i + 1), 16)
        else -1
      val lo = if (hi >= 0) Character.digit(s.charAt(i + 2), 16) else -1
      if (hi >= 0 && lo >= 0) {
        out.write(hi * 16 + lo)
        i += 3
      } else {
        flush()
        sb.append(c)
        i += 1
      }
    }
    flush()
    sb.toString
  }
}
