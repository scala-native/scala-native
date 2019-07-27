package java.nio.file.attribute

class PosixFilePermission private (name: String, ordinal: Int)
    extends Enum[PosixFilePermission](name, ordinal)
object PosixFilePermission {
  final val OWNER_READ     = new PosixFilePermission("OWNER_READ", 0)
  final val OWNER_WRITE    = new PosixFilePermission("OWNER_WRITE", 1)
  final val OWNER_EXECUTE  = new PosixFilePermission("OWNER_EXECUTE", 2)
  final val GROUP_READ     = new PosixFilePermission("GROUP_READ", 3)
  final val GROUP_WRITE    = new PosixFilePermission("GROUP_WRITE", 4)
  final val GROUP_EXECUTE  = new PosixFilePermission("GROUP_EXECUTE", 5)
  final val OTHERS_READ    = new PosixFilePermission("OTHERS_READ", 6)
  final val OTHERS_WRITE   = new PosixFilePermission("OTHERS_WRITE", 7)
  final val OTHERS_EXECUTE = new PosixFilePermission("OTHERS_EXECUTE", 8)

  def values(): Array[PosixFilePermission] = _values.clone()

  private[this] val _values = Array(OWNER_READ,
                                    OWNER_WRITE,
                                    OWNER_EXECUTE,
                                    GROUP_READ,
                                    GROUP_WRITE,
                                    GROUP_EXECUTE,
                                    OTHERS_READ,
                                    OTHERS_WRITE,
                                    OTHERS_EXECUTE)
}
