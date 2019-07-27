package java.nio.file.attribute

import java.util.{HashSet, Set}

object PosixFilePermissions {
  def asFileAttribute(perms: Set[PosixFilePermission])
    : FileAttribute[Set[PosixFilePermission]] =
    new FileAttribute[Set[PosixFilePermission]] {
      override def name(): String                    = "posix:permissions"
      override def value(): Set[PosixFilePermission] = perms
    }

  def fromString(perms: String): Set[PosixFilePermission] =
    if (perms.length == 9) {
      val set = new HashSet[PosixFilePermission]()
      var i   = 0
      while (i < 3) {
        if (isR(perms, i)) set.add(PosixFilePermission.values()(3 * i))
        if (isW(perms, i)) set.add(PosixFilePermission.values()(3 * i + 1))
        if (isX(perms, i)) set.add(PosixFilePermission.values()(3 * i + 2))
        i += 1
      }
      set
    } else {
      throw new IllegalArgumentException("Invalid mode")
    }

  def toString(perms: Set[PosixFilePermission]): String = {
    import PosixFilePermission._
    val builder = new StringBuilder
    if (perms.contains(OWNER_READ)) builder.append('r')
    else builder.append('-')
    if (perms.contains(OWNER_WRITE)) builder.append('w')
    else builder.append('-')
    if (perms.contains(OWNER_EXECUTE)) builder.append('x')
    else builder.append('-')
    if (perms.contains(GROUP_READ)) builder.append('r')
    else builder.append('-')
    if (perms.contains(GROUP_WRITE)) builder.append('w')
    else builder.append('-')
    if (perms.contains(GROUP_EXECUTE)) builder.append('x')
    else builder.append('-')
    if (perms.contains(OTHERS_READ)) builder.append('r')
    else builder.append('-')
    if (perms.contains(OTHERS_WRITE)) builder.append('w')
    else builder.append('-')
    if (perms.contains(OTHERS_EXECUTE)) builder.append('x')
    else builder.append('-')
    builder.toString
  }

  private def isR(perms: String, i: Int): Boolean = {
    if (perms(3 * i) == 'r') true
    else if (perms(3 * i) == '-') false
    else throw new IllegalArgumentException("Invalid mode")
  }
  private def isW(perms: String, i: Int): Boolean = {
    if (perms(3 * i + 1) == 'w') true
    else if (perms(3 * i + 1) == '-') false
    else throw new IllegalArgumentException("Invalid mode")
  }

  private def isX(perms: String, i: Int): Boolean = {
    if (perms(3 * i + 2) == 'x') true
    else if (perms(3 * i + 2) == '-') false
    else throw new IllegalArgumentException("Invalid mode")
  }
}
