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

import scala.scalanative.meta.LinktimeInfo

/** Runs `TimerTask`s on one background thread, in the order of their scheduled
 *  times.
 */
class Timer(name: String, isDaemon: Boolean) {
  import Timer._

  if (name == null) throw new NullPointerException("name cannot be null")

  // The thread must never reference the Timer, or the Timer is never cleaned
  private val impl = new TimerImpl(name, isDaemon)

  if (LinktimeInfo.isMultithreadingEnabled &&
      LinktimeInfo.isWeakReferenceSupported)
    cleaner.register(this, new Stopper(impl))

  def this(name: String) = this(name, false)

  def this(isDaemon: Boolean) = this("Timer-" + Timer.nextId(), isDaemon)

  def this() = this(false)

  /** Discards all scheduled tasks; a running task completes. */
  def cancel(): Unit = impl.cancel()

  /** Removes cancelled tasks from the queue; returns how many. */
  def purge(): Int = impl.synchronized(impl.purge())

  def schedule(task: TimerTask, time: Date): Unit = {
    if (time.getTime() < 0)
      throw new IllegalArgumentException("Illegal execution time.")
    val delay = time.getTime() - System.currentTimeMillis()
    scheduleImpl(task, if (delay < 0) 0 else delay, -1, false)
  }

  def schedule(task: TimerTask, delay: Long): Unit = {
    if (delay < 0) throw new IllegalArgumentException("Negative delay.")
    scheduleImpl(task, delay, -1, false)
  }

  def schedule(task: TimerTask, delay: Long, period: Long): Unit = {
    if (delay < 0) throw new IllegalArgumentException("Negative delay.")
    if (period <= 0) throw new IllegalArgumentException("Non-positive period.")
    scheduleImpl(task, delay, period, false)
  }

  def schedule(task: TimerTask, firstTime: Date, period: Long): Unit = {
    if (period <= 0) throw new IllegalArgumentException("Non-positive period.")
    if (firstTime.getTime() < 0)
      throw new IllegalArgumentException("Illegal execution time.")
    val delay = firstTime.getTime() - System.currentTimeMillis()
    scheduleImpl(task, if (delay < 0) 0 else delay, period, false)
  }

  def scheduleAtFixedRate(task: TimerTask, delay: Long, period: Long): Unit = {
    if (delay < 0) throw new IllegalArgumentException("Negative delay.")
    if (period <= 0) throw new IllegalArgumentException("Non-positive period.")
    scheduleImpl(task, delay, period, true)
  }

  def scheduleAtFixedRate(
      task: TimerTask,
      firstTime: Date,
      period: Long
  ): Unit = {
    if (period <= 0) throw new IllegalArgumentException("Non-positive period.")
    if (firstTime.getTime() < 0)
      throw new IllegalArgumentException("Illegal execution time.")
    // A first time in the past makes a fixed-rate task catch up
    val delay = firstTime.getTime() - System.currentTimeMillis()
    scheduleImpl(task, delay, period, true)
  }

  private def scheduleImpl(
      task: TimerTask,
      delay: Long,
      period: Long,
      fixed: Boolean
  ): Unit =
    impl.synchronized {
      if (impl.cancelled)
        throw new IllegalStateException("Timer already cancelled.")
      val when = delay + System.currentTimeMillis()
      if (when < 0)
        throw new IllegalArgumentException("Illegal execution time.")
      task.lock.synchronized {
        if (task.isScheduled || task.cancelled)
          throw new IllegalStateException("Task already scheduled or cancelled")
        task.when = when
        task.period = period
        task.fixedRate = fixed
      }
      impl.insertTask(task)
    }
}

object Timer {
  private val timerId = new java.util.concurrent.atomic.AtomicInteger(0)

  private def nextId(): Int = timerId.getAndIncrement()

  private lazy val cleaner = Cleaner.create()

  /** Tells the thread of an unreachable timer to end once its queue is empty.
   */
  private final class Stopper(impl: TimerImpl) extends Runnable {
    def run(): Unit = impl.stopWhenEmpty()
  }

