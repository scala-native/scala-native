package java.nio.file.attribute

import java.util.Objects

final class AclEntryFlag private (name: String, ordinal: Int)
    extends _Enum[AclEntryFlag](name, ordinal)

object AclEntryFlag {
  final val FILE_INHERIT = new AclEntryFlag("FILE_INHERIT", 0)
  final val DIRECTORY_INHERIT = new AclEntryFlag("DIRECTORY_INHERIT", 1)
  final val NO_PROPAGATE_INHERIT = new AclEntryFlag("NO_PROPAGATE_INHERIT", 2)
  final val INHERIT_ONLY = new AclEntryFlag("INHERIT_ONLY", 3)

  private val _values = Array(
    FILE_INHERIT,
    DIRECTORY_INHERIT,
    NO_PROPAGATE_INHERIT,
    INHERIT_ONLY
  )

  def values(): Array[AclEntryFlag] = _values.clone()

  def valueOf(name: String): AclEntryFlag = {
    Objects.requireNonNull(name, "Name is null")
    _values.find(_.name() == name).getOrElse {
      throw new IllegalArgumentException(
        "No enum constant java.nio.file.attribute.AclEntryFlag." + name
      )
    }
  }
}
