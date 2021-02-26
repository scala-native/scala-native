object CanReadTest {
  import Files._

  def main(args: Array[String]): Unit = {
    assert(!emptyNameFile.canRead())

    assert(readableFile.canRead())
    assert(!unreadableFile.canRead())
    assert(!nonexistentFile.canRead())

    assert(readableDirectory.canRead())
    assert(!unreadableDirectory.canRead())
    assert(!nonexistentDirectory.canRead())
  }

}
