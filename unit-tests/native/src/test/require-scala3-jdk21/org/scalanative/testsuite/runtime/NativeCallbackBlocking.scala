package org.scalanative.testsuite.runtime

import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.LockSupport

import scala.scalanative.runtime.NativeThread
import scala.scalanative.unsafe.CFuncPtr0

/** Blocking native callback; outside runtime packages so it is guarded. */
object NativeCallbackBlocking {
  object Mode {
    final val TimedPark = 0
    final val Park = 1
    final val MonitorWait = 2
  }

  @volatile private var mode = Mode.TimedPark
  @volatile var carrierChanged = false
  private val monitor = new Object

  def reset(selected: Int): Unit = {
    mode = selected
    carrierChanged = false
  }

  def wake(thread: Thread): Unit = mode match {
    case Mode.Park        => LockSupport.unpark(thread)
    case Mode.MonitorWait => monitor.synchronized(monitor.notifyAll())
    case _                => ()
  }

  val callback: CFuncPtr0[Int] = () => {
    val carrier = NativeThread.currentNativeThread
    mode match {
      case Mode.TimedPark =>
        LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(20))
      case Mode.Park =>
        LockSupport.park()
      case Mode.MonitorWait =>
        monitor.synchronized(monitor.wait())
    }
    carrierChanged = NativeThread.currentNativeThread ne carrier
    1
  }
}
