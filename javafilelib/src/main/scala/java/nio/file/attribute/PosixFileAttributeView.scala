package java.nio.file.attribute

import java.util.Set

trait PosixFileAttributeView
    extends BasicFileAttributeView
    with FileOwnerAttributeView {
  def name(): String
  def setGroup(group: GroupPrincipal): Unit
  def setPermissions(perms: Set[PosixFilePermission]): Unit
}
