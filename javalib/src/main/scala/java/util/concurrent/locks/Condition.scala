/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent
package locks

import java.util.Date

trait Condition {

  def await(): Unit

  def await(time: Long, unit: TimeUnit): Boolean

  def awaitNanos(nanosTimeout: Long): Long

  def awaitUninterruptibly(): Unit

  def awaitUntil(deadLine: Date): Boolean

  def signal(): Unit

  def signalAll(): Unit

}
