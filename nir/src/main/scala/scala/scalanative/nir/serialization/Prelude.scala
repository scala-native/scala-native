package scala.scalanative.nir
package serialization

import java.nio.ByteBuffer
import java.io.DataOutputStream
import Prelude.Offsets
import Versions.Version

case class Prelude(
    magic: Int,
    compat: Int,
    revision: Int,
    sections: Offsets,
    hasEntryPoints: Boolean
) {
  def this(version: Version, sections: Offsets, hasEntryPoints: Boolean) = this(
    magic = Versions.magic,
    compat = version.compat,
    revision = version.revision,
    sections = sections,
    hasEntryPoints = hasEntryPoints
  )

  private[scalanative] val requiresParamTypeAdaption = revision < 11
}

object Prelude {
  // { magic: int, version: int[2], offsets: int[8], bool }
  val length = 45
  case class Offsets(
      offsets: Int,
      strings: Int,
      positions: Int,
      globals: Int,
      types: Int,
      defns: Int,
      vals: Int,
      insts: Int
  )

  private def readNIRVersion(
      buffer: ByteBuffer,
      fileName: => String
  ): Either[NirDeserializationException, Version] = {
    buffer.position(0)
    if (Versions.magic != buffer.getInt()) Left(UnknownFormat)
    else {
      val compat = buffer.getInt()
      val revision = buffer.getInt()
      val version = Version(compat, revision)
      if (compat == Versions.compat && revision <= Versions.revision)
        Right(version)
      else
        Left(new IncompatibleVersion(version, fileName))
    }
  }

  def readFrom(buffer: ByteBuffer, fileName: => String): Prelude =
    tryReadFrom(buffer, fileName) match {
      case Left(exception) => throw exception
      case Right(prelude)  => prelude
    }

  def tryReadFrom(
      buffer: ByteBuffer,
      fileName: => String
  ): Either[NirDeserializationException, Prelude] = {
    readNIRVersion(buffer, fileName).map { nirVersion =>
      val offsets = Offsets(
        offsets = buffer.getInt(),
        strings = buffer.getInt(),
        positions = buffer.getInt(),
        globals = buffer.getInt(),
        types = buffer.getInt(),
        defns = buffer.getInt(),
        vals = buffer.getInt(),
        insts = buffer.getInt()
      )

      // indicates whether this NIR file has entry points
      // and thus should be made reachable, no matter
      // what the reachability algorithm does
      // example: reflectively instantiatable classes
      val hasEntryPoints = buffer.get() != 0

      new Prelude(nirVersion, offsets, hasEntryPoints)
    }
  }

  def writeTo(out: DataOutputStream, prelude: Prelude): DataOutputStream = {
    val Prelude(magic, compat, revision, offsets, hasEntryPoints) = prelude
    out.writeInt(magic)
    out.writeInt(compat)
    out.writeInt(revision)

    out.writeInt(offsets.offsets)
    out.writeInt(offsets.strings)
    out.writeInt(offsets.positions)
    out.writeInt(offsets.globals)
    out.writeInt(offsets.types)
    out.writeInt(offsets.defns)
    out.writeInt(offsets.vals)
    out.writeInt(offsets.insts)

    out.writeBoolean(hasEntryPoints)
    out
  }
}
