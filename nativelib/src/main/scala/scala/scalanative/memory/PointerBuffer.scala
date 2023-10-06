package scala.scalanative.memory

import scala.language.implicitConversions

import java.nio._

import scala.scalanative.unsafe._
import scala.scalanative.javalibintf.{PointerBuffer => Intf}

/** Factory methods to create direct buffers from valid memory pointers.
 *
 *  All buffers created by the methods of this object are direct buffers with
 *  the native byte order of the platform.
 */
object PointerBuffer {

  /** Wraps a [[scala.scalanative.unsafe.Ptr]] pointing to memory of given size
   *  expressed in number of bytes, in a direct
   *  [[java.nio.ByteBuffer ByteBuffer]]
   */
  def wrap(ptr: Ptr[Byte], size: Int): ByteBuffer =
    Intf.wrapPointerByte(ptr, size)
}

/** Additional operations on a [[java.nio.Buffer Buffer]] with interoperability
 *  with ScalaNative PointerBuffers.
 */
final class PointerBufferOps[ElementType] private (private val buffer: Buffer)
    extends AnyVal {

  /** Tests whether this direct buffer has a valid associated
   *  [[scala.scalanative.unsafe.Ptr]].
   *
   *  If this buffer is read-only, returns false.
   */
  def hasPointer(): Boolean =
    Intf.hasPointer(buffer)

  /** [[scala.scalanative.unsafe.Ptr]] backing this direct buffer _(optional
   *  operation)_.
   *
   *  @throws UnsupportedOperationException
   *    If this buffer does not have a backing [[scala.scalanative.unsafe.Ptr]],
   *    i.e., !hasPointer().
   */
  def pointer(): Ptr[ElementType] =
    Intf.pointer(buffer).asInstanceOf[Ptr[ElementType]]
}

/** Extensions to [[java.nio.Buffer Buffer]]s for interoperability with
 *  ScalaNative pointers.
 */
object PointerBufferOps {
  implicit def bufferOps(buffer: Buffer): PointerBufferOps[_] =
    new PointerBufferOps(buffer)

  implicit def byteBufferOps(buffer: ByteBuffer): PointerBufferOps[Byte] =
    new PointerBufferOps(buffer)

  implicit def charBufferOps(buffer: CharBuffer): PointerBufferOps[Char] =
    new PointerBufferOps(buffer)

  implicit def shortBufferOps(buffer: ShortBuffer): PointerBufferOps[Short] =
    new PointerBufferOps(buffer)

  implicit def intBufferOps(buffer: IntBuffer): PointerBufferOps[Int] =
    new PointerBufferOps(buffer)

  implicit def longBufferOps(buffer: LongBuffer): PointerBufferOps[Long] =
    new PointerBufferOps(buffer)

  implicit def floatBufferOps(buffer: FloatBuffer): PointerBufferOps[Float] =
    new PointerBufferOps(buffer)

  implicit def doubleBufferOps(buffer: DoubleBuffer): PointerBufferOps[Double] =
    new PointerBufferOps(buffer)
}
