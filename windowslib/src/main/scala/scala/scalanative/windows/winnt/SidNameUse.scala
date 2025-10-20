package scala.scalanative.windows.winnt

import scalanative.unsafe.*
@extern
object SidNameUse {
  @name("scalanative_sidtypeuser")
  def SidTypeUser: SidNameUse = extern

  @name("scalanative_sidtypegroup")
  def SidTypeGroup: SidNameUse = extern

  @name("scalanative_sidtypedomain")
  def SidTypeDomain: SidNameUse = extern

  @name("scalanative_sidtypealias")
  def SidTypeAlias: SidNameUse = extern

  @name("scalanative_sidtypewellknowngroup")
  def SidTypeWellKnownGroup: SidNameUse = extern

  @name("scalanative_sidtypedeletedaccount")
  def SidTypeDeletedAccount: SidNameUse = extern

  @name("scalanative_sidtypeinvalid")
  def SidTypeInvalid: SidNameUse = extern

  @name("scalanative_sidtypeunknown")
  def SidTypeUnknown: SidNameUse = extern

  @name("scalanative_sidtypecomputer")
  def SidTypeComputer: SidNameUse = extern

  @name("scalanative_sidtypelabel")
  def SidTypeLabel: SidNameUse = extern

  @name("scalanative_sidtypelogonsession")
  def SidTypeLogonSession: SidNameUse = extern
}
