package scala.scalanative.nir
package serialization

import java.nio.ByteBuffer
import java.io.DataOutputStream

case class Prelude(
    magic: Int,
    compat: Int,
    revision: Int,
    sections: Prelude.Offsets,
    hasEntryPoints: Boolean
)

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

  def readFrom(buffer: ByteBuffer, fileName: => String): Prelude = {
    buffer.position(0)
    val magic = buffer.getInt()
    val compat = buffer.getInt()
    val revision = buffer.getInt()
    assert(magic == Versions.magic, "Can't read non-NIR file.")
    assert(
      compat == Versions.compat && revision <= Versions.revision,
      "Can't read binary-incompatible version of NIR from '" + fileName +
        "' (expected compat=" + Versions.compat + ", got " + compat +
        "; expected revision=" + Versions.revision + ", got " + revision + ")."
    )

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

    Prelude(magic, compat, revision, offsets, hasEntryPoints)
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
