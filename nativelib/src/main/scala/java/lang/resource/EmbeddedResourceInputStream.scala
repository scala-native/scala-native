package java.lang.resource

import java.io.InputStream

private[lang] class EmbeddedResourceInputStream(resourceId: Int)
    extends InputStream {

  // Position in Base64 encoded bytes
  var position: Int = 0
  var leftSeq = Seq[Byte]()
  val size = EmbeddedResourceReader.getContentLength(resourceId)

  var markPosition: Int = 0
  var markSeq = Seq[Byte]()
  var markReadLimit: Int = 0

  override def close(): Unit = ()

  override def read(): Int = {
    if (position >= size) {
      -1
    } else {
      val res = EmbeddedResourceHelper.getContentPtr(resourceId)(position)
      position += 1
      java.lang.Byte.toUnsignedInt(res)
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
}
