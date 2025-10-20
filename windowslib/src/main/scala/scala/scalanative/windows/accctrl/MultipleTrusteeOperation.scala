package scala.scalanative.windows.accctrl

import scala.scalanative.unsafe.*

@extern
object MultipleTrusteeOperation {
  @name("scalanative_no_multiple_trustee")
  def NO_MULTIPLE_TRUSTEE: MultipleTruteeOperation = extern
  @name("scalanative_trustee_is_impersonate")
  def TRUSTEE_IS_IMPERSONATE: MultipleTruteeOperation = extern
}
