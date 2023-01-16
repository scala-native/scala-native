package org.scalanative.testsuite.javalib.nio.file

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
    assertEquals(
      "reason",
      new FileSystemException(null, null, "reason").getMessage()
    )
    assertEquals(
      " -> other",
      new FileSystemException(null, "other", null).getMessage()
    )
    assertEquals(
      "file",
      new FileSystemException("file", null, null).getMessage()
    )
    assertEquals(null, new FileSystemException(null, null, null).getMessage())

    assertEquals("file", new FileSystemException("file").getMessage())
    assertEquals(null, new FileSystemException(null).getMessage())
  }
}
