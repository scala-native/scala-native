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

import java.lang.ref.Cleaner
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
    queue.schedule(task, Math.max(0L, delayUntil(time)), -1, false)

  def schedule(task: TimerTask, delay: Long): Unit =
    queue.schedule(task, checkDelay(delay), -1, false)

  def schedule(task: TimerTask, delay: Long, period: Long): Unit =
    queue.schedule(task, checkDelay(delay), checkPeriod(period), false)

  def schedule(task: TimerTask, firstTime: Date, period: Long): Unit = {
    checkPeriod(period)
    queue.schedule(task, Math.max(0L, delayUntil(firstTime)), period, false)
  }

  def scheduleAtFixedRate(task: TimerTask, delay: Long, period: Long): Unit =
    queue.schedule(task, checkDelay(delay), checkPeriod(period), true)

  def scheduleAtFixedRate(
      task: TimerTask,
      firstTime: Date,
      period: Long
  ): Unit = {
    checkPeriod(period)
    // A first time in the past makes a fixed-rate task catch up
    queue.schedule(task, delayUntil(firstTime), period, true)
  }
}

object Timer {
  private val timerId = new AtomicInteger(0)

  // Runs the actions on the reference handler: they only notify a queue
  private lazy val cleaner = Cleaner.create(null)

  private def checkDelay(delay: Long): Long = {
    if (delay < 0) throw new IllegalArgumentException("Negative delay.")
    delay
  }

  private def checkPeriod(period: Long): Long = {
    if (period <= 0) throw new IllegalArgumentException("Non-positive period.")
    period
  }

  private def delayUntil(time: Date): Long = {
    if (time.getTime() < 0)
      throw new IllegalArgumentException("Illegal execution time.")
    time.getTime() - System.currentTimeMillis()
  }

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
        delay: Long,
        period: Long,
        fixedRate: Boolean
    ): Unit = synchronized {
      if (cancelled)
        throw new IllegalStateException("Timer already cancelled.")
      val when = delay + System.currentTimeMillis()
      if (when < 0)
        throw new IllegalArgumentException("Illegal execution time.")
      task.lock.synchronized {
        // A task gets a positive `when` when scheduled and a positive
        // `scheduledTime` when run
        if (task.when > 0 || task.scheduledTime > 0 || task.cancelled)
          throw new IllegalStateException("Task already scheduled or cancelled")
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
      var removed = 0
      val it = tasks.iterator()
      while (it.hasNext()) {
        if (it.next().cancelled) {
          it.remove()
          removed += 1
        }
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
                val remaining = first.when - System.currentTimeMillis()
                if (remaining <= 0) {
                  first.scheduledTime = first.when
                  tasks.poll()
                  if (first.period > 0) {
                    // A repeating task goes back into the queue
                    first.when =
                      if (first.fixedRate) first.when + first.period
                      else System.currentTimeMillis() + first.period
                    tasks.add(first)
                  } else first.when = 0
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
