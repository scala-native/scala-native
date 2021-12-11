package java.nio.channels

abstract class FileLock private (
    _channel: Channel,
    final val position: Long,
    final val size: Long,
    shared: Boolean
) extends AutoCloseable {
  protected def this(
      channel: AsynchronousFileChannel,
      position: Long,
      size: Long,
      shared: Boolean
  ) =
    this(channel: Channel, position, size, shared)
  protected def this(
      channel: FileChannel,
      position: Long,
      size: Long,
      shared: Boolean
  ) =
    this(channel: Channel, position, size, shared)

  require(position >= 0 && size >= 0, "position and size must be non negative")

  final def channel(): FileChannel =
    _channel match {
      case fc: FileChannel => fc
      case _               => null
    }

  def acquiredBy(): Channel =
    _channel

  final def isShared(): Boolean =
    shared

  final def overlaps(pos: Long, siz: Long): Boolean =
    (pos + siz) > position && (position + size) > pos

  def isValid(): Boolean

  def release(): Unit

  override final def close(): Unit =
    release()

  override final def toString(): String =
    s"FileLock(${_channel}, $position, $size, $shared), isValid = ${isValid()}"

}
