package java.time.temporal

// A near minimal implementation strictly for Scala Native internal testing.

trait TemporalUnit {
  def addTo[R <: Temporal](temporal: R, amount: Long): R = ???

  def between(
      temporal1Inclusive: Temporal,
      temporal2Exclusive: Temporal
  ): Long = ???

  def getDuration(): java.time.Duration = ???
  def isDateBased(): Boolean = ???
  def isDurationEstimated(): Boolean = ???
  def isTimeBased(): Boolean = ???
}
