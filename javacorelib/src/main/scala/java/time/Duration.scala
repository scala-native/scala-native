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
package java.time

import java.time.temporal.TemporalAmount

final class Duration private (seconds: Long, nanos: Int)
    extends TemporalAmount
    with Comparable[Duration]
    with java.io.Serializable {

  def getSeconds(): Long = seconds

  def getNano(): Int = nanos

  def toMillis: Long = {
    val result: Long = Math.multiplyExact(seconds, Duration.MILLIS_PER_SEC)
    Math.addExact(result, nanos / Duration.NANOS_PER_MILLI)
  }

  def compareTo(that: Duration): Int = {
    val secCmp = seconds.compareTo(that.getSeconds)
    if (secCmp == 0) nanos.compareTo(that.getNano)
    else secCmp
  }

  override def equals(that: Any): Boolean = that match {
    case that: Duration =>
      seconds == that.getSeconds && nanos == that.getNano
    case _ => false
  }

  override def hashCode: Int = (seconds ^ (seconds >>> 32)).toInt + (51 * nanos)
}

object Duration {
  private final val NANOS_PER_MILLI: Int = 1000000
  private final val MILLIS_PER_SEC: Int  = 1000
  private final val NANOS_PER_SEC: Int   = NANOS_PER_MILLI * MILLIS_PER_SEC

  private def create(seconds: Long, nanoAdjustment: Int): Duration =
    if ((seconds | nanoAdjustment) == 0) ZERO
    else new Duration(seconds, nanoAdjustment)

  final val ZERO: Duration = new Duration(0, 0)

  def ofMillis(millis: Long): Duration = {
    var secs: Long = millis / MILLIS_PER_SEC
    var mos: Int   = (millis % MILLIS_PER_SEC).toInt
    if (mos < 0) {
      mos += MILLIS_PER_SEC
      secs -= 1
    }
    create(secs, mos * NANOS_PER_MILLI)
  }

  def ofSeconds(seconds: Long, nanoAdjustment: Long): Duration = {
    val secs: Long =
      Math.addExact(seconds, Math.floorDiv(nanoAdjustment, NANOS_PER_SEC))
    val nos: Int = Math.floorMod(nanoAdjustment, NANOS_PER_SEC).toInt
    create(secs, nos)
  }
}
