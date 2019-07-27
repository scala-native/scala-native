package java.nio.file.attribute

import java.util.List

trait AclFileAttributeView extends FileOwnerAttributeView {
  def getAcl(): List[AclEntry]
  def setAcl(acl: List[AclEntry]): Unit
}
