package java.nio.channels

abstract class FileLock private (channel: Channel,
                                 final val position: Long,
                                 final val size: Long,
                                 shared: Boolean)
    extends AutoCloseable {
  protected def this(channel: AsynchronousFileChannel,
                     position: Long,
                     size: Long,
                     shared: Boolean) =
    this(channel: Channel, position, size, shared)
  protected def this(channel: FileChannel,
                     position: Long,
                     size: Long,
                     shared: Boolean) =
    this(channel: Channel, position, size, shared)

  final def channel(): FileChannel =
    channel match {
      case fc: FileChannel => fc
      case _               => null
    }

  def acquiredBy(): Channel =
    channel

  final def isShared(): Boolean =
    shared

  final def overlaps(pos: Long, siz: Long): Boolean =
    (pos + siz) >= position || (position + size) >= pos

  def isValid(): Boolean

  def release(): Unit

  override final def close(): Unit =
    release()

  override final def toString(): String =
    s"FileLock($channel, $position, $size, $shared), isValid = $isValid"

}
