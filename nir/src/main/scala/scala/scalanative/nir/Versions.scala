package scala.scalanative
package nir

object Versions {
  /* NIR magic number */
  final val magic: Int = 0x2e4e4952 // '.NIR' in hex

  /* NIR is internally versioned by two version numbers. First one is
   * compatibility level, and the second one is revision of the format. Both
   * numbers are monotonically increasing. Revision is increased on any change
   * to the format. Compatibility level is increased whenever the given change
   * is breaking. Versioning starts at 1.1 version that corresponds to 0.1.0
   * release of Scala Native.
   *
   * For example here is a possible sequence of release number changes:
   *
   *     1.1 -> 1.1 -> 1.2 -> 2.3 -> ...
   *
   * NIR emitted via 1.1-compatible compiler should read without any changes
   * in 1.2-based compiler, but not the other way around. On the other hand
   * when 2.3-based release happens all of the code needs to recompiled with
   * new version of the toolchain.
   */
  final val compat: Int   = 1
  final val revision: Int = 1

  /* Current public release version of Scala Native. */
  final val current: String = "0.1.0"
}