  /** A binary min-heap of tasks ordered by `when`. */
  private final class TimerHeap {
    private var timers = new Array[TimerTask](256)
    private var size = 0
    var deletedCancelledNumber = 0

    def minimum(): TimerTask = timers(0)

    def isEmpty: Boolean = size == 0

    def insert(task: TimerTask): Unit = {
      if (timers.length == size) timers = Arrays.copyOf(timers, size * 2)
      timers(size) = task
      size += 1
      upHeap()
    }

    def delete(pos: Int): Unit =
      if (pos >= 0 && pos < size) {
        size -= 1
        timers(pos) = timers(size)
        timers(size) = null
        downHeap(pos)
      }

    private def upHeap(): Unit = {
      var current = size - 1
      var parent = (current - 1) / 2
      while (timers(current).when < timers(parent).when) {
        val tmp = timers(current)
        timers(current) = timers(parent)
        timers(parent) = tmp
        current = parent
        parent = (current - 1) / 2
      }
    }

    private def downHeap(pos: Int): Unit = {
      var current = pos
      var child = 2 * current + 1
      var done = false
      while (!done && child < size) {
        if (child + 1 < size && timers(child + 1).when < timers(child).when)
          child += 1
        if (timers(current).when < timers(child).when) done = true
        else {
          val tmp = timers(current)
          timers(current) = timers(child)
          timers(child) = tmp
          current = child
          child = 2 * current + 1
        }
      }
    }

    def reset(): Unit = {
      timers = new Array[TimerTask](256)
      size = 0
    }

    def deleteIfCancelled(): Unit = {
      var i = 0
      while (i < size) {
        if (timers(i).cancelled) {
          deletedCancelledNumber += 1
          delete(i)
        } else i += 1
      }
    }

    def indexOf(task: TimerTask): Int = {
      var i = 0
      while (i < size) {
        if (timers(i) eq task) return i
        i += 1
      }
      -1
    }
  }

  /** The timer thread; its monitor guards the queue. */
  private final class TimerImpl(name: String, isDaemon: Boolean)
      extends Thread {
    var cancelled = false

    /** Set once the Timer is unreachable: no task can be scheduled anymore. */
    private var unreachable = false
    private val tasks = new TimerHeap

    setName(name)
    setDaemon(isDaemon)
    start()

    override def run(): Unit = {
      var running = true
      while (running) {
        var task: TimerTask = null
        synchronized {
          if (cancelled || (unreachable && tasks.isEmpty)) running = false
          else if (tasks.isEmpty) {
            try wait()
            catch { case _: InterruptedException => () }
          } else {
            val first = tasks.minimum()
            // Never wait while holding the task's lock: TimerTask.cancel takes it
            val timeToSleep = first.lock.synchronized {
              if (first.cancelled) {
                tasks.delete(0)
                0L
              } else {
                val remaining = first.when - System.currentTimeMillis()
                if (remaining <= 0) {
                  first.setScheduledTime(first.when)
                  tasks.delete(0)
                  if (first.period >= 0) {
                    // A repeating task goes back into the queue
                    first.when =
                      if (first.fixedRate) first.when + first.period
                      else System.currentTimeMillis() + first.period
                    insertTask(first)
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
          var completed = false
          try {
            task.run()
            completed = true
          } finally {
            // A task that throws ends the timer, as in the JDK
            if (!completed) synchronized { cancelled = true }
          }
        }
      }
    }

    /** Callers hold this monitor. */
    def insertTask(task: TimerTask): Unit = {
      tasks.insert(task)
      notify()
    }

    def stopWhenEmpty(): Unit = synchronized {
      unreachable = true
      notify()
    }

    def cancel(): Unit = synchronized {
      cancelled = true
      tasks.reset()
      notify()
    }

    /** Callers hold this monitor. */
    def purge(): Int =
      if (tasks.isEmpty) 0
      else {
        tasks.deletedCancelledNumber = 0
        tasks.deleteIfCancelled()
        tasks.deletedCancelledNumber
      }
  }
}
