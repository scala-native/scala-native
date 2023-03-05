package scala.scalanative.runtime 

import language.experimental.captureChecking
import scala.scalanative.safe.SafeZone

object SafeZone {
  def allocate[T](sz: {*} SafeZone, obj: T): {sz} T = intrinsic
}
