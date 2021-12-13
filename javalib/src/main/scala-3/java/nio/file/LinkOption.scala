// Enums are not source-compatbile, make sure to sync this file with Scala 2 implementation

package java.nio.file

enum LinkOption extends Enum[LinkOption]() with OpenOption with CopyOption:
  case NOFOLLOW_LINKS extends LinkOption
