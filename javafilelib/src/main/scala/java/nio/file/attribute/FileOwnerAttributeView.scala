package java.nio.file.attribute

trait FileOwnerAttributeView extends FileAttributeView {
  def getOwner(): UserPrincipal
  def name(): String
  def setOwner(owner: UserPrincipal): Unit
}
