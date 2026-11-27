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

// HTTP/1.1 message framing (RFC 9112); Apache Harmony's HttpURLConnectionImpl
// has the same streams as inner classes.

package scala.scalanative.javalib.net.www.protocol.http

import java.io.{
  FilterInputStream, FilterOutputStream, IOException, InputStream, OutputStream
}
import java.nio.charset.StandardCharsets.ISO_8859_1

private[http] object HttpStreams {

  private final val MaxLineLength = 64 * 1024

  /** The next line without its terminator, or `null` at the end of the stream.
   *  CR characters are dropped.
   */
  @throws[IOException]
  def readLine(in: InputStream): String = {
    val sb = new java.lang.StringBuilder()
    var c = in.read()
    if (c == -1) return null
    while (c != -1 && c != '\n') {
      if (c != '\r') {
        if (sb.length() >= MaxLineLength)
          throw new IOException("HTTP header line too long")
        sb.append(c.toChar)
      }
      c = in.read()
    }
    sb.toString
  }

  /** Skips by reading, so that the stream sees the skipped bytes. */
  @throws[IOException]
  def skipByReading(in: InputStream, n: Long): Long =
    if (n <= 0) 0
    else {
      val buf = new Array[Byte](math.min(n, 4096L).toInt)
      val k = in.read(buf, 0, buf.length)
      if (k < 0) 0 else k
    }
}

/** The socket input, failing once the connection is closed. */
private[http] final class SocketInput(in: InputStream)
    extends FilterInputStream(in) {
  @volatile private var closed = false

  private def check(): Unit =
    if (closed) throw new IOException("stream is closed")

  override def read(): Int = { check(); in.read() }

  override def read(b: Array[Byte], off: Int, len: Int): Int = {
    check()
    in.read(b, off, len)
  }

  override def skip(n: Long): Long = { check(); in.skip(n) }

  override def available(): Int = if (closed) 0 else in.available()

  override def close(): Unit = closed = true
}

/** A body with a `Content-Length`; `onEnd` runs once, at its end or on close.
 */
private[http] final class FixedLengthInputStream(
    in: InputStream,
    private var remaining: Long,
    onEnd: () => Unit
) extends FilterInputStream(in) {
  private var ended = false

  private def end(): Unit = if (!ended) { ended = true; onEnd() }

  if (remaining <= 0) end()

  override def read(): Int =
    if (remaining <= 0) -1
    else {
      val c = in.read()
      if (c == -1) throw new IOException("Premature EOF")
      remaining -= 1
      if (remaining == 0) end()
      c
    }

  override def read(b: Array[Byte], off: Int, len: Int): Int =
    if (len == 0) 0
    else if (remaining <= 0) -1
    else {
      val n = in.read(b, off, math.min(len.toLong, remaining).toInt)
      if (n == -1) throw new IOException("Premature EOF")
      remaining -= n
      if (remaining == 0) end()
      n
    }

  override def available(): Int =
    if (remaining <= 0) 0 else math.min(in.available().toLong, remaining).toInt

  override def skip(n: Long): Long =
    if (n <= 0 || remaining <= 0) 0
    else {
      val k = in.skip(math.min(n, remaining))
      remaining -= k
      if (remaining == 0) end()
      k
    }

  override def markSupported(): Boolean = false

  override def close(): Unit = end()
}

/** A chunked body; trailers are skipped. */
private[http] final class ChunkedInputStream(in: InputStream, onEnd: () => Unit)
    extends FilterInputStream(in) {
  private var remaining = 0L
  private var started = false
  private var done = false

  private def nextChunk(): Unit =
    if (remaining == 0 && !done) {
      // The CRLF after the previous chunk's data
      if (started && HttpStreams.readLine(in) != "")
        throw new IOException("Bogus chunk terminator")
      started = true
      val line = HttpStreams.readLine(in)
      if (line == null) throw new IOException("Premature EOF")
      val semi = line.indexOf(';')
      val size = (if (semi >= 0) line.substring(0, semi) else line).trim()
      remaining =
        try java.lang.Long.parseLong(size, 16)
        catch {
          case _: NumberFormatException =>
            throw new IOException("Bogus chunk size")
        }
      if (remaining < 0 || size.startsWith("+") || size.startsWith("-"))
        throw new IOException("Bogus chunk size")
      if (remaining == 0) {
        var trailer = HttpStreams.readLine(in)
        while (trailer != null && !trailer.isEmpty)
          trailer = HttpStreams.readLine(in)
        done = true
        onEnd()
      }
    }

  override def read(): Int = {
    nextChunk()
    if (done) -1
    else {
      val c = in.read()
      if (c == -1) throw new IOException("Premature EOF")
      remaining -= 1
      c
    }
  }

  override def read(b: Array[Byte], off: Int, len: Int): Int =
    if (len == 0) 0
    else {
      nextChunk()
      if (done) -1
      else {
        val n = in.read(b, off, math.min(len.toLong, remaining).toInt)
        if (n == -1) throw new IOException("Premature EOF")
        remaining -= n
        n
      }
    }

  override def available(): Int =
    if (done) 0 else math.min(in.available().toLong, remaining).toInt

  override def skip(n: Long): Long = HttpStreams.skipByReading(this, n)

  override def markSupported(): Boolean = false

  override def close(): Unit = if (!done) { done = true; onEnd() }
}

