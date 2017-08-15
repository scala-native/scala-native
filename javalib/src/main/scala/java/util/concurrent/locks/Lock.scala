package java.util.concurrent
package locks

trait Lock {

  def lock(): Unit

  def lockInterruptibility(): Unit

  def newCondition(): Condition

  def tryLock(): Boolean

  def tryLock(time: Long, unit: TimeUnit)

  def unlock(): Unit

}
