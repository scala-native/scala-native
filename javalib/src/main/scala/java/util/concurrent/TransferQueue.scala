/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util
package concurrent

trait TransferQueue[E] extends BlockingQueue[E] {

  def tryTransfer(e: E): Boolean

  def transfer(e: E): Unit

  def tryTransfer(e: E, timeout: Long, unit: TimeUnit): Boolean

  def hasWaitingConsumer(): Boolean

  def getWaitingConsumerCount(): Int
}
