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

class PipedOutputStream() extends OutputStream {
  private var dest: PipedInputStream = _

  def this(dest: PipedInputStream) = {
    this()
    connect(dest)
  }

  override def close() = { // Is the pipe connected?
    if (dest != null) {
      dest.done()
      dest = null
    }
  }

  def connect(stream: PipedInputStream) = synchronized {
    if (null == stream) throw new NullPointerException
    if (this.dest != null) throw new IOException("Already connected")
    stream.synchronized {
      if (stream.isConnected)
        throw new IOException("Target stream is already connected")
      stream.buffer = new Array[Byte](PipedInputStream.PIPE_SIZE)
      stream.isConnected = true
      this.dest = stream
    }
  }

  override def flush() = synchronized {
    if (dest != null) {
      dest.synchronized { dest.notifyAll() }
    }
  }

  override def write(buffer: Array[Byte], offset: Int, count: Int) =
    super.write(buffer, offset, count)

  override def write(oneByte: Int) = {
    if (dest == null) throw new IOException("Not connected")
    dest.receive(oneByte)
  }
}
