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

  def this(dest: PipedReader) = {
    this()
    this.lock = dest
    connect(dest)
  }

  override def close() = lock.synchronized {
    // Is the pipe connected?
    if (dest != null) {
      dest.done()
      dest = null
    }
    closed = true

  }

  def connect(stream: PipedReader) = lock.synchronized {
    if (this.dest != null)
      throw new IOException("Already connected")
    if (closed) throw new IOException("Writer closed")
    stream.establishConnection()
    this.dest = stream

  }

  override def flush() = if (dest != null) dest.flush()

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

  override def write(c: Int) = lock.synchronized {
    if (closed) throw new IOException("Writer closed")
    if (dest == null) throw new IOException("Not connected")
    dest.receive(c.toChar)

  }
}
