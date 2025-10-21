package org.scalanative.testsuite.javalib.nio.file

import java.nio.file._

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class ReadOnlyFileSystemExceptionTest {

  @Test def readOnlyFileSystemExceptionExists(): Unit = {
    assertThrows(
      classOf[ReadOnlyFileSystemException],
      throw new ReadOnlyFileSystemException()
    )
  }
}
