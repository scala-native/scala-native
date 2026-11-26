package java.nio.file.attribute

import java.util.Objects

final class AclEntryType private (name: String, ordinal: Int)
    extends _Enum[AclEntryType](name, ordinal)

object AclEntryType {
  final val ALLOW = new AclEntryType("ALLOW", 0)
  final val DENY = new AclEntryType("DENY", 1)
  final val AUDIT = new AclEntryType("AUDIT", 2)
  final val ALARM = new AclEntryType("ALARM", 3)

  private val _values = Array(
    ALLOW,
    DENY,
    AUDIT,
    ALARM
  )

  def values(): Array[AclEntryType] = _values.clone()

  def valueOf(name: String): AclEntryType = {
    Objects.requireNonNull(name, "Name is null")
    _values.find(_.name() == name).getOrElse {
      throw new IllegalArgumentException(
        "No enum constant java.nio.file.attribute.AclEntryType." + name
      )
    }
  }
}
