/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent

trait Future[V] {

  def cancel(mayInterruptIfRunning: Boolean): Boolean

  def isCancelled(): Boolean

  def isDone(): Boolean

  @throws[InterruptedException]
  @throws[ExecutionException]
  def get(): V

  @throws[InterruptedException]
  @throws[ExecutionException]
  @throws[TimeoutException]
  def get(timeout: Long, unit: TimeUnit): V

}
