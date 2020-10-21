package java.nio.file.attribute

import scala.collection.JavaConverters._

import PosixFilePermission._

import org.junit.Test
import org.junit.Assert._

class PosixFilePermissionsTest {

  @Test def anEmptyPermissionsSetProducesTheRightString(): Unit = {
    assertTrue(
      PosixFilePermissions
        .toString(Set.empty[PosixFilePermission].asJava) == "---------")
  }

  @Test def justOwnerReadPermissionProducesTheRightString(): Unit = {
    assertTrue(
      PosixFilePermissions.toString(Set(OWNER_READ).asJava) == "r--------")
  }

  @Test def justOwnerWritePermissionProducesTheRightString(): Unit = {
    assertTrue(
      PosixFilePermissions.toString(Set(OWNER_WRITE).asJava) == "-w-------")
  }

  @Test def justOwnerExecutepermissionProducesTheRightString(): Unit = {
    assertTrue(
      PosixFilePermissions.toString(Set(OWNER_EXECUTE).asJava) == "--x------")
  }

  @Test def justGroupReadpermissionProducesTheRightString(): Unit = {
    assertTrue(
      PosixFilePermissions.toString(Set(GROUP_READ).asJava) == "---r-----")
  }

  @Test def justGroupWritepermissionProducesTheRightString(): Unit = {
    assertTrue(
      PosixFilePermissions.toString(Set(GROUP_WRITE).asJava) == "----w----")
  }

  @Test def justGroupExecutepermissionProducesTheRightString(): Unit = {
    assertTrue(
      PosixFilePermissions.toString(Set(GROUP_EXECUTE).asJava) == "-----x---")
  }

  @Test def justOthersReadpermissionProducesTheRightString(): Unit = {
    assertTrue(
      PosixFilePermissions.toString(Set(OTHERS_READ).asJava) == "------r--")
  }

  @Test def justOthersWritepermissionProducesTheRightString(): Unit = {
    assertTrue(
      PosixFilePermissions.toString(Set(OTHERS_WRITE).asJava) == "-------w-")
  }

  @Test def justOthersExecutepermissionProducesTheRightString(): Unit = {
    assertTrue(
      PosixFilePermissions.toString(Set(OTHERS_EXECUTE).asJava) == "--------x")
  }

  @Test def parsingTheEmptyPermissionsGivesAnEmptySet(): Unit = {
    assertTrue(
      PosixFilePermissions
        .fromString("---------") == Set.empty[PosixFilePermission].asJava)
  }

  @Test def parsingOwnerReadpermissionProducesTheRightPermissions(): Unit = {
    assertTrue(
      PosixFilePermissions.fromString("r--------") == Set(OWNER_READ).asJava)
  }

  @Test def parsingOwnerWritepermissionProducesTheRightPermissions(): Unit = {
    assertTrue(
      PosixFilePermissions.fromString("-w-------") == Set(OWNER_WRITE).asJava)
  }

  @Test def parsingOwnerExecutepermissionProducesTheRightPermissions(): Unit = {
    assertTrue(
      PosixFilePermissions
        .fromString("--x------") == Set(OWNER_EXECUTE).asJava)
  }

  @Test def parsingGroupReadpermissionProducesTheRightPermissions(): Unit = {
    assertTrue(
      PosixFilePermissions.fromString("---r-----") == Set(GROUP_READ).asJava)
  }

  @Test def parsingGroupWritepermissionProducesTheRightPermissions(): Unit = {
    assertTrue(
      PosixFilePermissions.fromString("----w----") == Set(GROUP_WRITE).asJava)
  }

  @Test def parsingGroupExecutepermissionProducesTheRightPermissions(): Unit = {
    assertTrue(
      PosixFilePermissions
        .fromString("-----x---") == Set(GROUP_EXECUTE).asJava)
  }

  @Test def parsingOthersReadpermissionProducesTheRightPermissions(): Unit = {
    assertTrue(
      PosixFilePermissions.fromString("------r--") == Set(OTHERS_READ).asJava)
  }

  @Test def parsingOthersWritepermissionProducesTheRightPermissions(): Unit = {
    assertTrue(
      PosixFilePermissions.fromString("-------w-") == Set(OTHERS_WRITE).asJava)
  }

  @Test def parsingOthersExecutepermissionProducesTheRightPermissions()
      : Unit = {
    assertTrue(
      PosixFilePermissions
        .fromString("--------x") == Set(OTHERS_EXECUTE).asJava)
  }
}
