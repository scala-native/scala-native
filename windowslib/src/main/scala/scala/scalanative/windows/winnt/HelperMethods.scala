package scala.scalanative.windows.winnt

import scala.scalanative.unsafe.extern
import scala.scalanative.unsafe.*

import scala.scalanative.windows.SecurityBaseApi.*

@link("advapi32")
@extern
object HelperMethods {
  @name("scalanative_winnt_setupUsersGroupSid")
  def setupUserGroupSid(ref: Ptr[SIDPtr]): Boolean = extern
}
