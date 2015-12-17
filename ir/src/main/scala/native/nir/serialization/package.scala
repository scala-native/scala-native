package native
package nir

import java.nio._
import java.nio.file._
import java.nio.file.{StandardOpenOption => OpenOpt}
import java.nio.channels._

package object serialization {
  private val defaultBuffer = ByteBuffer.allocateDirect(128 * 1024 * 1024)

  def serializeText(defns: Seq[Defn], buffer: ByteBuffer): Unit =
    buffer.put(Shows.showDefns(defns).toString.getBytes)

  def serializeBinary(defns: Seq[Defn], buffer: ByteBuffer): Unit =
    (new BinarySerializer(buffer)).putDefns(defns)

  def serializeFile[T](serialize: (T, ByteBuffer) => Unit, input: T, path: String,
                       buffer: ByteBuffer = defaultBuffer): Unit = {
    buffer.clear
    serialize(input, buffer)
    buffer.flip
    val channel =
      FileChannel.open(Paths.get(path), OpenOpt.CREATE, OpenOpt.WRITE, OpenOpt.TRUNCATE_EXISTING)
    try channel.write(buffer)
    finally channel.close
  }

  def serializeTextFile(defns: Seq[Defn], path: String,
                        buffer: ByteBuffer = defaultBuffer): Unit =
    serializeFile(serializeText, defns, path, buffer)

  def serializeIRFile(defns: Seq[Defn], path: String,
                      buffer: ByteBuffer = defaultBuffer): Unit =
    serializeFile(serializeBinary, defns, path, buffer)
}
