package java.nio.file

class StandardOpenOption private (name: String, ordinal: Int)
    extends Enum[StandardOpenOption](name, ordinal)
    with OpenOption
object StandardOpenOption {
  final val READ              = new StandardOpenOption("READ", 0)
  final val WRITE             = new StandardOpenOption("WRITE", 1)
  final val APPEND            = new StandardOpenOption("APPEND", 2)
  final val TRUNCATE_EXISTING = new StandardOpenOption("TRUNCATE_EXISTING", 3)
  final val CREATE            = new StandardOpenOption("CREATE", 4)
  final val CREATE_NEW        = new StandardOpenOption("CREATE_NEW", 5)
  final val DELETE_ON_CLOSE   = new StandardOpenOption("DELETE_ON_CLOSE", 6)
  final val SPARSE            = new StandardOpenOption("SPARSE", 7)
  final val SYNC              = new StandardOpenOption("SYNC", 8)
  final val DSYNC             = new StandardOpenOption("DSYNC", 9)

  def values(): Array[StandardOpenOption] = _values.clone()

  private[this] val _values = Array(READ,
                                    WRITE,
                                    APPEND,
                                    TRUNCATE_EXISTING,
                                    CREATE,
                                    CREATE_NEW,
                                    DELETE_ON_CLOSE,
                                    SPARSE,
                                    SYNC,
                                    DSYNC)
}
