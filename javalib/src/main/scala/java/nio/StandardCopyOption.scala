package java.nio.file

import java.util.Objects

class StandardCopyOption private (name: String, ordinal: Int)
    extends _Enum[StandardCopyOption](name, ordinal)
    with CopyOption
object StandardCopyOption {
  final val REPLACE_EXISTING = new StandardCopyOption("REPLACE_EXISTING", 0)
  final val COPY_ATTRIBUTES = new StandardCopyOption("COPY_ATTRIBUTES", 1)
  final val ATOMIC_MOVE = new StandardCopyOption("ATOMIC_MOVE", 2)

  def values(): Array[StandardCopyOption] = _values.clone()

  def valueOf(name: String): StandardCopyOption = {
    Objects.requireNonNull(name, "Name is null")
    _values.find(_.name() == name).getOrElse {
      throw new IllegalArgumentException(
        "No enum constant java.nio.file.StandardCopyOption." + name
      )
    }
  }

  private val _values =
    Array(REPLACE_EXISTING, COPY_ATTRIBUTES, ATOMIC_MOVE)

}
