// Ported from Apache Harmony
package java.io

object PipedReader {

  /** The size of the default pipe in characters
   */
  private val PIPE_SIZE = 1024
}
class PipedReader() extends Reader {
  private var data = new Array[Char](PipedReader.PIPE_SIZE)
  private var lastReader: Thread = null
  private var lastWriter: Thread = null
  private var isClosed = false

  /** The index in {@code buffer} where the next character will be written.
   */
  private var in = -1

  /** The index in {@code buffer} where the next character will be read.
   */
  private var out = 0

  /** Indicates if this pipe is connected
   */
  private var isConnected = false

  /** Constructs a new {@code PipedReader} connected to the {@link PipedWriter}
   *  {@code out}. Any data written to the writer can be read from the this
   *  reader.
   *
   *  @param out
   *    the {@code PipedWriter} to connect to.
   *  @throws IOException
   *    if {@code out} is already connected.
   */
  def this(out: PipedWriter) = {
    this()
    connect(out)
  }

  /** Closes this reader. This implementation releases the buffer used for the
   *  pipe and notifies all threads waiting to read or write.
   *
   *  @throws IOException
   *    if an error occurs while closing this reader.
   */
  @throws[IOException]
  override def close() =
    lock.synchronized {
      // No exception thrown if already closed
      if (data != null) {
        // Release buffer to indicate closed.
        data = null
      }

    }

  /** Connects this {@code PipedReader} to a {@link PipedWriter}. Any data
   *  written to the writer becomes readable in this reader.
   *
   *  @param src
   *    the writer to connect to.
   *  @throws IOException
   *    if this reader is closed or already connected, or if {@code src} is
   *    already connected.
   */
  @throws[IOException]
  def connect(src: PipedWriter) = lock.synchronized {
    src.connect(this)
  }

  /** Establishes the connection to the PipedWriter.
   *
   *  @throws IOException
   *    If this Reader is already connected.
   */
  @throws[IOException]
  private[io] def establishConnection() = lock.synchronized {
    if (data == null) throw new IOException("Reader closed")
    if (isConnected) throw new IOException("Reader already connected")
    isConnected = true
  }

  /** Reads a single character from this reader and returns it as an integer
   *  with the two higher-order bytes set to 0. Returns -1 if the end of the
   *  reader has been reached. If there is no data in the pipe, this method
   *  blocks until data is available, the end of the reader is detected or an
   *  exception is thrown. <p> Separate threads should be used to read from a
   *  {@code PipedReader} and to write to the connected {@link PipedWriter}. If
   *  the same thread is used, a deadlock may occur.
   *
   *  @return
   *    the character read or -1 if the end of the reader has been reached.
   *  @throws IOException
   *    if this reader is closed or some other I/O error occurs.
   */
  @throws[IOException]
  override def read(): Int = {
    val carray = new Array[Char](1)
    val result = read(carray, 0, 1)
    if (result != -1) carray(0)
    else result
  }

  /** Reads at most {@code count} characters from this reader and stores them in
   *  the character array {@code buffer} starting at {@code offset}. If there is
   *  no data in the pipe, this method blocks until at least one byte has been
   *  read, the end of the reader is detected or an exception is thrown. <p>
   *  Separate threads should be used to read from a {@code PipedReader} and to
   *  write to the connected {@link PipedWriter}. If the same thread is used, a
   *  deadlock may occur.
   *
   *  @param buffer
   *    the character array in which to store the characters read.
   *  @param offset
   *    the initial position in {@code bytes} to store the characters read from
   *    this reader.
   *  @param count
   *    the maximum number of characters to store in {@code buffer}.
   *  @return
   *    the number of characters read or -1 if the end of the reader has been
   *    reached.
   *  @throws IndexOutOfBoundsException
   *    if {@code offset < 0} or {@code count < 0}, or if {@code offset + count}
   *    is greater than the size of {@code buffer}.
   *  @throws InterruptedIOException
   *    if the thread reading from this reader is interrupted.
   *  @throws IOException
   *    if this reader is closed or not connected to a writer, or if the thread
   *    writing to the connected writer is no longer alive.
   */
  @throws[IOException]
  override def read(buffer: Array[Char], offset: Int, count: Int): Int =
    lock.synchronized {
      if (!isConnected) throw new IOException("Reader not connected")
      if (data == null) throw new IOException("Reader closed")
      // avoid int overflow
      if (offset < 0 || count > buffer.length - offset || count < 0)
        throw new IndexOutOfBoundsException
      if (count == 0) return 0

      /** Set the last thread to be reading on this PipedReader. If lastReader
       *  dies while someone is waiting to write an IOException of "Pipe broken"
       *  will be thrown in receive()
       */
      lastReader = Thread.currentThread()
      try {
        var first = true
        while ({ in == -1 }) { // Are we at end of stream?
          if (isClosed) return -1
          if (!first && lastWriter != null && !lastWriter.isAlive())
            throw new IOException("Broken pipe")
          first = false
          // Notify callers of receive()
          lock.notifyAll()
          lock.wait(1000)
        }
      } catch {
        case e: InterruptedException => throw new InterruptedIOException
      }
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

  /** Indicates whether this reader is ready to be read without blocking.
   *  Returns {@code true} if this reader will not block when {@code read} is
   *  called, {@code false} if unknown or blocking will occur. This
   *  implementation returns {@code true} if the internal buffer contains
   *  characters that can be read.
   *
   *  @return
   *    always {@code false}.
   *  @throws IOException
   *    if this reader is closed or not connected, or if some other I/O error
   *    occurs.
   *  @see
   *    #read()
   *  @see
   *    #read(char[], int, int)
   */
  @throws[IOException]
  override def ready(): Boolean = lock.synchronized {
    if (!isConnected) throw new IOException("Not connected")
    if (data == null) throw new IOException("Reader closed")
    in != -1
  }

  /** Receives a char and stores it into the PipedReader. This called by
   *  PipedWriter.write() when writes occur. <P> If the buffer is full and the
   *  thread sending #receive is interrupted, the InterruptedIOException will be
   *  thrown.
   *
   *  @param oneChar
   *    the char to store into the pipe.
   *
   *  @throws IOException
   *    If the stream is already closed or another IOException occurs.
   */
  @throws[IOException]
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

  /** Receives a char array and stores it into the PipedReader. This called by
   *  PipedWriter.write() when writes occur. <P> If the buffer is full and the
   *  thread sending #receive is interrupted, the InterruptedIOException will be
   *  thrown.
   *
   *  @param chars
   *    the char array to store into the pipe.
   *  @param offset
   *    offset to start reading from
   *  @param count
   *    total characters to read
   *
   *  @throws IOException
   *    If the stream is already closed or another IOException occurs.
   */
  @throws[IOException]
  private[io] def receive(chars: Array[Char], _offset: Int, _count: Int): Unit =
    lock.synchronized {
      var offset = _offset
      var count = _count
      if (data == null) throw new IOException("Reader closed")
      if (lastReader != null && !lastReader.isAlive())
        throw new IOException("Broken pipe")

      /** Set the last thread to be writing on this PipedWriter. If lastWriter
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
  private[io] def flush() = lock.synchronized { lock.notifyAll() }
}
