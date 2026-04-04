package scala.scalanative
package runtime

import java.lang.Thread

/** Marker for Scala Native virtual threads.
 *
 *  Platform code (for example `ObjectMonitor`) calls into these hooks.
 */
trait VirtualThread extends Any { self: Thread =>
  import VirtualThread.*

  /** Platform carrier thread while this virtual thread is mounted, or `null`
   *  when unmounted.
   */
  def carrierThread: Thread

  /** Pool used to run this fiber again after async wakeups (park unpark,
   *  monitor notify, etc.).
   */
  def scheduler: VirtualThreadScheduler

  // ObjectMonitor integration (Object.wait / contended monitor enter)
  protected type Continuation = () => Unit
  protected type SetResumeContinuation = (Continuation, scala.Long) => Unit
  protected def doBlockForMonitorWait(
      nanos: scala.Long,
      setResume: SetResumeContinuation
  ): Unit
  private[runtime] def blockForMonitorWait(
      nanos: scala.Long,
      setResume: SetResumeContinuation
  ): Unit =
    doBlockForMonitorWait(nanos, setResume)

  private[runtime] def blockForMonitorEnter(
      setResume: SetResumeContinuation
  ): Unit =
    doBlockForMonitorEnter(setResume)
  protected def doBlockForMonitorEnter(setResume: SetResumeContinuation): Unit

  private[runtime] def scheduleWithResume(
      resume: Continuation,
      generation: scala.Long
  ): Unit = doScheduleWithResume(resume, generation)
  protected def doScheduleWithResume(
      resume: Continuation,
      generation: scala.Long
  ): Unit
}

object VirtualThread {
  private[runtime] var defaultScheduler: VirtualThreadScheduler /*| Null*/ = _

  /** Defines the scheduler for virtual threads whose creator is not itself a
   *  virtual thread (typically the first carrier used when spawning VTs from
   *  platform threads).
   */
  def setDefaultScheduler(scheduler: VirtualThreadScheduler): Unit = {
    defaultScheduler = scheduler
  }
}
