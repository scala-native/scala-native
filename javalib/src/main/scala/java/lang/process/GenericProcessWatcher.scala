package java.lang.process

import java.{lang => jl, util => ju}

import scala.util.Try

import scala.scalanative.annotation.alwaysinline
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
          removeProcessesIf(_.hasExited)
          while (processes.isEmpty())
            hasProcessesToWatch.await()
          if (!reapSomeProcesses()) Thread.sleep(100) // ms
        }
      } finally lock.unlock()
    }

  // return true if something has been reaped
  private val reapSomeProcesses: () => Boolean =
    if (LinktimeInfo.isWindows) claimAllCompleted
    else if (UnixProcess.useGen2) claimAllCompleted
    else UnixProcessGen1.waitpidAny

  @alwaysinline
  def claimAllCompleted(): Boolean =
    removeProcessesIf(_.checkIfExited())

  def completeWith(pid: Long)(ec: => Int): Boolean = {
    val ref = processes.remove(pid)
    (ref ne null) && ref.setCachedExitCode(ec)
  }

  @alwaysinline
  def removeProcessesIf(f: GenericProcessHandle => Boolean): Boolean =
    removeIf(processes.values())(f)

  private def removeIf[A](coll: ju.Collection[A])(f: A => Boolean): Boolean = {
    var ok = false
    val it = coll.iterator()
    while (it.hasNext()) {
      val ref = it.next()
      if (f(ref)) {
        ok = true
        it.remove()
      }
    }
    ok
  }

}
