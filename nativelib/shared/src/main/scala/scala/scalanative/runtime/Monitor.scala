package scala.scalanative.runtime

final class Monitor private () {
  def _notify(): Unit                              = ()
  def _notifyAll(): Unit                           = ()
  def _wait(): Unit                                = ()
  def _wait(timeout: scala.Long): Unit             = ()
  def _wait(timeout: scala.Long, nanos: Int): Unit = ()
  def enter(): Unit                                = ()
  def exit(): Unit                                 = ()
}

object Monitor {
  val dummy = new Monitor()
}
