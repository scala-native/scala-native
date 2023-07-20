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

object PipedReader {
  private val PIPE_SIZE = 1024
}
class PipedReader() extends Reader {
  private var data = new Array[Char](PipedReader.PIPE_SIZE)
  private var lastReader: Thread = _
  private var lastWriter: Thread = _
  private var isClosed = false
  private var isConnected = false
  private var in = -1
  private var out = 0

  def this(out: PipedWriter) = {
    this()
    connect(out)
  }

  override def close() =
    lock.synchronized {
      // No exception thrown if already closed
      if (data != null) {
        // Release buffer to indicate closed.
        data = null
      }

    }

  def connect(src: PipedWriter) = lock.synchronized {
    src.connect(this)
  }

  private[io] def establishConnection() = lock.synchronized {
    if (data == null) throw new IOException("Reader closed")
    if (isConnected) throw new IOException("Reader already connected")
    isConnected = true
  }

  override def read(): Int = {
    val carray = new Array[Char](1)
    val result = read(carray, 0, 1)
    if (result != -1) carray(0)
    else result
  }

  override def read(buffer: Array[Char], offset: Int, count: Int): Int =
    lock.synchronized {
      if (!isConnected) throw new IOException("Reader not connected")
      if (data == null) throw new IOException("Reader closed")
      // avoid int overflow
      if (offset < 0 || count > buffer.length - offset || count < 0)
        throw new IndexOutOfBoundsException
      if (count == 0) return 0

      /* Set the last thread to be reading on this PipedReader. If lastReader
       *  dies while someone is waiting to write an IOException of "Pipe broken"
       *  will be thrown in receive()
       */
      lastReader = Thread.currentThread()
      var wasClosed = false
      try {
        var first = true
        while (!wasClosed && in == -1) { // Are we at end of stream?
          if (isClosed) wasClosed = true
          else {
            if (!first && lastWriter != null && !lastWriter.isAlive())
              throw new IOException("Broken pipe")
            first = false
            // Notify callers of receive()
            lock.notifyAll()
            lock.wait(1000)
          }
        }
      } catch {
        case e: InterruptedException => throw new InterruptedIOException
      }
      if (wasClosed) return -1
      /* Copy chars from out to end of buffer first */
      val copyLength =
        if (out < in) 0
        else {
          val copyLength =
            if (count > data.length - out) data.length - out
            else count
          System.arraycopy(data, out, buffer, offset, copyLength)
          out += copyLength
          if (out == data.length) out = 0
          if (out == in) { // empty buffer
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
        val charsCopied = copyLength
        /* Copy bytes from 0 to the number of available bytes */
        val newCopyLength =
          if (in - out > count - copyLength) count - copyLength
          else in - out
        System.arraycopy(data, out, buffer, offset + charsCopied, newCopyLength)
        out += newCopyLength
        if (out == in) {
          in = -1
          out = 0
        }
        charsCopied + newCopyLength
      }

    }

  override def ready(): Boolean = lock.synchronized {
    if (!isConnected) throw new IOException("Not connected")
    if (data == null) throw new IOException("Reader closed")
    in != -1
  }

  private[io] def receive(oneChar: Char): Unit = lock.synchronized {
    if (data == null) throw new IOException("Closed stream")
    if (lastReader != null && !lastReader.isAlive())
      throw new IOException("Broken pipe")
    /*
     * Set the last thread to be writing on this PipedWriter. If
     * lastWriter dies while someone is waiting to read an IOException
     * of "Pipe broken" will be thrown in read()
     */
    lastWriter = Thread.currentThread()
    try
      while (data != null && out == in) {
        lock.notifyAll()
        wait(1000)
        if (lastReader != null && !lastReader.isAlive())
          throw new IOException("Broken pipe")
      }
    catch {
      case e: InterruptedException => throw new InterruptedIOException
    }
    if (data != null) {
      if (in == -1) in = 0
      data(in) = oneChar
      in += 1
      if (in == data.length) in = 0
    }

  }

  private[io] def receive(chars: Array[Char], _offset: Int, _count: Int): Unit =
    lock.synchronized {
      var offset = _offset
      var count = _count
      if (data == null) throw new IOException("Reader closed")
      if (lastReader != null && !lastReader.isAlive())
        throw new IOException("Broken pipe")

      /* Set the last thread to be writing on this PipedWriter. If lastWriter
       *  dies while someone is waiting to read an IOException of "Pipe broken"
       *  will be thrown in read()
       */
      lastWriter = Thread.currentThread()
      while (count > 0) {
        try
          while (data != null && out == in) {
            lock.notifyAll()
            wait(1000)
            if (lastReader != null && !lastReader.isAlive())
              throw new IOException("Broken pipe")
          }
        catch {
          case e: InterruptedException => throw new InterruptedIOException
        }
        if (data == null) return ()
        if (in == -1) in = 0
        if (in >= out) {
          var length = data.length - in
          if (count < length) length = count
          System.arraycopy(chars, offset, data, in, length)
          offset += length
          count -= length
          in += length
          if (in == data.length) in = 0
        }
        if (count > 0 && in != out) {
          var length = out - in
          if (count < length) length = count
          System.arraycopy(chars, offset, data, in, length)
          offset += length
          count -= length
          in += length
        }
      }
    }

  private[io] def done() = lock.synchronized {
    isClosed = true
    lock.notifyAll()
  }

  private[io] def flush() = lock.synchronized {
    lock.notifyAll()
  }
}
