package org.scalanative.testsuite.javalib.io

import java.io._

import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class FileReaderTest {

  @Test def throwsWhenCreatingFileReaderWithNonExistingFilePath(): Unit = {
    assertThrows(
      classOf[FileNotFoundException],
      new FileReader("/the/path/does/not/exist/for/sure")
    )
  }
}
