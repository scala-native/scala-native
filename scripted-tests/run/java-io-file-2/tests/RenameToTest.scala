object RenameToTest {
  import Files._

  def run(): Unit = {
    assert(willBeRenamedFrom.exists)
    assert(!willBeRenamedTo.exists)
    assert(willBeRenamedFrom.renameTo(willBeRenamedTo))
    assert(!willBeRenamedFrom.exists)
    assert(willBeRenamedTo.exists)
  }
}
