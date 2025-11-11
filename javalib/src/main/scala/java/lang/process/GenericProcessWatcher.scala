package java.lang.process

import java.{lang => jl, util => ju}

import scala.util.Try

import scala.scalanative.annotation.alwaysinline
import scala.scalanative.meta.LinktimeInfo

private[process] object GenericProcessWatcher {

  import ju.concurrent._

  @alwaysinline
  def watchForTermination(handle: GenericProcessHandle): Unit =
    Processes.add(handle)

  private object Processes {
    private val processes = new ConcurrentHashMap[jl.Long, GenericProcessHandle]

    private val exitChecker: ProcessExitChecker.Multi =
      ProcessExitChecker.factoryOpt
        .fold[ProcessExitChecker.Multi](AllProcessExitChecker)(_.createMulti)

    implicit val pr: ProcessRegistry = new ProcessRegistry {
      override def completeWith(pid: Long)(ec: Int): Unit =
        complete(pid, ec)
    }

    def add(handle: GenericProcessHandle): Unit = {
      val pid = handle.pid()
      // add to processes first, before registering with exit checker
      // otherwise, exit checker might try to complete a quick exit and fail
      processes.put(pid, handle)
      if (!exitChecker.addOrReap(handle)) {
        processes.remove(pid)
        if (!handle.checkIfExited())
          throw new RuntimeException(
            s"Failed to register $pid for exit checking"
          )
      }
    }

    private final val watcherThread: Thread = Thread
      .ofPlatform()
      .daemon(true)
      .group(ThreadGroup.System)
      .name("ScalaNative-GenericProcessWatcher")
      .priority((Thread.NORM_PRIORITY + Thread.MAX_PRIORITY) / 2)
      .startInternal(Task)

    private object Task extends Runnable {
      override def run(): Unit = {
        while (true) Try {
          removeProcessesIf(_.hasExited)
          if (!exitChecker.waitAndReapSome(0, None)) Thread.sleep(100) // ms
        }
      }
    }

    @alwaysinline
    def claimAllCompleted(): Boolean =
      removeProcessesIf(_.checkIfExited())

    def complete(pid: Long, ec: Int): Unit = {
      val ref = processes.remove(pid)
      if (ref ne null) ref.setCachedExitCode(ec)
    }

    @alwaysinline
    def removeProcessesIf(f: GenericProcessHandle => Boolean): Boolean =
      removeIf(processes.values())(f)
  }

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

  object AllProcessExitChecker extends ProcessExitChecker.Multi {
    override def addOrReap(handle: GenericProcessHandle): Boolean = true
    override def close(): Unit = {}
    override def waitAndReapSome(
        timeout: Long,
        unitOpt: Option[TimeUnit]
    ): Boolean = Processes.claimAllCompleted()
  }

}
