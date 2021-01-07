package java.io

import org.junit.Test

import scalanative.junit.utils.AssertThrows._

class FileReaderTest {

  @Test def throwsWhenCreatingFileReaderWithNonExistingFilePath(): Unit = {
    assertThrows(classOf[FileNotFoundException],
                 new FileReader("/the/path/does/not/exist/for/sure"))
  }
}
