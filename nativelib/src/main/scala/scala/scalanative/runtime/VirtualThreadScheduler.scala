package scala.scalanative.runtime

import java.util.concurrent.TimeUnit

/** Schedules virtual-thread continuations and delayed work. Default
 *  implementation is in javalib (`DefaultVirtualThreadScheduler`); alternate
 *  schedulers (e.g. event-loop based) may implement this trait for embedding.
 */
trait VirtualThreadScheduler {
  def execute(task: Runnable): Unit
  def schedule(
      task: Runnable,
      delay: scala.Long,
      unit: TimeUnit
  ): VirtualThreadScheduler.Cancellable

  def lazyExecute(task: Runnable): Unit = execute(task)
  def isCarrierThread(thread: Thread): scala.Boolean = false
  def isCarrierIdle(thread: Thread): scala.Boolean = false

  /** Invoked when a virtual thread may block in native code on `carrier`.
   *  ForkJoin-based schedulers may grow the pool; others usually no-op.
   *
   *  @return
   *    `true` if compensation started — must be paired with
   *    `endCarrierCompensatedBlock`.
   */
  def beginCarrierCompensatedBlock(carrier: Thread): scala.Boolean = false

  def endCarrierCompensatedBlock(
      carrier: Thread,
      attempted: scala.Boolean
  ): Unit = ()

  /** Invoked on `carrier` after a virtual thread unmounts following a
   *  continuation segment (park, monitor block, yield, etc.)
   */
  def afterYieldOnCarrier(carrier: Thread): Unit = ()
}

object VirtualThreadScheduler {
  trait Cancellable {
    def cancel(): scala.Boolean
    def isDone(): scala.Boolean
  }
}
