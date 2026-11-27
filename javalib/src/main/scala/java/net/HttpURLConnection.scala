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

abstract class HttpURLConnection protected (u: URL) extends URLConnection(u) {

  protected var method: String = "GET"

  /** -1 when not in chunked streaming mode. */
  protected var chunkLength: Int = -1

  /** -1 when not set. */
  protected var fixedContentLength: Int = -1

  /** -1 when not set. */
  protected var fixedContentLengthLong: Long = -1L

  /** -1 until known. */
  protected var responseCode: Int = -1

  protected var responseMessage: String = null

  protected var instanceFollowRedirects: Boolean =
    HttpURLConnection.followRedirects

  @throws[UnsupportedOperationException]
  def setAuthenticator(auth: Authenticator): Unit =
    throw new UnsupportedOperationException(
      "Supplying an authenticator is not supported by " + getClass()
    )

  override def getHeaderFieldKey(n: Int): String = null

  def setFixedLengthStreamingMode(contentLength: Int): Unit = {
    checkStreamingMode(contentLength)
    fixedContentLength = contentLength
  }

  def setFixedLengthStreamingMode(contentLength: Long): Unit = {
    checkStreamingMode(contentLength)
    fixedContentLengthLong = contentLength
  }

  /** `chunklen` of 0 or less picks a default. */
  def setChunkedStreamingMode(chunklen: Int): Unit = {
    if (connected)
      throw new IllegalStateException(
        "Can't set streaming mode: already connected"
      )
    if (fixedContentLength != -1 || fixedContentLengthLong != -1L)
      throw new IllegalStateException("Fixed length streaming mode set")
    chunkLength =
      if (chunklen <= 0) HttpURLConnection.DefaultChunkLength else chunklen
  }

  private def checkStreamingMode(contentLength: Long): Unit = {
    if (connected) throw new IllegalStateException("Already connected")
    if (chunkLength != -1)
      throw new IllegalStateException("Chunked encoding streaming mode set")
    if (contentLength < 0)
      throw new IllegalArgumentException("invalid content length")
  }

  /** The status line for 0. */
  override def getHeaderField(n: Int): String = null

  def setInstanceFollowRedirects(followRedirects: Boolean): Unit =
    instanceFollowRedirects = followRedirects

  def getInstanceFollowRedirects(): Boolean = instanceFollowRedirects

  @throws[ProtocolException]
  def setRequestMethod(method: String): Unit = {
    if (connected) throw new IllegalStateException("connect in progress")
    if (!HttpURLConnection.isMethod(method))
      throw new ProtocolException("Invalid HTTP method: " + method)
    this.method = method
  }

  def getRequestMethod(): String = method

  /** -1 if the status line is not valid HTTP. */
  @throws[IOException]
  def getResponseCode(): Int = {
    if (responseCode != -1) return responseCode
    try getInputStream()
    catch { case e: IOException => if (getHeaderField(0) == null) throw e }
    val status = getHeaderField(0)
    if (status == null || !status.startsWith("HTTP/")) return -1
    val sp = status.indexOf(' ')
    if (sp > 0) {
      val sp2 = status.indexOf(' ', sp + 1)
      if (sp2 > 0) responseMessage = status.substring(sp2 + 1)
      try
        responseCode = Integer.parseInt(
          status.substring(sp + 1, if (sp2 > 0) sp2 else status.length)
        )
      catch { case _: NumberFormatException => () }
    }
    responseCode
  }

  @throws[IOException]
  def getResponseMessage(): String = {
    getResponseCode()
    responseMessage
  }

  override def getHeaderFieldDate(name: String, default: Long): Long =
    super.getHeaderFieldDate(name, default)

  def disconnect(): Unit

  def usingProxy(): Boolean

  /** The body of an error response once connected, else `null`. */
  def getErrorStream(): InputStream = null
}

object HttpURLConnection {

  private final val DefaultChunkLength = 4096

  private val methods =
    Array("GET", "POST", "HEAD", "OPTIONS", "PUT", "DELETE", "TRACE")

  private def isMethod(m: String): Boolean = {
    var i = 0
    while (i < methods.length && methods(i) != m) i += 1
    i < methods.length
  }

  @volatile private var followRedirects = true

  def setFollowRedirects(set: Boolean): Unit = followRedirects = set

  def getFollowRedirects(): Boolean = followRedirects

  final val HTTP_OK = 200
  final val HTTP_CREATED = 201
  final val HTTP_ACCEPTED = 202
  final val HTTP_NOT_AUTHORITATIVE = 203
  final val HTTP_NO_CONTENT = 204
  final val HTTP_RESET = 205
  final val HTTP_PARTIAL = 206
  final val HTTP_MULT_CHOICE = 300
  final val HTTP_MOVED_PERM = 301
  final val HTTP_MOVED_TEMP = 302
  final val HTTP_SEE_OTHER = 303
  final val HTTP_NOT_MODIFIED = 304
  final val HTTP_USE_PROXY = 305
  final val HTTP_BAD_REQUEST = 400
  final val HTTP_UNAUTHORIZED = 401
  final val HTTP_PAYMENT_REQUIRED = 402
  final val HTTP_FORBIDDEN = 403
  final val HTTP_NOT_FOUND = 404
  final val HTTP_BAD_METHOD = 405
  final val HTTP_NOT_ACCEPTABLE = 406
  final val HTTP_PROXY_AUTH = 407
  final val HTTP_CLIENT_TIMEOUT = 408
  final val HTTP_CONFLICT = 409
  final val HTTP_GONE = 410
  final val HTTP_LENGTH_REQUIRED = 411
  final val HTTP_PRECON_FAILED = 412
  final val HTTP_ENTITY_TOO_LARGE = 413
  final val HTTP_REQ_TOO_LONG = 414
  final val HTTP_UNSUPPORTED_TYPE = 415
  @deprecated("Use HTTP_INTERNAL_ERROR", "1.3")
  final val HTTP_SERVER_ERROR = 500
  final val HTTP_INTERNAL_ERROR = 500
  final val HTTP_NOT_IMPLEMENTED = 501
  final val HTTP_BAD_GATEWAY = 502
  final val HTTP_UNAVAILABLE = 503
  final val HTTP_GATEWAY_TIMEOUT = 504
  final val HTTP_VERSION = 505
}
