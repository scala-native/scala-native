package java.lang.process

import java.{lang => jl, util => ju}

import scala.util.Try

import scala.scalanative.meta.LinktimeInfo

private[process] object GenericProcessWatcher extends ProcessRegistry {

  import ju.concurrent._

  private val processes = new ConcurrentHashMap[jl.Long, GenericProcessHandle]

  private val lock = new locks.ReentrantLock()
  private val hasProcessesToWatch = lock.newCondition()

  def watchForTermination(handle: GenericProcessHandle): Unit = {
    val pid = handle.pid()
    processes.put(pid, handle)
    eventWatcher.add(pid)
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
          if (!eventWatcher.waitAndReapSome(0, None)(this))
            Thread.sleep(100) // ms
        }
      } finally {
        lock.unlock()
        eventWatcher.close()
      }
    }

  private lazy val eventWatcher: EventWatcher =
    EventWatcher.factoryOpt.map(_.create()).getOrElse {
      if (LinktimeInfo.isWindows) AllEventWatcher else AnyEventWatcher
    }

  override def complete(pid: Long): Boolean = {
    var ok = false
    processes.computeIfPresent(
      pid,
      (_, ref) => {
        ok = ref.checkIfExited()
        if (ok) null else ref
      }
    )
    ok
  }

  override def completeWith(pid: Long)(ec: => Int): Boolean = {
    val ref = processes.remove(pid)
    (ref ne null) && ref.setCachedExitCode(ec)
  }

  def removeCompleted(): Unit =
    removeSomeProcesses(_.getValue().hasExited)

  private def removeSomeProcesses(
      f: ju.Map.Entry[jl.Long, GenericProcessHandle] => Boolean
  ): Boolean = {
    var ok = false
    val it = processes.entrySet().iterator()
    while (it.hasNext()) if (f(it.next())) {
      ok = true
      it.remove()
    }
    ok
  }

  object AnyEventWatcher extends EventWatcher {
    override def add(pid: Long): Unit = {}
    override def close(): Unit = {}
    override def waitAndReapSome(timeout: Long, unitOpt: Option[TimeUnit])(
        pr: ProcessRegistry
    ): Boolean = UnixProcessHandle.waitpidAny(pr)
  }

  object AllEventWatcher extends EventWatcher {
    override def add(pid: Long): Unit = {}
    override def close(): Unit = {}

    override def waitAndReapSome(timeout: Long, unitOpt: Option[TimeUnit])(
        pr: ProcessRegistry
    ): Boolean = removeSomeProcesses(_.getValue().checkIfExited())
  }

}
