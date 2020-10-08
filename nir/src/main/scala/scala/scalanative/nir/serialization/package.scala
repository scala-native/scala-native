package scala.scalanative
package nir

import java.io.OutputStream
import java.nio._

package object serialization {
  def serializeText(defns: Seq[Defn], buffer: ByteBuffer): Unit = {
    val builder = Show.newBuilder
    builder.defns_(defns)
    buffer.put(builder.toString.getBytes)
  }

  @inline
  private def withBigEndian[T](buf: ByteBuffer)(body: ByteBuffer => T): T = {
    val o = buf.order()
    buf.order(ByteOrder.BIG_ENDIAN)
    try body(buf)
    finally buf.order(o)
  }

  def serializeBinary(defns: Seq[Defn], out: OutputStream): Unit =
    new BinarySerializer().serialize(defns, out)

  def deserializeBinary(buffer: ByteBuffer): Seq[Defn] =
    withBigEndian(buffer) {
      new BinaryDeserializer(_).deserialize()
    }
}
