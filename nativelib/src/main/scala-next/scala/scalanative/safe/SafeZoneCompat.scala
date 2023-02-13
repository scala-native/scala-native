package scala.scalanative.safe

import language.experimental.captureChecking

object SafeZoneCompat {
  def withSafeZone[T](sz: {*} SafeZone, obj: T): {sz} T = obj
}
