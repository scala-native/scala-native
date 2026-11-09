package java.lang.impl

import scala.scalanative.annotation.alwaysinline
import scala.scalanative.libc.stdatomic.AtomicInt

/** Lets unpark run without the target thread's monitor while termination
 *  releases its native park resources. The guard holds a Closed bit and the
 *  number of unpark calls still using those resources.
 */
private[impl] object UnparkGuard {
  private final val Closed = Int.MinValue

  @alwaysinline def tryEnter(guard: AtomicInt): Boolean =
    (guard.fetchAdd(1) & Closed) == 0 || {
      guard.fetchSub(1)
      false
    }

  @alwaysinline def exit(guard: AtomicInt): Unit = {
    guard.fetchSub(1)
    ()
  }

  /** Rejects new unpark calls and waits for in-flight ones to finish. */
  def close(guard: AtomicInt): Unit = {
    guard.fetchOr(Closed)
    while (guard.load() != Closed) Thread.`yield`()
  }
}
