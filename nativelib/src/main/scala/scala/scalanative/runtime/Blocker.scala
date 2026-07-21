package scala.scalanative
package runtime

import java.lang.Thread

import scala.scalanative.annotation.alwaysinline

/** Marks possibly-blocking regions when a [[VirtualThread]] runs on a carrier
 *  thread. Applied automatically by the compiler to the extern blocking calls.
 */
private[runtime] object Blocker {

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
    Thread.currentThread() match {
      case vt: VirtualThread =>
        val carrier = vt.carrierThread
        if (carrier eq null) false
        else vt.scheduler.beginCarrierCompensatedBlock(carrier)
      case _ => false
    }

  def begin(blocking: scala.Boolean): scala.Boolean =
    if (blocking) begin()
    else false

  def end(attempted: scala.Boolean): Unit =
    if (attempted)
      Thread.currentThread() match {
        case vt: VirtualThread =>
          val carrier = vt.carrierThread
          if (carrier ne null)
            vt.scheduler.endCarrierCompensatedBlock(carrier, true)
        case _ => ()
      }
}
