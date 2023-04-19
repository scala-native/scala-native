package scala.scalanative.windows

import scala.scalanative.unsafe._

@link("Advapi32")
@extern()
object SddlApi {
  import SecurityBaseApi._
  def ConvertSidToStringSidW(sid: SIDPtr, stringSid: Ptr[CWString]): Boolean =
    extern

  def ConvertStringSidToSidW(
      sidString: CWString,
      sidRef: Ptr[SIDPtr]
  ): Boolean =
    extern
}
