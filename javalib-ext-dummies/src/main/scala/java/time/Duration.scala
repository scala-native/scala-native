package java.time

/* This file is a near minimal implementation of java.time classes and
 * methods for Scala Native internal tests, usually in unit-tests-ext.
 * It contains declarations necessary for implementing the class and
 * for resolving methods used by tests such as TimeUnitTestExt.
 *
 * 'toString()' and possibly a few other methods are added to ease
 * SN development & verification.
 *
 * NOT ALL JAVA API METHODS ARE IMPLMENTED. This implementation is
 * NOT INTENDED for use outside the testsExt project.
 *
 * Applications using java.time.Duration must have a dependency on
 * a third party library, such as 'scala-java-time'.
 */

final class Duration(private val seconds: Long, private val nano: Int)
    extends Comparable[Duration]
    with java.io.Serializable {

  override def compareTo(that: Duration): Int = {
    val secondsCompare = seconds.compareTo(that.seconds)
    if (secondsCompare == 0) {
      nano.compareTo(that.nano)
    } else secondsCompare
  }

  override def equals(that: Any): Boolean = that match {
    case that: Duration =>
      this.seconds == that.seconds &&
        this.nano == that.nano
    case _ =>
      false
  }

  def getNano(): Int = nano

  def getSeconds(): Long = seconds

  override def hashCode(): Int =
    java.lang.Long.hashCode(seconds) ^ java.lang.Integer.hashCode(nano)

  // non compliant, for debugging purposes only
  override def toString(): String =
    "Duration(" + seconds + ", " + nano + ")"
}

object Duration {

  def ofSeconds(seconds: Long, nanoAdjustment: Long): Duration = {
    val adjustedSeconds =
      Math.addExact(seconds, Math.floorDiv(nanoAdjustment, 1000000000L))
    val adjustedNano = Math.floorMod(nanoAdjustment, 1000000000L).toInt

    new Duration(adjustedSeconds, adjustedNano)
  }
}
