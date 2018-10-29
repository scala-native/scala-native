package scala.scalanative.build

sealed class Bits(count: Int) {
  override def toString = s"x$count"
}

case object ThirtyTwo extends Bits(32)
case object SixtyFour extends Bits(64)

/**
 * Represents the platform the built code will be run on.
 */
sealed class TargetArchitecture(val bits: Bits) {
  val is32 = bits == ThirtyTwo
}

object TargetArchitecture {
  case object i386    extends TargetArchitecture(ThirtyTwo)
  case object i686    extends TargetArchitecture(ThirtyTwo)
  case object x86_64  extends TargetArchitecture(SixtyFour)
  case object armv7l  extends TargetArchitecture(ThirtyTwo)
  case object aarch64 extends TargetArchitecture(SixtyFour)
}
