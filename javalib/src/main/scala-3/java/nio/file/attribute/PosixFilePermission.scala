// Enums are not source-compatbile, make sure to sync this file with Scala 2 implementation

package java.nio.file.attribute

enum PosixFilePermission extends Enum[PosixFilePermission]():
  case OWNER_READ extends PosixFilePermission
  case OWNER_WRITE extends PosixFilePermission
  case OWNER_EXECUTE extends PosixFilePermission
  case GROUP_READ extends PosixFilePermission
  case GROUP_WRITE extends PosixFilePermission
  case GROUP_EXECUTE extends PosixFilePermission
  case OTHERS_READ extends PosixFilePermission
  case OTHERS_WRITE extends PosixFilePermission
  case OTHERS_EXECUTE extends PosixFilePermission
