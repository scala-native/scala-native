package java.lang.impl

import scala.scalanative.unsigned._
import scala.scalanative.windows.SynchApi._

object WindowsThread {
  def sleep(millis: scala.Long, nanos: scala.Int): Unit = {
    // No support for nanos sleep on Windows
    Sleep(millis.toUInt)
    if (Thread.interrupted()) {
      throw new InterruptedException("Sleep was interrupted")
    }
  }
}
