object RenamedToTest {
  import Files.*

  def main(args: Array[String]): Unit = {
    assert(willBeRenamedFrom.exists)
    assert(!willBeRenamedTo.exists)
    assert(willBeRenamedFrom.renameTo(willBeRenamedTo))
    assert(!willBeRenamedFrom.exists)
    assert(willBeRenamedTo.exists)
  }
}
