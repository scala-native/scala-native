package java.nio.file

object FileSystemExceptionSuite extends tests.Suite {

  test("FileSystemException.getMessage() formats error message") {
    assert(
      new FileSystemException("file", "other", "reason")
        .getMessage() == "file -> other: reason")
    assert(
      new FileSystemException("file", "other", null)
        .getMessage() == "file -> other")
    assert(
      new FileSystemException("file", null, "reason")
        .getMessage() == "file: reason")
    assert(
      new FileSystemException(null, "other", "reason")
        .getMessage() == " -> other: reason")
    assert(
      new FileSystemException(null, null, "reason").getMessage() == ": reason")
    assert(
      new FileSystemException(null, "other", null).getMessage() == " -> other")
    assert(new FileSystemException("file", null, null).getMessage() == "file")
    assert(new FileSystemException(null, null, null).getMessage() == "")

    assert(new FileSystemException("file").getMessage() == "file")
    assert(new FileSystemException(null).getMessage() == "")
  }
}
