package scala.scalanative.windows.accctrl

import scala.scalanative.unsafe.*

@extern
object TrusteeForm {
  @name("scalanative_trustee_is_sid")
  def TRUSTEE_IS_SID: TrusteeForm = extern
  @name("scalanative_trustee_is_name")
  def TRUSTEE_IS_NAME: TrusteeForm = extern
  @name("scalanative_trustee_bad_form")
  def TRUSTEE_BAD_FORM: TrusteeForm = extern
  @name("scalanative_trustee_is_objects_and_sid")
  def TRUSTEE_IS_OBJECTS_AND_SID: TrusteeForm = extern
  @name("scalanative_trustee_is_objects_and_name")
  def TRUSTEE_IS_OBJECTS_AND_NAME: TrusteeForm = extern
}
