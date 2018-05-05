package scala.scalanative.build

/**
 * Represents the platform the built code will be run on.
 */
sealed abstract class NativePlatform(val is32: Boolean, val isIntel: Boolean)

case object x86_64 extends NativePlatform(false, true)
case object i386 extends NativePlatform(true, true)
