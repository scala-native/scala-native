package java.time.temporal

// A near minimal implementation strictly for Scala Native internal testing.

/* _Enum is a HACK to avoid having to have separate enum code for Scala 2 & 3.
 * Original code is in java.lang. Cut & paste is "gag me with a spoon"
 * ugly but I could not get an import of java.lang underbarEnum to work in
 * this project within economic time.
 */
abstract class _Enum[E <: _Enum[E]] protected (_name: String, _ordinal: Int)
    extends Comparable[E]
    with java.io.Serializable {
  def name(): String = _name
  def ordinal(): Int = _ordinal
  override def toString(): String = _name
  final def compareTo(o: E): Int = _ordinal.compareTo(o.ordinal())
}

class ChronoUnit private (name: String, ordinal: Int)
    extends _Enum[ChronoUnit](name, ordinal)
    with TemporalUnit {

  override def toString() = this.name
}

object ChronoUnit {
  final val NANOS: ChronoUnit = new ChronoUnit("Nanos", 0)
  final val MICROS: ChronoUnit = new ChronoUnit("Micros", 1)
  final val MILLIS: ChronoUnit = new ChronoUnit("Millis", 2)
  final val SECONDS: ChronoUnit = new ChronoUnit("Seconds", 3)
  final val MINUTES: ChronoUnit = new ChronoUnit("Minutes", 4)
  final val HOURS: ChronoUnit = new ChronoUnit("Hours", 5)
  final val HALF_DAYS: ChronoUnit = new ChronoUnit("HalfDays", 6)
  final val DAYS: ChronoUnit = new ChronoUnit("Days", 7)
  final val WEEKS: ChronoUnit = new ChronoUnit("Weeks", 8)
  final val MONTHS: ChronoUnit = new ChronoUnit("Months", 9)
  final val YEARS: ChronoUnit = new ChronoUnit("Years", 10)
  final val DECADES: ChronoUnit = new ChronoUnit("Decades", 11)
  final val CENTURIES: ChronoUnit = new ChronoUnit("Centuries", 12)
  final val MILLENNIA: ChronoUnit = new ChronoUnit("Millennia", 13)
  final val ERAS: ChronoUnit = new ChronoUnit("Eras", 14)
  final val FOREVER: ChronoUnit = new ChronoUnit("Forever", 15)
}
