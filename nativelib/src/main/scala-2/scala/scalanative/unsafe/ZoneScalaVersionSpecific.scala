package scala.scalanative.unsafe

trait ZoneCompanionScalaVersionSpecific { self: Zone.type =>
  /** Run given function with a fresh zone and destroy it afterwards. */
  def apply[T](f: Zone => T): T = acquire(f)
}
