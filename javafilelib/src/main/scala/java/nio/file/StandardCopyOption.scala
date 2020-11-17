package java.nio.file

class StandardCopyOption private (name: String, ordinal: Int)
    extends Enum[StandardCopyOption](name, ordinal)
    with CopyOption
object StandardCopyOption {
  final val REPLACE_EXISTING = new StandardCopyOption("REPLACE_EXISTING", 0)
  final val COPY_ATTRIBUTES  = new StandardCopyOption("COPY_ATTRIBUTES", 1)
  final val ATOMIC_MOVE      = new StandardCopyOption("ATOMIC_MOVE", 2)

  def values(): Array[StandardCopyOption] = _values.clone()

  private[this] val _values =
    Array(REPLACE_EXISTING, COPY_ATTRIBUTES, ATOMIC_MOVE)

}
