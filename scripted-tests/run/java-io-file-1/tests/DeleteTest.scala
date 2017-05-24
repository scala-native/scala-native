object DeleteTest {
  import Files._

  def main(args: Array[String]): Unit = {
    assert(willBeDeletedFile.exists())
    assert(willBeDeletedFile.delete())
    assert(!willBeDeletedFile.exists())

    assert(willBeDeletedDirectory.exists())
    assert(willBeDeletedDirectory.delete())
    assert(!willBeDeletedDirectory.exists())
  }
}
