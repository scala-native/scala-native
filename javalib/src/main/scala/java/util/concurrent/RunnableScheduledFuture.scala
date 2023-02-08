/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent

trait RunnableScheduledFuture[V]
    extends RunnableFuture[V]
    with ScheduledFuture[V] {

  def isPeriodic(): Boolean

}
