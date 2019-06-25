package scala.scalanative.runtime

import scalanative.annotation.alwaysinline

sealed class Monitor private () {
  @alwaysinline def _notify(): Unit                              = ()
  @alwaysinline def _notifyAll(): Unit                           = ()
  @alwaysinline def _wait(): Unit                                = ()
  @alwaysinline def _wait(timeout: scala.Long): Unit             = ()
  @alwaysinline def _wait(timeout: scala.Long, nanos: Int): Unit = ()
  @alwaysinline def enter(): Unit                                = ()
  @alwaysinline def exit(): Unit                                 = ()
}

object Monitor {
  @alwaysinline def dummy: Monitor = new Monitor
}
