// Enums are not source-compatible, make sure to sync this file with Scala 3 implementation

package java.nio.file

final class LinkOption private (name: String, ordinal: Int)
    extends Enum[LinkOption](name, ordinal)
    with OpenOption
    with CopyOption

object LinkOption {
  final val NOFOLLOW_LINKS = new LinkOption("NOFOLLOW_LINKS", 0)
}
