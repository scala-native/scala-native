package java.lang.management

import scala.scalanative.runtime.NativeThread

trait ThreadMXBean {

  /** Returns the current number of threads.
   */
  def getThreadCount(): Int

  /** Returns the current number of daemon threads.
   */
  def getDaemonThreadCount(): Int

  /** Returns IDs of all threads.
   */
  def getAllThreadIds(): Array[Long]

  /** Returns a [[ThreadInfo]] for a thread with the given `id`. Returns `null`
   *  if the info is unavailable for the given id (thread doesn't exist).
   *
   *  An equivalent of `getThreadInfo(id, 0)`.
   *
   *  @param id
   *    the id of the thread
   */
  def getThreadInfo(id: Long): ThreadInfo

  /** Returns a [[ThreadInfo]] for threads with corresponding ids. Returns
   *  `null` for the corresponding array element if the info is unavailable for
   *  the given id (thread doesn't exist).
   *
   *  An equivalent of `getThreadInfo(ids, 0)`.
   *
   *  @param ids
   *    the IDs of the threads, each id must be positive (> 0).
   */
  def getThreadInfo(ids: Array[Long]): Array[ThreadInfo]

  /** Returns a [[ThreadInfo]] for the given thread id. Returns `null` if the
   *  info is unavailable for the given id (thread doesn't exist).
   *
   *  @param id
   *    the id of the thread, must be positive (> 0)
   *
   *  @param maxDepth
   *    the max depth of a stacktrace, must be non-negative (>= 0). it has no
   *    effect currently
   */
  def getThreadInfo(id: Long, maxDepth: Int): ThreadInfo

  /** Returns ThreadInfo for the given thread ids. Returns `null` for the
   *  corresponding array element if the info is unavailable for the given id
   *  (thread doesn't exist).
   *
   *  @param ids
   *    the IDs of the threads, each id must be positive (> 0)
   *
   *  @param maxDepth
   *    the max depth of a stacktrace, must be non-negative (>= 0). it has no
   *    effect currently
   */
  def getThreadInfo(ids: Array[Long], maxDepth: Int): Array[ThreadInfo]

  /** Returns a [[ThreadInfo]] for every available thread.
   *
   *  An equivalent of `dumpAllThread(lockedMonitors, lockedSynchronizers, 0)`.
   *
   *  @note
   *    the stacktrace will not be filled. Use overloaded alternative with the
   *    `maxDepth` parameter to get the stacktrace.
   *
   *  @param lockedMonitors
   *    whether to dump locked monitors. it has no effect currently
   *
   *  @param lockedSynchronizers
   *    whether to dump locked synchronizers. it has no effect currently
   */
  def dumpAllThreads(
      lockedMonitors: Boolean,
      lockedSynchronizers: Boolean
  ): Array[ThreadInfo]

  /** Returns a [[ThreadInfo]] for every available thread.
   *
   *  @param lockedMonitors
   *    whether to dump locked monitors. it has no effect currently
   *
   *  @param lockedSynchronizers
   *    whether to dump locked synchronizers. it has no effect currently
   *
   *  @param maxDepth
   *    the max depth of a stacktrace, must be non-negative (>= 0). it has no
   *    effect currently
   */
  def dumpAllThreads(
      lockedMonitors: Boolean,
      lockedSynchronizers: Boolean,
      maxDepth: Int
  ): Array[ThreadInfo]
}

object ThreadMXBean {

  private[management] def apply(): ThreadMXBean =
    new Impl

  private class Impl extends ThreadMXBean {
    def getThreadCount(): Int =
      aliveThreads.size

    def getDaemonThreadCount(): Int =
      aliveThreads.count(_.thread.isDaemon())

    @annotation.nowarn // Thread.getId is deprecated since JDK 19
    def getAllThreadIds(): Array[Long] =
      aliveThreads.map(_.thread.getId()).toArray

    def getThreadInfo(id: Long): ThreadInfo =
      getThreadInfo(id, 0)

    def getThreadInfo(ids: Array[Long]): Array[ThreadInfo] =
      getThreadInfo(ids, 0)

    def getThreadInfo(id: Long, maxDepth: Int): ThreadInfo = {
      checkThreadId(id)
      checkMaxDepth(maxDepth)

      NativeThread.Registry.getById(id).map(t => ThreadInfo(t)).orNull
    }

    def getThreadInfo(ids: Array[Long], maxDepth: Int): Array[ThreadInfo] = {
      checkMaxDepth(maxDepth)

      ids.map { id =>
        checkThreadId(id)
        NativeThread.Registry.getById(id).map(t => ThreadInfo(t)).orNull
      }
    }

    def dumpAllThreads(
        lockedMonitors: Boolean,
        lockedSynchronizers: Boolean
    ): Array[ThreadInfo] =
      dumpAllThreads(lockedMonitors, lockedSynchronizers, 0)

    def dumpAllThreads(
        lockedMonitors: Boolean,
        lockedSynchronizers: Boolean,
        maxDepth: Int
    ): Array[ThreadInfo] = {
      checkMaxDepth(maxDepth)
      aliveThreads.map(thread => ThreadInfo(thread)).toArray
    }

    @inline private def aliveThreads: Iterable[NativeThread] =
      NativeThread.Registry.aliveThreads

    @inline private def checkThreadId(id: Long): Unit =
      require(id > 0, s"Invalid thread ID parameter: $id")

    @inline private def checkMaxDepth(depth: Int): Unit =
      require(depth >= 0, s"Invalid maxDepth parameter: $depth")
  }

}
