package java.io

import java.nio.file.{Files, Paths}

object FileDescriptorSuite extends tests.Suite {
  val in  = FileDescriptor.in
  val out = FileDescriptor.out
  val err = FileDescriptor.err
  val fd  = new FileDescriptor()

  test("FileDescriptor.openReadOnly should report errno on failure") {

    // FileDescriptor.openReadOnly is private[io] and hard to invoke
    // directly.
    //
    // The FileInputStream constructor below will call
    // FileDescriptor.openReadOnly. Because it is public, it is both
    // easier to work with and the way most users would get to
    // FileDescriptor.openReadOnly in the first place.
    //
    // Test here, rather than in FileInputStreamSuite so that the Suite
    // and code throwing the exception have corresponding names.

    val file = File.createTempFile("scala-native-test-FileDescriptor", "")

    assert(file.setReadable(false, false), s"setReadable() failed")
    assert(file.setWritable(false, false), s"setWritable(false) failed")

    assertThrows[IOException] {
      val unused = new FileInputStream(file)
    }

    // Clean up only if expected throw happened, else leave audit trail.
    assert(file.setWritable(true, true), s"setWritable(true) failed")
    assert(file.delete(), s"delete() failed")
  }

  test("valid descriptors") {
    assert(in.valid())
    assert(out.valid())
    assert(err.valid())
  }

  test("invalid descriptors") {
    assertNot(fd.valid())
  }

  test("sync should throw java.io.SyncFailedException for stdin") {
    assertThrows[SyncFailedException] {
      in.sync()
    }
  }

  test("sync should throw java.io.SyncFailedException for stdout") {
    assertThrows[SyncFailedException] {
      out.sync()
    }
  }

  test("sync should throw java.io.SyncFailedException for stderr") {
    assertThrows[SyncFailedException] {
      err.sync()
    }
  }

  test("sync should throw java.io.SyncFailedException for new fd") {
    assertThrows[SyncFailedException] {
      fd.sync()
    }
  }

  test("valid should verify that file descriptor is still valid") {
    val tmpFile = File.createTempFile("tmp", ".tmp")
    assert(tmpFile.exists())
    val fis = new FileInputStream(tmpFile)
    assert(fis.getFD().valid())
    fis.close()
    assertNot(fis.getFD().valid())
  }
}
