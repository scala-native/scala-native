package java.util

import scalanative.libc.{errno, string}
import scalanative.posix.time, time._
import scalanative.unsafe._

import java.io.IOException

/** Ported from Scala JS and Apache Harmony
 * - omits deprecated methods
 * - toString code is new for Scala Native.
 */
class Date(var milliseconds: Long)
    extends Object
    with Serializable
    with Cloneable
    with Comparable[Date] {

  def this() = this(System.currentTimeMillis())

  def after(when: Date): Boolean = milliseconds > when.getTime()

  def before(when: Date): Boolean = milliseconds < when.getTime()

  override def clone(): Object = new Date(milliseconds)

  override def compareTo(anotherDate: Date): Int =
    milliseconds.compareTo(anotherDate.getTime())

  override def equals(obj: Any): Boolean = obj match {
    case d: Date => d.getTime == milliseconds
    case _       => false
  }

  def getTime(): Long = milliseconds

  override def hashCode(): Int = milliseconds.hashCode()

  def setTime(time: Long): Unit =
    milliseconds = time

  override def toString(): String = {
    Date
      .secondsToStringOpt(milliseconds / 1000L)
      .getOrElse(s"Date($milliseconds)")
  }

}

private object Date {

  var tzsetDone = false

  def secondsToStringOpt(seconds: Long): Option[String] = Zone { implicit z =>
    val ttPtr = alloc[time_t]
    !ttPtr = seconds
    val tmPtr = alloc[tm]

    if (!tzsetDone) {
      time.tzset() // needed once in order to portably use localtime_r
      tzsetDone = true
    }

    if (localtime_r(ttPtr, tmPtr) == null) {
      throw new IOException(fromCString(string.strerror(errno.errno)))
    } else {
      // 70 is gross overprovisioning based on fear.
      // Most result strings should be about 28 + 1 for terminal NULL
      // + 2 because some IANA timezone appreviation can have 5 characters.
      val bufSize = 70
      val buf     = alloc[Byte](bufSize)

      val n = strftime(buf, bufSize, c"%a %b %d %T %Z %Y", tmPtr)

      // strftime does not set errno on error
      val result = if (n == 0) None else Some(fromCString(buf))

      result
    }
  }
}
