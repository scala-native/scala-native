package scala.scalanative
package nir

import java.io.OutputStream
import java.nio._
import java.nio.channels.WritableByteChannel
import java.nio.file.Path

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
        incompatibleVersionMessage(version, fileName)
      )

  private def incompatibleVersionMessage(
      version: Versions.Version,
      fileName: String
  ): String = {
    val explanation =
      if (version.compat < Versions.compat)
        "This library uses an older, backward-incompatible NIR format. " +
          "Use a release of this library built for your Scala Native toolchain, " +
          "or rebuild the library with your current Scala Native version."
      else if (version.compat > Versions.compat)
        "This library uses a newer, binary-incompatible NIR format. " +
          "Upgrade Scala Native to a toolchain supporting this format and use compatible versions of your dependencies, " +
          "or use an earlier release of this library built for your Scala Native toolchain."
      else
        "This library uses a newer NIR revision that is not forward compatible with your Scala Native toolchain. " +
          "Upgrade Scala Native, or use an earlier release of this library built with a compatible Scala Native version."

    s"Can't read binary-incompatible version of NIR from '$fileName' using Scala Native ${Versions.current}. " +
      s"$explanation Supported: (compat=${Versions.compat}, revision<=${Versions.revision}); " +
      s"found: (compat=${version.compat}, revision=${version.revision})."
  }

}
