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

// Ported from Apache Harmony, without the permission checks and the content
// handler packages.

package java.net

import java.io.{IOException, InputStream, OutputStream}
import java.util.concurrent.ConcurrentHashMap
import java.util.{ArrayList, Collections, LinkedHashMap, List, Map}

import scala.scalanative.javalib.net.HttpDates
import scala.scalanative.javalib.net.www.MimeTable

abstract class URLConnection protected (protected var url: URL) {

  protected var doInput: Boolean = true
  protected var doOutput: Boolean = false
  protected var allowUserInteraction: Boolean =
    URLConnection.defaultAllowUserInteraction
  protected var useCaches: Boolean = URLConnection.defaultUseCaches
  protected var ifModifiedSince: Long = 0L
  protected var connected: Boolean = false

  private var connectTimeout = 0
  private var readTimeout = 0

  /** (key, value) pairs in the order they were added. */
  private val requestProperties = new ArrayList[Array[String]]()

  @throws[IOException]
  def connect(): Unit

  def setConnectTimeout(timeout: Int): Unit = {
    if (timeout < 0)
      throw new IllegalArgumentException("timeout can not be negative")
    connectTimeout = timeout
  }

  def getConnectTimeout(): Int = connectTimeout

  def setReadTimeout(timeout: Int): Unit = {
    if (timeout < 0)
      throw new IllegalArgumentException("timeout can not be negative")
    readTimeout = timeout
  }

  def getReadTimeout(): Int = readTimeout

  def getURL(): URL = url

  def getContentLength(): Int = getHeaderFieldInt("content-length", -1)

  def getContentLengthLong(): Long = getHeaderFieldLong("content-length", -1L)

  def getContentType(): String = getHeaderField("content-type")

  def getContentEncoding(): String = getHeaderField("content-encoding")

  def getExpiration(): Long = getHeaderFieldDate("expires", 0L)

  def getDate(): Long = getHeaderFieldDate("date", 0L)

  def getLastModified(): Long = getHeaderFieldDate("last-modified", 0L)

  def getHeaderField(name: String): String = null

  def getHeaderFields(): Map[String, List[String]] = Collections.emptyMap()

  def getHeaderFieldInt(name: String, default: Int): Int =
    try Integer.parseInt(getHeaderField(name))
    catch { case _: NumberFormatException => default }

  def getHeaderFieldLong(name: String, default: Long): Long =
    try java.lang.Long.parseLong(getHeaderField(name))
    catch { case _: NumberFormatException => default }

  /** Dates without a zone are read as GMT, not in the default time zone. */
  def getHeaderFieldDate(name: String, default: Long): Long = {
    val value = getHeaderField(name)
    if (value == null) default
    else {
      val t = HttpDates.parseLenient(value)
      if (t == Long.MinValue) default else t
    }
  }

  def getHeaderFieldKey(n: Int): String = null

  def getHeaderField(n: Int): String = null

  /** Without a content handler factory, the content is the input stream. */
  @throws[IOException]
  def getContent(): AnyRef = contentHandler().getContent(this)

  @throws[IOException]
  def getContent(classes: Array[Class[_]]): AnyRef =
    contentHandler().getContent(this, classes)

  private def contentHandler(): ContentHandler = {
    val tpe = getContentType()
    if (tpe == null) throw new UnknownServiceException("no content-type")
    URLConnection.contentHandlerFor(tpe)
  }

  @throws[IOException]
  def getInputStream(): InputStream =
    throw new UnknownServiceException("protocol doesn't support input")

  @throws[IOException]
  def getOutputStream(): OutputStream =
    throw new UnknownServiceException("protocol doesn't support output")

  override def toString(): String = getClass().getName() + ":" + url

  private def checkNotConnected(): Unit =
    if (connected) throw new IllegalStateException("Already connected")

  def setDoInput(doinput: Boolean): Unit = {
    checkNotConnected()
    doInput = doinput
  }

  def getDoInput(): Boolean = doInput

  def setDoOutput(dooutput: Boolean): Unit = {
    checkNotConnected()
    doOutput = dooutput
  }

  def getDoOutput(): Boolean = doOutput

