object ListTest {
  import Files.*

  def main(args: Array[String]): Unit = {
    val listedFiles = nonEmptyDirectory.list().sorted
    assert(listedFiles.length == 3)
    assert(listedFiles(0) == firstChildFile.getName)
    assert(listedFiles(1) == secondChildFile.getName)
    assert(listedFiles(2) == thirdChildDirectory.getName)
  }
}
