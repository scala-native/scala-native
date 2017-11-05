object IsFileTest {
  import Files._
  import scala.scalanative.runtime.Platform

  def main(args: Array[String]): Unit = if (!Platform.isWindows) {
    assert(executableFile.isFile())
    assert(unexecutableFile.isFile())
    assert(readableFile.isFile())
    assert(unreadableFile.isFile())
    assert(writableFile.isFile())
    assert(unwritableFile.isFile())

    assert(!executableDirectory.isFile())
    assert(!unexecutableDirectory.isFile())
    assert(!readableDirectory.isFile())
    assert(!unreadableDirectory.isFile())
    assert(!writableDirectory.isFile())
    assert(!unwritableDirectory.isFile())

    assert(!nonexistentFile.isFile())
    assert(!nonexistentDirectory.isFile())
  }
}
