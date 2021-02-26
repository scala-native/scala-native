object SetLastModifiedTest {
  import Files._

  def main(args: Array[String]): Unit = {
    assert(!nonexistentFile.exists())
    assert(!nonexistentFile.setLastModified(10000))

    assert(willBeSetLastModified.exists())
    assert(willBeSetLastModified.setLastModified(expectedLastModified))
    assert(willBeSetLastModified.lastModified == expectedLastModified)
  }
}
