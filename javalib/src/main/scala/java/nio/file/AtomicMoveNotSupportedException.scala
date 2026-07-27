package java.nio.file

class AtomicMoveNotSupportedException(
    source: String,
    target: String,
    reason: String
) extends FileSystemException(source, target, reason)
