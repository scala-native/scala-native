package scala.scalanative.safe

/** Mockup of the Scala 3 version of the SafeZoneCompat object. It's needed
 *  because both scala-3 and scala-next versions of nscplugin are compiled with
 *  scala-3 version of nativelib.
 */
object SafeZoneCompat {
  def withSafeZone: Unit = ???
}
