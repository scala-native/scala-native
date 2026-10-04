package java.nio.file

import java.util.Objects

final class LinkOption private (name: String, ordinal: Int)
    extends _Enum[LinkOption](name, ordinal)
    with OpenOption
    with CopyOption

object LinkOption {
  final val NOFOLLOW_LINKS = new LinkOption("NOFOLLOW_LINKS", 0)

  def values(): Array[LinkOption] = Array(NOFOLLOW_LINKS)

  def valueOf(name: String): LinkOption = {
    Objects.requireNonNull(name, "Name is null")
    if (name == NOFOLLOW_LINKS.name()) NOFOLLOW_LINKS
    else
      throw new IllegalArgumentException(
        "No enum constant java.nio.file.LinkOption." + name
      )
  }
}
