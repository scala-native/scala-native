package scala.scalanative
package runtime

import scala.scalanative.native.{CFunctionPtr2, CString}

object Platform {
  def isMac(): Boolean                 = ExternPlatform.isMac()
  def isWindows(): Boolean             = ExternPlatform.isWindows()
  def windowsGetUserLang(): CString    = ExternPlatform.windowsGetUserLang()
  def windowsGetUserCountry(): CString = ExternPlatform.windowsGetUserCountry()
  def littleEndian(): Boolean          = ExternPlatform.littleEndian()
  def setOSProps(addProp: CFunctionPtr2[CString, CString, Unit]): Unit =
    ExternPlatform.setOSProps(addProp)

  final val is32 = false
  @inline final def cross3264[A, B](thirtyTwo: => A,
                                    sixtyFour: => B): Cross3264[A, B] = {
    sixtyFour
  }

  type Cross3264[ThirtyTwo, SixtyFour] = SixtyFour
}
