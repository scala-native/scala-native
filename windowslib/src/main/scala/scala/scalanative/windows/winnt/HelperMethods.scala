package scala.scalanative.windows.winnt

import scala.scalanative.unsafe._
import scala.scalanative.windows.SecurityBaseApi._

@link("advapi32")
@extern
object HelperMethods {
  @name("scalanative_winnt_setupUsersGroupSid")
  def setupUserGroupSid(ref: Ptr[SIDPtr]): Boolean = extern
}
