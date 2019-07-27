package java.nio.file.attribute

import java.util.Set

trait PosixFileAttributes extends BasicFileAttributes {
  def group(): GroupPrincipal
  def owner(): UserPrincipal
  def permissions(): Set[PosixFilePermission]
}
