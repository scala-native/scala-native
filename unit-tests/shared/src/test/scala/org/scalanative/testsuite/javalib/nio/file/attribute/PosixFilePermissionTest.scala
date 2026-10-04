package org.scalanative.testsuite.javalib.nio.file.attribute

import java.nio.file.attribute.PosixFilePermission

import org.scalanative.testsuite.javalib.nio.file.FileOptionEnumTest

class PosixFilePermissionTest extends FileOptionEnumTest[PosixFilePermission] {
  protected def expectedNames: Array[String] = Array(
    "OWNER_READ",
    "OWNER_WRITE",
    "OWNER_EXECUTE",
    "GROUP_READ",
    "GROUP_WRITE",
    "GROUP_EXECUTE",
    "OTHERS_READ",
    "OTHERS_WRITE",
    "OTHERS_EXECUTE"
  )
  protected def enumTypeName: String =
    "java.nio.file.attribute.PosixFilePermission"
  protected def values(): Array[PosixFilePermission] =
    PosixFilePermission.values()
  protected def lookup(name: String): PosixFilePermission =
    PosixFilePermission.valueOf(name)
}
