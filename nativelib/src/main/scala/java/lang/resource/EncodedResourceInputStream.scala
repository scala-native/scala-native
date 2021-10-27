package java.lang.resource

import java.io.InputStream
import java.util.Base64

private[lang] class EncodedResourceInputStream(resourceId: Int)
    extends InputStream {

  // Position in Base64 encoded bytes
  var position: Long = 0
  var leftSeq = Seq[Byte]()
  val size = EmbeddedResourceReader.getContentLength(resourceId).toLong

  var markPosition: Long = 0
  var markSeq = Seq[Byte]()
  var markReadLimit: Long = 0

  override def close(): Unit = ()

  override def read(): Int = {
    if (position == size) {
      -1
    } else {
      val res = EmbeddedResourceHelper.getContentByte(resourceId, position)
      position += 1
      res
    }
  }

  override def mark(readLimit: Int): Unit = {
    markPosition = position
    markSeq = leftSeq
    markReadLimit = readLimit
  }

  override def markSupported(): Boolean = true

  override def reset(): Unit = {
    position = markPosition
    leftSeq = markSeq
    markReadLimit = 0
  }

  private def invalidateMark(): Unit = {
    markPosition = 0
    markSeq = Seq()
    markReadLimit = 0
  }
}
