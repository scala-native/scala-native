package scala.scalanative.windows.accctrl

import scala.scalanative.unsafe.*

@extern
object AccessMode {
  @name("scalanative_not_used_access")
  def NOT_USED_ACCESS: AccessMode = extern
  @name("scalanative_grant_access")
  def GRANT_ACCESS: AccessMode = extern
  @name("scalanative_set_access")
  def SET_ACCESS: AccessMode = extern
  @name("scalanative_deny_access")
  def DENY_ACCESS: AccessMode = extern
  @name("scalanative_revoke_access")
  def REVOKE_ACCESS: AccessMode = extern
  @name("scalanative_set_audit_success")
  def SET_AUDIT_SUCCESS: AccessMode = extern
  @name("scalanative_set_audit_failure")
  def SET_AUDIT_FAILURE: AccessMode = extern
}
