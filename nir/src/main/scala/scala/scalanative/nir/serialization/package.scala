package scala.scalanative
package nir

import java.io.OutputStream
import java.nio._
import java.nio.file.Path
import java.nio.channels.WritableByteChannel
import scala.scalanative.io.VirtualDirectory

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

  def deserializeBinary(directory: VirtualDirectory, path: Path): Seq[Defn] = {
    val buffer = directory.read(path)
    withBigEndian(buffer) {
      new BinaryDeserializer(
        _,
        new NIRSource(directory.path, path)
      ).deserialize()
    }
  }

  abstract class NirDeserializationException(message: String)
      extends IllegalStateException(message)
  object UnknownFormat
      extends NirDeserializationException("Can't read non-NIR file")
  class IncompatibleVersion(version: Versions.Version, fileName: String)
      extends NirDeserializationException(
        s"Can't read binary-incompatible version of NIR from '$fileName': expected (compat=${Versions.compat}, revision=${Versions.revision}), got (compat=${version.compat}, revision=${version.revision})."
      )

}
