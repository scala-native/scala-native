package scala.scalanative
package nir

object Versions {
  /* NIR magic number */
  final val magic: Int = 0x2e4e4952 // '.NIR' in hex

  /* NIR is internally versioned by two version numbers. First one is
   * compatibility level, and the second one is revision of the format. Both
   * numbers are monotonically increasing. Revision is increased on any change
   * to the format. Compatibility level is increased whenever the given change
   * is breaking.
   *
   * For example here is a possible sequence of release number changes:
   *
   *     0.0 -> 0.1 -> 0.2 -> 1.3 -> ...
   *
   * NIR emitted via 0.0-compatible compiler should read without any changes
   * in 0.2-based compiler, but not the other way around. On the other hand
   * when 1.3-based release happens all of the code needs to recompiled with
   * new version of the toolchain.
   */
  final val compat: Int   = 6
  final val revision: Int = 7

  /* Current public release version of Scala Native. */
  final val current: String = "0.1-SNAPSHOT"
}
