package java.util.concurrent

sealed abstract class TimeUnit(private val id: Int)
    extends Serializable
    with Comparable[TimeUnit] {
  import TimeUnit._
  override def compareTo(o: TimeUnit): Int =
    id.compareTo(o.id)

  protected def makeNext: Long => Long
  protected def makePrev: Long => Long
  protected def next: TimeUnit
  protected def prev: TimeUnit

  def convert(sourceDuration: Long, sourceUnit: TimeUnit): Long =
    if (id > sourceUnit.id) convert(makeNext(sourceDuration), sourceUnit.next)
    else if (id == sourceUnit.id) sourceDuration
    else convert(makePrev(sourceDuration), sourceUnit.prev)

  def toNanos(duration: Long): Long =
    NANOSECONDS.convert(duration, this)

  def toMicros(duration: Long): Long =
    MICROSECONDS.convert(duration, this)

  def toMillis(duration: Long): Long =
    MILLISECONDS.convert(duration, this)

  def toSeconds(duration: Long): Long =
    SECONDS.convert(duration, this)

  def toMinutes(duration: Long): Long =
    MINUTES.convert(duration, this)

  def toHours(duration: Long): Long =
    HOURS.convert(duration, this)

  def toDays(duration: Long): Long =
    DAYS.convert(duration, this)
}

object TimeUnit {
  val DAYS         = _DAYS
  val HOURS        = _HOURS
  val MINUTES      = _MINUTES
  val SECONDS      = _SECONDS
  val MILLISECONDS = _MILLISECONDS
  val MICROSECONDS = _MICROSECONDS
  val NANOSECONDS  = _NANOSECONDS

  case object _DAYS extends TimeUnit(7) {
    override def makeNext: Long => Long = ???
    override def next: TimeUnit         = ???
    override val makePrev: Long => Long = _ * 24L
    override val prev                   = HOURS
  }

  case object _HOURS extends TimeUnit(6) {
    override val makeNext: Long => Long = _ / 60L
    override val next                   = DAYS
    override val makePrev: Long => Long = _ * 60L
    override val prev                   = MINUTES
  }

  case object _MINUTES extends TimeUnit(5) {
    override val makeNext: Long => Long = _ / 60L
    override val next                   = HOURS
    override val makePrev: Long => Long = _ * 60L
    override val prev                   = SECONDS
  }

  case object _SECONDS extends TimeUnit(4) {
    override val makeNext: Long => Long = _ / 60L
    override val next                   = MINUTES
    override val makePrev: Long => Long = _ * 1000L
    override val prev                   = MILLISECONDS
  }

  case object _MILLISECONDS extends TimeUnit(3) {
    override val makeNext: Long => Long = _ / 1000L
    override val next                   = SECONDS
    override val makePrev: Long => Long = _ * 1000L
    override val prev                   = MICROSECONDS
  }

  case object _MICROSECONDS extends TimeUnit(2) {
    override val makeNext: Long => Long = _ / 1000L
    override val next                   = MILLISECONDS
    override val makePrev: Long => Long = _ * 1000L
    override val prev                   = NANOSECONDS
  }

  case object _NANOSECONDS extends TimeUnit(1) {
    override val makeNext: Long => Long = _ / 1000L
    override val next                   = MICROSECONDS
    override def makePrev: Long => Long = ???
    override def prev                   = ???
  }
}
