package java.lang.process

import java.{lang => jl, util => ju}

import scala.util.Try

import scala.scalanative.annotation.alwaysinline
import scala.scalanative.meta.LinktimeInfo

private[process] object GenericProcessWatcher {

  import ju.concurrent._

  @alwaysinline def watchForTermination(handle: GenericProcessHandle): Unit =
    Processes.add(handle)

  private object Processes {
    private val processes = new ConcurrentHashMap[jl.Long, GenericProcessHandle]

    /* Custom "mutex" to make sure we don't try to reap until there are processes
     * to wait for.
     * - release() is called anytime a process is added, and will mark this open
     * - acquire() is called ONLY if processes was empty:
     *   - if it's closed, blocks until it's marked open (a process is added)
     *   - if it's open, close it and check if processes is empty again
     * Between processes.isEmpty and processesMutex.acquire, several scenarios
     * could play out:
     * - processes isn't empty: we proceed to reaping
     *   - the state of processesMutex is irrelevant here, although it
     *     is likely open since it's opened anytime a process is added,
     *     and not closed until processes is empty;
     * - processes is empty but processesMutex is open:
     *   - the check is repeated after processesMutex is closed
     * - processes is empty and processesMutex is closed:
     *   - acquire() blocks until released
     *   - it could be released even before acquire() is called, if a
     *     process is added during the gap between these two calls
     *   - or it could be released after acquire() is called
     */
    private val processesMutex = new locks.AbstractQueuedSynchronizer {
      // initial state is 0, for "closed"
      // we don't need the "Shared" versions as we'll have only one waiter
      override protected final def tryRelease(releases: Int): Boolean =
        compareAndSetState(0, 1) // if was closed, open it, unblock waiters
      override protected final def tryAcquire(releases: Int): Boolean =
        // called only if processes is empty; see detailed explanation above
        compareAndSetState(1, 0) // if was open, close it, else block
    }

    def add(handle: GenericProcessHandle): Unit = {
      processes.put(handle.pid(), handle)
      assert(
        watcherThread.isAlive(),
        "GenericProcessWatcher watch thread is terminated"
      )
      processesMutex.release(1)
    }

    @alwaysinline
    def remove(pid: Long): GenericProcessHandle = processes.remove(pid)

    private final val watcherThread: Thread = Thread
      .ofPlatform()
      .daemon(true)
      .group(ThreadGroup.System)
      .name("ScalaNative-GenericProcessWatcher")
      .startInternal(Task)

    private object Task extends Runnable {
      override def run(): Unit = {
        while (true) Try {
          removeProcessesIf(_.hasExited)
          while (processes.isEmpty()) processesMutex.acquire(1)
          if (!reapSomeProcesses()) Thread.sleep(100) // ms
        }
      }
    }

    // return true if something has been reaped
    private val reapSomeProcesses: () => Boolean =
      if (LinktimeInfo.isWindows) claimAllCompleted
      else if (ProcessExitChecker.unixFactoryOpt.isDefined) claimAllCompleted
      else () => ProcessExitCheckerWaitpid.waitAndReap(-1, 0, None)

    @alwaysinline
    def claimAllCompleted(): Boolean = removeProcessesIf(_.checkIfExited())

    @alwaysinline
    def removeProcessesIf(f: GenericProcessHandle => Boolean): Boolean =
      removeIf(processes.values())(f)
  }

  implicit val processRegistry: ProcessRegistry = new ProcessRegistry {
    override def completeWith(pid: Long)(ec: Int): Unit = {
      val ref = Processes.remove(pid)
      if (ref ne null) ref.setCachedExitCode(ec)
    }
  }

  private def removeIf[A](coll: ju.Collection[A])(f: A => Boolean): Boolean = {
    var ok = false
    val it = coll.iterator()
    while (it.hasNext()) {
      val ref = it.next()
      try
        if (f(ref)) {
          ok = true
          it.remove()
        }
      catch {
        case ex: Throwable if !ex.isInstanceOf[InterruptedException] =>
          System.err.println("[GenericProcessWatcher] failure:")
          ex.printStackTrace()
      }
    }
    ok
  }

}
