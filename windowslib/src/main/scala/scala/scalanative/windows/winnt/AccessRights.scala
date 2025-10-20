package scala.scalanative.windows.winnt

import scala.scalanative.unsafe.*

@link("advapi32")
@extern
object AccessRights {
  @name("scalanative_generic_all")
  def FILE_GENERIC_ALL: AccessRights = extern

  // execute
  @name("scalanative_generic_execute")
  def FILE_GENERIC_EXECUTE: AccessRights = extern

  @name("scalanative_execute")
  def FILE_EXECUTE: AccessRights = extern

  @name("scalanative_read_attributes")
  def STANDARD_RIGHTS_EXECUTE: AccessRights = extern

  // read
  @name("scalanative_generic_read")
  def FILE_GENERIC_READ: AccessRights = extern

  @name("scalanative_read_attributes")
  def FILE_READ_ATTRIBUTES: AccessRights = extern

  @name("scalanative_read_data")
  def FILE_READ_DATA: AccessRights = extern

  @name("scalanative_read_ea")
  def FILE_READ_EA: AccessRights = extern

  @name("scalanative_standard_rights_read")
  def STANDARD_RIGHTS_READ: AccessRights = extern

  // write
  @name("scalanative_generic_write")
  def FILE_GENERIC_WRITE: AccessRights = extern

  @name("scalanative_append_data")
  def FILE_APPEND_DATA: AccessRights = extern

  @name("scalanative_write_attributes")
  def FILE_WRITE_ATTRIBUTES: AccessRights = extern

  @name("scalanative_write_data")
  def FILE_WRITE_DATA: AccessRights = extern

  @name("scalanative_write_ea")
  def FILE_WRITE_EA: AccessRights = extern

  @name("scalanative_standard_rights_write")
  def STANDARD_RIGHTS_WRITE: AccessRights = extern

  @name("scalanative_synchronize")
  def SYNCHRONIZE: AccessRights = extern
}
