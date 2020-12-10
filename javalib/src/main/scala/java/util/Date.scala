package java.util

import scalanative.posix.time, time.time_t
import scalanative.posix.sys.types.size_t
import scalanative.unsafe._

/** Ported from Scala JS and Apache Harmony
 * - omits deprecated methods
 * - toString code created ab ovo for Scala Native.
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

  override def toString(): String = {
    val seconds = milliseconds / 1000L
    val default = s"Date($milliseconds)"
    Date.secondsToString(seconds, default)
  }
}

@extern
private object DateC {

  def scalanative_ju_secondsToLocaltime(seconds: Ptr[time_t],
                                        format: CString,
                                        fallback: CString,
                                        buf: Ptr[Byte],
                                        maxsize: size_t): CString = extern
}

private object Date {

  def secondsToString(seconds: Long, default: String): String = Zone {
    implicit z =>
      val ttPtr = alloc[time_t]
      !ttPtr = seconds

      // 72 is gross over-provisioning based on fear.
      // Most result strings should be about 28 + 1 for terminal NULL
      // + 2 because some IANA timezone abbreviation can have 5 characters.
      val bufSize = 72
      val buf     = alloc[Byte](bufSize)

      val cstr = DateC.scalanative_ju_secondsToLocaltime(
        ttPtr,
        c"%a %b %d %T %Z %Y",
        toCString(default),
        buf,
        bufSize
      )

      fromCString(cstr)
  }
}
