object RenamedToTest {
  import Files._

  def main(args: Array[String]): Unit = {
    assert(willBeRenamedFrom.exists)
    assert(!willBeRenamedTo.exists)
    assert(willBeRenamedFrom.renameTo(willBeRenamedTo))
    assert(!willBeRenamedFrom.exists)
    assert(willBeRenamedTo.exists)
  }
}
