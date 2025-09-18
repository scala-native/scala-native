package java.lang.process

import java.util.concurrent._

private[process] object GenericProcessWatcher {
  // Identity map, no valid concurrent structure
  private val watchedProcesses = new ConcurrentHashMap[
    CompletableFuture[ProcessHandle],
    GenericProcess.Handle
  ]()

  private val lock = new locks.ReentrantLock()
  private val hasProcessesToWatch = lock.newCondition()

  def watchForTermination(
      completion: CompletableFuture[ProcessHandle],
      handle: GenericProcess.Handle
  ): Unit = {
    watchedProcesses.put(completion, handle)
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
          while (watchedProcesses.isEmpty()) {
            hasProcessesToWatch.await()
          }
          watchedProcesses.forEach { (completion, ref) =>
            if (completion.isCancelled())
              watchedProcesses.remove(completion)
            else if (ref.process.waitFor(5, TimeUnit.MILLISECONDS)) {
              completion.complete(ref)
              watchedProcesses.remove(completion)
            }
          }
          Thread.sleep(100 /*ms*/ )
        } catch { case scala.util.control.NonFatal(_) => () }
      } finally lock.unlock()
    }

}
