package javalib.nio.file

import java.nio.file._

import org.junit.Test
import org.junit.Assert._

class FileSystemExceptionTest {

  @Test def fileSystemExceptionGetMessageFormatsErrorMessage(): Unit = {
    assertTrue(
      new FileSystemException("file", "other", "reason")
        .getMessage() == "file -> other: reason"
    )
    assertTrue(
      new FileSystemException("file", "other", null)
        .getMessage() == "file -> other"
    )
    assertTrue(
      new FileSystemException("file", null, "reason")
        .getMessage() == "file: reason"
    )
    assertTrue(
      new FileSystemException(null, "other", "reason")
        .getMessage() == " -> other: reason"
    )
    assertTrue(
      new FileSystemException(null, null, "reason").getMessage() == ": reason"
    )
    assertTrue(
      new FileSystemException(null, "other", null).getMessage() == " -> other"
    )
    assertTrue(
      new FileSystemException("file", null, null).getMessage() == "file"
    )
    assertTrue(new FileSystemException(null, null, null).getMessage() == "")

    assertTrue(new FileSystemException("file").getMessage() == "file")
    assertTrue(new FileSystemException(null).getMessage() == "")
  }
}
