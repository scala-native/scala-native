package scala.scalanative.javalibintf

import java.nio.ByteBuffer
import java.nio.charset.Charset

object String {

  def fromByteBuffer(data: ByteBuffer, encoding: Charset): java.lang._String =
    new java.lang._String(data, encoding)

}
