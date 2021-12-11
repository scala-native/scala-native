// Enums are not source-compatbile, make sure to sync this file Scala 2 implementation

package java.nio.file

enum FileVisitOption extends Enum[FileVisitOption]():
  case FOLLOW_LINKS extends FileVisitOption
