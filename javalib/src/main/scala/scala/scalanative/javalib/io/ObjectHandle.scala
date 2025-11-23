package scala.scalanative.javalib.io

import scala.scalanative.meta.LinktimeInfo
import scala.scalanative.posix.unistd
import scala.scalanative.runtime
import scala.scalanative.windows.HandleApi._
import scala.scalanative.windows.HandleApiExt._

/* Universal type allowing to store references to both Unix integer-based
 * (file descriptors, process ids etc.), and Windows pointer-based object handles */
class ObjectHandle(val value: Long) extends AnyVal {
  def asInt: Int = value.toInt

  def asHandle: Handle =
    runtime.fromRawPtr[Byte](runtime.Intrinsics.castLongToRawPtr(value))

  def valid(): Boolean = value != ObjectHandle.Invalid.value

  def close(): Unit = if (valid())
    if (LinktimeInfo.isWindows) CloseHandle(asHandle) else unistd.close(asInt)
}

object ObjectHandle {
  def apply(id: Int): ObjectHandle = new ObjectHandle(id.toLong)
  def apply(handle: Handle): ObjectHandle =
    new ObjectHandle(handle.toLong)
  val Invalid: ObjectHandle =
    if (LinktimeInfo.isWindows) ObjectHandle(INVALID_HANDLE_VALUE)
    else ObjectHandle(-1)
}
