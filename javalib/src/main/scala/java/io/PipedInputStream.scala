/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package java.io

object PipedInputStream {
  final val PIPE_SIZE = 1024
}

/** Constructs a new unconnected {@code PipedInputStream}. The resulting stream
 *  must be connected to a {@link PipedOutputStream} before data may be read
 *  from it.
 */
class PipedInputStream() extends InputStream {
  private var lastReader: Thread = _
  private var lastWriter: Thread = _
  private var isClosed = false

  /** The circular buffer through which data is passed.
   */
  private[io] var buffer: Array[Byte] = _

  /** Indicates if this pipe is connected.
   */
  private[io] var isConnected = false // Modified by PipedOutputStream

  /** The index in {@code buffer} where the next byte will be written.
   */
  protected var in: Int = -1

  /** The index in {@code buffer} where the next byte will be read.
   */
  protected var out: Int = 0

  def this(out: PipedOutputStream) = {
    this()
    connect(out)
  }

  /** Returns the number of bytes that are available before this stream will
   *  block. This implementation returns the number of bytes written to this
   *  pipe that have not been read yet.
   *
   *  @return
   *    the number of bytes available before blocking.
   *  @throws IOException
   *    if an error occurs in this stream.
   */
  @throws[IOException]
  override def available(): Int = synchronized {
    if (buffer == null || in == -1) 0
    else if (in <= out) buffer.length - out + in
    else in - out
  }

  /** Closes this stream. This implementation releases the buffer used for the
   *  pipe and notifies all threads waiting to read or write.
   *
   *  @throws IOException
   *    if an error occurs while closing this stream.
   */
  @throws[IOException]
  override def close() = synchronized {
    // No exception thrown if already closed */
    // Release buffer to indicate closed.
    if (buffer != null) buffer = null
  }

  /** Connects this {@code PipedInputStream} to a {@link PipedOutputStream}. Any
   *  data written to the output stream becomes readable in this input stream.
   *
   *  @param src
   *    the source output stream.
   *  @throws IOException
   *    if either stream is already connected.
   */
  @throws[IOException]
  def connect(src: PipedOutputStream) = src.connect(this)

  /** Reads a single byte from this stream and returns it as an integer in the
   *  range from 0 to 255. Returns -1 if the end of this stream has been
   *  reached. If there is no data in the pipe, this method blocks until data is
   *  available, the end of the stream is detected or an exception is thrown.
   *  <p> Separate threads should be used to read from a {@code
   *  PipedInputStream} and to write to the connected {@link PipedOutputStream}.
   *  If the same thread is used, a deadlock may occur.
   *
   *  @return
   *    the byte read or -1 if the end of the source stream has been reached.
   *  @throws IOException
   *    if this stream is closed or not connected to an output stream, or if the
   *    thread writing to the connected output stream is no longer alive.
   */
  @throws[IOException]
  override def read(): Int = {
    if (!isConnected) throw new IOException("Not connected")
    if (buffer == null) throw new IOException("InputStream is closed")
    if (isClosed && in == -1) {
      // write end closed and no more need to read
      return -1
    }
    if (lastWriter != null && !lastWriter.isAlive() && (in < 0))
      throw new IOException("Write end dead")

    /** Set the last thread to be reading on this PipedInputStream. If
     *  lastReader dies while someone is waiting to write an IOException of
     *  "Pipe broken" will be thrown in receive()
     */
    lastReader = Thread.currentThread()
    try {
      var attempts = 3
      while (in == -1) {
        // Are we at end of stream?
        if (isClosed) return -1

        if ({
          val a = attempts; attempts -= 1; a <= 0
        } && lastWriter != null && !lastWriter.isAlive()) {
          throw new IOException("Pipe broken")
        }
        // Notify callers of receive()
        notifyAll()
        wait(1000)
      }
    } catch {
      case e: InterruptedException => throw new InterruptedIOException
    }

    val result = buffer(out)
    out += 1
    if (out == buffer.length) out = 0
    if (out == in) {
      // empty buffer
      in = -1
      out = 0
    }
    result & 0xff
  }

