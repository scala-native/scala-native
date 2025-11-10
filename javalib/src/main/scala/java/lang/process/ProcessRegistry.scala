package java.lang.process

private[process] trait ProcessRegistry {
  def completeWith(pid: Long)(ec: Int): Unit
}
