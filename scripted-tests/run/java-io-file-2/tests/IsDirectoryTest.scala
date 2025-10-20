object IsDirectoryTest {
  import Files.*

  def main(args: Array[String]): Unit = {
    assert(!executableFile.isDirectory())
    assert(!unexecutableFile.isDirectory())
    assert(!readableFile.isDirectory())
    assert(!unreadableFile.isDirectory())
    assert(!writableFile.isDirectory())
    assert(!unwritableFile.isDirectory())

    assert(executableDirectory.isDirectory())
    assert(unexecutableDirectory.isDirectory())
    assert(readableDirectory.isDirectory())
    assert(unreadableDirectory.isDirectory())
    assert(writableDirectory.isDirectory())
    assert(unwritableDirectory.isDirectory())

    assert(!nonexistentFile.isDirectory())
    assert(!nonexistentDirectory.isDirectory())
  }
}
