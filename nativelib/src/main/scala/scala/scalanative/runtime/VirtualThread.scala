package scala.scalanative
package runtime

import java.lang.Thread

/** Marker + monitor API for Scala Native virtual threads. Implementation lives in
 *  `java.lang.VirtualThread` (`VirtualThreadImpl` in javalib, Scala 3).
 */
trait VirtualThread extends Any { self: Thread =>

  import VirtualThread._

  def blockForMonitorWait(nanos: scala.Long, setResume: SetResume): Unit
  def blockForMonitorEnter(setResume: SetResume): Unit
  def scheduleWithResume(resume: () => Unit, generation: scala.Long): Unit
  def carrierForUnpark: Thread
  def signalBlockPermit(): Unit
}

object VirtualThread {
  type SetResume = (() => Unit, scala.Long) => Unit

  /** Sentinel on WaiterNode.resumeForWait so ObjectMonitor spin-waits for real resume. */
  val RESUME_SENTINEL: () => Unit = () => ()
}
