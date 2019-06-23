/*
 * Copyright (c) 2007-present, Stephen Colebourne & Michael Nascimento Santos
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  * Neither the name of JSR-310 nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

// see the license file for more information about java.time

// Extensively re-worked for Scala Native to provide Java 8 nanosecond
// resolution.
//
// Informed by Scala JS Instant.scala & other files at URL:
//     https://raw.githubusercontent.com/scala-js/scala-js-java-time\
//           /master/src/main/scala/java/time/

package java.time

import java.io.IOException

import scalanative.libc.errno
import scalanative.posix.{time, timeOps}, timeOps.timespecOps
import scalanative.unsafe.{alloc, Ptr}
import scalanative.unsafe.Zone, Zone._

private[time] object Constants {

  final val NANOS_IN_MICRO = 1000

  final val MICROS_IN_MILLI = 1000

  final val NANOS_IN_MILLI = MICROS_IN_MILLI * NANOS_IN_MICRO

  final val MILLIS_IN_SECOND = 1000

  final val NANOS_IN_SECOND = MILLIS_IN_SECOND * NANOS_IN_MILLI
}

import Constants._

final class Instant(private val seconds: Long, private val nanos: Int)
    extends Comparable[Instant]
    with java.io.Serializable {

  import Instant._

  requireDateTime(seconds >= MinSecond && seconds <= MaxSecond,
                  s"Invalid seconds: $seconds")

  requireDateTime(nanos >= 0 && nanos <= MaxNanosInSecond,
                  s"Invalid nanos: $nanos")

  // Like scala.Predef.require, but throws a DateTimeException.
  private def requireDateTime(requirement: Boolean, message: => Any): Unit = {
    if (!requirement)
      throw new DateTimeException(message.toString)
  }

  def compareTo(that: Instant): Int = {
    val cmp = seconds compareTo that.seconds
    if (cmp != 0) {
      cmp
    } else {
      nanos compareTo that.nanos
    }
  }

  override def equals(other: Any): Boolean = other match {
    case that: Instant => seconds == that.seconds && nanos == that.nanos
    case _             => false
  }

  override def hashCode(): Int = (seconds + 51 * nanos).hashCode

  // This is method has non-standard output. It is a debugging aid.
  // A proper implementation of toString requires parts of java.time
  // which have not yet been implemented.

  override def toString: String = s"${seconds}.${nanos}"

  def getEpochSecond(): Long = seconds

  def getNano(): Int = nanos

  def isAfter(that: Instant): Boolean = compareTo(that) > 0

  def isBefore(that: Instant): Boolean = compareTo(that) < 0

  def toEpochMilli(): Long = {
    val millis = Math.multiplyExact(seconds, MILLIS_IN_SECOND.toLong)
    millis + nanos / NANOS_IN_MILLI
  }

}

object Instant {

  final val EPOCH = new Instant(0, 0)

  private val MinSecond        = -31557014167219200L
  private val MaxSecond        = 31556889864403199L
  private val MaxNanosInSecond = 999999999

  final val MIN = ofEpochSecond(MinSecond)
  final val MAX = ofEpochSecond(MaxSecond, MaxNanosInSecond)

  def now(): Instant = Zone { implicit z =>
    val nowTs = alloc[time.timespec]

    errno.errno = 0
    val status = time.clock_gettime(time.CLOCK_REALTIME, nowTs)

    if (status != 0) {
      throw new IOException(s"clock_gettime errno: ${errno.errno}")
    }

    new Instant(nowTs.tv_sec, nowTs.tv_nsec.toInt)
  }

  def ofEpochSecond(epochSecond: Long): Instant =
    ofEpochSecond(epochSecond, 0)

  def ofEpochSecond(epochSecond: Long, nanos: Long): Instant = {
    val adjustedSeconds =
      Math.addExact(epochSecond, Math.floorDiv(nanos, NANOS_IN_SECOND))
    val adjustedNanos = Math.floorMod(nanos, NANOS_IN_SECOND).toInt
    new Instant(adjustedSeconds, adjustedNanos)
  }

  def ofEpochMilli(epochMilli: Long): Instant = {
    val seconds = Math.floorDiv(epochMilli, MILLIS_IN_SECOND)
    val nanos   = Math.floorMod(epochMilli, MILLIS_IN_SECOND)
    new Instant(seconds, nanos.toInt * NANOS_IN_MILLI)
  }

}
