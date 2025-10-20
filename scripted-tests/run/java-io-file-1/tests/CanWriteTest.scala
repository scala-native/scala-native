object CanWriteTest {
  import Files.*
  import Utils.*

  def main(args: Array[String]): Unit = {
    assert(!emptyNameFile.canWrite())

    assert(writableFile.canWrite())
    assert(!unwritableFile.canWrite())
    assert(!nonexistentFile.canWrite())

    assert(writableDirectory.canWrite())
    assertOsSpecific(
      unwritableDirectory.canWrite(),
      "unwritableDirectory.canWrite()"
    )(onUnix = false, onWindows = true)
    assert(!nonexistentDirectory.canWrite())
  }

}
