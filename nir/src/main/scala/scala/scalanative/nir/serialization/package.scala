package scala.scalanative
package nir

import java.io.OutputStream
import java.nio._
import java.nio.channels.ByteChannel

package object serialization {
  @inline
  private def withBigEndian[T](buf: ByteBuffer)(body: ByteBuffer => T): T = {
    val o = buf.order()
    buf.order(ByteOrder.BIG_ENDIAN)
    try body(buf)
    finally buf.order(o)
  }

  def serializeBinary(defns: Seq[Defn], channel: ByteChannel): Unit = {
    val writer = new NIRWriter()
    writer.put(defns)
    writer.write(channel)
  } 
  def serializeBinary(defns: Seq[Defn], out: OutputStream): Unit =
    ???
    // new BinarySerializer().serialize(defns, out)

  def deserializeBinary(buffer: ByteBuffer, bufferName: String): Seq[Defn] =
    withBigEndian(buffer) {
      new BinaryReader(_, bufferName).deserialize()
    }
}
