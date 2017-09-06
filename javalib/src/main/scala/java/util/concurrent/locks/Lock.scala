package java.util.concurrent
package locks

trait Lock {

  def lock(): Unit

  def lockInterruptibly(): Unit

  def newCondition(): Condition

  def tryLock(): Boolean

  def tryLock(time: Long, unit: TimeUnit): Boolean

  def unlock(): Unit

}
