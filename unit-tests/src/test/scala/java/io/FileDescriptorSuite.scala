package java.io

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

    assertThrows[IOException] {
      // /root is unlikely to be readable to users running this test.
      val unused = new FileInputStream("/root")
    }
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
