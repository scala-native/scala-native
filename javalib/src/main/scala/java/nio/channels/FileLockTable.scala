package java.nio.channels

import java.util.{ArrayList, HashMap}

/* Locks held or requested by this process, per file. OS record locks do not
 * exclude threads of one process, so overlaps are found here. Entries stay
 * from the overlap check until the OS lock is gone, so a waiting request
 * counts, and no thread can lock a range whose OS unlock is still to come.
 * No OS call or park happens under this lock.
 */
private[channels] object FileLockTable {

  // Unix: st_dev and st_ino. Windows: volume serial number and file index.
  final case class Key(device: Long, file: Long)

  private val files = new HashMap[Key, ArrayList[FileLockImpl]]()

  // Registers a pending request, or throws if it overlaps another one.
  def add(lock: FileLockImpl): Unit = synchronized {
    if (!lock.channel().isOpen()) throw new ClosedChannelException()
    var list = files.get(lock.key)
    if (list == null) {
      list = new ArrayList[FileLockImpl]()
      files.put(lock.key, list)
    }
    var i = 0
    while (i < list.size()) {
      if (list.get(i).overlaps(lock.position, lock.size))
        throw new OverlappingFileLockException()
      i += 1
    }
    list.add(lock)
  }

  // Before an OS attempt; false once close() came first.
  def beginAttempt(lock: FileLockImpl): Boolean = synchronized {
    val ok = lock.state == FileLockImpl.Pending && lock.channel().isOpen()
    if (ok) lock.attempting = true
    ok
  }

  /* After an OS attempt. A grant becomes held, or false if close() came
   * first: the entry then stays until the caller has undone the OS lock.
   */
  def endAttempt(lock: FileLockImpl, granted: Boolean): Boolean =
    synchronized {
      if (!granted) {
        lock.attempting = false
        true
      } else {
        val ok = lock.state == FileLockImpl.Pending && lock.channel().isOpen()
        if (ok) {
          lock.state = FileLockImpl.Held
          lock.attempting = false
        }
        ok
      }
    }

  // Held to releasing, for one caller only.
  def startRelease(lock: FileLockImpl): Boolean = synchronized {
    val ok = lock.state == FileLockImpl.Held
    if (ok) lock.state = FileLockImpl.Releasing
    ok
  }

  // Drops a request that was never granted; no-op otherwise.
  def cancel(lock: FileLockImpl): Unit = synchronized {
    if (lock.state == FileLockImpl.Pending) removeLocked(lock)
  }

  /* After an OS unlock: the lock ends, unless the unlock failed on an open
   * channel, where it stays held so release() can be retried.
   */
  def endRelease(lock: FileLockImpl, unlocked: Boolean): Unit = synchronized {
    if (unlocked || !lock.channel().isOpen()) removeLocked(lock)
    else lock.state = FileLockImpl.Held
  }

  /* For close(): starts releasing the channel's held locks, which the caller
   * unlocks (state Releasing), and returns its pending requests for waking.
   * A request inside an OS attempt stays until its owner cancels it, as it
   * must undo a grant before another channel takes the range; others go.
   */
  def closeChannel(key: Key, channel: Channel): ArrayList[FileLockImpl] =
    synchronized {
      val result = new ArrayList[FileLockImpl]()
      val list = files.get(key)
      if (list != null) {
        var i = 0
        while (i < list.size()) {
          val lock = list.get(i)
          if (lock.channel() eq channel) {
            if (lock.state == FileLockImpl.Held) {
              lock.state = FileLockImpl.Releasing
              result.add(lock)
            } else if (lock.state == FileLockImpl.Pending) result.add(lock)
          }
          i += 1
        }
        // Not in place: removeLocked edits the list.
        i = 0
        while (i < result.size()) {
          val lock = result.get(i)
          if (lock.state == FileLockImpl.Pending && !lock.attempting)
            removeLocked(lock)
          i += 1
        }
      }
      result
    }

  private def removeLocked(lock: FileLockImpl): Unit = {
    lock.state = FileLockImpl.Released
    val list = files.get(lock.key)
    if (list != null) {
      list.remove(lock)
      if (list.isEmpty()) files.remove(lock.key)
    }
  }
}
