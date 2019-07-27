package java.util.concurrent.locks

abstract class AbstractQueuedSynchronizer protected ()
    extends AbstractOwnableSynchronizer() {

  def acquireSharedInterruptibly(arg: Int): Unit = ()

  def releaseSharedInterruptibly(arg: Int): Boolean = true

  def releaseShared(arg: Int): Boolean = true

  override def toString(): String = "AbstractQueuedSynchronizer"

  def tryAcquireSharedNanos(arg: Int, nanos: Long): Boolean = true

}