  def setAllowUserInteraction(allowuserinteraction: Boolean): Unit = {
    checkNotConnected()
    allowUserInteraction = allowuserinteraction
  }

  def getAllowUserInteraction(): Boolean = allowUserInteraction

  def setUseCaches(usecaches: Boolean): Unit = {
    checkNotConnected()
    useCaches = usecaches
  }

  def getUseCaches(): Boolean = useCaches

  def setIfModifiedSince(ifmodifiedsince: Long): Unit = {
    checkNotConnected()
    ifModifiedSince = ifmodifiedsince
  }

  def getIfModifiedSince(): Long = ifModifiedSince

  def getDefaultUseCaches(): Boolean = URLConnection.defaultUseCaches

  def setDefaultUseCaches(defaultusecaches: Boolean): Unit =
    URLConnection.defaultUseCaches = defaultusecaches

  /** Replaces the last value of a property whose name equals `key` ignoring
   *  case, or adds the property.
   */
  def setRequestProperty(key: String, value: String): Unit = {
    checkNotConnected()
    if (key == null) throw new NullPointerException("key is null")
    val i = lastRequestProperty(key)
    if (i >= 0) requestProperties.get(i)(1) = value
    else requestProperties.add(Array(key, value))
  }

  def addRequestProperty(key: String, value: String): Unit = {
    checkNotConnected()
    if (key == null) throw new NullPointerException("key is null")
    requestProperties.add(Array(key, value))
  }

  def getRequestProperty(key: String): String = {
    checkNotConnected()
    if (key == null) null
    else {
      val i = lastRequestProperty(key)
      if (i < 0) null else requestProperties.get(i)(1)
    }
  }

  /** Keyed by the first spelling of each name. */
  def getRequestProperties(): Map[String, List[String]] = {
    checkNotConnected()
    URLConnection.group(requestProperties)
  }

  private def lastRequestProperty(key: String): Int = {
    var i = requestProperties.size() - 1
    while (i >= 0 && !requestProperties.get(i)(0).equalsIgnoreCase(key)) i -= 1
    i
  }
}

object URLConnection {

  /** An unmodifiable map from (key, value) pairs: names that are equal ignoring
   *  case are merged under their first spelling, values keep their order.
   */
  private[net] def group(
      pairs: java.util.List[Array[String]]
  ): Map[String, List[String]] = {
    val map = new LinkedHashMap[String, List[String]]()
    val it = pairs.iterator()
    while (it.hasNext()) {
      val kv = it.next()
      var key: String = null
      val keys = map.keySet().iterator()
      while (key == null && keys.hasNext()) {
        val k = keys.next()
        if (k != null && kv(0) != null && k.equalsIgnoreCase(kv(0))) key = k
      }
      if (key == null) {
        key = kv(0)
        if (!map.containsKey(key)) map.put(key, new ArrayList[String]())
      }
      map.get(key).add(kv(1))
    }
    val ro = new LinkedHashMap[String, List[String]]()
    val es = map.entrySet().iterator()
    while (es.hasNext()) {
      val e = es.next()
      ro.put(e.getKey(), Collections.unmodifiableList(e.getValue()))
    }
    Collections.unmodifiableMap(ro)
  }

  @volatile private var defaultAllowUserInteraction = false
  @volatile private var defaultUseCaches = true
  @volatile private var fileNameMap: FileNameMap = null
  @volatile private var contentHandlerFactory: ContentHandlerFactory = null
  private val contentHandlers = new ConcurrentHashMap[String, ContentHandler]()

  def getFileNameMap(): FileNameMap = {
    val m = fileNameMap
    if (m != null) m else MimeTable
  }

  /** `null` restores the default map. */
  def setFileNameMap(map: FileNameMap): Unit = fileNameMap = map

  def setDefaultAllowUserInteraction(
      defaultallowuserinteraction: Boolean
  ): Unit =
    defaultAllowUserInteraction = defaultallowuserinteraction

  def getDefaultAllowUserInteraction(): Boolean = defaultAllowUserInteraction

