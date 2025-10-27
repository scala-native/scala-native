package java.lang.process

import java.{lang => jl, util => ju}

import scala.util.Try

import scala.scalanative.meta.LinktimeInfo

private[process] object GenericProcessWatcher {

  import ju.concurrent._

  private val processes = new ConcurrentHashMap[jl.Long, GenericProcessHandle]

  private val lock = new locks.ReentrantLock()
  private val hasProcessesToWatch = lock.newCondition()

  def watchForTermination(handle: GenericProcessHandle): Unit = {
    processes.put(handle.pid(), handle)
    assert(
      watcherThread.isAlive(),
      "GenericProcessWatcher watch thread is terminated"
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
    .name("ScalaNative-GenericProcessWatcher")
    .startInternal { () =>
      lock.lock()
      try {
        while (true) Try {
          removeCompleted()
          while (processes.isEmpty())
            hasProcessesToWatch.await()
          if (!reapSomeProcesses()) Thread.sleep(100) // ms
        }
      } finally lock.unlock()
    }

  // return true if something has been reaped
  private val reapSomeProcesses: () => Boolean =
    if (LinktimeInfo.isWindows) WindowsProcess.reapSomeProcesses
    else if (UnixProcess.useGen2) claimAllCompleted
    else UnixProcessGen1.waitpidAny

  def claimAllCompleted(): Boolean = {
    var ok = false
    removeSomeProcesses { entry =>
      val remove = entry.getValue().checkIfExited()
      ok ||= remove
      remove
    }
    ok
  }

  // return true if pid is under our supervision
  def claimCompleted(pid: Long): Boolean = {
    var ok = false
    processes.computeIfPresent(
      pid,
      (_, ref) => {
        ok = true
        if (ref.checkIfExited()) null else ref
      }
    )
    ok
  }

  def completeWith(pid: Long)(ec: => Int): Boolean = {
    val ref = processes.remove(pid)
    (ref ne null) && ref.setExitCode(ec)
  }

  def removeCompleted(): Unit =
    removeSomeProcesses(_.getValue().hasExited)

  private def removeSomeProcesses(
      f: ju.Map.Entry[jl.Long, GenericProcessHandle] => Boolean
  ): Unit = {
    val it = processes.entrySet().iterator()
    while (it.hasNext()) if (f(it.next())) it.remove()
  }

}
