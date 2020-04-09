package scala.scalanative
package nir

import java.nio._

package object serialization {
  def serializeText(defns: Seq[Defn], buffer: ByteBuffer): Unit = {
    val builder = Show.newBuilder
    builder.defns_(defns)
    buffer.put(builder.toString.getBytes)
  }

  def serializeBinary(defns: Seq[Defn], buffer: ByteBuffer): Unit =
    (new BinarySerializer(buffer)).serialize(defns)

  def deserializeBinary(buffer: ByteBuffer): Seq[Defn] =
    (new BinaryDeserializer(buffer)).deserialize()
}
