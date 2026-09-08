package java.time.temporal

class ChronoUnit private (name: String, ordinal: Int)
    extends _Enum[ChronoUnit](name, ordinal)
    with TemporalUnit

object ChronoUnit {
  final val NANOS = new ChronoUnit("NANOS", 0)
  final val MICROS = new ChronoUnit("MICROS", 1)
  final val MILLIS = new ChronoUnit("MILLIS", 2)
  final val SECONDS = new ChronoUnit("SECONDS", 3)
  final val MINUTES = new ChronoUnit("MINUTES", 4)
  final val HOURS = new ChronoUnit("HOURS", 5)
  final val HALF_DAYS = new ChronoUnit("HALF_DAYS", 6)
  final val DAYS = new ChronoUnit("DAYS", 7)
  final val WEEKS = new ChronoUnit("WEEKS", 8)
  final val MONTHS = new ChronoUnit("MONTHS", 9)
  final val YEARS = new ChronoUnit("YEARS", 10)
  final val DECADES = new ChronoUnit("DECADES", 11)
  final val CENTURIES = new ChronoUnit("CENTURIES", 12)
  final val MILLENNIA = new ChronoUnit("MILLENNIA", 13)
  final val ERAS = new ChronoUnit("ERAS", 14)
  final val FOREVER = new ChronoUnit("FOREVER", 15)

  private val _values: Array[ChronoUnit] =
    Array(
      NANOS,
      MICROS,
      MILLIS,
      SECONDS,
      MINUTES,
      HOURS,
      HALF_DAYS,
      DAYS,
      WEEKS,
      MONTHS,
      YEARS,
      DECADES,
      CENTURIES,
      MILLENNIA,
      ERAS,
      FOREVER
    )

  def values(): Array[ChronoUnit] = _values.clone()

  def valueOf(name: String): ChronoUnit =
    _values.find(_.name() == name).getOrElse {
      throw new IllegalArgumentException("No enum const ChronoUnit." + name)
    }
}
