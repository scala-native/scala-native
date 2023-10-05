package scala.scalanative.javalibintf

import java.nio._
import scala.scalanative.unsafe._
import scala.scalanative.annotation.alwaysinline

object PointerBuffer {

  def wrapPointerByte(ptr: Any, length: Int): ByteBuffer =
    ByteBuffer.wrapPtr(ptr.asInstanceOf, length)

  def hasPointer(buffer: Buffer): Boolean =
    buffer.hasPointer()

  def pointer(buffer: Buffer): Ptr[_] =
    buffer.pointer()

}
