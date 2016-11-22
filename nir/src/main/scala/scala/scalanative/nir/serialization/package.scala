package scala.scalanative
package nir

import java.nio._
import java.nio.file.{StandardOpenOption => OpenOpt, _}
import java.nio.channels._

package object serialization {
  def serializeText(defns: Seq[Defn], buffer: ByteBuffer): Unit =
    buffer.put(Shows.showDefns(defns).toString.getBytes)

  def serializeBinary(defns: Seq[Defn], buffer: ByteBuffer): Unit =
    (new BinarySerializer(buffer)).serialize(defns)

  def deserializeBinary(buffer: ByteBuffer): BinaryDeserializer =
    new BinaryDeserializer(buffer)
}
