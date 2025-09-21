package java.lang.process

import scala.scalanative.unsafe._
import scala.scalanative.meta.LinktimeInfo

import java.{util => ju}
import ju.Optional
import ju.concurrent.CompletableFuture

private[process] abstract class GenericProcess() extends Process {
  protected val builder: ProcessBuilder

  private val processInfo = GenericProcessInfo(builder)
  private lazy val handle = new GenericProcess.Handle(this)

  def checkResult(): CInt

  override def toHandle(): ProcessHandle = handle

  override def onExit(): CompletableFuture[Process] =
    toHandle().onExit().thenApply(_ => this)

  override def info(): ProcessHandle.Info = processInfo
}

private[lang] object GenericProcess {
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
        GenericProcessWatcher.watchForTermination(completion, this)
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

  def apply(pb: ProcessBuilder): GenericProcess =
    if (LinktimeInfo.isWindows) WindowsProcess(pb) else UnixProcess(pb)

}
