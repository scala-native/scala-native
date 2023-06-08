package scala.scalanative
package nir

import java.io.OutputStream
import java.nio._
import java.nio.channels.WritableByteChannel

package object serialization {
  @inline
  private def withBigEndian[T](buf: ByteBuffer)(body: ByteBuffer => T): T = {
    val o = buf.order()
    buf.order(ByteOrder.BIG_ENDIAN)
    try body(buf)
    finally buf.order(o)
  }

  def serializeBinary(defns: Seq[Defn], channel: WritableByteChannel): Unit = {
    new BinarySerializer(channel).serialize(defns)
  }

  def deserializeBinary(buffer: ByteBuffer, fileName: String): Seq[Defn] =
    withBigEndian(buffer) {
      new BinaryDeserializer(_, fileName).deserialize()
    }
}
