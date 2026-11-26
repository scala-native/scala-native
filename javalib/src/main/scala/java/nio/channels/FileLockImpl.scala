package java.nio.channels

/* A lock of a FileChannelImpl. It is in FileLockTable from its overlap check
 * until its OS lock is gone; `state` changes only under that table's lock.
 */
private[java] final class FileLockImpl(
    channel: FileChannelImpl,
    position: Long,
    size: Long,
    shared: Boolean,
    private[channels] val key: FileLockTable.Key,
    // The thread waiting in lock(), woken by close(); null for tryLock.
    private[channels] val waiter: Thread
) extends FileLock(channel, position, size, shared) {
  @volatile private[channels] var state: Int = FileLockImpl.Pending

  override def isValid(): Boolean =
    state == FileLockImpl.Held && channel.isOpen()

  override def release(): Unit = {
    if (!channel.isOpen()) throw new ClosedChannelException()
    if (FileLockTable.startRelease(this)) channel.unlock(this)
  }
}

private[channels] object FileLockImpl {
  final val Pending = 0
  final val Held = 1
  final val Releasing = 2
  final val Released = 3
}
