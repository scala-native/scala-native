package java.nio
import scala.scalanative.unsafe._
import scala.scalanative.runtime.ByteArray
import scala.scalanative.libc.string
import scala.scalanative.runtime
import scala.scalanative.unsigned._

private[nio] case class PtrByteArray(val ptr: Ptr[Byte], val length: Int) {

  @inline def update(index: Int, value: Byte): Unit =
    ptr(index) = value

  @inline def apply(index: Int): Byte =
    ptr(index)

  @inline def cpyToArray(): Array[Byte] = {
    val a = Array.ofDim[Byte](length)
    string.memcpy(a.asInstanceOf[runtime.ByteArray].at(0), ptr, length.toUInt)
    a
  }
}
