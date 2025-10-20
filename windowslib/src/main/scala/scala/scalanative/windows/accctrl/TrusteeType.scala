package scala.scalanative.windows.accctrl

import scala.scalanative.unsafe.*

@extern
object TrusteeType {
  @name("scalanative_trustee_is_unknown")
  def TRUSTEE_IS_UNKNOWN: TrusteeType = extern
  @name("scalanative_trustee_is_user")
  def TRUSTEE_IS_USER: TrusteeType = extern
  @name("scalanative_trustee_is_group")
  def TRUSTEE_IS_GROUP: TrusteeType = extern
  @name("scalanative_trustee_is_domain")
  def TRUSTEE_IS_DOMAIN: TrusteeType = extern
  @name("scalanative_trustee_is_alias")
  def TRUSTEE_IS_ALIAS: TrusteeType = extern
  @name("scalanative_trustee_is_well_known_group")
  def TRUSTEE_IS_WELL_KNOWN_GROUP: TrusteeType = extern
  @name("scalanative_trustee_is_deleted")
  def TRUSTEE_IS_DELETED: TrusteeType = extern
  @name("scalanative_trustee_is_invalid")
  def TRUSTEE_IS_INVALID: TrusteeType = extern
  @name("scalanative_trustee_is_computer")
  def TRUSTEE_IS_COMPUTER: TrusteeType = extern
}
