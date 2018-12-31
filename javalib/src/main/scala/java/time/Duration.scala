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

package java.time

import java.time.temporal.TemporalAmount

@SerialVersionUID(3078945930695997490L)
final class Duration private (private val seconds: Long, private val nanos: Int)
    extends TemporalAmount
    with Ordered[Duration]
    with Serializable {

  def toMillis: Long = {
    val result: Long = Math.multiplyExact(seconds, Duration.MILLIS_PER_SEC)
    Math.addExact(result, nanos / Duration.NANOS_PER_MILLI)
  }

  def compare(otherDuration: Duration): Int = {
    val cmp: Int = java.lang.Long.compare(seconds, otherDuration.seconds)
    if (cmp != 0) cmp
    else nanos - otherDuration.nanos
  }

  override def compareTo(other: Duration): Int = compare(other)

  override def equals(other: Any): Boolean =
    other match {
      case otherDuration: Duration =>
        (this eq otherDuration) || (this.seconds == otherDuration.seconds && this.nanos == otherDuration.nanos)
      case _ => false
    }

  override def hashCode: Int = (seconds ^ (seconds >>> 32)).toInt + (51 * nanos)
}

@SerialVersionUID(3078945930695997490L)
object Duration {
  private val NANOS_PER_MILLI: Int = 1000000
  private val MILLIS_PER_SEC: Int  = 1000
  private val NANOS_PER_SEC: Int   = NANOS_PER_MILLI * MILLIS_PER_SEC

  private def create(seconds: Long, nanoAdjustment: Int): Duration =
    if ((seconds | nanoAdjustment) == 0) ZERO
    else new Duration(seconds, nanoAdjustment)

  val ZERO: Duration = new Duration(0, 0)

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
