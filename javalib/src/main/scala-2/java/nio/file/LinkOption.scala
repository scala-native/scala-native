// Enums are not source-compatbile, make sure to sync this file Scala 3 implementation

package java.nio.file

final class LinkOption private (name: String, ordinal: Int)
    extends Enum[LinkOption](name, ordinal)
    with OpenOption
    with CopyOption

object LinkOption {
  final val NOFOLLOW_LINKS = new LinkOption("NOFOLLOW_LINKS", 0)
}
