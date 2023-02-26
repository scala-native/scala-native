/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util
package concurrent

trait BlockingDeque[E] extends BlockingQueue[E] with Deque[E] {

  def addFirst(e: E): Unit

  def addLast(e: E): Unit

  def putFirst(e: E): Unit

  def putLast(e: E): Unit

  def offerFirst(e: E, timeout: Long, unit: TimeUnit): Boolean

  def offerLast(e: E, timeout: Long, unit: TimeUnit): Boolean

  def takeFirst(): E

  def takeLast(): E

  def pollFirst(timeout: Long, unit: TimeUnit): E

  def pollLast(timeout: Long, unit: TimeUnit): E
}
