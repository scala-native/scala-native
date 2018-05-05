package scala.scalanative.build

/**
 * Represents the platform the built code will be run on.
 */
sealed abstract class TargetArchitecture(val is32: Boolean, val isIntel: Boolean)

case object i386 extends TargetArchitecture(true, true)
case object x86_64 extends TargetArchitecture(false, true)
case object ARM extends TargetArchitecture(true, false)
case object ARM64 extends TargetArchitecture(false, false)
