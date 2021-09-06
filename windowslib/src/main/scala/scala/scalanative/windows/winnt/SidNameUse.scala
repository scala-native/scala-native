package scala.scalanative.windows.winnt

import scalanative.unsafe._
@extern
object SidNameUse {
  @name("scalanative_win32_winnt_sid_name_use_sidtypeuser")
  def SidTypeUser: SidNameUse = extern

  @name("scalanative_win32_winnt_sid_name_use_sidtypegroup")
  def SidTypeGroup: SidNameUse = extern

  @name("scalanative_win32_winnt_sid_name_use_sidtypedomain")
  def SidTypeDomain: SidNameUse = extern

  @name("scalanative_win32_winnt_sid_name_use_sidtypealias")
  def SidTypeAlias: SidNameUse = extern

  @name("scalanative_win32_winnt_sid_name_use_sidtypewellknowngroup")
  def SidTypeWellKnownGroup: SidNameUse = extern

  @name("scalanative_win32_winnt_sid_name_use_sidtypedeletedaccount")
  def SidTypeDeletedAccount: SidNameUse = extern

  @name("scalanative_win32_winnt_sid_name_use_sidtypeinvalid")
  def SidTypeInvalid: SidNameUse = extern

  @name("scalanative_win32_winnt_sid_name_use_sidtypeunknown")
  def SidTypeUnknown: SidNameUse = extern

  @name("scalanative_win32_winnt_sid_name_use_sidtypecomputer")
  def SidTypeComputer: SidNameUse = extern

  @name("scalanative_win32_winnt_sid_name_use_sidtypelabel")
  def SidTypeLabel: SidNameUse = extern

  @name("scalanative_win32_winnt_sid_name_use_sidtypelogonsession")
  def SidTypeLogonSession: SidNameUse = extern
}
