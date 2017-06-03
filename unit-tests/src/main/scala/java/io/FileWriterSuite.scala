package java.io

import scala.util.Try

object FileWriterSuite extends tests.Suite {

  test(
    "throws FileNotFoundException when writing to a directory (or non-existing path)") {
    assertThrows[FileNotFoundException] {
      new FileWriter("/etc")
    }
  }
}
