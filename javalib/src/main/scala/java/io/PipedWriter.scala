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

class PipedWriter() extends Writer() {
  private var dest: PipedReader = _

  private var closed = false

  /** Connects this {@code PipedWriter} to a {@link PipedReader}. Any data
   *  written to this writer becomes readable in the reader.
   *
   *  @param stream
   *    the reader to connect to.
   *  @throws IOException
   *    if this writer is closed or already connected, or if {@code stream} is
   *    already connected.
   */
  def this(dest: PipedReader) = {
    this()
    this.lock = dest
    connect(dest)
  }

  /** Closes this writer. If a {@link PipedReader} is connected to this writer,
   *  it is closed as well and the pipe is disconnected. Any data buffered in
   *  the reader can still be read.
   *
   *  @throws IOException
   *    if an error occurs while closing this writer.
   */
  @throws[IOException]
  override def close() = lock.synchronized {
    // Is the pipe connected?
    if (dest != null) {
      dest.done()
      dest = null
    }
    closed = true

  }

  /** Connects this {@code PipedWriter} to a {@link PipedReader}. Any data
   *  written to this writer becomes readable in the reader.
   *
   *  @param stream
   *    the reader to connect to.
   *  @throws IOException
   *    if this writer is closed or already connected, or if {@code stream} is
   *    already connected.
   */
  @throws[IOException]
  def connect(stream: PipedReader) = lock.synchronized {
    if (this.dest != null)
      throw new IOException("Already connected")
    if (closed) throw new IOException("Writer closed")
    stream.establishConnection()
    this.dest = stream

  }

  /** Notifies the readers of this {@code PipedReader} that characters can be
   *  read. This method does nothing if this Writer is not connected.
   *
   *  @throws IOException
   *    if an I/O error occurs while flushing this writer.
   */
  @throws[IOException]
  override def flush() = if (dest != null) dest.flush()

  /** Writes {@code count} characters from the character array {@code buffer}
   *  starting at offset {@code index} to this writer. The written data can then
   *  be read from the connected {@link PipedReader} instance. <p> Separate
   *  threads should be used to write to a {@code PipedWriter} and to read from
   *  the connected {@code PipedReader}. If the same thread is used, a deadlock
   *  may occur.
   *
   *  @param buffer
   *    the buffer to write.
   *  @param offset
   *    the index of the first character in {@code buffer} to write.
   *  @param count
   *    the number of characters from {@code buffer} to write to this writer.
   *  @throws IndexOutOfBoundsException
   *    if {@code offset < 0} or {@code count < 0}, or if {@code offset + count}
   *    is bigger than the length of {@code buffer}.
   *  @throws InterruptedIOException
   *    if the pipe is full and the current thread is interrupted waiting for
   *    space to write data. This case is not currently handled correctly.
   *  @throws IOException
   *    if this writer is closed or not connected, if the target reader is
   *    closed or if the thread reading from the target reader is no longer
   *    alive. This case is currently not handled correctly.
   *  @throws NullPointerException
   *    if {@code buffer} is {@code null}.
   */
  @throws[IOException]
  override def write(buffer: Array[Char], offset: Int, count: Int) =
    lock.synchronized {
      if (closed) throw new IOException("Writer closed")
      if (dest == null) throw new IOException("Not connected")
      if (buffer == null)
        throw new NullPointerException("Buffer not set")
      // avoid int overflow
      if (offset < 0 || offset > buffer.length || count < 0 || count > buffer.length - offset)
        throw new IndexOutOfBoundsException()
      dest.receive(buffer, offset, count)
    }

  /** Writes a single character {@code c} to this writer. This character can
   *  then be read from the connected {@link PipedReader} instance. <p> Separate
   *  threads should be used to write to a {@code PipedWriter} and to read from
   *  the connected {@code PipedReader}. If the same thread is used, a deadlock
   *  may occur.
   *
   *  @param c
   *    the character to write.
   *  @throws InterruptedIOException
   *    if the pipe is full and the current thread is interrupted waiting for
   *    space to write data. This case is not currently handled correctly.
   *  @throws IOException
   *    if this writer is closed or not connected, if the target reader is
   *    closed or if the thread reading from the target reader is no longer
   *    alive. This case is currently not handled correctly.
   */
  @throws[IOException]
  override def write(c: Int) = lock.synchronized {
    if (closed) throw new IOException("Writer closed")
    if (dest == null) throw new IOException("Not connected")
    dest.receive(c.toChar)

  }
}
