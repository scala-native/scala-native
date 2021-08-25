package javalib.io

import java.io._

import org.junit.Test

import scalanative.junit.utils.AssertThrows.assertThrows

class FileReaderTest {

  @Test def throwsWhenCreatingFileReaderWithNonExistingFilePath(): Unit = {
    assertThrows(
      classOf[FileNotFoundException],
      new FileReader("/the/path/does/not/exist/for/sure")
    )
  }
}
