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
  final val compat: Int = 6 // a.k.a. MAJOR version
  final val revision: Int = 11 // a.k.a. MINOR version
  case class Version(compat: Int, revision: Int)

  /* Current public release version of Scala Native. */
  final val current: String = "0.5.8-SNAPSHOT"
  final val currentBinaryVersion: String = binaryVersion(current)

  private object FullVersion {
    final val FullVersionRE = """^(\d+)\.(\d+)\.(\d+)(-.*)?$""".r

    private def preRelease(s: String) = Option(s).map(_.stripPrefix("-"))

    def unapply(version: String): Option[(Int, Int, Int, Option[String])] = {
      version match {
        case FullVersionRE(major, minor, patch, preReleaseString) =>
          Some(
            (
              major.toInt,
              minor.toInt,
              patch.toInt,
              preRelease(preReleaseString)
            )
          )
        case _ => None
      }
    }
  }

  private[nir] def binaryVersion(full: String): String = full match {
    case FullVersion(0, minor, 0, Some(suffix)) => full
    case FullVersion(0, minor, _, _)            => s"0.$minor"
    case FullVersion(major, 0, 0, Some(suffix)) => s"$major.0-$suffix"
    case FullVersion(major, _, _, _)            => major.toString
  }

}
