package scala.scalanative.runtime.monitor

import java.lang.Thread
import java.util.concurrent.locks.LockSupport

/** Registry for monitor wait/unpark callbacks so virtual threads can block in
 *  Object.wait() without consuming the LockSupport permit and without parking
 *  the carrier (so many VTs can wait on the same monitor). blockForMonitorEnter
 *  lets virtual threads yield on contended monitor enter.
 */
object MonitorWaitSupport {
  type SetResume = (() => Unit, Long) => Unit
  type BlockForMonitorWait = (Thread, Long, SetResume) => Unit

  /** VT blocks on monitor enter: suspend and register resume; monitor exit will
   *  call scheduleWithResume.
   */
  type BlockForMonitorEnter = (Thread, SetResume) => Unit
  type ScheduleWithResume = (Thread, () => Unit, Long) => Unit
  type GetCarrierForUnpark = Thread => Thread

  /** Sentinel placed on WaiterNode.resumeForWait before releasing the monitor
   *  so onExit knows a continuation resume is forthcoming and can spin-wait
   *  instead of falling back to LockSupport.unpark (which would consume the
   *  LockSupport parking permit).
   */
  val RESUME_SENTINEL: () => Unit = () => ()

  /** Returns true if the given thread uses continuation-based resume for
   *  Object.wait(). Set by VirtualThread registration; default returns false.
   */
  @volatile var usesContResume: Thread => Boolean = _ => false

  @volatile var blockForMonitorWait: BlockForMonitorWait = (_, nanos, _) =>
    if (nanos == 0) LockSupport.park()
    else LockSupport.parkNanos(nanos)

  /** Set by VirtualThread registration; default no-op (platform threads use
   *  carrier park in ObjectMonitor).
   */
  @volatile var blockForMonitorEnter: BlockForMonitorEnter = (_, _) => ()

  @volatile var scheduleWithResume: ScheduleWithResume = (thread, _, _) =>
    LockSupport.unpark(thread)

  @volatile var getCarrierForUnpark: GetCarrierForUnpark = t => t

  /** Signal a VT that it has been selected as successor but its resumeForEnter
   *  callback is not yet available (VT is between clearing and re-suspending).
   *  Sets blockPermit so the VT's blockForMonitorEnter will return early
   *  instead of suspending. No-op for platform threads.
   */
  type SignalBlockPermit = Thread => Unit
  @volatile var signalBlockPermit: SignalBlockPermit = _ => ()
}
