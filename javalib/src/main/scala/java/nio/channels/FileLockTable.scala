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

  // Pending to held; false if close() came first.
  def grant(lock: FileLockImpl): Boolean = synchronized {
    val ok = lock.state == FileLockImpl.Pending && lock.channel().isOpen()
    if (ok) lock.state = FileLockImpl.Held
    ok
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

  // Ends a lock whose OS unlock is done or no longer needed.
  def remove(lock: FileLockImpl): Unit = synchronized(removeLocked(lock))

  /* For close(): cancels the channel's pending requests and starts releasing
   * its held locks. Returns both; the caller unlocks the held ones (state
   * Releasing) and wakes the waiters of the others.
   */
  def closeChannel(key: Key, channel: Channel): ArrayList[FileLockImpl] =
    synchronized {
      val result = new ArrayList[FileLockImpl]()
      val list = files.get(key)
      if (list != null) {
        val it = list.iterator()
        while (it.hasNext()) {
          val lock = it.next()
          if (lock.channel() eq channel) {
            if (lock.state == FileLockImpl.Pending) {
              lock.state = FileLockImpl.Released
              it.remove()
              result.add(lock)
            } else if (lock.state == FileLockImpl.Held) {
              lock.state = FileLockImpl.Releasing
              result.add(lock)
            }
          }
        }
        if (list.isEmpty()) files.remove(key)
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
