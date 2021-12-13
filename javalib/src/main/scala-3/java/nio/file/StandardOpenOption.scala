// Enums are not source-compatbile, make sure to sync this file with Scala 2 implementation

package java.nio.file

enum StandardOpenOption extends Enum[StandardOpenOption]() with OpenOption:
  case READ extends StandardOpenOption
  case WRITE extends StandardOpenOption
  case APPEND extends StandardOpenOption
  case TRUNCATE_EXISTING extends StandardOpenOption
  case CREATE extends StandardOpenOption
  case CREATE_NEW extends StandardOpenOption
  case DELETE_ON_CLOSE extends StandardOpenOption
  case SPARSE extends StandardOpenOption
  case SYNC extends StandardOpenOption
  case DSYNC extends StandardOpenOption
