package scala.scalanative.windows

import scala.scalanative.unsafe._
import scala.scalanative.unsigned._
import HandleApi.Handle

@extern()
object SynchApi {
  type CallbackContext = Ptr[Byte]
  type WaitOrTimerCallback = CFuncPtr2[CallbackContext, Boolean, Unit]

  def WaitForSingleObject(
      ref: Handle,
      miliseconds: DWord
  ): DWord = extern

}

object SynchApiExt {
  final val WAIT_ABANDONED = 0x00000080L.toUInt
  final val WAIT_OBJECT_0 = 0x00000000L.toUInt
  final val WAIT_TIMEOUT = 0x00000102L.toUInt
  final val WAIT_FAILED = 0xffffffff.toUInt
}
