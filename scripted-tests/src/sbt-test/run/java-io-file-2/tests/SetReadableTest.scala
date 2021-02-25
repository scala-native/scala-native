object SetReadableTest {
  import Files._
  def main(args: Array[String]): Unit = {
    assert(willBeSetReadableFile.exists())
    assert(!willBeSetReadableFile.canExecute())
    assert(!willBeSetReadableFile.canRead())
    assert(!willBeSetReadableFile.canWrite())

    assert(willBeSetReadableFile.setReadable(true))
    assert(!willBeSetReadableFile.canExecute())
    assert(willBeSetReadableFile.canRead())
    assert(!willBeSetReadableFile.canWrite())

    assert(willBeSetReadableFile.setReadable(false))
    assert(!willBeSetReadableFile.canExecute())
    assert(!willBeSetReadableFile.canRead())
    assert(!willBeSetReadableFile.canWrite())

    assert(willBeSetReadableDirectory.exists())
    assert(!willBeSetReadableDirectory.canExecute())
    assert(!willBeSetReadableDirectory.canRead())
    assert(!willBeSetReadableDirectory.canWrite())

    assert(willBeSetReadableDirectory.setReadable(true))
    assert(!willBeSetReadableDirectory.canExecute())
    assert(willBeSetReadableDirectory.canRead())
    assert(!willBeSetReadableDirectory.canWrite())

    assert(willBeSetReadableDirectory.setReadable(false))
    assert(!willBeSetReadableDirectory.canExecute())
    assert(!willBeSetReadableDirectory.canRead())
    assert(!willBeSetReadableDirectory.canWrite())

    assert(!nonexistentFile.exists)
    assert(!nonexistentFile.setReadable(true))

  }
}
