// Ported from Scala.js commit: 9dc4d5b dated: 11 Oct 2018

package java.util.concurrent.locks

import java.util.concurrent.TimeUnit

trait Lock {
  def lock(): Unit
  def lockInterruptibly(): Unit
  def tryLock(): Boolean
  def tryLock(time: Long, unit: TimeUnit): Boolean
  def unlock(): Unit

  // Not implemented:
  // def newCondition(): Condition
}
