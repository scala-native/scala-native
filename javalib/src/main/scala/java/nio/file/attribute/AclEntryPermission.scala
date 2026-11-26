package java.nio.file.attribute

import java.util.Objects

final class AclEntryPermission private (name: String, ordinal: Int)
    extends _Enum[AclEntryPermission](name, ordinal)

object AclEntryPermission {
  final val READ_DATA = new AclEntryPermission("READ_DATA", 0)
  final val WRITE_DATA = new AclEntryPermission("WRITE_DATA", 1)
  final val APPEND_DATA = new AclEntryPermission("APPEND_DATA", 2)
  final val READ_NAMED_ATTRS = new AclEntryPermission("READ_NAMED_ATTRS", 3)
  final val WRITE_NAMED_ATTRS = new AclEntryPermission("WRITE_NAMED_ATTRS", 4)
  final val EXECUTE = new AclEntryPermission("EXECUTE", 5)
  final val DELETE_CHILD = new AclEntryPermission("DELETE_CHILD", 6)
  final val READ_ATTRIBUTES = new AclEntryPermission("READ_ATTRIBUTES", 7)
  final val WRITE_ATTRIBUTES = new AclEntryPermission("WRITE_ATTRIBUTES", 8)
  final val DELETE = new AclEntryPermission("DELETE", 9)
  final val READ_ACL = new AclEntryPermission("READ_ACL", 10)
  final val WRITE_ACL = new AclEntryPermission("WRITE_ACL", 11)
  final val WRITE_OWNER = new AclEntryPermission("WRITE_OWNER", 12)
  final val SYNCHRONIZE = new AclEntryPermission("SYNCHRONIZE", 13)

  final val LIST_DIRECTORY = READ_DATA
  final val ADD_FILE = WRITE_DATA
  final val ADD_SUBDIRECTORY = APPEND_DATA

  private val _values = Array(
    READ_DATA,
    WRITE_DATA,
    APPEND_DATA,
    READ_NAMED_ATTRS,
    WRITE_NAMED_ATTRS,
    EXECUTE,
    DELETE_CHILD,
    READ_ATTRIBUTES,
    WRITE_ATTRIBUTES,
    DELETE,
    READ_ACL,
    WRITE_ACL,
    WRITE_OWNER,
    SYNCHRONIZE
  )

  def values(): Array[AclEntryPermission] = _values.clone()

  def valueOf(name: String): AclEntryPermission = {
    Objects.requireNonNull(name, "Name is null")
    _values.find(_.name() == name).getOrElse {
      throw new IllegalArgumentException(
        "No enum constant java.nio.file.attribute.AclEntryPermission." + name
      )
    }
  }
}
