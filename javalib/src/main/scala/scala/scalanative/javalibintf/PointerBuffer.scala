package scala.scalanative.javalibintf

import java.nio.*
import scala.scalanative.unsafe.*
import scala.scalanative.annotation.alwaysinline

object PointerBuffer {

  def wrapPointerByte(ptr: Any, length: Int): ByteBuffer =
    ByteBuffer.wrapPointerByte(ptr.asInstanceOf[Ptr[Byte]], length)

  def hasPointer(buffer: Buffer): Boolean =
    buffer.hasPointer()

  def pointer(buffer: Buffer): Any =
    buffer.pointer()

}