  /** Reads at most {@code count} bytes from this stream and stores them in the
   *  byte array {@code bytes} starting at {@code offset}. Blocks until at least
   *  one byte has been read, the end of the stream is detected or an exception
   *  is thrown. <p> Separate threads should be used to read from a {@code
   *  PipedInputStream} and to write to the connected {@link PipedOutputStream}.
   *  If the same thread is used, a deadlock may occur.
   *
   *  @param bytes
   *    the array in which to store the bytes read.
   *  @param offset
   *    the initial position in {@code bytes} to store the bytes read from this
   *    stream.
   *  @param count
   *    the maximum number of bytes to store in {@code bytes}.
   *  @return
   *    the number of bytes actually read or -1 if the end of the stream has
   *    been reached.
   *  @throws IndexOutOfBoundsException
   *    if {@code offset < 0} or {@code count < 0}, or if {@code offset + count}
   *    is greater than the size of {@code bytes}.
   *  @throws InterruptedIOException
   *    if the thread reading from this stream is interrupted.
   *  @throws IOException
   *    if this stream is closed or not connected to an output stream, or if the
   *    thread writing to the connected output stream is no longer alive.
   *  @throws NullPointerException
   *    if {@code bytes} is {@code null}.
   */
  @throws[IOException]
  override def read(bytes: Array[Byte], offset: Int, count: Int): Int = {
    if (bytes == null) throw new NullPointerException
    if (offset < 0 || offset > bytes.length || count < 0 || count > bytes.length - offset)
      throw new IndexOutOfBoundsException
    if (count == 0) return 0
    if (isClosed && in == -1) return -1
    if (!isConnected) throw new IOException("Not connected")
    if (buffer == null) throw new IOException("InputStream is closed")
    if (lastWriter != null && !lastWriter.isAlive() && (in < 0))
      throw new IOException("Write end dead")
    lastReader = Thread.currentThread()
    try {
      var attempts = 3
      while (in == -1) {
        if (isClosed) return -1
        val attempt = attempts
        attempts -= 1
        if (attempt <= 0 && lastWriter != null && !lastWriter.isAlive())
          throw new IOException("Pipe broken")
        notifyAll()
        wait(1000)
      }
    } catch {
      case e: InterruptedException => throw new InterruptedIOException
    }
    /* Copy bytes from out to end of buffer first */
    val copyLength =
      if (out < in) 0
      else {
        val copyLength =
          if (count > (buffer.length - out)) buffer.length - out
          else count
        System.arraycopy(buffer, out, bytes, offset, copyLength)
        out += copyLength
        if (out == buffer.length) out = 0
        if (out == in) {
          in = -1
          out = 0
        }
        copyLength
      }
    /*
     * Did the read fully succeed in the previous copy or is the buffer
     * empty?
     */
    if (copyLength == count || in == -1) copyLength
    else {
      val bytesCopied = copyLength
      /* Copy bytes from 0 to the number of available bytes */
      val newCopyLength = {
        if (in - out > (count - bytesCopied)) count - bytesCopied
        else in - out
      }
      System.arraycopy(buffer, out, bytes, offset + bytesCopied, newCopyLength)
      out += newCopyLength
      if (out == in) {
        in = -1
        out = 0
      }
      bytesCopied + newCopyLength
    }
  }

  /** Receives a byte and stores it in this stream's {@code buffer}. This method
   *  is called by {@link PipedOutputStream#write(int)}. The least significant
   *  byte of the integer {@code oneByte} is stored at index {@code in} in the
   *  {@code buffer}. <p> This method blocks as long as {@code buffer} is full.
   *
   *  @param oneByte
   *    the byte to store in this pipe.
   *  @throws InterruptedIOException
   *    if the {@code buffer} is full and the thread that has called this method
   *    is interrupted.
   *  @throws IOException
   *    if this stream is closed or the thread that has last read from this
   *    stream is no longer alive.
   */
  @throws[IOException]
  private[io] def receive(oneByte: Int): Unit = synchronized {
    if (buffer == null || isClosed)
      throw new IOException("Closed pipe")
    if (lastReader != null && !lastReader.isAlive())
      throw new IOException("Pipe broken")

    /** Set the last thread to be writing on this PipedInputStream. If
     *  lastWriter dies while someone is waiting to read an IOException of "Pipe
     *  broken" will be thrown in read()
     */
    lastWriter = Thread.currentThread()
    try
      while (buffer != null && out == in) {
        notifyAll()
        wait(1000)
        if (lastReader != null && !lastReader.isAlive())
          throw new IOException("Pipe broken")
      }
    catch {
      case e: InterruptedException => throw new InterruptedIOException
    }
    if (buffer != null) {
      if (in == -1) in = 0
      buffer(in) = oneByte.toByte
      in += 1
      if (in == buffer.length) in = 0
      return
    }
  }
  private[io] def done() = synchronized {
    isClosed = true
    notifyAll()
  }
}
