package java.lang.process

import scala.scalanative.unsafe._
import scala.scalanative.meta.LinktimeInfo

import java.time.{Duration, Instant}
import java.{util => ju}
import ju.{Optional, Arrays}
import ju.concurrent.{CompletableFuture, ConcurrentHashMap, TimeUnit}

abstract class GenericProcess() extends java.lang.Process {
  private lazy val handle = new GenericProcess.Handle(this)

  private[lang] def checkResult(): CInt
  private[process] def processInfo: GenericProcess.Info

  override def toHandle(): ProcessHandle = handle

  override def onExit(): CompletableFuture[Process] =
    toHandle().onExit().thenApply(_ => this)

  override def info(): ProcessHandle.Info = processInfo
  override def pid(): scala.Long = processInfo.pid
}

private object GenericProcess {
  // Represents ProcessHandle for process started by Scala Native runtime
  // Cannot be used with processes started by other programs
  class Handle(val process: GenericProcess) extends ProcessHandle {
    private val createdAt = System.nanoTime()

    override def parent(): Optional[ProcessHandle] = Optional.empty()

    // We don't track transitive children
    override def children(): ju.stream.Stream[ProcessHandle] =
      ju.stream.Stream.empty()
    override def descendants(): ju.stream.Stream[ProcessHandle] =
      ju.stream.Stream.empty()

    override def destroy(): Boolean = {
      if (isAlive()) process.destroy()
      true // all implementations are always successful
    }
    override def destroyForcibly(): Boolean = {
      if (isAlive()) process.destroyForcibly()
      true // all implementations are always successful
    }
    override def supportsNormalTermination(): Boolean =
      process.supportsNormalTermination()

    override def onExit(): CompletableFuture[ProcessHandle] = {
      val completion = new CompletableFuture[ProcessHandle]()
      if (LinktimeInfo.isMultithreadingEnabled)
        GenericProcess.ProcessWatcher.watchForTermination(completion, this)
      completion
    }

    override def pid(): scala.Long = process.pid()
    override def isAlive(): Boolean = process.isAlive()
    override def info(): ProcessHandle.Info = process.info()

    override def compareTo(other: ProcessHandle): Int = other match {
      case handle: GenericProcess.Handle =>
        this.process.pid().compareTo(handle.process.pid()) match {
          case 0     => this.createdAt.compareTo(handle.createdAt)
          case value => value
        }
      case _ => -1
    }
    override def equals(that: Any): Boolean = that match {
      case other: ProcessHandle => this.compareTo(other) == 0
      case _                    => false
    }
    override def hashCode(): Int =
      ((31 * this.pid().##) * 31) + this.createdAt.##
    override def toString(): String = process.pid().toString() // JVM compliance
  }

  object Info {
    def create(builder: ProcessBuilder, pid: scala.Long): Info = {
      val cmd = builder.command()
      val cmdAsArray = cmd.toArray(new Array[String](cmd.size()))
      val command =
        if (cmd.isEmpty()) Optional.empty[String]()
        else Optional.of(cmdAsArray.head)
      val args =
        if (cmd.isEmpty()) Optional.empty[Array[String]]()
        else Optional.of(cmdAsArray.tail)
      new Info(pid = pid, cmd = command, args = args)
    }
  }
  class Info(
      val pid: scala.Long,
      cmd: Optional[String],
      args: Optional[Array[String]]
  ) extends ProcessHandle.Info {
    override def command(): Optional[String] = cmd
    override def arguments(): Optional[Array[String]] = args
    override def commandLine(): Optional[String] =
      // For comprehension variant does not compile on Scala 2.12
      command().flatMap[String] { cmd =>
        arguments().map[String] { args =>
          s"$cmd ${args.mkString(" ")}"
        }
      }

    override def user(): Optional[String] =
      Optional.ofNullable(System.getProperty("user.name"))

    // Instant not implemented
    override def startInstant(): Optional[Instant] = Optional.empty()
    override def totalCpuDuration(): Optional[Duration] = Optional.empty()

    override def toString(): String = {
      val args = this
        .arguments()
        .orElseGet(() => Array[String]())
        .asInstanceOf[Array[AnyRef]]
      s"[user: ${user()}, cmd: ${{ command() }}, args: ${Arrays.toString(args)}"
    }
  }

  object ProcessWatcher {
    // Identity map, no valid concurrent structure
    val watchedProcesses =
      new ConcurrentHashMap[CompletableFuture[
        ProcessHandle
      ], GenericProcess.Handle]()

    private val lock = new ju.concurrent.locks.ReentrantLock()
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

    lazy val watcherThread: Thread = Thread
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
            watchedProcesses.forEach { (completion, handle) =>
              if (completion.isCancelled())
                watchedProcesses.remove(completion)
              else if (handle.process.waitFor(5, TimeUnit.MILLISECONDS)) {
                completion.complete(handle)
                watchedProcesses.remove(completion)
              }
            }
            Thread.sleep(100 /*ms*/ )
          } catch { case scala.util.control.NonFatal(_) => () }
        } finally lock.unlock()
      }
  }
}
