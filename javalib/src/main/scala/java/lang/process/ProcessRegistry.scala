package java.lang.process

private[process] trait ProcessRegistry {
  def complete(pid: Long): Boolean
  def completeWith(pid: Long)(ec: => Int): Boolean
}
