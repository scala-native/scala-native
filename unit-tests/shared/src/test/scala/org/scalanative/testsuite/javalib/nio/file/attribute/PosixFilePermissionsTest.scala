package org.scalanative.testsuite.javalib.nio.file.attribute

import java.nio.file.attribute.*

import PosixFilePermission.*
import scala.scalanative.junit.utils.CollectionConverters.*
import org.junit.Test
import org.junit.Assert.*

class PosixFilePermissionsTest {

  @Test def anEmptyPermissionsSetProducesTheRightString(): Unit = {
    assertTrue(
      PosixFilePermissions
        .toString(Set.empty[PosixFilePermission].toJavaSet) == "---------"
    )
  }

  @Test def justOwnerReadPermissionProducesTheRightString(): Unit = {
    assertTrue(
      PosixFilePermissions.toString(Set(OWNER_READ).toJavaSet) == "r--------"
    )
  }

  @Test def justOwnerWritePermissionProducesTheRightString(): Unit = {
    assertTrue(
      PosixFilePermissions.toString(Set(OWNER_WRITE).toJavaSet) == "-w-------"
    )
  }

  @Test def justOwnerExecutepermissionProducesTheRightString(): Unit = {
    assertTrue(
      PosixFilePermissions
        .toString(Set(OWNER_EXECUTE).toJavaSet) == "--x------"
    )
  }

  @Test def justGroupReadpermissionProducesTheRightString(): Unit = {
    assertTrue(
      PosixFilePermissions.toString(Set(GROUP_READ).toJavaSet) == "---r-----"
    )
  }

  @Test def justGroupWritepermissionProducesTheRightString(): Unit = {
    assertTrue(
      PosixFilePermissions.toString(Set(GROUP_WRITE).toJavaSet) == "----w----"
    )
  }

  @Test def justGroupExecutepermissionProducesTheRightString(): Unit = {
    assertTrue(
      PosixFilePermissions
        .toString(Set(GROUP_EXECUTE).toJavaSet) == "-----x---"
    )
  }

  @Test def justOthersReadpermissionProducesTheRightString(): Unit = {
    assertTrue(
      PosixFilePermissions.toString(Set(OTHERS_READ).toJavaSet) == "------r--"
    )
  }

  @Test def justOthersWritepermissionProducesTheRightString(): Unit = {
    assertTrue(
      PosixFilePermissions.toString(Set(OTHERS_WRITE).toJavaSet) == "-------w-"
    )
  }

  @Test def justOthersExecutepermissionProducesTheRightString(): Unit = {
    assertTrue(
      PosixFilePermissions
        .toString(Set(OTHERS_EXECUTE).toJavaSet) == "--------x"
    )
  }

  @Test def parsingTheEmptyPermissionsGivesAnEmptySet(): Unit = {
    assertTrue(
      PosixFilePermissions
        .fromString("---------") == Set.empty[PosixFilePermission].toJavaSet
    )
  }

  @Test def parsingOwnerReadpermissionProducesTheRightPermissions(): Unit = {
    assertTrue(
      PosixFilePermissions.fromString("r--------") == Set(OWNER_READ).toJavaSet
    )
  }

  @Test def parsingOwnerWritepermissionProducesTheRightPermissions(): Unit = {
    assertTrue(
      PosixFilePermissions
        .fromString("-w-------") == Set(OWNER_WRITE).toJavaSet
    )
  }

  @Test def parsingOwnerExecutepermissionProducesTheRightPermissions(): Unit = {
    assertTrue(
      PosixFilePermissions
        .fromString("--x------") == Set(OWNER_EXECUTE).toJavaSet
    )
  }

  @Test def parsingGroupReadpermissionProducesTheRightPermissions(): Unit = {
    assertTrue(
      PosixFilePermissions.fromString("---r-----") == Set(GROUP_READ).toJavaSet
    )
  }

  @Test def parsingGroupWritepermissionProducesTheRightPermissions(): Unit = {
    assertTrue(
      PosixFilePermissions
        .fromString("----w----") == Set(GROUP_WRITE).toJavaSet
    )
  }

  @Test def parsingGroupExecutepermissionProducesTheRightPermissions(): Unit = {
    assertTrue(
      PosixFilePermissions
        .fromString("-----x---") == Set(GROUP_EXECUTE).toJavaSet
    )
  }

  @Test def parsingOthersReadpermissionProducesTheRightPermissions(): Unit = {
    assertTrue(
      PosixFilePermissions
        .fromString("------r--") == Set(OTHERS_READ).toJavaSet
    )
  }

  @Test def parsingOthersWritepermissionProducesTheRightPermissions(): Unit = {
    assertTrue(
      PosixFilePermissions
        .fromString("-------w-") == Set(OTHERS_WRITE).toJavaSet
    )
  }

  @Test def parsingOthersExecutepermissionProducesTheRightPermissions()
      : Unit = {
    assertTrue(
      PosixFilePermissions
        .fromString("--------x") == Set(OTHERS_EXECUTE).toJavaSet
    )
  }
}
