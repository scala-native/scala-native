/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent

trait CompletionService[V] {

  def submit(task: Callable[V]): Future[V]

  def submit(task: Runnable, result: V): Future[V]

  def take(): Future[V]

  def poll(): Future[V]

  def poll(timeout: Long, unit: TimeUnit): Future[V]

}
