package java.util

import java.time.Instant

import scala.scalanative.meta.LinktimeInfo.isWindows
import scalanative.posix.time._
import scalanative.unsafe._
import scalanative.unsigned._
import scalanative.windows.crt.{time => winTime}

/** Ported from Scala JS and Apache Harmony
 *    - omits deprecated methods
 *    - toString code created ab ovo for Scala Native.
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
    case d: Date => d.getTime() == milliseconds
    case _       => false
  }

  def getTime(): Long = milliseconds

  override def hashCode(): Int = milliseconds.hashCode()

  def setTime(time: Long): Unit =
    milliseconds = time

  def toInstant(): Instant = Instant.ofEpochMilli(getTime())

  override def toString(): String = {
    val seconds = milliseconds / 1000L
    def default = s"Date($milliseconds)"
    Date.secondsToString(seconds, default)
  }
}

object Date {
  // Provide prerequisite for localtime_r calls.
  // Applications which must track timezone changes over their lifetime
  // must do timely subsequent tzset() calls, either directly or through
  // an occasional localtime().
  if (isWindows) winTime.tzset()
  else tzset()

  private def secondsToString(seconds: Long, default: => String): String =
    Zone.acquire { implicit z =>
      val ttPtr = alloc[time_t]()
      !ttPtr = seconds.toSize

      val tmPtr = alloc[tm]()
      def getLocalTime() =
        if (isWindows) winTime.localtime_s(tmPtr, ttPtr) != 0
        else localtime_r(ttPtr, tmPtr) == null

      if (getLocalTime()) {
        default
      } else {
        // 40 is over-provisioning.
        // Most result strings should be about 28 + 1 for terminal NULL
        // + 2 because some IANA timezone abbreviation can have 5 characters.
        val bufSize = 40.toUSize
        val buf: Ptr[Byte] = alloc[Byte](bufSize)

        val n = {
          // %Z on Windows might produce long, localized names of variable length
          if (isWindows)
            winTime.strftime(buf, bufSize, c"%a %b %d %T %Y", tmPtr)
          else
            strftime(buf, bufSize, c"%a %b %d %T %Z %Y", tmPtr)
        }
        if (n.toInt == 0) default else fromCString(buf)
      }
    }

  def from(instant: Instant): Date = {
    try {
      new Date(instant.toEpochMilli())
    } catch {
      case ex: ArithmeticException =>
        throw new IllegalArgumentException(ex)
    }
  }

  def getMillisOf(date: Date): Long = date.milliseconds
}
