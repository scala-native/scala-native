package scala.scalanative
package nir

import java.nio._
import java.nio.file.{StandardOpenOption => OpenOpt, _}
import java.nio.channels._

package object serialization {
  private val default = ByteBuffer.allocateDirect(128 * 1024 * 1024)

  def serializeText(defns: Seq[Defn], buffer: ByteBuffer): Unit =
    buffer.put(Shows.showDefns(defns).toString.getBytes)

  def serializeBinary(defns: Seq[Defn], buffer: ByteBuffer): Unit =
    (new BinarySerializer(buffer)).serialize(defns)

  def serializeFile(serialize: (Seq[Defn], ByteBuffer) => Unit,
                    defns: Seq[Defn],
                    path: String,
                    buffer: ByteBuffer = default): Unit = {
    buffer.clear
    serialize(defns, buffer)
    buffer.flip
    val channel = FileChannel.open(Paths.get(path),
                                   OpenOpt.CREATE,
                                   OpenOpt.WRITE,
                                   OpenOpt.TRUNCATE_EXISTING)
    try channel.write(buffer) finally channel.close
  }

  def serializeTextFile(defns: Seq[Defn],
                        path: String,
                        buffer: ByteBuffer = default): Unit =
    serializeFile(serializeText, defns, path, buffer)

  def serializeBinaryFile(defns: Seq[Defn],
                          path: String,
                          buffer: ByteBuffer = default): Unit =
    serializeFile(serializeBinary, defns, path, buffer)

  def deserializeBinaryFile(path: String): BinaryDeserializer =
    new BinaryDeserializer({
      val bytes  = Files.readAllBytes(Paths.get(path))
      val buffer = ByteBuffer.wrap(bytes)
      buffer
    })
}
