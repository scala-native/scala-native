package scala.scalanative
package nir

import java.nio.ByteBuffer
import java.io.DataOutputStream

case class Prelude(magic: Int,
                   compat: Int,
                   revision: Int,
                   hasEntryPoints: Boolean)

object Prelude {
  val length = 13

  def readFrom(buffer: ByteBuffer): Prelude = {
    val magic    = buffer.getInt()
    val compat   = buffer.getInt()
    val revision = buffer.getInt()

    assert(magic == Versions.magic, "Can't read non-NIR file.")
    assert(compat == Versions.compat && revision <= Versions.revision,
           "Can't read binary-incompatible version of NIR.")

    // indicates whether this NIR file has entry points
    // and thus should be made reachable, no matter
    // what the reachability algorithm does
    // example: reflectively instantiatable classes
    val hasEntryPoints = buffer.get() != 0

    Prelude(magic, compat, revision, hasEntryPoints)
  }

  def writeTo(out: DataOutputStream, prelude: Prelude): DataOutputStream = {
    val Prelude(magic, compat, revision, hasEntryPoints) = prelude
    out.writeInt(magic)
    out.writeInt(compat)
    out.writeInt(revision)
    out.writeBoolean(hasEntryPoints)
    out
  }
}
