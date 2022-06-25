// Ported from Apache HArmony
package java.io

class PipedOutputStream() extends OutputStream {

  /** The destination PipedInputStream
   */
  private var dest: PipedInputStream = null

  /** Constructs a new {@code PipedOutputStream} connected to the {@link
   *  PipedInputStream} {@code dest}. Any data written to this stream can be
   *  read from the target stream.
   *
   *  @param dest
   *    the piped input stream to connect to.
   *  @throws IOException
   *    if this stream or {@code dest} are already connected.
   */
  def this(dest: PipedInputStream) = {
    this()
    connect(dest)
  }

  /** Closes this stream. If this stream is connected to an input stream, the
   *  input stream is closed and the pipe is disconnected.
   *
   *  @throws IOException
   *    if an error occurs while closing this stream.
   */
  @throws[IOException]
  override def close() = { // Is the pipe connected?
    if (dest != null) {
      dest.done()
      dest = null
    }
  }

  /** Connects this stream to a {@link PipedInputStream}. Any data written to
   *  this output stream becomes readable in the input stream.
   *
   *  @param stream
   *    the destination input stream.
   *  @throws IOException
   *    if either stream is already connected.
   */
  @throws[IOException]
  def connect(stream: PipedInputStream) = {
    if (null == stream) throw new NullPointerException
    if (this.dest != null) throw new IOException("Already connected")
    stream.synchronized {
      if (stream.isConnected)
        throw new IOException("Target stream is already connected")
      stream.buffer = new Array[Byte](stream.pipeSize)
      stream.isConnected = true
      this.dest = stream
    }
  }

  /** Notifies the readers of this {@link PipedInputStream} that bytes can be
   *  read. This method does nothing if this stream is not connected.
   *
   *  @throws IOException
   *    if an I/O error occurs while flushing this stream.
   */
  @throws[IOException]
  override def flush() =
    if (dest != null) {
      dest.synchronized { dest.notifyAll() }
    }

  /** Writes {@code count} bytes from the byte array {@code buffer} starting at
   *  {@code offset} to this stream. The written data can then be read from the
   *  connected input stream. <p> Separate threads should be used to write to a
   *  {@code PipedOutputStream} and to read from the connected {@link
   *  PipedInputStream}. If the same thread is used, a deadlock may occur.
   *
   *  @param buffer
   *    the buffer to write.
   *  @param offset
   *    the index of the first byte in {@code buffer} to write.
   *  @param count
   *    the number of bytes from {@code buffer} to write to this stream.
   *  @throws IndexOutOfBoundsException
   *    if {@code offset < 0} or {@code count < 0}, or if {@code offset + count}
   *    is bigger than the length of {@code buffer}.
   *  @throws InterruptedIOException
   *    if the pipe is full and the current thread is interrupted waiting for
   *    space to write data. This case is not currently handled correctly.
   *  @throws IOException
   *    if this stream is not connected, if the target stream is closed or if
   *    the thread reading from the target stream is no longer alive. This case
   *    is currently not handled correctly.
   */
  @throws[IOException]
  override def write(buffer: Array[Byte], offset: Int, count: Int) =
    super.write(buffer, offset, count)

  /** Writes a single byte to this stream. Only the least significant byte of
   *  the integer {@code oneByte} is written. The written byte can then be read
   *  from the connected input stream. <p> Separate threads should be used to
   *  write to a {@code PipedOutputStream} and to read from the connected {@link
   *  PipedInputStream}. If the same thread is used, a deadlock may occur.
   *
   *  @param oneByte
   *    the byte to write.
   *  @throws InterruptedIOException
   *    if the pipe is full and the current thread is interrupted waiting for
   *    space to write data. This case is not currently handled correctly.
   *  @throws IOException
   *    if this stream is not connected, if the target stream is closed or if
   *    the thread reading from the target stream is no longer alive. This case
   *    is currently not handled correctly.
   */
  @throws[IOException]
  override def write(oneByte: Int) = {
    if (dest == null) throw new IOException("Not connected")
    dest.receive(oneByte)
  }
}
