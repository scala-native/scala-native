package scala.scalanative.windows.winnt

import scala.scalanative.unsafe._

@link("Advapi32")
@extern
object AccessRights {
  @name("scalanative_winnt_access_rights_file_generic_all")
  def FILE_GENERIC_ALL: AccessRights = extern

  //execute
  @name("scalanative_winnt_access_rights_file_generic_execute")
  def FILE_GENERIC_EXECUTE: AccessRights = extern

  @name("scalanative_winnt_access_rights_file_execute")
  def FILE_EXECUTE: AccessRights = extern

  @name("scalanative_winnt_access_rights_file_read_attributes")
  def STANDARD_RIGHTS_EXECUTE: AccessRights = extern

  //read
  @name("scalanative_winnt_access_rights_file_generic_read")
  def FILE_GENERIC_READ: AccessRights = extern

  @name("scalanative_winnt_access_rights_file_read_attributes")
  def FILE_READ_ATTRIBUTES: AccessRights = extern

  @name("scalanative_winnt_access_rights_file_read_data")
  def FILE_READ_DATA: AccessRights = extern

  @name("scalanative_winnt_access_rights_file_read_ea")
  def FILE_READ_EA: AccessRights = extern

  @name("scalanative_winnt_access_rights_standard_rights_read")
  def STANDARD_RIGHTS_READ: AccessRights = extern

  //write
  @name("scalanative_winnt_access_rights_file_generic_write")
  def FILE_GENERIC_WRITE: AccessRights = extern

  @name("scalanative_winnt_access_rights_file_append_data")
  def FILE_APPEND_DATA: AccessRights = extern

  @name("scalanative_winnt_access_rights_file_write_attributes")
  def FILE_WRITE_ATTRIBUTES: AccessRights = extern

  @name("scalanative_winnt_access_rights_file_write_data")
  def FILE_WRITE_DATA: AccessRights = extern

  @name("scalanative_winnt_access_rights_file_write_ea")
  def FILE_WRITE_EA: AccessRights = extern

  @name("scalanative_winnt_access_rights_standard_rights_write")
  def STANDARD_RIGHTS_WRITE: AccessRights = extern

  @name("scalanative_winnt_access_rights_synchronize")
  def SYNCHRONIZE: AccessRights = extern
}
