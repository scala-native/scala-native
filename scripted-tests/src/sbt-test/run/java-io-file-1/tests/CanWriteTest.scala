object CanWriteTest {
  import Files._

  def main(args: Array[String]): Unit = {
    assert(!emptyNameFile.canWrite())

    assert(writableFile.canWrite())
    assert(!unwritableFile.canWrite())
    assert(!nonexistentFile.canWrite())

    assert(writableFile.canWrite())
    assert(!unwritableFile.canWrite())
    assert(!nonexistentDirectory.canWrite())
  }

}
