package java.lang.impl

import scala.scalanative.unsigned._
import scala.scalanative.windows.SynchApi._

object WindowsThread {
  def sleep(millis: scala.Long, nanos: scala.Int): Unit = {
    // No support for nanos sleep on Windows,
    // assume minimal granularity equal to 1ms
    val sleepForMillis = nanos match {
      case 0 => millis
      case _ => millis + 1
    }
    // Make sure that we don't pass 0 as argument, otherwise it would
    // sleep infinitely.
    Sleep(sleepForMillis.max(1L).toUInt)
    if (Thread.interrupted()) {
      throw new InterruptedException("Sleep was interrupted")
    }
  }
}
