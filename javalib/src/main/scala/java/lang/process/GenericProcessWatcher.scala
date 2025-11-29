package java.lang.process

import java.{lang => jl, util => ju}

import scala.util.Try

import scala.scalanative.annotation.alwaysinline
import scala.scalanative.meta.LinktimeInfo
import scala.scalanative.unsafe.resolvedAtLinktime

private[process] object GenericProcessWatcher {

  @resolvedAtLinktime()
  def isEnabled: Boolean = LinktimeInfo.isMultithreadingEnabled

  import ju.concurrent._

  @alwaysinline def watchForTermination(handle: GenericProcessHandle): Unit =
    if (isEnabled) Processes.add(handle)

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

    private val exitChecker = ProcessExitChecker.factory match {
      case factory: ProcessExitChecker.MultiFactory =>
        implicit val processRegistry: ProcessRegistry = new ProcessRegistry {
          override def completeWith(pid: Long)(ec: Int): Boolean = {
            val ref = remove(pid)
            (ref eq null) || ref.setOrCheckCachedExitCode(ec)
          }
        }
        factory.createMulti
      case _ => AllProcessExitChecker
    }

    def add(handle: GenericProcessHandle): Unit = {
      val pid = handle.pid()
      // add to processes first, before registering with exit checker
      // otherwise, exit checker might try to complete a quick exit and fail
      processes.put(pid, handle)
      if (exitChecker.addOrReap(handle)) {
        assert(
          watcherThread.isAlive(),
          "GenericProcessWatcher watch thread is terminated"
        )
        processesMutex.release(1)
      } else {
        processes.remove(pid)
        if (!handle.checkIfExited())
          throw new RuntimeException(
            s"Failed to register $pid for exit checking"
          )
      }
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
          if (!exitChecker.waitAndReapSome(0, None)) Thread.sleep(100) // ms
        }
      }
    }

    @alwaysinline
    def claimAllCompleted(): Boolean = removeProcessesIf(_.checkIfExited())

    @alwaysinline
    def removeProcessesIf(f: GenericProcessHandle => Boolean): Boolean =
      removeIf(processes.values())(f)
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

  object AllProcessExitChecker extends ProcessExitChecker.Multi {
    override def addOrReap(handle: GenericProcessHandle): Boolean = true
    override def close(): Unit = {}
    override def waitAndReapSome(
        timeout: Long,
        unitOpt: Option[TimeUnit]
    ): Boolean = Processes.claimAllCompleted()
  }

}
