// revision 1.47
/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent

trait Future[V] {

  def cancel(mayInterruptIfRunning: Boolean): Boolean

  def isCancelled(): Boolean

  def isDone(): Boolean

  @throws[InterruptedException]
  @throws[ExecutionException]
  def get(): V

  @throws[InterruptedException]
  @throws[ExecutionException]
  @throws[TimeoutException]
  def get(timeout: Long, unit: TimeUnit): V

  // since JDK 19
  def resultNow(): V = {
    if (!isDone())
      throw new IllegalStateException("Task has not completed")
    var interrupted = false
    try
      while (true) {
        try return get()
        catch {
          case _: InterruptedException => interrupted = true
          case _: ExecutionException =>
            throw new IllegalStateException("Task completed with exception")
          case _: CancellationException =>
            throw new IllegalStateException("Task was cancelled")
        }
      }
    finally {
      if (interrupted)
        Thread.currentThread().interrupt()
    }
    ??? // unreachable
  }

  // Since JDK 19
  def exceptionNow(): Throwable = {
    if (!isDone())
      throw new IllegalStateException("Task has not completed")
    if (isCancelled())
      throw new IllegalStateException("Task was cancelled")
    var interrupted = false
    try
      while (true) {
        try {
          get()
          throw new IllegalStateException("Task completed with a result")
        } catch {
          case _: InterruptedException => interrupted = true
          case e: ExecutionException   => return e.getCause()
        }
      }
    finally {
      if (interrupted)
        Thread.currentThread().interrupt()
    }
    ??? // unreachable
  }

  def state(): Future.State = {
    if (!isDone())
      return Future.State.RUNNING
    if (isCancelled())
      return Future.State.CANCELLED

    var interrupted = false
    try
      while (true) {
        try {
          get() // may throw InterruptedException when done
          return Future.State.SUCCESS
        } catch {
          case _: InterruptedException =>
            interrupted = true
          case _: ExecutionException =>
            return Future.State.FAILED
        }
      }
    finally {
      if (interrupted) Thread.currentThread().interrupt()
    }
    ??? // unreachable
  }
}

object Future {
  // Since JDK 19
  sealed class State(name: String, ordinal: Int)
      extends java.lang._Enum[State](name, ordinal) {
    override def toString() = this.name
  }
  object State {
    final val RUNNING = new State("RUNNING", 0)
    final val SUCCESS = new State("SUCCESS", 1)
    final val FAILED = new State("FAILED", 2)
    final val CANCELLED = new State("CANCELLED", 3)

    private[this] val cachedValues =
      Array(RUNNING, SUCCESS, FAILED, CANCELLED)
    def values(): Array[State] = cachedValues.clone()
    def valueOf(name: String): State = {
      cachedValues.find(_.name() == name).getOrElse {
        throw new IllegalArgumentException(
          "No enum const java.util.concurrent.Future.State." + name
        )
      }
    }
  }
}
