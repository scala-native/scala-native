import java.io.File

object ExistsTest {
  import Files.*

  def main(args: Array[String]): Unit = {
    assert(executableFile.exists())
    assert(unexecutableFile.exists())
    assert(readableFile.exists())
    assert(unreadableFile.exists())
    assert(writableFile.exists())
    assert(unwritableFile.exists())

    assert(executableDirectory.exists())
    assert(unexecutableDirectory.exists())
    assert(readableDirectory.exists())
    assert(unreadableDirectory.exists())
    assert(writableDirectory.exists())
    assert(unwritableDirectory.exists())

    assert(!nonexistentFile.exists())
    assert(!nonexistentDirectory.exists())

    assert(!new File(nonexistentDirectory, "somefile").exists())
  }
}
