/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent

trait ScheduledExecutorService extends ExecutorService {

  def schedule(
      command: Runnable,
      delay: Long,
      unit: TimeUnit
  ): ScheduledFuture[AnyRef]

  def schedule[V <: AnyRef](
      command: Callable[V],
      delay: Long,
      unit: TimeUnit
  ): ScheduledFuture[V]

  def scheduleAtFixedRate(
      command: Runnable,
      initialDelay: Long,
      period: Long,
      unit: TimeUnit
  ): ScheduledFuture[AnyRef]

  def scheduleWithFixedDelay(
      command: Runnable,
      initialDelay: Long,
      period: Long,
      unit: TimeUnit
  ): ScheduledFuture[AnyRef]

}
