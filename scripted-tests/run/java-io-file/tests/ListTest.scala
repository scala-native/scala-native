object ListTest {
  import Files._

  def main(args: Array[String]): Unit = {
    assert(nonEmptyDirectory.list().length == 3)
    assert(nonEmptyDirectory.list()(0) == firstChildFile.getName)
    assert(nonEmptyDirectory.list()(1) == secondChildFile.getName)
    assert(nonEmptyDirectory.list()(2) == thirdChildDirectory.getName)
  }
}
