package org.scalanative.testsuite.javalib.nio.file

import java.nio.file.*

import org.junit.Test
import org.junit.Assert.*

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class ReadOnlyFileSystemExceptionTest {

  @Test def readOnlyFileSystemExceptionExists(): Unit = {
    assertThrows(
      classOf[ReadOnlyFileSystemException],
      throw new ReadOnlyFileSystemException()
    )
  }
}
