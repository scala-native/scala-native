package java.io

object FileReaderSuite extends tests.Suite {

  test(
    "throws FileNotFoundException when creating new FileReader with non-existing file path") {
    assertThrows[FileNotFoundException] {
      new FileReader("/the/path/does/not/exist/for/sure")
    }
  }
}
