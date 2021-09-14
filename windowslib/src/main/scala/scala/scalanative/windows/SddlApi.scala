package scala.scalanative.windows

import scala.scalanative.unsafe._
import scala.scalanative.unsigned._
import scala.scalanative.windows.HandleApi.Handle

@link("Advapi32")
@extern()
object SddlApi {
  import MinWinBaseApi._
  import SecurityBaseApi._
  def ConvertSidToStringSidW(sid: SIDPtr, stringSid: Ptr[CWString]): Boolean =
    extern

  def ConvertStringSidToSidW(
      sidString: CWString,
      sidRef: Ptr[SIDPtr]
  ): Boolean =
    extern
}
