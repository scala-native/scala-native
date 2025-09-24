package java.lang.process

import java.util.concurrent._

private[process] object GenericProcessWatcher {
  private val watchedProcesses = ConcurrentHashMap.newKeySet[GenericProcess]

  private val lock = new locks.ReentrantLock()
  private val hasProcessesToWatch = lock.newCondition()

  def watchForTermination(handle: GenericProcess): Unit = {
    watchedProcesses.add(handle)
    assert(
      watcherThread.isAlive(),
      "Process termination watch thread is terminated"
    )
    // If we cannot lock it means that watcher thread is already executing, no need to wake it up
    if (lock.tryLock()) {
      try hasProcessesToWatch.signal()
      finally lock.unlock()
    }
  }

  private lazy val watcherThread: Thread = Thread
    .ofPlatform()
    .daemon(true)
    .group(ThreadGroup.System)
    .name("ScalaNative-ProcessTerminationWatcher")
    .startInternal { () =>
      lock.lock()
      try {
        while (true) try {
          while (watchedProcesses.isEmpty())
            hasProcessesToWatch.await()
          watchedProcesses.forEach { ref =>
            if (ref.handle.checkIfExited())
              watchedProcesses.remove(ref)
          }
          Thread.sleep(100 /*ms*/ )
        } catch { case scala.util.control.NonFatal(_) => () }
      } finally lock.unlock()
    }

}
