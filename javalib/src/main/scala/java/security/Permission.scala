package java.security

abstract class Permission(name: String) extends java.io.Serializable {
  def implies(permission: Permission): scala.Boolean

  def getActions(): String

  final def getName(): String = name

  override def toString(): String = {
    val actions = getActions()
    val suffix =
      if (actions == null || actions.isEmpty()) ")"
      else " \"" + actions + "\")"
    "(\"" + getClass().getName() + "\" \"" + name + "\"" + suffix
  }
}
