package java.lang.process

import java.util.concurrent.{CompletableFuture, TimeUnit, TimeoutException}
import java.util.stream.Stream
import java.util.{Optional, function}

import scala.scalanative.javalib.io.ObjectHandle

// Represents ProcessHandle for process started by Scala Native runtime
// Cannot be used with processes started by other programs
private[process] abstract class GenericProcessHandle(
    protected val processId: ObjectHandle
) extends ProcessHandle {
  protected def getExitCodeImpl: Option[Int]
  protected def destroyImpl(force: Boolean): Boolean

  /** Closes any resources associated with a running process.
   *
   *  After this call completes, no attempts should be made to call [[close]]
   *  itself, or any of the `Impl` methods above.
   *
   *  Currently, [[close]] is called only by [[setCachedExitCode]] upon
   *  successful reaping of the terminated child.
   */
  protected def close(): Unit = exitChecker.close()

  val builder: ProcessBuilder

  private val processInfo: GenericProcessInfo = GenericProcessInfo(builder)
  protected val completion = new CompletableFuture[java.lang.Integer]()

  /* Make sure we have exactly one entity calling waitpid on a process.
   * Reasons are having fewer kernel interactions, plus an unlikely but
   * theoretically possible scenario whereby by the time a second waiter
   * attempts to waitpid (hoping for an ECHILD), a completely new child
   * had been forked with the same pid. */
  protected val exitChecker: ProcessExitChecker = {
    val useWatcher = // see if we use GenericProcessWatcher
      if (GenericProcessWatcher.isEnabled)
        ProcessExitChecker.factory.isInstanceOf[ProcessExitChecker.MultiFactory]
      else false
    if (useWatcher)
      ProcessExitCheckerCompletion
    else {
      implicit val processRegistry: ProcessRegistry = new ProcessRegistry {
        override def completeWith(pid: Long)(ec: Int): Boolean =
          setOrCheckCachedExitCode(ec)
      }
      ProcessExitChecker.factory.createSingle(processId)
    }
  }

  override final def isAlive(): Boolean = !hasExited

  final def checkIfExited(): Boolean =
    hasExited || checkAndSetExitCode() || hasExited

  final def hasExited: Boolean = completion.isDone()

  final def getCachedExitCode: Option[Int] = {
    if (!completion.isDone()) None
    else if (completion.isCompletedExceptionally()) Some(-1)
    else {
      val res = completion.getNow(null)
      Some(if (res == null) -1 else res.intValue())
    }
  }

  final def setOrCheckCachedExitCode(value: Int): Boolean =
    if (value < 0) checkIfExited() else setCachedExitCode(value)

  final def setCachedExitCode(value: Int): Boolean = {
    val ok = completion.complete(value)
    if (ok) close()
    ok
  }

  protected final def checkAndSetExitCode(): Boolean =
    getExitCodeImpl.exists(setCachedExitCode)

  def onExitApply[A <: AnyRef](
      fn: function.Function[java.lang.Integer, A]
  ): CompletableFuture[A] =
    completion.thenApplyAsync(fn)

  def onExitHandle[A <: AnyRef](
      fn: function.BiFunction[java.lang.Integer, Throwable, A]
  ): CompletableFuture[A] =
    completion.handleAsync(fn)

  override def parent(): Optional[ProcessHandle] = Optional.empty()

  // We don't track transitive children
  override def children(): Stream[ProcessHandle] = Stream.empty()
  override def descendants(): Stream[ProcessHandle] = Stream.empty()

  override final def destroy(): Boolean = destroy(force = false)
  override final def destroyForcibly(): Boolean = destroy(force = true)
  @inline private def destroy(force: Boolean) =
    hasExited || destroyImpl(force = force)

  private def waitForWith(check: => Boolean) = hasExited || check && hasExited
  def waitFor(): Boolean = waitForWith(exitChecker.waitAndReapSome(0, None))
  def waitFor(timeout: scala.Long, unit: TimeUnit): Boolean =
    waitForWith(
      timeout > 0L && exitChecker.waitAndReapSome(timeout, Some(unit))
    )

  override def onExit(): CompletableFuture[ProcessHandle] =
    onExitApply(_ => this: ProcessHandle)

  override def info(): ProcessHandle.Info = processInfo

  override def compareTo(other: ProcessHandle): Int = other match {
    case other: GenericProcessHandle =>
      val res = pid().compareTo(other.pid())
      if (res != 0) res
      else processInfo.createdAt.compareTo(other.processInfo.createdAt)
    case _ => -1
  }
  override def equals(that: Any): Boolean = that match {
    case other: ProcessHandle => this.compareTo(other) == 0
    case _                    => false
  }
  override def hashCode(): Int =
    ((31 * this.pid().##) * 31) + processInfo.createdAt.##
  override def toString: String =
    s"Process[pid=${pid()}, exitValue=${getCachedExitCode.getOrElse("\"not exited\"")}"

  protected object ProcessExitCheckerCompletion extends ProcessExitChecker {
    override def close(): Unit = {}
    override def waitAndReapSome(
        timeout: Long,
        unitOpt: Option[TimeUnit]
    ): Boolean =
      unitOpt.fold { completion.get(); true } { unit =>
        timeout > 0L && {
          try { completion.get(timeout, unit); true }
          catch { case _: TimeoutException => false }
        }
      }
  }

}
