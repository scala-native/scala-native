package scala.scalanative.runtime

import java.util.concurrent.TimeUnit

/** Schedules virtual-thread continuations and delayed work. Default implementation is in
  * javalib (`DefaultVirtualThreadScheduler`); alternate schedulers (e.g. event-loop based) may
  * implement this trait for embedding.
  */
trait VirtualThreadScheduler {
  def execute(task: Runnable): Unit
  def schedule(task: Runnable, delay: scala.Long, unit: TimeUnit): VirtualThreadScheduler.Cancellable

  def lazyExecute(task: Runnable): Unit = execute(task)
  def isCarrierThread(thread: Thread): scala.Boolean = false
  def isCarrierIdle(thread: Thread): scala.Boolean = false
}

object VirtualThreadScheduler {
  trait Cancellable {
    def cancel(): scala.Boolean
    def isDone(): scala.Boolean
  }
}