  /** Does nothing. */
  @deprecated("Use setRequestProperty on each connection", "1.3")
  def setDefaultRequestProperty(key: String, value: String): Unit = ()

  /** Always `null`. */
  @deprecated("Use getRequestProperty on each connection", "1.3")
  def getDefaultRequestProperty(key: String): String = null

  /** @throws Error
   *    if a factory is already set
   */
  def setContentHandlerFactory(fac: ContentHandlerFactory): Unit =
    synchronized {
      if (contentHandlerFactory != null)
        throw new Error("factory already defined")
      contentHandlerFactory = fac
    }

  /** The factory's handler for a content type (parameters ignored), else one
   *  that returns the input stream.
   */
  private def contentHandlerFor(contentType: String): ContentHandler = {
    var tpe = contentType
    val semi = tpe.indexOf(';')
    if (semi >= 0) tpe = tpe.substring(0, semi)
    tpe = tpe.trim().toLowerCase()
    var h = contentHandlers.get(tpe)
    if (h == null) {
      val fac = contentHandlerFactory
      if (fac != null) h = fac.createContentHandler(tpe)
      if (h == null) h = StreamContentHandler
      else contentHandlers.put(tpe, h)
    }
    h
  }

  private object StreamContentHandler extends ContentHandler {
    @throws[IOException]
    def getContent(urlc: URLConnection): AnyRef = urlc.getInputStream()
  }

  def guessContentTypeFromName(fname: String): String =
    getFileNameMap().getContentTypeFor(fname)

  /** Guesses the content type from the first bytes of a stream that supports
   *  `mark`, then resets it. `null` if unknown or the stream does not support
   *  `mark`.
   */
  @throws[IOException]
  def guessContentTypeFromStream(is: InputStream): String = {
    if (!is.markSupported()) return null
    is.mark(64)
    val b = new Array[Int](64)
    var n = 0
    var c = 0
    while (n < 64 && { c = is.read(); c != -1 }) {
      b(n) = c
      n += 1
    }
    is.reset()

    def at(bytes: Int*): Boolean =
      bytes.length <= n && bytes.indices.forall(i => b(i) == bytes(i))
    def text(s: String): Boolean = at(s.map(_.toInt): _*)
    def utf(unit: Int, bigEndian: Boolean, skip: Int, s: String): Boolean =
      s.length * unit + skip <= n && s.indices.forall { i =>
        val base = skip + i * unit
        (0 until unit).forall { k =>
          val expected =
            if ((k == unit - 1) == bigEndian) s.charAt(i).toInt else 0
          b(base + k) == expected
        }
      }

    if (at(0xca, 0xfe, 0xba, 0xbe)) "application/java-vm"
    else if (at(0xac, 0xed)) "application/x-java-serialized-object"
    else if (text("<!")) "text/html"
    else if (text("<html") || text("<HTML") || text("<head") ||
        text("<HEAD") || text("<body") || text("<BODY")) "text/html"
    else if (text("<?xml ")) "application/xml"
    else if (at(0xef, 0xbb, 0xbf) && utf(1, true, 3, "<?xml"))
      "application/xml"
    else if (at(0xfe, 0xff) && utf(2, true, 2, "<?xml")) "application/xml"
    else if (at(0xff, 0xfe) && utf(2, false, 2, "<?xml")) "application/xml"
    else if (at(0, 0, 0xfe, 0xff) && utf(4, true, 4, "<?xml"))
      "application/xml"
    else if (at(0xff, 0xfe, 0, 0) && utf(4, false, 4, "<?xml"))
      "application/xml"
    else if (text("GIF8")) "image/gif"
    else if (text("#def")) "image/x-bitmap"
    else if (text("! XPM2")) "image/x-pixmap"
    else if (at(0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a)) "image/png"
    else if (at(0xff, 0xd8, 0xff, 0xe0) || at(0xff, 0xd8, 0xff, 0xee))
      "image/jpeg"
    else if (at(0x49, 0x49, 0x2a, 0) || at(0x4d, 0x4d, 0, 0x2a)) "image/tiff"
    else if (text("RIFF")) "audio/x-wav"
    else if (text(".snd")) "audio/basic"
    else null
  }
}
