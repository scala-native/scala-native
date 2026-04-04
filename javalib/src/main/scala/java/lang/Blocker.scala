package java.lang

import scala.scalanative.annotation.alwaysinline

/** Marks possibly-blocking regions when a [[VirtualThread]] runs on a
 *  [[VirtualThreadCarrier]], so the default scheduler can compensate the
 *  [[java.util.concurrent.ForkJoinPool]].
 */
private[java] object Blocker {

  /** Runs `body` with `begin`/`end` pairing; inlined at every call site. */
  @alwaysinline
  def apply[T](body: => T): T = {
    val attempted = begin()
    try body
    finally end(attempted)
  }

  /** Like [[apply]] when `mayBlock` mirrors JDK `Blocker.begin(boolean)`. */
  @alwaysinline
  def when[T](mayBlock: scala.Boolean)(body: => T): T =
    if (mayBlock) apply(body)
    else body

  def begin(): scala.Boolean =
    if (!Thread.currentThread().isVirtual()) false
    else
      Thread.currentThread() match {
        case vt: VirtualThread =>
          val carrier = Thread.currentCarrierThread()
          vt.scheduler.beginCarrierCompensatedBlock(carrier)
        case _ => false
      }

  def begin(blocking: scala.Boolean): scala.Boolean =
    if (blocking) begin()
    else false

  def end(attempted: scala.Boolean): Unit =
    if (attempted)
      Thread.currentThread() match {
        case vt: VirtualThread =>
          val carrier = Thread.currentCarrierThread()
          vt.scheduler.endCarrierCompensatedBlock(carrier, true)
        case _ => ()
      }
}
