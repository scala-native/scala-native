package scala.scalanative.runtime 

import language.experimental.captureChecking
import scala.scalanative.memory.SafeZone

object SafeZoneAllocator {
  def allocate[T](sz: {*} SafeZone, obj: T): {sz} T = intrinsic
}
