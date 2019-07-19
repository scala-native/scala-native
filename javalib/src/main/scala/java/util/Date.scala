package java.util

import scalanative.libc.{errno, string}
import scalanative.posix.time._
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

  private var tzsetDone = false

  private def secondsToString(seconds: Long): String = Zone { implicit z =>
    val ttPtr = alloc[time_t]
    !ttPtr = seconds
    val tmPtr = alloc[tm]

    if (!tzsetDone) {
      tzset() // needed in order to portably use localtime_r
      tzsetDone = true
    }

    if (localtime_r(ttPtr, tmPtr) == null) {
      throw new IOException(fromCString(string.strerror(errno.errno)))
    } else {
      val bufSize = "Thu Jul 18 09:16:29 PDT 2019".length + 1
      val buf     = alloc[Byte](bufSize)

      val n = strftime(buf, bufSize, c"%a %b %d %T %Z %Y", tmPtr)

      // strftime does not set errno on error
      var result = if (n == 0) "" else fromCString(buf)

      result
    }
  }

  override def toString(): String = secondsToString(milliseconds / 1000L)

}
