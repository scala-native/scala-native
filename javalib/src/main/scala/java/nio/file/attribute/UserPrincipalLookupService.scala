package java.nio.file.attribute

abstract class UserPrincipalLookupService protected () {
  def lookupPrincipalByGroupName(group: String): GroupPrincipal
  def lookupPrincipalByName(name: String): UserPrincipal
}
