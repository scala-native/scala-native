package scala.scalanative.nir.serialization

// ported from Scala.js

/** A ByteArrayOutput stream that allows to jump back to a given
 *  position and complete some bytes. Methods must be called in the
 *  following one of two orders:
 *  - [[markJump]]
 *  - [[jumpBack]]
 *  - [[continue]]
 *  or
 *  - [[jumpTo(pos)]]
 *  - [[continue]]
 */
private[serialization] class JumpBackByteArrayOutputStream
    extends java.io.ByteArrayOutputStream {
  protected var jumpBackPos: Int = -1
  protected var headPos: Int     = -1

  /** Marks the current location for a jumpback */
  def markJump(): Unit = {
    assert(jumpBackPos == -1)
    assert(headPos == -1)
    jumpBackPos = currentPosition()
  }

  /** Jumps back to the mark. Returns the number of bytes jumped */
  def jumpBack(): Int = {
    assert(jumpBackPos >= 0)
    assert(headPos == -1)
    val jumped = count - jumpBackPos
    headPos = currentPosition()
    count = jumpBackPos
    jumpBackPos = -1
    jumped
  }

  /** Jumps to passed position. Returns the number of bytes jumped */
  def jumpTo(pos: Int): Int = {
    assert(jumpBackPos == -1)
    assert(headPos == -1)
    val jumped = currentPosition() - pos
    headPos = currentPosition()
    count = pos
    jumpBackPos = -1
    jumped
  }

  /** Returns current head position */
  def currentPosition(): Int = count

  /** Continues to write at the head. */
  def continue(): Unit = {
    assert(jumpBackPos == -1)
    assert(headPos >= 0)
    count = headPos
    headPos = -1
  }
}
