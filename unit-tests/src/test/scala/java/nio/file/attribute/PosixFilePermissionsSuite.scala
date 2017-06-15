package java.nio.file.attribute

import scala.collection.JavaConverters._

import PosixFilePermission._

object PosixFilePermissionsSuite extends tests.Suite {

  test("An empty permissions set produces the right string: ---------") {
    assert(
      PosixFilePermissions
        .toString(Set.empty[PosixFilePermission].asJava) == "---------")
  }

  test("Just OWNER_READ permission produces the right string: r--------") {
    assert(
      PosixFilePermissions.toString(Set(OWNER_READ).asJava) == "r--------")
  }

  test("Just OWNER_WRITE permission produces the right string: -w-------") {
    assert(
      PosixFilePermissions.toString(Set(OWNER_WRITE).asJava) == "-w-------")
  }

  test("Just OWNER_EXECUTE permission produces the right string: --x------") {
    assert(
      PosixFilePermissions.toString(Set(OWNER_EXECUTE).asJava) == "--x------")
  }

  test("Just GROUP_READ permission produces the right string: ---r-----") {
    assert(
      PosixFilePermissions.toString(Set(GROUP_READ).asJava) == "---r-----")
  }

  test("Just GROUP_WRITE permission produces the right string: ----w----") {
    assert(
      PosixFilePermissions.toString(Set(GROUP_WRITE).asJava) == "----w----")
  }

  test("Just GROUP_EXECUTE permission produces the right string: -----x---") {
    assert(
      PosixFilePermissions.toString(Set(GROUP_EXECUTE).asJava) == "-----x---")
  }

  test("Just OTHERS_READ permission produces the right string: ------r--") {
    assert(
      PosixFilePermissions.toString(Set(OTHERS_READ).asJava) == "------r--")
  }

  test("Just OTHERS_WRITE permission produces the right string: -------w-") {
    assert(
      PosixFilePermissions.toString(Set(OTHERS_WRITE).asJava) == "-------w-")
  }

  test("Just OTHERS_EXECUTE permission produces the right string: --------x") {
    assert(
      PosixFilePermissions.toString(Set(OTHERS_EXECUTE).asJava) == "--------x")
  }

  test("Parsing the empty permissions gives an empty set") {
    assert(
      PosixFilePermissions
        .fromString("---------") == Set.empty[PosixFilePermission].asJava)
  }

  test("Parsing OWNER_READ permission produces the right permissions") {
    assert(
      PosixFilePermissions.fromString("r--------") == Set(OWNER_READ).asJava)
  }

  test("Parsing OWNER_WRITE permission produces the right permissions") {
    assert(
      PosixFilePermissions.fromString("-w-------") == Set(OWNER_WRITE).asJava)
  }

  test("Parsing OWNER_EXECUTE permission produces the right permissions") {
    assert(
      PosixFilePermissions
        .fromString("--x------") == Set(OWNER_EXECUTE).asJava)
  }

  test("Parsing GROUP_READ permission produces the right permissions") {
    assert(
      PosixFilePermissions.fromString("---r-----") == Set(GROUP_READ).asJava)
  }

  test("Parsing GROUP_WRITE permission produces the right permissions") {
    assert(
      PosixFilePermissions.fromString("----w----") == Set(GROUP_WRITE).asJava)
  }

  test("Parsing GROUP_EXECUTE permission produces the right permissions") {
    assert(
      PosixFilePermissions
        .fromString("-----x---") == Set(GROUP_EXECUTE).asJava)
  }

  test("Parsing OTHERS_READ permission produces the right permissions") {
    assert(
      PosixFilePermissions.fromString("------r--") == Set(OTHERS_READ).asJava)
  }

  test("Parsing OTHERS_WRITE permission produces the right permissions") {
    assert(
      PosixFilePermissions.fromString("-------w-") == Set(OTHERS_WRITE).asJava)
  }

  test("Parsing OTHERS_EXECUTE permission produces the right permissions") {
    assert(
      PosixFilePermissions
        .fromString("--------x") == Set(OTHERS_EXECUTE).asJava)
  }

}
