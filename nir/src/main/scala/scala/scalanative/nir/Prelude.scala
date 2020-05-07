package scala.scalanative
package nir

import java.nio.ByteBuffer

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
    // since: compat = 4, revision = 7
    val hasEntryPoints =
      if (revision < 7)
        false
      else
        buffer.get() != 0

    Prelude(magic, compat, revision, hasEntryPoints)
  }

  def writeTo(buffer: ByteBuffer, prelude: Prelude): ByteBuffer = {
    val Prelude(magic, compat, revision, hasEntryPoints) = prelude
    buffer.putInt(magic)
    buffer.putInt(compat)
    buffer.putInt(revision)
    buffer.put((if (hasEntryPoints) 1 else 0).toByte)
  }
}
