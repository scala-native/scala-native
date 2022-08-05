package java.io

class ByteArrayInputStream(
    protected val buf: Array[Byte],
    offset: Int,
    length: Int
) extends InputStream {

  protected val count: Int = offset + length
  protected var mark: Int = offset
  protected var pos: Int = offset

  def this(buf: Array[Byte]) = this(buf, 0, buf.length)

  override def read(): Int = {
    if (pos >= count)
      -1
    else {
      val res = buf(pos) & 0xff // convert to unsigned int
      pos += 1
      res
    }
  }

  override def read(b: Array[Byte], off: Int, reqLen: Int): Int = {
    if (off < 0 || reqLen < 0 || reqLen > b.length - off)
      throw new IndexOutOfBoundsException

    val len = Math.min(reqLen, count - pos)

    if (reqLen == 0)
      0 // 0 requested, 0 returned
    else if (len == 0)
      -1 // nothing to read at all
    else {
      System.arraycopy(buf, pos, b, off, len)
      pos += len
      len
    }
  }

  def readNBytes(b: Array[Byte], off: Int, len: Int): Int = {
    val n = read(b, off, len)
    if (n == -1) 0 else n
  }

  def readNBytes(len: Int): Array[Byte] = {
    if (len < 0) throw new IllegalArgumentException("can't read negative bytes")
    val result = Array.ofDim[Byte](len)
    read(result, 0, len)
    result
  }

  override def skip(n: Long): Long = {
    val k = Math.max(0, Math.min(n, count - pos))
    pos += k.toInt
    k.toLong
  }

  override def available(): Int = count - pos

  override def markSupported(): Boolean = true

  override def mark(readlimit: Int): Unit =
    mark = pos

  override def reset(): Unit =
    pos = mark

  override def close(): Unit = ()
}
