package scala.scalanative.windows.accctrl

import scala.scalanative.unsafe._

@extern
object MultipleTrusteeOperation {
  @name("scalanative_win32_accctrl_no_multiple_trustee")
  def NO_MULTIPLE_TRUSTEE: MultipleTruteeOperation = extern
  @name("scalanative_win32_accctrl_trustee_is_impersonate")
  def TRUSTEE_IS_IMPERSONATE: MultipleTruteeOperation = extern
}
