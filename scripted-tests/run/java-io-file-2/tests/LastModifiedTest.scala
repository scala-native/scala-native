object LastModifiedTest {
  import Files.*

  def main(args: Array[String]): Unit = {
    assert(!nonexistentFile.exists())
    assert(nonexistentFile.lastModified() == 0L)

    assert(fileWithLastModifiedSet.exists())
    assert(fileWithLastModifiedSet.lastModified() == expectedLastModified)
  }
}
