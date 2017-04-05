package java.util

/** Ported from Scala JS and Apache Harmony
 * - omits deprecation
 * - immutable
 *
 * TODO: needs TimeZone, Calendar or java.util.time for compatible toString
 *       and other deprecated methods if needed
 */
class Date(milliseconds: Long)
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

  // only implement if mutable is required
//  def setTime(time: Long): Unit =
//    milliseconds = time

  override def toString(): String = s"Date($milliseconds)"

}
