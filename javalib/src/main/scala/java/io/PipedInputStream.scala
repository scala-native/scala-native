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

class PipedInputStream() extends InputStream {

  protected var in: Int = -1
  protected var out: Int = 0
  private[io] var buffer: Array[Byte] = _
  private[io] var isConnected = false // Modified by PipedOutputStream
  private var lastReader: Thread = _
  private var isClosed = false
  private var lastWriter: Thread = _

  def this(out: PipedOutputStream) = {
    this()
    connect(out)
  }

  override def available(): Int = synchronized {
    if (buffer == null || in == -1) 0
    else if (in <= out) buffer.length - out + in
    else in - out
  }

  override def close() = synchronized {
    // No exception thrown if already closed */
    // Release buffer to indicate closed.
    if (buffer != null) buffer = null
  }

  def connect(src: PipedOutputStream) = src.connect(this)

  override def read(): Int = synchronized {
    if (!isConnected) throw new IOException("Not connected")
    if (buffer == null) throw new IOException("InputStream is closed")
    if (isClosed && in == -1) {
      // write end closed and no more need to read
      return -1
    }
    if (lastWriter != null && !lastWriter.isAlive() && (in < 0))
      throw new IOException("Write end dead")

    /* Set the last thread to be reading on this PipedInputStream. If
     * lastReader dies while someone is waiting to write an IOException of
     * "Pipe broken" will be thrown in receive()
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

  override def read(bytes: Array[Byte], offset: Int, count: Int): Int =
    synchronized {
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
        System.arraycopy(
          buffer,
          out,
          bytes,
          offset + bytesCopied,
          newCopyLength
        )
        out += newCopyLength
        if (out == in) {
          in = -1
          out = 0
        }
        bytesCopied + newCopyLength
      }
    }

  private[io] def receive(oneByte: Int): Unit = synchronized {
    if (buffer == null || isClosed)
      throw new IOException("Closed pipe")
    if (lastReader != null && !lastReader.isAlive())
      throw new IOException("Pipe broken")

    /* Set the last thread to be writing on this PipedInputStream. If
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
