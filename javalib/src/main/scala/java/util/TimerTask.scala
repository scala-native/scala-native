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

// Ported from Apache Harmony

package java.util

/** A task that a `Timer` runs once or repeatedly. */
abstract class TimerTask protected () extends Runnable {

  /** Guards the scheduling state; the timer also locks it. */
  private[util] val lock = new Object

  private[util] var cancelled = false

  /** Time of the next run, 0 if none. */
  private[util] var when = 0L

  /** Period of a repeating task, -1 for a one-shot task. */
  private[util] var period = 0L

  private[util] var fixedRate = false

  /** Time the most recent run was scheduled for. */
  private var scheduledTime = 0L

  private[util] def setScheduledTime(time: Long): Unit = lock.synchronized {
    scheduledTime = time
  }

  private[util] def isScheduled: Boolean = lock.synchronized {
    when > 0 || scheduledTime > 0
  }

  /** Stops future runs; `true` if that prevented a run (a scheduled one-shot
   *  task that has not run, or a repeating task).
   */
  def cancel(): Boolean = lock.synchronized {
    val willRun = !cancelled && when > 0
    cancelled = true
    willRun
  }

  /** The scheduled time of the most recent run (of the current one inside
   *  `run`).
   */
  def scheduledExecutionTime(): Long = lock.synchronized(scheduledTime)

  def run(): Unit
}
