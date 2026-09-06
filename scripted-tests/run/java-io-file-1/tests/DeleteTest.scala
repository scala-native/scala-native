object DeleteTest {
  import Files._

  def run(): Unit = {
    assert(willBeDeletedFile.exists())
    assert(willBeDeletedFile.delete())
    assert(!willBeDeletedFile.exists())

    assert(willBeDeletedDirectory.exists())
    assert(willBeDeletedDirectory.delete())
    assert(!willBeDeletedDirectory.exists())
  }
}