/** A body that ends when the server closes the connection. */
private[http] final class UntilCloseInputStream(
    in: InputStream,
    onEnd: () => Unit
) extends FilterInputStream(in) {
  private var ended = false

  private def end(): Unit = if (!ended) { ended = true; onEnd() }

  override def read(): Int = {
    val c = if (ended) -1 else in.read()
    if (c == -1) end()
    c
  }

  override def read(b: Array[Byte], off: Int, len: Int): Int =
    if (len == 0) 0
    else {
      val n = if (ended) -1 else in.read(b, off, len)
      if (n == -1) end()
      n
    }

  override def available(): Int = if (ended) 0 else in.available()

  override def markSupported(): Boolean = false

  override def close(): Unit = end()
}

/** Copies what is read into a cache, which is aborted unless the whole body was
 *  read.
 */
private[http] final class CachingInputStream(
    in: InputStream,
    request: java.net.CacheRequest,
    cache: OutputStream
) extends FilterInputStream(in) {
  private var finished = false

  private def finish(complete: Boolean): Unit = if (!finished) {
    finished = true
    if (complete)
      try cache.close()
      catch { case _: IOException => request.abort() }
    else request.abort()
  }

  private def copy(n: Int)(write: => Unit): Int = {
    if (n == -1) finish(true)
    else if (!finished)
      try write
      catch { case _: IOException => finish(false) }
    n
  }

  override def read(): Int = {
    val c = in.read()
    copy(c)(cache.write(c))
  }

  override def read(b: Array[Byte], off: Int, len: Int): Int = {
    val n = in.read(b, off, len)
    copy(n)(if (n > 0) cache.write(b, off, n))
  }

  override def skip(n: Long): Long = HttpStreams.skipByReading(this, n)

  override def markSupported(): Boolean = false

  override def close(): Unit = {
    finish(false)
    in.close()
  }
}

/** A request body with a fixed length (`setFixedLengthStreamingMode`). */
private[http] final class FixedLengthOutputStream(
    out: OutputStream,
    length: Long
) extends FilterOutputStream(out) {
  private var written = 0L
  private var closed = false

  override def write(b: Int): Unit = {
    if (closed) throw new IOException("Stream is closed")
    if (written + 1 > length) throw new IOException("too many bytes written")
    out.write(b)
    written += 1
  }

  override def write(b: Array[Byte], off: Int, len: Int): Unit = {
    if (closed) throw new IOException("Stream is closed")
    if (written + len > length) throw new IOException("too many bytes written")
    out.write(b, off, len)
    written += len
  }

  override def close(): Unit = if (!closed) {
    closed = true
    out.flush()
    if (written < length) throw new IOException("insufficient data written")
  }
}

/** A chunked request body (`setChunkedStreamingMode`). */
private[http] final class ChunkedOutputStream(
    out: OutputStream,
    chunkLength: Int
) extends FilterOutputStream(out) {
  private val buf = new Array[Byte](chunkLength)
  private var count = 0
  private var closed = false

  private def writeChunk(): Unit = if (count > 0) {
    out.write((Integer.toHexString(count) + "\r\n").getBytes(ISO_8859_1))
    out.write(buf, 0, count)
    out.write('\r')
    out.write('\n')
    count = 0
  }

  override def write(b: Int): Unit = {
    if (closed) throw new IOException("Stream is closed")
    buf(count) = b.toByte
    count += 1
    if (count == buf.length) writeChunk()
  }

  override def write(b: Array[Byte], off: Int, len: Int): Unit = {
    if (closed) throw new IOException("Stream is closed")
    var i = off
    val end = off + len
    while (i < end) {
      val n = math.min(end - i, buf.length - count)
      System.arraycopy(b, i, buf, count, n)
      count += n
      i += n
      if (count == buf.length) writeChunk()
    }
  }

  override def flush(): Unit = {
    writeChunk()
    out.flush()
  }

  override def close(): Unit = if (!closed) {
    writeChunk()
    closed = true
    out.write("0\r\n\r\n".getBytes(ISO_8859_1))
    out.flush()
  }
}
