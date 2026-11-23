object LastModifiedTest {
  import Files._

  def run(): Unit = {
    assert(!nonexistentFile.exists())
    assert(nonexistentFile.lastModified() == 0L)

    assert(fileWithLastModifiedSet.exists())
    assert(fileWithLastModifiedSet.lastModified() == expectedLastModified)
  }
}
