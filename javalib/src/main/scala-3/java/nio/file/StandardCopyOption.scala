// Enums are not source-compatbile, make sure to sync this file with Scala 2 implementation

package java.nio.file

enum StandardCopyOption extends Enum[StandardCopyOption]() with CopyOption:
  case REPLACE_EXISTING extends StandardCopyOption
  case COPY_ATTRIBUTES extends StandardCopyOption
  case ATOMIC_MOVE extends StandardCopyOption
