package scala.scalanative
package util

import java.nio.ByteBuffer

case class NirPrelude(magic: Int,
                      compat: Int,
                      revision: Int,
                      hasEntryPoints: Boolean)

object NirPrelude {
  def readFrom(buffer: ByteBuffer): NirPrelude = {
    val magic    = buffer.getInt()
    val compat   = buffer.getInt()
    val revision = buffer.getInt()

    // indicates whether this NIR file has entry points
    // and thus should be made reachable, no matter
    // what the reachability algorithm does
    // example: reflectively instantiatable classes
    // since: compat = 4, revision = 7
    val hasEntryPoints = {
      import scala.math.Ordering.Implicits._
      if ((compat, revision) < (4, 7))
        false
      else buffer.get() != 0
    }

    NirPrelude(magic, compat, revision, hasEntryPoints)
  }

  def writeTo(buffer: ByteBuffer, prelude: NirPrelude): ByteBuffer = {
    val NirPrelude(magic, compat, revision, hasEntryPoints) = prelude
    buffer.putInt(magic)
    buffer.putInt(compat)
    buffer.putInt(revision)
    buffer.put((if (hasEntryPoints) 1 else 0).toByte)
  }
}
