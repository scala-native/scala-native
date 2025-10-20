object CanReadTest {
  import Files.*
  import Utils.*

  def main(args: Array[String]): Unit = {
    assert(!emptyNameFile.canRead())

    assert(readableFile.canRead())
    assertOsSpecific(
      unreadableFile.canRead(),
      "unreadableFile.canRead()"
    )(onUnix = false, onWindows = true)
    assert(!nonexistentFile.canRead())

    assert(readableDirectory.canRead())
    assertOsSpecific(
      unreadableDirectory.canRead(),
      "unreadableDirectory.canRead()"
    )(onUnix = false, onWindows = true)
    assert(!nonexistentDirectory.canRead())
  }

}
