/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Ported from Apache Harmony. Harmony's FinalizerHelper is replaced by a
// java.lang.ref.Cleaner: once a Timer is unreachable, its thread ends after the
// queued tasks have run. With a GC without weak references the thread of an
// unreachable timer keeps waiting, as there is no Cleaner. Harmony's shutdown
// hook for daemon timers is dropped, as the JDK has none.

package java.util

import java.lang.ref.{Cleaner, Reference}
import java.util.concurrent.atomic.AtomicInteger

import scala.scalanative.meta.LinktimeInfo

/** Runs `TimerTask`s on one background thread, in the order of their scheduled
 *  times.
 */
class Timer(name: String, isDaemon: Boolean) {
  import Timer._

  if (name == null) throw new NullPointerException("name cannot be null")

  // Neither the queue nor the thread may reference the Timer, or the Timer is
  // never cleaned
  private val queue = new TaskQueue

  locally {
    val thread = new Thread(queue, name)
    thread.setDaemon(isDaemon)
    thread.start()
  }

  if (LinktimeInfo.isWeakReferenceSupported)
    cleaner.register(this, new Stopper(queue))

  def this(name: String) = this(name, false)

  def this(isDaemon: Boolean) =
    this("Timer-" + Timer.timerId.getAndIncrement(), isDaemon)

  def this() = this(false)

  /** Discards all scheduled tasks; a running task completes. */
  def cancel(): Unit = queue.cancel()

  /** Removes cancelled tasks from the queue; returns how many. */
  def purge(): Int = queue.purge()

  def schedule(task: TimerTask, time: Date): Unit =
    add(task, timeOf(time), -1, false)

  def schedule(task: TimerTask, delay: Long): Unit =
    add(task, after(delay), -1, false)

  def schedule(task: TimerTask, delay: Long, period: Long): Unit = {
    val when = after(delay)
    add(task, when, checkPeriod(period), false)
  }

  def schedule(task: TimerTask, firstTime: Date, period: Long): Unit = {
    checkPeriod(period)
    add(task, timeOf(firstTime), period, false)
  }

  def scheduleAtFixedRate(task: TimerTask, delay: Long, period: Long): Unit = {
    val when = after(delay)
    add(task, when, checkPeriod(period), true)
  }

  // A first time in the past makes a fixed-rate task catch up
  def scheduleAtFixedRate(
      task: TimerTask,
      firstTime: Date,
      period: Long
  ): Unit = {
    checkPeriod(period)
    add(task, timeOf(firstTime), period, true)
  }

  private def add(
      task: TimerTask,
      when: Long,
      period: Long,
      fixedRate: Boolean
  ): Unit = {
    queue.schedule(task, when, period, fixedRate)
    // Until the task is queued, the Cleaner must not stop the thread
    Reference.reachabilityFence(this)
  }
}

object Timer {
  private val timerId = new AtomicInteger(0)

  // Runs the actions on the reference handler: they only notify a queue
  private lazy val cleaner = Cleaner.create(null)

  /** The time `delay` milliseconds from now. */
  private def after(delay: Long): Long = {
    if (delay < 0) throw new IllegalArgumentException("Negative delay.")
    val when = System.currentTimeMillis() + delay
    if (when < 0) throw new IllegalArgumentException("Illegal execution time.")
    when
  }

  private def checkPeriod(period: Long): Long = {
    if (period <= 0) throw new IllegalArgumentException("Non-positive period.")
    period
  }

  private def timeOf(time: Date): Long = {
    val when = time.getTime()
    if (when < 0) throw new IllegalArgumentException("Illegal execution time.")
    when
  }

  /** `time + period`, saturated: a huge period must not wrap. */
  private def next(time: Long, period: Long): Long =
    if (time > Long.MaxValue - period) Long.MaxValue else time + period

  /** Tells the thread of an unreachable timer to end once its queue is empty.
   */
  private final class Stopper(queue: TaskQueue) extends Runnable {
    def run(): Unit = queue.stopWhenEmpty()
  }

  /** The tasks of a timer and the loop of its thread; its monitor, never
   *  exposed, guards the state.
   */
  private final class TaskQueue extends Runnable {
    private val tasks =
      new PriorityQueue[TimerTask]((a: TimerTask, b: TimerTask) =>
        java.lang.Long.compare(a.when, b.when)
      )

    private var cancelled = false

    /** Set once the Timer is unreachable: no task can be scheduled anymore. */
    private var unreachable = false

    def schedule(
        task: TimerTask,
        when: Long,
        period: Long,
        fixedRate: Boolean
    ): Unit = synchronized {
      if (cancelled)
        throw new IllegalStateException("Timer already cancelled.")
      task.lock.synchronized {
        if (task.scheduled || task.cancelled)
          throw new IllegalStateException("Task already scheduled or cancelled")
        task.scheduled = true
        task.pending = true
        task.when = when
        task.period = period
        task.fixedRate = fixedRate
      }
      tasks.add(task)
      // Only the timer thread waits; it re-checks only for a new first task
      if (tasks.peek() eq task) notify()
    }

    def cancel(): Unit = synchronized {
      cancelled = true
      tasks.clear()
      notify()
    }

    def purge(): Int = synchronized {
      // Rebuild once: removing through the iterator searches the queue each time
      val kept = new ArrayList[TimerTask](tasks.size())
      val it = tasks.iterator()
      while (it.hasNext()) {
        val task = it.next()
        if (!task.lock.synchronized(task.cancelled)) kept.add(task)
      }
      val removed = tasks.size() - kept.size()
      if (removed > 0) {
        tasks.clear()
        tasks.addAll(kept)
      }
      removed
    }

    def stopWhenEmpty(): Unit = synchronized {
      unreachable = true
      notify()
    }

    override def run(): Unit = {
      var running = true
      while (running) {
        var task: TimerTask = null
        synchronized {
          if (cancelled || (unreachable && tasks.isEmpty())) running = false
          else if (tasks.isEmpty()) {
            try wait()
            catch { case _: InterruptedException => () }
          } else {
            val first = tasks.peek()
            // Never wait while holding the task's lock: TimerTask.cancel takes it
            val timeToSleep = first.lock.synchronized {
              if (first.cancelled) {
                tasks.poll()
                0L
              } else {
                val now = System.currentTimeMillis()
                val remaining = first.when - now
                if (remaining <= 0) {
                  // A late fixed-delay run counts from when it is dispatched
                  val fixedDelay = first.period > 0 && !first.fixedRate
                  first.scheduledTime = if (fixedDelay) now else first.when
                  tasks.poll()
                  if (first.period > 0) {
                    // A repeating task goes back into the queue
                    first.when = next(first.scheduledTime, first.period)
                    tasks.add(first)
                  } else first.pending = false
                  task = first
                }
                remaining
              }
            }
            if (timeToSleep > 0) {
              try wait(timeToSleep)
              catch { case _: InterruptedException => () }
            }
          }
        }
        if (task != null) {
          try task.run()
          catch {
            case t: Throwable =>
              // A task that throws ends the timer, as in the JDK
              cancel()
              throw t
          }
        }
      }
    }
  }
}
